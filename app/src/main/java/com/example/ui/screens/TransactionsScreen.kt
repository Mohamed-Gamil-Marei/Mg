package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.data.model.GoldTransaction
import com.example.data.model.TransactionType
import com.example.ui.components.FilterChipCustom
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.PayableRed
import com.example.ui.theme.ReceivableGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(
    transactions: List<GoldTransaction>,
    contacts: List<Contact>,
    currency: String,
    onAddNewTransaction: () -> Unit,
    onDeleteTransaction: (GoldTransaction) -> Unit,
    onSelectContact: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<String?>("ALL") }

    val contactsMap = remember(contacts) { contacts.associateBy { it.id } }
    val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH)

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "GOLD" -> transactions.filter { it.type.isGold }
            "CASH" -> transactions.filter { it.type.isCash }
            else -> transactions
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChipCustom(
                    label = "كل الحركات",
                    isSelected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    count = transactions.size
                )

                FilterChipCustom(
                    label = "حركات الذهب",
                    isSelected = selectedFilter == "GOLD",
                    onClick = { selectedFilter = "GOLD" }
                )

                FilterChipCustom(
                    label = "حركات النقدية",
                    isSelected = selectedFilter == "CASH",
                    onClick = { selectedFilter = "CASH" }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredTransactions.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PostAdd, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("لا توجد حركات مسجلة حالياً", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        val contact = contactsMap[tx.contactId]
                        val isPositive = tx.type == TransactionType.GOLD_GIVEN || tx.type == TransactionType.CASH_GIVEN
                        val statusColor = if (isPositive) ReceivableGreen else PayableRed

                        Card(
                            colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = contact?.name ?: "طرف محذوف",
                                        color = GoldPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Surface(
                                        color = statusColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = tx.type.titleAr,
                                            color = statusColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        if (tx.goldGrams > 0) {
                                            Text(
                                                text = "⚖️ ذهب: ${String.format(Locale.ENGLISH, "%.2f", tx.goldGrams)} جرام (عيار ${tx.goldKarat})",
                                                color = GoldBright,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (tx.cashAmount > 0) {
                                            Text(
                                                text = "💵 نقدية: ${String.format(Locale.ENGLISH, "%,.1f", tx.cashAmount)} $currency",
                                                color = ReceivableGreen,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (tx.notes.isNotBlank()) {
                                            Text(
                                                text = "البيان: ${tx.notes}",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Text(
                                            text = dateFormat.format(Date(tx.timestamp)),
                                            color = TextTertiary,
                                            fontSize = 10.sp
                                        )
                                    }

                                    IconButton(onClick = { onDeleteTransaction(tx) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف الحركة", tint = TextTertiary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddNewTransaction,
            containerColor = GoldPrimary,
            contentColor = Color(0xFF1B1607),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_tx_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "تسجيل حركة جديدة")
        }
    }
}
