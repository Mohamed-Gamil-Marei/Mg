package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.data.model.ContactBalanceSummary
import com.example.data.model.ContactType
import com.example.data.model.GoldMarketPrice
import com.example.data.model.GoldTransaction
import com.example.data.model.TransactionType
import com.example.ui.components.BalanceStatusBadge
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.OnGoldContainer
import com.example.ui.theme.PayableRed
import com.example.ui.theme.PayableRedBg
import com.example.ui.theme.ReceivableGreen
import com.example.ui.theme.ReceivableGreenBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun AccountStatementScreen(
    summary: ContactBalanceSummary,
    transactions: List<GoldTransaction>,
    goldPrice: GoldMarketPrice,
    onBack: () -> Unit,
    onExportPdfAndShareWhatsApp: () -> Unit,
    onAddTransaction: () -> Unit,
    onAddReminder: () -> Unit,
    onEditContact: () -> Unit,
    onDeleteContact: () -> Unit,
    onDeleteTransaction: (GoldTransaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contact = summary.contact
    val isCustomer = contact.type == ContactType.CUSTOMER
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianSurface)
    ) {
        // Top Header Bar
        Surface(
            color = ObsidianCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("statement_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = GoldPrimary
                        )
                    }

                    Column {
                        Text(
                            text = contact.name,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "كشف حساب: ${contact.type.titleAr}",
                            color = GoldLight,
                            fontSize = 11.sp
                        )
                    }
                }

                Row {
                    if (contact.phone.isNotBlank()) {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "اتصال", tint = ReceivableGreen)
                        }
                    }

                    IconButton(onClick = onEditContact) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل الحساب", tint = TextSecondary)
                    }

                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف الحساب", tint = PayableRed)
                    }
                }
            }
        }

        // Body Content
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Balance Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        GoldContainer.copy(alpha = 0.2f),
                                        ObsidianCard
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Text("الرصيد الصافي الحالي لهذا الحساب:", color = TextSecondary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Gold & Cash Dual Balance
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Gold Balance
                            val goldBal = summary.netGoldGrams21
                            val goldColor = if (goldBal > 0.0001) ReceivableGreen else if (goldBal < -0.0001) PayableRed else TextSecondary
                            val goldLabel = if (goldBal > 0.0001) "ليا عنده (ذهب)" else if (goldBal < -0.0001) "عليا له (ذهب)" else "خالص"

                            Surface(
                                color = ObsidianSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, goldColor.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(goldLabel, color = goldColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${String.format(Locale.ENGLISH, "%.3f", abs(goldBal))} جرام",
                                        color = TextPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text("معيار 21 موحد", color = TextTertiary, fontSize = 10.sp)
                                }
                            }

                            // Cash Balance
                            val cashBal = summary.netCash
                            val cashColor = if (cashBal > 0.01) ReceivableGreen else if (cashBal < -0.01) PayableRed else TextSecondary
                            val cashLabel = if (cashBal > 0.01) "ليا عنده (فلوس)" else if (cashBal < -0.01) "عليا له (فلوس)" else "خالص"

                            Surface(
                                color = ObsidianSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, cashColor.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(cashLabel, color = cashColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${String.format(Locale.ENGLISH, "%,.0f", abs(cashBal))} ${goldPrice.currency}",
                                        color = TextPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text("نقدية صافية", color = TextTertiary, fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Total Estimated Equivalent in Cash
                        val totalEstimate = summary.netCash + (summary.netGoldGrams21 * goldPrice.price21k)
                        val estColor = if (totalEstimate >= 0) ReceivableGreen else PayableRed
                        val estLabel = if (totalEstimate >= 0) "إجمالي مستحق للمحل تقديرياً" else "إجمالي مطلوب من المحل تقديرياً"

                        Surface(
                            color = ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(estLabel, color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        text = "بسعر الذهب اليوم (${goldPrice.price21k.toInt()} ${goldPrice.currency}/جرام)",
                                        color = TextTertiary,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = "${String.format(Locale.ENGLISH, "%,.1f", abs(totalEstimate))} ${goldPrice.currency}",
                                    color = estColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Core Actions: PDF Export & Direct WhatsApp Sharing
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Export PDF & WhatsApp Share Button (User Core Requirement)
                    Button(
                        onClick = onExportPdfAndShareWhatsApp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366), // WhatsApp Green
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_pdf_whatsapp_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تصدير كشف حساب PDF ومشاركته عبر واتساب",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Secondary Action Row: Add Transaction & Add Reminder
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onAddTransaction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color(0xFF1B1607)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_tx_for_contact_button")
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حركة جديدة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onAddReminder,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldContainer,
                                contentColor = OnGoldContainer
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_reminder_for_contact_button")
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("موعد سداد", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. Transactions History Header
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "سجل الحركات والمعاملات (${transactions.size}):",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 4. Transactions List
            if (transactions.isEmpty()) {
                item {
                    Surface(
                        color = ObsidianCard,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "لا توجد أي حركات مسجلة لهذا الحساب حتى الآن.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    val isPositive = tx.type == TransactionType.GOLD_GIVEN || tx.type == TransactionType.CASH_GIVEN
                    val statusColor = if (isPositive) ReceivableGreen else PayableRed

                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = tx.type.titleAr,
                                    color = statusColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = dateFormat.format(Date(tx.timestamp)),
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    if (tx.goldGrams > 0) {
                                        Text(
                                            text = "⚖️ ذهب: ${String.format(Locale.ENGLISH, "%.3f", tx.goldGrams)} جرام (عيار ${tx.goldKarat})",
                                            color = GoldBright,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "المعادل عيار 21: ${String.format(Locale.ENGLISH, "%.3f", tx.goldGrams21Equivalent)} جرام",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (tx.cashAmount > 0) {
                                        Text(
                                            text = "💵 نقدية: ${String.format(Locale.ENGLISH, "%,.2f", tx.cashAmount)} ${goldPrice.currency}",
                                            color = ReceivableGreen,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    if (tx.notes.isNotBlank()) {
                                        Text(
                                            text = "ملاحظات: ${tx.notes}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDeleteTransaction(tx) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف الحركة",
                                        tint = TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Contact Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("حذف الحساب بالكامل؟", color = TextPrimary) },
            text = {
                Text(
                    "هل أنت متأكد من رغبتك في حذف حساب '${contact.name}'؟ سيتم حذف جميع الحركات والديون والتنبيهات المسجلة له نهائياً.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteContact()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PayableRed)
                ) {
                    Text("تأكيد الحذف", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            },
            containerColor = ObsidianCard
        )
    }
}
