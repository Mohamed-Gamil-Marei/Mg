package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentReminder
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
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
fun RemindersScreen(
    reminders: List<PaymentReminder>,
    onToggleCompleted: (PaymentReminder) -> Unit,
    onDeleteReminder: (PaymentReminder) -> Unit,
    onAddNewReminder: () -> Unit,
    onTriggerTestNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
    val now = System.currentTimeMillis()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Header Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("تنبيهات مواعيد السداد", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("إشعارات بمواعيد الدفعات واستحقاق الذهب", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = onTriggerTestNotification,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldContainer,
                            contentColor = OnGoldContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("إشعار تجريبي", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (reminders.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("لا توجد مواعيد سداد مجدولة حالياً", color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("اضغط على '+' بالأسفل لجدولة موعد سداد جديد", color = TextTertiary, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        val isOverdue = reminder.dueDate < now && !reminder.isCompleted
                        val isDueToday = abs(reminder.dueDate - now) < 24 * 60 * 60 * 1000

                        val statusLabel = when {
                            reminder.isCompleted -> "تم السداد / مكتمل"
                            isOverdue -> "متأخر عن الموعد"
                            isDueToday -> "مستحق اليوم"
                            else -> "قادم في موعده"
                        }

                        val statusColor = when {
                            reminder.isCompleted -> TextSecondary
                            isOverdue -> PayableRed
                            isDueToday -> GoldBright
                            else -> ReceivableGreen
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isOverdue) PayableRed.copy(alpha = 0.5f) else ObsidianBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Checkbox(
                                    checked = reminder.isCompleted,
                                    onCheckedChange = { onToggleCompleted(reminder) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ReceivableGreen,
                                        uncheckedColor = TextSecondary
                                    )
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = reminder.contactName,
                                            color = if (reminder.isCompleted) TextSecondary else TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Surface(
                                            color = statusColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = statusLabel,
                                                color = statusColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "تاريخ الاستحقاق: ${dateFormat.format(Date(reminder.dueDate))}",
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )

                                    if (reminder.amountCash > 0 || reminder.amountGoldGrams > 0) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            if (reminder.amountCash > 0) {
                                                Text(
                                                    text = "المبلغ: ${String.format(Locale.ENGLISH, "%,.0f", reminder.amountCash)} ج.م",
                                                    color = ReceivableGreen,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            if (reminder.amountGoldGrams > 0) {
                                                Text(
                                                    text = "الذهب: ${reminder.amountGoldGrams} جرام",
                                                    color = GoldPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    if (reminder.notes.isNotBlank()) {
                                        Text(
                                            text = reminder.notes,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(onClick = { onDeleteReminder(reminder) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = TextTertiary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddNewReminder,
            containerColor = GoldPrimary,
            contentColor = Color(0xFF1B1607),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_reminder_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة موعد سداد جديد")
        }
    }
}
