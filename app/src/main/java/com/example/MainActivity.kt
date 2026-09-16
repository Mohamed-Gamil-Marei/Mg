package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Contact
import com.example.data.model.ContactType
import com.example.data.model.PaymentReminder
import com.example.ui.components.AddContactDialog
import com.example.ui.components.AddReminderDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.EditGoldPriceDialog
import com.example.ui.screens.AccountStatementScreen
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.BackupAndSettingsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.OnGoldContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.GoldLedgerViewModel
import com.example.util.NotificationHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    GoldLedgerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldLedgerApp(viewModel: GoldLedgerViewModel = viewModel()) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Android 13+ Notification Permission Request
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "يرجى تفعيل إذن الإشعارات لتصلك تنبيهات السداد", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // View Model States
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedContactId by viewModel.selectedContactId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val contactFilter by viewModel.contactFilter.collectAsState()
    val goldPrice by viewModel.goldPrice.collectAsState()
    val contactSummaries by viewModel.contactSummaries.collectAsState()
    val shopStats by viewModel.shopStats.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()
    val selectedContactSummary by viewModel.selectedContactSummary.collectAsState()
    val selectedContactTransactions by viewModel.selectedContactTransactions.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isLoadingPrice by viewModel.isLoadingPrice.collectAsState()

    // Show Snackbars on status message changes
    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Dialog Visibilities
    var showAddContactDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<Contact?>(null) }

    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var transactionPreSelectedContactId by remember { mutableStateOf<Long?>(null) }

    var showAddReminderDialog by remember { mutableStateOf(false) }
    var reminderPreSelectedContactId by remember { mutableStateOf<Long?>(null) }

    var showGoldPriceDialog by remember { mutableStateOf(false) }

    val allContacts = remember(contactSummaries) { contactSummaries.map { it.contact } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (selectedContactId == null) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Scale,
                                    contentDescription = null,
                                    tint = GoldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "  حسابات الذهب والمال",
                                color = GoldLight,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ObsidianSurface,
                        titleContentColor = GoldLight
                    )
                )
            }
        },
        bottomBar = {
            if (selectedContactId == null) {
                NavigationBar(
                    containerColor = ObsidianCard,
                    contentColor = TextPrimary,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = ObsidianBorder
                    )
                ) {
                    val tabs = listOf(
                        AppTab.DASHBOARD to Icons.Default.Home,
                        AppTab.ACCOUNTS to Icons.Default.People,
                        AppTab.TRANSACTIONS to Icons.Default.ReceiptLong,
                        AppTab.REMINDERS to Icons.Default.Alarm,
                        AppTab.SETTINGS to Icons.Default.Backup
                    )

                    tabs.forEach { (tab, icon) ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.titleAr,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.titleAr,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF1B1607),
                                selectedTextColor = GoldPrimary,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name}")
                        )
                    }
                }
            }
        },
        containerColor = ObsidianSurface,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedContactId != null && selectedContactSummary != null) {
                // Account Statement Screen (كشف الحساب المفصل للعميل أو المورد)
                AccountStatementScreen(
                    summary = selectedContactSummary!!,
                    transactions = selectedContactTransactions,
                    goldPrice = goldPrice,
                    onBack = { viewModel.selectContact(null) },
                    onExportPdfAndShareWhatsApp = {
                        viewModel.generateAndShareStatementPdf(
                            summary = selectedContactSummary!!,
                            transactions = selectedContactTransactions
                        )
                    },
                    onAddTransaction = {
                        transactionPreSelectedContactId = selectedContactId
                        showAddTransactionDialog = true
                    },
                    onAddReminder = {
                        reminderPreSelectedContactId = selectedContactId
                        showAddReminderDialog = true
                    },
                    onEditContact = {
                        contactToEdit = selectedContactSummary!!.contact
                        showAddContactDialog = true
                    },
                    onDeleteContact = {
                        viewModel.deleteContact(selectedContactSummary!!.contact)
                    },
                    onDeleteTransaction = { tx ->
                        viewModel.deleteTransaction(tx)
                    }
                )
            } else {
                // Main Navigation Tabs
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_switch"
                ) { tab ->
                    when (tab) {
                        AppTab.DASHBOARD -> DashboardScreen(
                            stats = shopStats,
                            goldPrice = goldPrice,
                            recentTransactions = allTransactions,
                            reminders = allReminders,
                            onOpenGoldPriceEdit = { showGoldPriceDialog = true },
                            onQuickAddTransaction = {
                                transactionPreSelectedContactId = null
                                showAddTransactionDialog = true
                            },
                            onQuickAddContact = {
                                contactToEdit = null
                                showAddContactDialog = true
                            },
                            onQuickAddReminder = {
                                reminderPreSelectedContactId = null
                                showAddReminderDialog = true
                            },
                            onViewAllAccounts = { viewModel.selectTab(AppTab.ACCOUNTS) },
                            onViewAllTransactions = { viewModel.selectTab(AppTab.TRANSACTIONS) }
                        )

                        AppTab.ACCOUNTS -> AccountsScreen(
                            summaries = contactSummaries,
                            searchQuery = searchQuery,
                            selectedFilter = contactFilter,
                            onSearchChange = viewModel::setSearchQuery,
                            onFilterSelect = viewModel::setContactFilter,
                            onSelectContactForStatement = { id -> viewModel.selectContact(id) },
                            onAddTransactionForContact = { id ->
                                transactionPreSelectedContactId = id
                                showAddTransactionDialog = true
                            },
                            onAddNewContact = {
                                contactToEdit = null
                                showAddContactDialog = true
                            }
                        )

                        AppTab.TRANSACTIONS -> TransactionsScreen(
                            transactions = allTransactions,
                            contacts = allContacts,
                            currency = goldPrice.currency,
                            onAddNewTransaction = {
                                transactionPreSelectedContactId = null
                                showAddTransactionDialog = true
                            },
                            onDeleteTransaction = viewModel::deleteTransaction,
                            onSelectContact = { id -> viewModel.selectContact(id) }
                        )

                        AppTab.REMINDERS -> RemindersScreen(
                            reminders = allReminders,
                            onToggleCompleted = viewModel::toggleReminderCompleted,
                            onDeleteReminder = viewModel::deleteReminder,
                            onAddNewReminder = {
                                reminderPreSelectedContactId = null
                                showAddReminderDialog = true
                            },
                            onTriggerTestNotification = {
                                val testReminder = PaymentReminder(
                                    contactName = "تجربة إشعار سداد",
                                    amountCash = 50000.0,
                                    amountGoldGrams = 15.5,
                                    notes = "هذا تنبيه تجريبي لمواعيد استحقاق سداد ديون الذهب والمال."
                                )
                                NotificationHelper.showPaymentReminderNotification(context, testReminder)
                                Toast.makeText(context, "تم إرسال الإشعار التنبيهي التجريبي بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        )

                        AppTab.SETTINGS -> BackupAndSettingsScreen(
                            goldPrice = goldPrice,
                            isLoadingPrice = isLoadingPrice,
                            onOpenGoldPriceEdit = { showGoldPriceDialog = true },
                            onFetchLivePrice = viewModel::fetchLiveGoldPrices,
                            onExportBackup = viewModel::exportBackup,
                            onRestoreBackup = viewModel::restoreBackup
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddContactDialog) {
        AddContactDialog(
            initialContact = contactToEdit,
            onDismiss = {
                showAddContactDialog = false
                contactToEdit = null
            },
            onConfirm = { name, phone, type, notes ->
                viewModel.saveContact(
                    name = name,
                    phone = phone,
                    type = type,
                    notes = notes,
                    existingId = contactToEdit?.id ?: 0
                )
                showAddContactDialog = false
                contactToEdit = null
            }
        )
    }

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            contacts = allContacts,
            preSelectedContactId = transactionPreSelectedContactId,
            onDismiss = {
                showAddTransactionDialog = false
                transactionPreSelectedContactId = null
            },
            onConfirm = { contactId, type, goldGrams, goldKarat, cashAmount, notes ->
                viewModel.addTransaction(
                    contactId = contactId,
                    type = type,
                    goldGrams = goldGrams,
                    goldKarat = goldKarat,
                    cashAmount = cashAmount,
                    notes = notes
                )
                showAddTransactionDialog = false
                transactionPreSelectedContactId = null
            }
        )
    }

    if (showAddReminderDialog) {
        AddReminderDialog(
            contacts = allContacts,
            preSelectedContactId = reminderPreSelectedContactId,
            onDismiss = {
                showAddReminderDialog = false
                reminderPreSelectedContactId = null
            },
            onConfirm = { contactId, dueDate, amountCash, amountGold, notes ->
                viewModel.addReminder(
                    contactId = contactId,
                    dueDate = dueDate,
                    amountCash = amountCash,
                    amountGold = amountGold,
                    notes = notes
                )
                showAddReminderDialog = false
                reminderPreSelectedContactId = null
            }
        )
    }

    if (showGoldPriceDialog) {
        EditGoldPriceDialog(
            currentPrice21k = goldPrice.price21k,
            currency = goldPrice.currency,
            isLoading = isLoadingPrice,
            onDismiss = { showGoldPriceDialog = false },
            onSave = { newPrice ->
                viewModel.updateGoldPrice(newPrice, goldPrice.currency)
                showGoldPriceDialog = false
            },
            onFetchLive = {
                viewModel.fetchLiveGoldPrices()
            }
        )
    }
}
