package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.data.model.ContactBalanceSummary
import com.example.data.model.ContactType
import com.example.ui.components.BalanceStatusBadge
import com.example.ui.components.FilterChipCustom
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
import java.util.Locale

@Composable
fun AccountsScreen(
    summaries: List<ContactBalanceSummary>,
    searchQuery: String,
    selectedFilter: ContactType?,
    onSearchChange: (String) -> Unit,
    onFilterSelect: (ContactType?) -> Unit,
    onSelectContactForStatement: (Long) -> Unit,
    onAddTransactionForContact: (Long) -> Unit,
    onAddNewContact: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("بحث بالاسم أو رقم الهاتف...", color = TextSecondary, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "بحث", tint = GoldPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = ObsidianCard,
                    unfocusedContainerColor = ObsidianCard
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accounts_search_field")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (الكل، عملاء، موردين)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChipCustom(
                    label = "الكل",
                    isSelected = selectedFilter == null,
                    onClick = { onFilterSelect(null) },
                    count = summaries.size
                )

                FilterChipCustom(
                    label = "العملاء",
                    isSelected = selectedFilter == ContactType.CUSTOMER,
                    onClick = { onFilterSelect(ContactType.CUSTOMER) }
                )

                FilterChipCustom(
                    label = "الموردين / التجار",
                    isSelected = selectedFilter == ContactType.SUPPLIER,
                    onClick = { onFilterSelect(ContactType.SUPPLIER) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Accounts List
            if (summaries.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "لا توجد حسابات مسجلة بعد" else "لا توجد نتائج مطابقة للبحث",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "اضغط على زر '+' بالأسفل لإضافة عميل أو مورد جديد",
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(summaries, key = { it.contact.id }) { summary ->
                        ContactCardItem(
                            summary = summary,
                            onOpenStatement = { onSelectContactForStatement(summary.contact.id) },
                            onAddTransaction = { onAddTransactionForContact(summary.contact.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to add contact
        FloatingActionButton(
            onClick = onAddNewContact,
            containerColor = GoldPrimary,
            contentColor = Color(0xFF1B1607),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_contact_fab")
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "إضافة عميل أو مورد")
        }
    }
}

@Composable
fun ContactCardItem(
    summary: ContactBalanceSummary,
    onOpenStatement: () -> Unit,
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contact = summary.contact
    val isCustomer = contact.type == ContactType.CUSTOMER

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenStatement() }
            .testTag("contact_card_${contact.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Name, Type Badge, Phone
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isCustomer) GoldContainer else ObsidianSurfaceVariant)
                    ) {
                        Text(
                            text = contact.name.take(1),
                            color = if (isCustomer) OnGoldContainer else GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = contact.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (contact.phone.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(contact.phone, color = TextTertiary, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Surface(
                    color = if (isCustomer) GoldPrimary.copy(alpha = 0.15f) else ObsidianSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = contact.type.titleAr,
                        color = if (isCustomer) GoldPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Balances Row (Gold 21K & Cash)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Gold Balance
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(ObsidianSurface, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Text("رصيد الذهب (عيار 21):", color = TextSecondary, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    val goldBalance = summary.netGoldGrams21
                    val goldText = if (goldBalance > 0.0001) {
                        "ليا: ${String.format(Locale.ENGLISH, "%.2f", goldBalance)} ج"
                    } else if (goldBalance < -0.0001) {
                        "عليا: ${String.format(Locale.ENGLISH, "%.2f", kotlin.math.abs(goldBalance))} ج"
                    } else {
                        "خالص (0.00 ج)"
                    }
                    val goldColor = if (goldBalance > 0.0001) ReceivableGreen else if (goldBalance < -0.0001) PayableRed else TextSecondary

                    Text(
                        text = goldText,
                        color = goldColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Cash Balance
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(ObsidianSurface, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Text("رصيد النقدية:", color = TextSecondary, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    val cashBalance = summary.netCash
                    val cashText = if (cashBalance > 0.01) {
                        "ليا: ${String.format(Locale.ENGLISH, "%,.0f", cashBalance)} ج.م"
                    } else if (cashBalance < -0.01) {
                        "عليا: ${String.format(Locale.ENGLISH, "%,.0f", kotlin.math.abs(cashBalance))} ج.م"
                    } else {
                        "خالص (0.00)"
                    }
                    val cashColor = if (cashBalance > 0.01) ReceivableGreen else if (cashBalance < -0.01) PayableRed else TextSecondary

                    Text(
                        text = cashText,
                        color = cashColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action footer
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${summary.transactionCount} حركة مسجلة",
                    color = TextTertiary,
                    fontSize = 11.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = ObsidianSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onAddTransaction() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تسجيل حركة", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Surface(
                        color = GoldPrimary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenStatement() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF1B1607), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("كشف الحساب", color = Color(0xFF1B1607), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
