package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ContactBalanceSummary
import java.io.File
import java.util.Locale
import kotlin.math.abs

object WhatsAppHelper {

    fun generateSummaryMessage(
        summary: ContactBalanceSummary,
        shopName: String = "محل الذهب والمجوهرات",
        goldPrice21k: Double = 3630.0,
        currency: String = "ج.م"
    ): String {
        val contact = summary.contact

        val goldBalance = summary.netGoldGrams21
        val goldStatus = when {
            goldBalance > 0.0001 -> "ليا عندك (مدين بالذهب): ${String.format(Locale.ENGLISH, "%.3f", goldBalance)} جرام عيار 21"
            goldBalance < -0.0001 -> "عليا ليك (دائن بالذهب): ${String.format(Locale.ENGLISH, "%.3f", abs(goldBalance))} جرام عيار 21"
            else -> "رصيد الذهب: خالص (0.00 جرام)"
        }

        val cashBalance = summary.netCash
        val cashStatus = when {
            cashBalance > 0.01 -> "ليا عندك (مدين بالمال): ${String.format(Locale.ENGLISH, "%,.2f", cashBalance)} $currency"
            cashBalance < -0.01 -> "عليا ليك (دائن بالمال): ${String.format(Locale.ENGLISH, "%,.2f", abs(cashBalance))} $currency"
            else -> "رصيد النقدية: خالص (0.00 $currency)"
        }

        val totalEst = cashBalance + (goldBalance * goldPrice21k)
        val estStatus = when {
            totalEst > 0.01 -> "إجمالي المستحق لكلا الطرفين تقديرياً بسعر اليوم: ${String.format(Locale.ENGLISH, "%,.2f", totalEst)} $currency (لصالح المحل)"
            totalEst < -0.01 -> "إجمالي المستحق لكلا الطرفين تقديرياً بسعر اليوم: ${String.format(Locale.ENGLISH, "%,.2f", abs(totalEst))} $currency (لصالحك)"
            else -> "الحساب متزن وخالص بالكامل"
        }

        return """
            السلام عليكم ورحمة الله وبركاته،
            الأخ الفاضل / ${contact.name}
            
            تحية طيبة من [ $shopName ]
            مرفق كشف حسابكم بالتفصيل:
            
            ⚖️ الذهب:
            $goldStatus
            
            💵 النقدية:
            $cashStatus
            
            📊 تقييم الذهب والمال حالياً:
            $estStatus
            
            (تجدون ملف PDF المرفق مشتملاً على جميع تواريخ وتفاصيل الحركات السابقة بدقة)
            شكراً لتعاملكم الراقي معنا!
        """.trimIndent()
    }

    fun sharePdfReport(
        context: Context,
        pdfFile: File,
        message: String,
        targetPhone: String = ""
    ) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_SUBJECT, "كشف حساب ذهب ومال")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Try specifically targeting WhatsApp if available
            val cleanPhone = targetPhone.replace(Regex("[^0-9+]"), "")
            if (cleanPhone.isNotBlank()) {
                // If phone exists, we can target whatsapp with phone or share chooser
                shareIntent.setPackage("com.whatsapp")
            }

            try {
                context.startActivity(shareIntent)
            } catch (e: Exception) {
                // WhatsApp direct package launch failed, open universal system chooser
                shareIntent.setPackage(null)
                val chooser = Intent.createChooser(shareIntent, "مشاركة كشف الحساب عبر واتساب أو تطبيق آخر")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر مشاركة الملف: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareBackupFile(context: Context, backupFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, backupFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية لحسابات الذهب")
                putExtra(Intent.EXTRA_TEXT, "مرفق ملف النسخة الاحتياطية لتطبيق حسابات الذهب والمال.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "مشاركة / حفظ النسخة الاحتياطية")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر مشاركة ملف النسخة: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
