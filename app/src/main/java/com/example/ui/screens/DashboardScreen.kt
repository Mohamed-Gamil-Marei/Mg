package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoldMarketPrice
import com.example.data.model.GoldTransaction
import com.example.data.model.PaymentReminder
import com.example.data.model.ShopOverallStats
import com.example.data.model.TransactionType
import com.example.ui.components.StatCard
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
import com.example.ui.theme.ReceivableGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun DashboardScreen(
    stats: ShopOverallStats,
    goldPrice: GoldMarketPrice,
    recentTransactions: List<GoldTransaction>,
    reminders: List<PaymentReminder>,
    onOpenGoldPriceEdit: () -> Unit,
    onQuickAddTransaction: () -> Unit,
    onQuickAddContact: () -> Unit,
    onQuickAddReminder: () -> Unit,
    onViewAllAccounts: () -> Unit,
    onViewAllTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH)
    val pendingReminders = reminders.filter { !it.isCompleted }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Gold Price Board (تحديث أسعار الذهب بشكل يومي ومباشر)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gold_price_card")
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    GoldContainer.copy(alpha = 0.25f),
                                    ObsidianCard
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Scale,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "أسعار الذهب اليوم (الصاغة)",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "آخر تحديث: ${SimpleDateFormat("hh:mm a - yyyy/MM/dd", Locale.ENGLISH).format(Date(goldPrice.lastUpdated))}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onOpenGoldPriceEdit,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ObsidianSurfaceVariant)
                                .testTag("edit_gold_price_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل الأسعار",
                                tint = GoldPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4-Column Gold Rates: 24K, 21K, 18K, الجنيه
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 21K (Highlight with subtle warm accent)
                        Surface(
                            color = GoldContainer,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text("عيار 21 (الأساس)", color = OnGoldContainer, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${goldPrice.price21k.toInt()}", color = GoldLight, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                Text(goldPrice.currency, color = OnGoldContainer.copy(alpha = 0.8f), fontSize = 10.sp)
                            }
                        }

                        // 24K
                        Surface(
                            color = ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text("عيار 24", color = TextSecondary, fontSize = 11.sp)
                                Text("${goldPrice.price24k.toInt()}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(goldPrice.currency, color = TextTertiary, fontSize = 10.sp)
                            }
                        }

                        // 18K
                        Surface(
                            color = ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text("عيار 18", color = TextSecondary, fontSize = 11.sp)
                                Text("${goldPrice.price18k.toInt()}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(goldPrice.currency, color = TextTertiary, fontSize = 10.sp)
                            }
                        }

                        // الجنيه الذهب
                        Surface(
                            color = ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text("جنيه دهب", color = TextSecondary, fontSize = 11.sp)
                                Text("${goldPrice.goldPoundPrice.toInt()}", color = GoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(goldPrice.currency, color = TextTertiary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. Comprehensive Wealth Valuation Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("التقييم الإجمالي لصافي ديون السوق", color = TextSecondary, fontSize = 12.sp)
                            val isNetPositive = stats.estimatedNetValueWithGoldPrice >= 0
                            val netColor = if (isNetPositive) ReceivableGreen else PayableRed
                            Text(
                                text = "${String.format(Locale.ENGLISH, "%,.2f", abs(stats.estimatedNetValueWithGoldPrice))} ${goldPrice.currency}",
                                color = netColor,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isNetPositive) "✨ إجمالي الصافي لصالح المحل (ليا في السوق)" else "⚠️ إجمالي الصافي مطلوب من المحل (عليا للسوق)",
                                color = netColor.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Surface(
                            color = ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("الحسابات", color = TextSecondary, fontSize = 11.sp)
                                Text("${stats.totalContacts}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 3. Stat Card 1: Gold Debts (جرام عيار 21)
        item {
            StatCard(
                title = "ديون وحسابات الذهب (بالجرام عيار 21)",
                owedToMe = stats.totalGoldOwedToMe21,
                owedByMe = stats.totalGoldOwedByMe21,
                unit = "جرام",
                icon = Icons.Default.Scale,
                accentColor = GoldPrimary
            )
        }

        // 4. Stat Card 2: Cash Debts (بالعملة)
        item {
            StatCard(
                title = "ديون وحسابات النقدية والمال",
                owedToMe = stats.totalCashOwedToMe,
                owedByMe = stats.totalCashOwedByMe,
                unit = goldPrice.currency,
                icon = Icons.Outlined.AccountBalanceWallet,
                accentColor = ReceivableGreen
            )
        }

        // 5. Quick Action Grid
        item {
            Column {
                Text("إجراءات سريعة:", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onQuickAddTransaction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color(0xFF1B1607)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_tx_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حركة جديدة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onQuickAddContact,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldContainer,
                            contentColor = OnGoldContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_contact_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("عميل / مورد", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // 6. Upcoming Payment Reminders Preview (if any)
        if (pendingReminders.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GoldLight, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("مواعيد سداد مستحقة قريباً (${pendingReminders.size})", color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        pendingReminders.take(3).forEach { reminder ->
                            val isOverdue = reminder.dueDate < System.currentTimeMillis()
                            val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(reminder.dueDate))

                            Surface(
                                color = ObsidianSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Column {
                                        Text(reminder.contactName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = if (isOverdue) "⚠️ متأخر: $dateStr" else "🗓️ المستحق: $dateStr",
                                            color = if (isOverdue) PayableRed else TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (reminder.amountCash > 0 || reminder.amountGoldGrams > 0) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            if (reminder.amountCash > 0) {
                                                Text("${String.format(Locale.ENGLISH, "%,.0f", reminder.amountCash)} ج.م", color = ReceivableGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                            if (reminder.amountGoldGrams > 0) {
                                                Text("${reminder.amountGoldGrams} جرام دهب", color = GoldPrimary, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Recent Transactions Header & List
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("أحدث المعاملات المسجلة:", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "عرض الكل",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onViewAllTransactions() }
                )
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Surface(
                    color = ObsidianCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "لا توجد معاملات مسجلة حتى الآن. اضغط على 'حركة جديدة' للبدء.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentTransactions.take(5)) { tx ->
                val isPositive = tx.type == TransactionType.GOLD_GIVEN || tx.type == TransactionType.CASH_GIVEN
                val color = if (isPositive) ReceivableGreen else PayableRed

                Card(
                    colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column {
                            Text(tx.type.titleAr, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH).format(Date(tx.timestamp)),
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                            if (tx.notes.isNotBlank()) {
                                Text(tx.notes, color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            if (tx.goldGrams > 0) {
                                Text(
                                    text = "${String.format(Locale.ENGLISH, "%.2f", tx.goldGrams)} ج (ع ${tx.goldKarat})",
                                    color = GoldPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (tx.cashAmount > 0) {
                                Text(
                                    text = "${String.format(Locale.ENGLISH, "%,.1f", tx.cashAmount)} ${goldPrice.currency}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
