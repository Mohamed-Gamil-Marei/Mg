package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.backup.BackupResult
import com.example.data.local.AppDatabase
import com.example.data.model.Contact
import com.example.data.model.ContactBalanceSummary
import com.example.data.model.ContactType
import com.example.data.model.GoldMarketPrice
import com.example.data.model.GoldTransaction
import com.example.data.model.PaymentReminder
import com.example.data.model.ShopOverallStats
import com.example.data.model.TransactionType
import com.example.data.pdf.PdfReportGenerator
import com.example.data.repository.GoldLedgerRepository
import com.example.util.GoldPriceManager
import com.example.util.NotificationHelper
import com.example.util.WhatsAppHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class AppTab(val titleAr: String) {
    DASHBOARD("الرئيسية"),
    ACCOUNTS("الحسابات"),
    TRANSACTIONS("الحركات"),
    REMINDERS("المواعيد"),
    SETTINGS("النسخ والإعدادات")
}

class GoldLedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = GoldLedgerRepository(
        database.contactDao(),
        database.transactionDao(),
        database.reminderDao()
    )
    val backupManager = BackupManager(application, repository)
    val goldPriceManager = GoldPriceManager(application)
    private val pdfGenerator = PdfReportGenerator(application)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Selected Contact for detailed statement
    private val _selectedContactId = MutableStateFlow<Long?>(null)
    val selectedContactId: StateFlow<Long?> = _selectedContactId.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Contact Type filter
    private val _contactFilter = MutableStateFlow<ContactType?>(null)
    val contactFilter: StateFlow<ContactType?> = _contactFilter.asStateFlow()

    // Live Gold Prices
    val goldPrice: StateFlow<GoldMarketPrice> = goldPriceManager.goldPrice

    // Raw contact summaries from repository
    private val rawContactSummaries = repository.getContactSummaries()

    // Filtered contact summaries based on search and contact type
    val contactSummaries: StateFlow<List<ContactBalanceSummary>> = combine(
        rawContactSummaries,
        _searchQuery,
        _contactFilter
    ) { summaries, query, filter ->
        summaries.filter { summary ->
            val matchesFilter = filter == null || summary.contact.type == filter
            val matchesQuery = query.isBlank() ||
                    summary.contact.name.contains(query, ignoreCase = true) ||
                    summary.contact.phone.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Overall shop stats dynamically updated with gold price
    @OptIn(ExperimentalCoroutinesApi::class)
    val shopStats: StateFlow<ShopOverallStats> = goldPrice.flatMapLatest { price ->
        repository.getShopOverallStats(price.price21k)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShopOverallStats()
    )

    // All transactions
    val allTransactions: StateFlow<List<GoldTransaction>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All reminders
    val allReminders: StateFlow<List<PaymentReminder>> = repository.allReminders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Transactions for selected contact statement
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedContactTransactions: StateFlow<List<GoldTransaction>> = _selectedContactId.flatMapLatest { id ->
        if (id != null) {
            repository.getTransactionsForContact(id)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Selected Contact Summary
    val selectedContactSummary: StateFlow<ContactBalanceSummary?> = combine(
        rawContactSummaries,
        _selectedContactId
    ) { summaries, selectedId ->
        if (selectedId == null) null
        else summaries.find { it.contact.id == selectedId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // UI Feedback Messages
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isLoadingPrice = MutableStateFlow(false)
    val isLoadingPrice: StateFlow<Boolean> = _isLoadingPrice.asStateFlow()

    // Navigation and Selection
    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectContact(contactId: Long?) {
        _selectedContactId.value = contactId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setContactFilter(filter: ContactType?) {
        _contactFilter.value = filter
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // Contact Operations
    fun saveContact(name: String, phone: String, type: ContactType, notes: String, existingId: Long = 0) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _statusMessage.value = "يرجى كتابة اسم العميل أو المورد"
                return@launch
            }
            if (existingId > 0) {
                repository.updateContact(
                    Contact(
                        id = existingId,
                        name = name.trim(),
                        phone = phone.trim(),
                        type = type,
                        notes = notes.trim()
                    )
                )
                _statusMessage.value = "تم تحديث بيانات الحساب بنجاح"
            } else {
                repository.insertContact(
                    Contact(
                        name = name.trim(),
                        phone = phone.trim(),
                        type = type,
                        notes = notes.trim()
                    )
                )
                _statusMessage.value = "تم إضافة الحساب بنجاح"
            }
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            if (_selectedContactId.value == contact.id) {
                _selectedContactId.value = null
            }
            _statusMessage.value = "تم حذف الحساب وجميع حركاته"
        }
    }

    // Transaction Operations
    fun addTransaction(
        contactId: Long,
        type: TransactionType,
        goldGrams: Double,
        goldKarat: Int,
        cashAmount: Double,
        notes: String
    ) {
        viewModelScope.launch {
            if (contactId <= 0) {
                _statusMessage.value = "يرجى اختيار العميل أو المورد أولاً"
                return@launch
            }
            if (goldGrams <= 0 && cashAmount <= 0) {
                _statusMessage.value = "يرجى إدخال وزن الذهب أو المبلغ النقدي"
                return@launch
            }

            val eq21 = GoldTransaction.calculate21Equivalent(goldGrams, goldKarat)
            val tx = GoldTransaction(
                contactId = contactId,
                type = type,
                goldGrams = goldGrams,
                goldKarat = goldKarat,
                goldGrams21Equivalent = eq21,
                cashAmount = cashAmount,
                notes = notes.trim(),
                timestamp = System.currentTimeMillis()
            )
            repository.insertTransaction(tx)
            _statusMessage.value = "تم تسجيل الحركة بنجاح"
        }
    }

    fun deleteTransaction(transaction: GoldTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            _statusMessage.value = "تم حذف الحركة"
        }
    }

    // Reminder Operations
    fun addReminder(
        contactId: Long,
        dueDate: Long,
        amountCash: Double,
        amountGold: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val contact = repository.getContactById(contactId)
            val reminder = PaymentReminder(
                contactId = contactId,
                contactName = contact?.name ?: "غير محدد",
                dueDate = dueDate,
                amountCash = amountCash,
                amountGoldGrams = amountGold,
                notes = notes.trim()
            )
            val id = repository.insertReminder(reminder)
            // Fire notification if due soon
            NotificationHelper.showPaymentReminderNotification(
                getApplication(),
                reminder.copy(id = id)
            )
            _statusMessage.value = "تم حفظ تذكير موعد السداد بنجاح"
        }
    }

    fun toggleReminderCompleted(reminder: PaymentReminder) {
        viewModelScope.launch {
            repository.setReminderCompleted(reminder.id, !reminder.isCompleted)
        }
    }

    fun deleteReminder(reminder: PaymentReminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            _statusMessage.value = "تم حذف التذكير"
        }
    }

    // Gold Price Operations
    fun updateGoldPrice(price21k: Double, currency: String = "ج.م") {
        viewModelScope.launch {
            if (price21k <= 0) return@launch
            goldPriceManager.savePrices(price21k = price21k, currency = currency, isAuto = false)
            _statusMessage.value = "تم تحديث سعر الذهب إلى ${price21k.toInt()} $currency للجرام عيار 21"
        }
    }

    fun fetchLiveGoldPrices() {
        viewModelScope.launch {
            _isLoadingPrice.value = true
            val result = goldPriceManager.fetchLiveGoldPrice()
            _isLoadingPrice.value = false
            result.onSuccess {
                _statusMessage.value = "تم تحديث أسعار الذهب بنجاح من السوق المباشر"
            }.onFailure {
                _statusMessage.value = "تعذر التحديث التلقائي. يمكنك إدخال السعر يدوياً بسهولة"
            }
        }
    }

    // PDF and WhatsApp Sharing
    fun generateAndShareStatementPdf(
        summary: ContactBalanceSummary,
        transactions: List<GoldTransaction>,
        shopName: String = "محل الذهب والمجوهرات"
    ) {
        viewModelScope.launch {
            try {
                val pdfFile = pdfGenerator.generateAccountStatementPdf(
                    summary = summary,
                    transactions = transactions,
                    shopName = shopName,
                    goldPrice21k = goldPrice.value.price21k,
                    currency = goldPrice.value.currency
                )

                val message = WhatsAppHelper.generateSummaryMessage(
                    summary = summary,
                    shopName = shopName,
                    goldPrice21k = goldPrice.value.price21k,
                    currency = goldPrice.value.currency
                )

                WhatsAppHelper.sharePdfReport(
                    context = getApplication(),
                    pdfFile = pdfFile,
                    message = message,
                    targetPhone = summary.contact.phone
                )
            } catch (e: Exception) {
                _statusMessage.value = "خطأ أثناء إنشاء تقرير PDF: ${e.localizedMessage}"
            }
        }
    }

    // Backup & Restore
    fun exportBackup(onFileReady: (File) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.exportBackupFile()
            if (result.success && result.backupFile != null) {
                _statusMessage.value = "تم إنشاء ملف النسخة الاحتياطية بنجاح"
                onFileReady(result.backupFile)
            } else {
                _statusMessage.value = result.message
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.restoreFromUri(uri)
            _statusMessage.value = result.message
        }
    }
}
