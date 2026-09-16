package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.model.Contact
import com.example.data.model.ContactType
import com.example.data.model.GoldTransaction
import com.example.data.model.PaymentReminder
import com.example.data.model.TransactionType
import com.example.data.repository.GoldLedgerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupResult(
    val success: Boolean,
    val message: String,
    val backupFile: File? = null,
    val contactCount: Int = 0,
    val transactionCount: Int = 0,
    val reminderCount: Int = 0
)

class BackupManager(
    private val context: Context,
    private val repository: GoldLedgerRepository
) {
    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val contacts = repository.getAllContactsSnapshot()
        val transactions = repository.getAllTransactionsSnapshot()
        val reminders = repository.getAllRemindersSnapshot()

        val root = JSONObject()
        root.put("app", "GoldLedger")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val contactsArray = JSONArray()
        for (c in contacts) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("type", c.type.name)
                put("notes", c.notes)
                put("createdAt", c.createdAt)
            }
            contactsArray.put(obj)
        }
        root.put("contacts", contactsArray)

        val transactionsArray = JSONArray()
        for (t in transactions) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("contactId", t.contactId)
                put("timestamp", t.timestamp)
                put("type", t.type.name)
                put("goldGrams", t.goldGrams)
                put("goldKarat", t.goldKarat)
                put("goldGrams21Equivalent", t.goldGrams21Equivalent)
                put("cashAmount", t.cashAmount)
                put("notes", t.notes)
            }
            transactionsArray.put(obj)
        }
        root.put("transactions", transactionsArray)

        val remindersArray = JSONArray()
        for (r in reminders) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("contactId", r.contactId)
                put("contactName", r.contactName)
                put("dueDate", r.dueDate)
                put("amountCash", r.amountCash)
                put("amountGoldGrams", r.amountGoldGrams)
                put("isCompleted", r.isCompleted)
                put("notes", r.notes)
                put("createdAt", r.createdAt)
            }
            remindersArray.put(obj)
        }
        root.put("reminders", remindersArray)

        root.toString(2)
    }

    suspend fun exportBackupFile(): BackupResult = withContext(Dispatchers.IO) {
        try {
            val jsonString = createBackupJson()
            val backupDir = File(context.cacheDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)
            val fileName = "GoldLedger_Backup_${dateFormat.format(Date())}.json"
            val file = File(backupDir, fileName)

            FileOutputStream(file).use { out ->
                out.write(jsonString.toByteArray(Charsets.UTF_8))
            }

            val contacts = repository.getAllContactsSnapshot()
            val transactions = repository.getAllTransactionsSnapshot()
            val reminders = repository.getAllRemindersSnapshot()

            BackupResult(
                success = true,
                message = "تم إنشاء النسخة الاحتياطية بنجاح",
                backupFile = file,
                contactCount = contacts.size,
                transactionCount = transactions.size,
                reminderCount = reminders.size
            )
        } catch (e: Exception) {
            BackupResult(
                success = false,
                message = "خطأ أثناء إنشاء النسخة الاحتياطية: ${e.localizedMessage}"
            )
        }
    }

    suspend fun restoreFromUri(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return@withContext BackupResult(false, "تعذر قراءة ملف النسخة الاحتياطية")

            restoreFromJson(content)
        } catch (e: Exception) {
            BackupResult(false, "فشل استعادة النسخة الاحتياطية: ${e.localizedMessage}")
        }
    }

    suspend fun restoreFromJson(jsonString: String): BackupResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("contacts") || !root.has("transactions")) {
                return@withContext BackupResult(false, "تنسيق ملف النسخة الاحتياطية غير صحيح")
            }

            val contactsArray = root.getJSONArray("contacts")
            val contacts = mutableListOf<Contact>()
            for (i in 0 until contactsArray.length()) {
                val obj = contactsArray.getJSONObject(i)
                contacts.add(
                    Contact(
                        id = obj.optLong("id", 0),
                        name = obj.getString("name"),
                        phone = obj.optString("phone", ""),
                        type = ContactType.fromString(obj.optString("type", "CUSTOMER")),
                        notes = obj.optString("notes", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val transactionsArray = root.getJSONArray("transactions")
            val transactions = mutableListOf<GoldTransaction>()
            for (i in 0 until transactionsArray.length()) {
                val obj = transactionsArray.getJSONObject(i)
                val grams = obj.optDouble("goldGrams", 0.0)
                val karat = obj.optInt("goldKarat", 21)
                val eq21 = obj.optDouble(
                    "goldGrams21Equivalent",
                    GoldTransaction.calculate21Equivalent(grams, karat)
                )
                transactions.add(
                    GoldTransaction(
                        id = obj.optLong("id", 0),
                        contactId = obj.getLong("contactId"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        type = TransactionType.fromString(obj.optString("type", "GOLD_GIVEN")),
                        goldGrams = grams,
                        goldKarat = karat,
                        goldGrams21Equivalent = eq21,
                        cashAmount = obj.optDouble("cashAmount", 0.0),
                        notes = obj.optString("notes", "")
                    )
                )
            }

            val reminders = mutableListOf<PaymentReminder>()
            if (root.has("reminders")) {
                val remindersArray = root.getJSONArray("reminders")
                for (i in 0 until remindersArray.length()) {
                    val obj = remindersArray.getJSONObject(i)
                    reminders.add(
                        PaymentReminder(
                            id = obj.optLong("id", 0),
                            contactId = obj.getLong("contactId"),
                            contactName = obj.optString("contactName", ""),
                            dueDate = obj.getLong("dueDate"),
                            amountCash = obj.optDouble("amountCash", 0.0),
                            amountGoldGrams = obj.optDouble("amountGoldGrams", 0.0),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            repository.restoreDatabase(contacts, transactions, reminders)

            BackupResult(
                success = true,
                message = "تمت استعادة البيانات بنجاح: ${contacts.size} حساب و ${transactions.size} حركة",
                contactCount = contacts.size,
                transactionCount = transactions.size,
                reminderCount = reminders.size
            )
        } catch (e: Exception) {
            BackupResult(false, "حدث خطأ أثناء معالجة البيانات: ${e.localizedMessage}")
        }
    }
}
