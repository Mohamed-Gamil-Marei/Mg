package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Contact
import com.example.data.model.ContactType
import com.example.data.model.GoldTransaction
import com.example.data.model.TransactionType
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddContactDialog(
    initialContact: Contact? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, type: ContactType, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialContact?.name ?: "") }
    var phone by remember { mutableStateOf(initialContact?.phone ?: "") }
    var type by remember { mutableStateOf(initialContact?.type ?: ContactType.CUSTOMER) }
    var notes by remember { mutableStateOf(initialContact?.notes ?: "") }
    var nameError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ObsidianSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (initialContact == null) "إضافة عميل أو مورد جديد" else "تعديل بيانات الحساب",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_contact_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Type selection (عميل أو مورد)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = if (type == ContactType.CUSTOMER) GoldPrimary else ObsidianSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { type = ContactType.CUSTOMER }
                    ) {
                        Text(
                            text = "عميل (زبون)",
                            color = if (type == ContactType.CUSTOMER) Color(0xFF1B1607) else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                        )
                    }

                    Surface(
                        color = if (type == ContactType.SUPPLIER) GoldPrimary else ObsidianSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { type = ContactType.SUPPLIER }
                    ) {
                        Text(
                            text = "مورد (تاجر / ورشة)",
                            color = if (type == ContactType.SUPPLIER) Color(0xFF1B1607) else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("الاسم بالكامل *") },
                    isError = nameError,
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = GoldPrimary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف (للتواصل وإرسال واتساب)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = GoldPrimary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_phone_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية (اختياري)") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_notes_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                            } else {
                                onConfirm(name, phone, type, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color(0xFF1B1607)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_contact_button")
                    ) {
                        Text(
                            text = if (initialContact == null) "حفظ الحساب" else "تحديث",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    contacts: List<Contact>,
    preSelectedContactId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        contactId: Long,
        type: TransactionType,
        goldGrams: Double,
        goldKarat: Int,
        cashAmount: Double,
        notes: String
    ) -> Unit
) {
    var selectedContact by remember {
        mutableStateOf(
            contacts.find { it.id == preSelectedContactId } ?: contacts.firstOrNull()
        )
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf(TransactionType.GOLD_GIVEN) }
    var goldGramsText by remember { mutableStateOf("") }
    var goldKarat by remember { mutableIntStateOf(21) }
    var cashAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val goldGrams = goldGramsText.toDoubleOrNull() ?: 0.0
    val cashAmount = cashAmountText.toDoubleOrNull() ?: 0.0
    val eq21 = GoldTransaction.calculate21Equivalent(goldGrams, goldKarat)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ObsidianSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "تسجيل حركة ذهب أو مال جديدة",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_tx_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Contact Selector
                Text("اختر الطرف (العميل أو المورد):", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedContact?.let { "${it.name} (${it.type.titleAr})" } ?: "اختر حسابه...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("tx_contact_selector")
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(ObsidianCard)
                    ) {
                        for (c in contacts) {
                            DropdownMenuItem(
                                text = { Text("${c.name} - ${c.type.titleAr}", color = TextPrimary) },
                                onClick = {
                                    selectedContact = c
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Transaction Type Options
                Text("نوع الحركة:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                val types = listOf(
                    TransactionType.GOLD_GIVEN to "أعطيته ذهب (ليا عنده)",
                    TransactionType.GOLD_RECEIVED to "استلمت منه ذهب (له عندي)",
                    TransactionType.CASH_GIVEN to "أعطيته نقدية (ليا عنده)",
                    TransactionType.CASH_RECEIVED to "استلمت منه نقدية (له عندي)",
                    TransactionType.SETTLEMENT to "تسوية مشتركة (ذهب ونقدية)"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.forEach { (type, label) ->
                        val isSelected = selectedType == type
                        val isPositive = type == TransactionType.GOLD_GIVEN || type == TransactionType.CASH_GIVEN
                        val activeColor = if (isPositive) ReceivableGreen else PayableRed

                        Surface(
                            color = if (isSelected) activeColor.copy(alpha = 0.15f) else ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, activeColor) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedType = type }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(if (isSelected) activeColor else TextTertiary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Gold Input Section (if type involves gold)
                if (selectedType.isGold) {
                    Text("وزن الذهب والعيار:", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = goldGramsText,
                            onValueChange = { goldGramsText = it },
                            label = { Text("الوزن بالجرام") },
                            placeholder = { Text("مثلاً: 15.250") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("tx_gold_grams_input")
                        )

                        // Karat Selector (18, 21, 24)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("العيار", color = TextSecondary, fontSize = 11.sp)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                listOf(18, 21, 24).forEach { k ->
                                    val isKSelected = goldKarat == k
                                    Surface(
                                        color = if (isKSelected) GoldPrimary else ObsidianSurfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { goldKarat = k }
                                    ) {
                                        Text(
                                            text = "$k",
                                            color = if (isKSelected) Color(0xFF1B1607) else TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (goldGrams > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚖️ المعادل لعيار 21 الموحد: ${String.format(Locale.ENGLISH, "%.3f", eq21)} جرام",
                            color = GoldLight,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Cash Input Section (if type involves cash)
                if (selectedType.isCash) {
                    Text("المبلغ النقدي:", color = ReceivableGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = cashAmountText,
                        onValueChange = { cashAmountText = it },
                        label = { Text("المبلغ بالجنيه") },
                        placeholder = { Text("مثلاً: 25000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ReceivableGreen,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_cash_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Notes / Description
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("بيان الحركة / رقم الفاتورة أو الصنف") },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_notes_input")
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(validationError ?: "", color = PayableRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (selectedContact == null) {
                                validationError = "يرجى اختيار العميل أو المورد أولاً"
                                return@Button
                            }
                            if (selectedType.isGold && goldGrams <= 0 && (!selectedType.isCash || cashAmount <= 0)) {
                                validationError = "يرجى إدخال وزن الذهب بالجرام"
                                return@Button
                            }
                            if (selectedType.isCash && cashAmount <= 0 && (!selectedType.isGold || goldGrams <= 0)) {
                                validationError = "يرجى إدخال المبلغ النقدي"
                                return@Button
                            }

                            onConfirm(
                                selectedContact!!.id,
                                selectedType,
                                goldGrams,
                                goldKarat,
                                cashAmount,
                                notes
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color(0xFF1B1607)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_tx_button")
                    ) {
                        Text("تسجيل الحركة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    contacts: List<Contact>,
    preSelectedContactId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (contactId: Long, dueDate: Long, amountCash: Double, amountGold: Double, notes: String) -> Unit
) {
    var selectedContact by remember {
        mutableStateOf(
            contacts.find { it.id == preSelectedContactId } ?: contacts.firstOrNull()
        )
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var daysOffset by remember { mutableIntStateOf(7) } // Default 7 days
    var amountCashText by remember { mutableStateOf("") }
    var amountGoldText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysOffset)
    val calculatedDueDate = calendar.timeInMillis
    val dateDisplayFormat = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ObsidianSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "تحديد موعد استحقاق سداد",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Contact selector
                Text("الطرف المعني:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedContact?.name ?: "اختر الحساب...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(ObsidianCard)
                    ) {
                        for (c in contacts) {
                            DropdownMenuItem(
                                text = { Text("${c.name} (${c.type.titleAr})", color = TextPrimary) },
                                onClick = {
                                    selectedContact = c
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Due Date Selection
                Text("موعد الاستحقاق:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                val dateOptions = listOf(
                    1 to "غداً",
                    3 to "بعد 3 أيام",
                    7 to "بعد أسبوع",
                    14 to "بعد أسبوعين",
                    30 to "بعد شهر"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    dateOptions.take(3).forEach { (days, label) ->
                        val isSelected = daysOffset == days
                        Surface(
                            color = if (isSelected) GoldPrimary else ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { daysOffset = days }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color(0xFF1B1607) else TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    dateOptions.drop(3).forEach { (days, label) ->
                        val isSelected = daysOffset == days
                        Surface(
                            color = if (isSelected) GoldPrimary else ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { daysOffset = days }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color(0xFF1B1607) else TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🗓️ تاريخ التنبيه: ${dateDisplayFormat.format(Date(calculatedDueDate))}",
                    color = GoldLight,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Amounts
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = amountCashText,
                        onValueChange = { amountCashText = it },
                        label = { Text("المبلغ (ج.م)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = amountGoldText,
                        onValueChange = { amountGoldText = it },
                        label = { Text("الذهب (جرام)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات التذكير (مثل: شيك أو موعد دفعة)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (selectedContact != null) {
                                onConfirm(
                                    selectedContact!!.id,
                                    calculatedDueDate,
                                    amountCashText.toDoubleOrNull() ?: 0.0,
                                    amountGoldText.toDoubleOrNull() ?: 0.0,
                                    notes
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color(0xFF1B1607)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ التنبيه", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditGoldPriceDialog(
    currentPrice21k: Double,
    currency: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (newPrice21k: Double) -> Unit,
    onFetchLive: () -> Unit
) {
    var priceText by remember { mutableStateOf(currentPrice21k.toInt().toString()) }
    val priceNum = priceText.toDoubleOrNull() ?: currentPrice21k

    val p24 = priceNum * 24.0 / 21.0
    val p18 = priceNum * 18.0 / 21.0
    val pound = priceNum * 8.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ObsidianSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "تحديث أسعار الذهب اليومية",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "أدخل سعر جرام الذهب عيار 21 (سعر الصاغة الرسمي اليوم):",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("سعر جرام عيار 21 ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Conversion breakdown
                Surface(
                    color = ObsidianCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("الأسعار المحتسبة تلقائياً:", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("عيار 24: ${p24.toInt()} $currency", color = TextPrimary, fontSize = 12.sp)
                            Text("عيار 18: ${p18.toInt()} $currency", color = TextPrimary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("الجنيه الذهب (8 جرام 21): ${pound.toInt()} $currency", color = GoldLight, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live fetch button
                Button(
                    onClick = onFetchLive,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldContainer,
                        contentColor = OnGoldContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = GoldPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("جاري الاتصال بالسوق...", fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تحديث مباشر من أسواق الذهب العالمية", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (priceNum > 0) {
                                onSave(priceNum)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color(0xFF1B1607)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ السعر", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
