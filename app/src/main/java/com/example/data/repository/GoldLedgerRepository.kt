package com.example.data.repository

import com.example.data.local.ContactDao
import com.example.data.local.ReminderDao
import com.example.data.local.TransactionDao
import com.example.data.model.Contact
import com.example.data.model.ContactBalanceSummary
import com.example.data.model.ContactType
import com.example.data.model.GoldMarketPrice
import com.example.data.model.GoldTransaction
import com.example.data.model.PaymentReminder
import com.example.data.model.ShopOverallStats
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GoldLedgerRepository(
    private val contactDao: ContactDao,
    private val transactionDao: TransactionDao,
    private val reminderDao: ReminderDao
) {
    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()
    val allTransactions: Flow<List<GoldTransaction>> = transactionDao.getAllTransactions()
    val allReminders: Flow<List<PaymentReminder>> = reminderDao.getAllReminders()
    val pendingReminders: Flow<List<PaymentReminder>> = reminderDao.getPendingReminders()

    fun getTransactionsForContact(contactId: Long): Flow<List<GoldTransaction>> {
        return transactionDao.getTransactionsForContact(contactId)
    }

    suspend fun getContactById(contactId: Long): Contact? {
        return contactDao.getContactById(contactId)
    }

    suspend fun getTransactionsForContactSnapshot(contactId: Long): List<GoldTransaction> {
        return transactionDao.getTransactionsForContactSnapshot(contactId)
    }

    suspend fun insertContact(contact: Contact): Long {
        return contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: Contact) {
        contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: Contact) {
        contactDao.deleteContact(contact)
    }

    suspend fun insertTransaction(transaction: GoldTransaction): Long {
        val calculated21 = GoldTransaction.calculate21Equivalent(
            transaction.goldGrams,
            transaction.goldKarat
        )
        val adjusted = transaction.copy(goldGrams21Equivalent = calculated21)
        return transactionDao.insertTransaction(adjusted)
    }

    suspend fun updateTransaction(transaction: GoldTransaction) {
        val calculated21 = GoldTransaction.calculate21Equivalent(
            transaction.goldGrams,
            transaction.goldKarat
        )
        val adjusted = transaction.copy(goldGrams21Equivalent = calculated21)
        transactionDao.updateTransaction(adjusted)
    }

    suspend fun deleteTransaction(transaction: GoldTransaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun insertReminder(reminder: PaymentReminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun setReminderCompleted(id: Long, isCompleted: Boolean) {
        reminderDao.setReminderCompleted(id, isCompleted)
    }

    suspend fun deleteReminder(reminder: PaymentReminder) {
        reminderDao.deleteReminder(reminder)
    }

    /**
     * Combines contacts and transactions to compute balance summaries for each contact
     */
    fun getContactSummaries(): Flow<List<ContactBalanceSummary>> {
        return combine(allContacts, allTransactions) { contacts, transactions ->
            val txByContact = transactions.groupBy { it.contactId }

            contacts.map { contact ->
                val list = txByContact[contact.id] ?: emptyList()
                calculateContactSummary(contact, list)
            }
        }
    }

    fun getShopOverallStats(goldPrice21k: Double): Flow<ShopOverallStats> {
        return combine(allContacts, allTransactions) { contacts, transactions ->
            var goldOwedToMe = 0.0  // ليا ذهب (أعطيته ذهب / هو مدين بالذهب)
            var goldOwedByMe = 0.0  // عليا ذهب (استلمت منه ذهب / أنا مدين بالذهب)
            var cashOwedToMe = 0.0  // ليا نقدية (أعطيته نقدية / هو مدين بالمال)
            var cashOwedByMe = 0.0  // عليا نقدية (استلمت منه نقدية / أنا مدين بالمال)

            val txByContact = transactions.groupBy { it.contactId }
            for (contact in contacts) {
                val list = txByContact[contact.id] ?: emptyList()
                val summary = calculateContactSummary(contact, list)
                if (summary.netGoldGrams21 > 0) {
                    goldOwedToMe += summary.netGoldGrams21
                } else {
                    goldOwedByMe += kotlin.math.abs(summary.netGoldGrams21)
                }

                if (summary.netCash > 0) {
                    cashOwedToMe += summary.netCash
                } else {
                    cashOwedByMe += kotlin.math.abs(summary.netCash)
                }
            }

            val netGold = goldOwedToMe - goldOwedByMe
            val netCash = cashOwedToMe - cashOwedByMe
            val goldCashValue = netGold * goldPrice21k

            ShopOverallStats(
                totalGoldOwedToMe21 = goldOwedToMe,
                totalGoldOwedByMe21 = goldOwedByMe,
                netGoldBalance21 = netGold,
                totalCashOwedToMe = cashOwedToMe,
                totalCashOwedByMe = cashOwedByMe,
                netCashBalance = netCash,
                totalContacts = contacts.size,
                totalCustomers = contacts.count { it.type == ContactType.CUSTOMER },
                totalSuppliers = contacts.count { it.type == ContactType.SUPPLIER },
                estimatedNetValueWithGoldPrice = netCash + goldCashValue
            )
        }
    }

    companion object {
        fun calculateContactSummary(contact: Contact, transactions: List<GoldTransaction>): ContactBalanceSummary {
            var totalGoldGiven21 = 0.0
            var totalGoldReceived21 = 0.0
            var totalCashGiven = 0.0
            var totalCashReceived = 0.0

            for (tx in transactions) {
                val eq21 = if (tx.goldGrams21Equivalent > 0) tx.goldGrams21Equivalent
                else GoldTransaction.calculate21Equivalent(tx.goldGrams, tx.goldKarat)

                when (tx.type) {
                    TransactionType.GOLD_GIVEN -> {
                        totalGoldGiven21 += eq21
                    }
                    TransactionType.GOLD_RECEIVED -> {
                        totalGoldReceived21 += eq21
                    }
                    TransactionType.CASH_GIVEN -> {
                        totalCashGiven += tx.cashAmount
                    }
                    TransactionType.CASH_RECEIVED -> {
                        totalCashReceived += tx.cashAmount
                    }
                    TransactionType.SETTLEMENT -> {
                        // Settlement can adjust both or either
                        if (tx.goldGrams > 0) {
                            totalGoldReceived21 += eq21
                        }
                        if (tx.cashAmount > 0) {
                            totalCashReceived += tx.cashAmount
                        }
                    }
                }
            }

            // netGoldGrams21: positive = shop is owed gold (ليا عنده), negative = shop owes gold (عليا له)
            val netGold = totalGoldGiven21 - totalGoldReceived21
            // netCash: positive = shop is owed cash (ليا عنده), negative = shop owes cash (عليا له)
            val netCash = totalCashGiven - totalCashReceived

            val lastDate = transactions.maxOfOrNull { it.timestamp }

            return ContactBalanceSummary(
                contact = contact,
                netGoldGrams21 = netGold,
                netCash = netCash,
                totalGoldGiven21 = totalGoldGiven21,
                totalGoldReceived21 = totalGoldReceived21,
                totalCashGiven = totalCashGiven,
                totalCashReceived = totalCashReceived,
                transactionCount = transactions.size,
                lastTransactionDate = lastDate
            )
        }
    }

    // Backup & Restore snapshots
    suspend fun getAllContactsSnapshot(): List<Contact> = contactDao.getAllContactsSnapshot()
    suspend fun getAllTransactionsSnapshot(): List<GoldTransaction> = transactionDao.getAllTransactionsSnapshot()
    suspend fun getAllRemindersSnapshot(): List<PaymentReminder> = reminderDao.getAllRemindersSnapshot()

    suspend fun restoreDatabase(
        contacts: List<Contact>,
        transactions: List<GoldTransaction>,
        reminders: List<PaymentReminder>
    ) {
        reminderDao.clearAll()
        transactionDao.clearAll()
        contactDao.clearAll()

        contactDao.insertContacts(contacts)
        transactionDao.insertTransactions(transactions)
        reminderDao.insertReminders(reminders)
    }
}
