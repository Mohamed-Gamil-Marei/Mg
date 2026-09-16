package com.example.data.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.model.ContactBalanceSummary
import com.example.data.model.GoldMarketPrice
import com.example.data.model.GoldTransaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class PdfReportGenerator(private val context: Context) {

    suspend fun generateAccountStatementPdf(
        summary: ContactBalanceSummary,
        transactions: List<GoldTransaction>,
        shopName: String = "محل الصاغة والمجوهرات",
        goldPrice21k: Double = 3630.0,
        currency: String = "ج.م"
    ): File = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Standard A4 points width
        val pageHeight = 842 // Standard A4 points height

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Background
        canvas.drawColor(Color.WHITE)

        val goldColor = Color.parseColor("#B8860B")
        val darkGoldColor = Color.parseColor("#8A6707")
        val charcoalColor = Color.parseColor("#1C1917")
        val grayBg = Color.parseColor("#F5F2EC")
        val greenColor = Color.parseColor("#15803D")
        val redColor = Color.parseColor("#B91C1C")
        val borderGray = Color.parseColor("#D6CEBF")

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Top Gold Header Bar
        paint.color = darkGoldColor
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 40f, paint)

        // Shop Title
        paint.color = Color.WHITE
        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(shopName, (pageWidth / 2).toFloat(), 26f, paint)

        // Subtitle: كشف حساب
        var currentY = 70f
        paint.color = charcoalColor
        paint.textSize = 18f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("كشف حساب: ${summary.contact.name}", (pageWidth - 30).toFloat(), currentY, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar"))
        val dateStr = "تاريخ التصدير: ${dateFormat.format(Date())}"
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(dateStr, 30f, currentY - 5f, paint)

        val phoneStr = if (summary.contact.phone.isNotBlank()) "الهاتف: ${summary.contact.phone}" else ""
        val typeStr = "الصفة: ${summary.contact.type.titleAr}"
        canvas.drawText("$typeStr  |  $phoneStr", 30f, currentY + 12f, paint)

        currentY += 35f

        // Separator Line
        paint.color = goldColor
        paint.strokeWidth = 2f
        canvas.drawLine(30f, currentY, (pageWidth - 30).toFloat(), currentY, paint)
        currentY += 15f

        // Summary Cards (Gold & Cash Net Balances)
        val cardWidth = (pageWidth - 60 - 20) / 3f
        val cardHeight = 70f

        // Card 1: صافي رصيد الذهب
        val card1Left = 30f
        paint.color = grayBg
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(card1Left, currentY, card1Left + cardWidth, currentY + cardHeight), 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = borderGray
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(card1Left, currentY, card1Left + cardWidth, currentY + cardHeight), 8f, 8f, paint)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("صافي رصيد الذهب (عيار 21)", card1Left + cardWidth / 2, currentY + 20f, paint)

        val goldBalance = summary.netGoldGrams21
        val goldText = String.format(Locale.ENGLISH, "%.3f جرام", abs(goldBalance))
        val goldStatus = if (goldBalance > 0.0001) "ليا عنده (مدين)" else if (goldBalance < -0.0001) "عليا له (دائن)" else "حساب خالص"
        paint.textSize = 13f
        paint.isFakeBoldText = true
        paint.color = if (goldBalance > 0.0001) greenColor else if (goldBalance < -0.0001) redColor else charcoalColor
        canvas.drawText(goldText, card1Left + cardWidth / 2, currentY + 42f, paint)
        paint.textSize = 9f
        canvas.drawText(goldStatus, card1Left + cardWidth / 2, currentY + 58f, paint)

        // Card 2: صافي رصيد النقدية
        val card2Left = card1Left + cardWidth + 10f
        paint.color = grayBg
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(card2Left, currentY, card2Left + cardWidth, currentY + cardHeight), 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = borderGray
        canvas.drawRoundRect(RectF(card2Left, currentY, card2Left + cardWidth, currentY + cardHeight), 8f, 8f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        canvas.drawText("صافي رصيد النقدية", card2Left + cardWidth / 2, currentY + 20f, paint)

        val cashBalance = summary.netCash
        val cashText = String.format(Locale.ENGLISH, "%,.2f %s", abs(cashBalance), currency)
        val cashStatus = if (cashBalance > 0.01) "ليا عنده (مدين)" else if (cashBalance < -0.01) "عليا له (دائن)" else "حساب خالص"
        paint.textSize = 13f
        paint.isFakeBoldText = true
        paint.color = if (cashBalance > 0.01) greenColor else if (cashBalance < -0.01) redColor else charcoalColor
        canvas.drawText(cashText, card2Left + cardWidth / 2, currentY + 42f, paint)
        paint.textSize = 9f
        canvas.drawText(cashStatus, card2Left + cardWidth / 2, currentY + 58f, paint)

        // Card 3: القيمة التقديرية الإجمالية
        val card3Left = card2Left + cardWidth + 10f
        paint.color = grayBg
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(card3Left, currentY, card3Left + cardWidth, currentY + cardHeight), 8f, 8f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = borderGray
        canvas.drawRoundRect(RectF(card3Left, currentY, card3Left + cardWidth, currentY + cardHeight), 8f, 8f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        canvas.drawText("التقييم الشامل بالسعر الحالي", card3Left + cardWidth / 2, currentY + 20f, paint)

        val totalEstimate = summary.netCash + (summary.netGoldGrams21 * goldPrice21k)
        val estText = String.format(Locale.ENGLISH, "%,.2f %s", abs(totalEstimate), currency)
        val estStatus = if (totalEstimate > 0.01) "إجمالي ليا" else if (totalEstimate < -0.01) "إجمالي عليا" else "متزن"
        paint.textSize = 13f
        paint.isFakeBoldText = true
        paint.color = darkGoldColor
        canvas.drawText(estText, card3Left + cardWidth / 2, currentY + 42f, paint)
        paint.textSize = 9f
        canvas.drawText(estStatus, card3Left + cardWidth / 2, currentY + 58f, paint)

        currentY += cardHeight + 25f

        // Table Header: سجل الحركات
        paint.color = charcoalColor
        paint.textSize = 14f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("جدول الحركات والمعاملات (${transactions.size} حركة):", (pageWidth - 30).toFloat(), currentY, paint)
        currentY += 12f

        // Table Column Config
        // Total usable width = pageWidth - 60 = 535
        val colDateW = 75f
        val colTypeW = 105f
        val colGoldW = 85f
        val colKaratW = 45f
        val colCashW = 95f
        val colNotesW = 130f

        val tableLeft = 30f
        val tableRight = (pageWidth - 30).toFloat()

        // Draw Table Header Bar
        paint.color = darkGoldColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(tableLeft, currentY, tableRight, currentY + 26f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER

        // Columns from Left to Right
        var colX = tableLeft
        canvas.drawText("التاريخ", colX + colDateW / 2, currentY + 17f, paint)
        colX += colDateW
        canvas.drawText("نوع الحركة", colX + colTypeW / 2, currentY + 17f, paint)
        colX += colTypeW
        canvas.drawText("الذهب (جرام)", colX + colGoldW / 2, currentY + 17f, paint)
        colX += colGoldW
        canvas.drawText("العيار", colX + colKaratW / 2, currentY + 17f, paint)
        colX += colKaratW
        canvas.drawText("النقدية ($currency)", colX + colCashW / 2, currentY + 17f, paint)
        colX += colKaratW
        canvas.drawText("البيان والملاحظات", colX + colNotesW / 2, currentY + 17f, paint)

        currentY += 26f

        // Draw Table Rows
        val rowDateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
        val maxRowsPerPage = 22
        val rowsToDraw = transactions.take(maxRowsPerPage)

        for ((index, tx) in rowsToDraw.withIndex()) {
            val rowHeight = 22f
            // Alternate background
            if (index % 2 == 1) {
                paint.color = Color.parseColor("#F9F8F5")
                paint.style = Paint.Style.FILL
                canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, paint)
            }

            paint.color = borderGray
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawLine(tableLeft, currentY + rowHeight, tableRight, currentY + rowHeight, paint)

            paint.style = Paint.Style.FILL
            paint.isFakeBoldText = false
            paint.textSize = 9.5f
            paint.color = charcoalColor
            paint.textAlign = Paint.Align.CENTER

            var rx = tableLeft
            // Date
            canvas.drawText(rowDateFormat.format(Date(tx.timestamp)), rx + colDateW / 2, currentY + 15f, paint)
            rx += colDateW

            // Type
            val isPositive = tx.type == TransactionType.GOLD_GIVEN || tx.type == TransactionType.CASH_GIVEN
            paint.color = if (isPositive) greenColor else redColor
            canvas.drawText(tx.type.titleAr, rx + colTypeW / 2, currentY + 15f, paint)
            paint.color = charcoalColor
            rx += colTypeW

            // Gold Grams
            val goldStr = if (tx.goldGrams > 0) String.format(Locale.ENGLISH, "%.2f ج", tx.goldGrams) else "-"
            canvas.drawText(goldStr, rx + colGoldW / 2, currentY + 15f, paint)
            rx += colGoldW

            // Karat
            val karatStr = if (tx.goldGrams > 0) "ع ${tx.goldKarat}" else "-"
            canvas.drawText(karatStr, rx + colKaratW / 2, currentY + 15f, paint)
            rx += colKaratW

            // Cash
            val cashStr = if (tx.cashAmount > 0) String.format(Locale.ENGLISH, "%,.1f", tx.cashAmount) else "-"
            canvas.drawText(cashStr, rx + colCashW / 2, currentY + 15f, paint)
            rx += colCashW

            // Notes
            val notesStr = if (tx.notes.isNotBlank()) tx.notes.take(22) else "-"
            canvas.drawText(notesStr, rx + colNotesW / 2, currentY + 15f, paint)

            currentY += rowHeight
        }

        if (transactions.size > maxRowsPerPage) {
            paint.color = Color.GRAY
            paint.textSize = 9f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("... وهناك ${transactions.size - maxRowsPerPage} حركات أخرى سابقة مسجلة بالتطبيق", (pageWidth / 2).toFloat(), currentY + 15f, paint)
            currentY += 20f
        }

        // Footer Note & Signatures
        val footerY = pageHeight - 50f
        paint.color = goldColor
        paint.strokeWidth = 1f
        canvas.drawLine(30f, footerY - 15f, (pageWidth - 30).toFloat(), footerY - 15f, paint)

        paint.color = Color.GRAY
        paint.textSize = 9f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("توقيع صاحب المحل / الختم: ............................", (pageWidth - 35).toFloat(), footerY, paint)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("توقيع العميل / المورد: ............................", 35f, footerY, paint)

        pdfDocument.finishPage(page)

        // Write to File
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val safeContactName = summary.contact.name.replace(Regex("[^\\p{L}\\p{Nd}]+"), "_")
        val fileName = "كشف_حساب_${safeContactName}_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(reportsDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        pdfFile
    }
}
