package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ContactType(val titleAr: String) {
    CUSTOMER("عميل"),
    SUPPLIER("مورد / تاجر");

    companion object {
        fun fromString(value: String): ContactType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: CUSTOMER
        }
    }
}

enum class TransactionType(val titleAr: String, val isGold: Boolean, val isCash: Boolean) {
    GOLD_GIVEN("أعطيته ذهب (ليا عنده)", isGold = true, isCash = false),
    GOLD_RECEIVED("استلمت منه ذهب (له عندي)", isGold = true, isCash = false),
    CASH_GIVEN("أعطيته نقدية (ليا عنده)", isGold = false, isCash = true),
    CASH_RECEIVED("استلمت منه نقدية (له عندي)", isGold = false, isCash = true),
    SETTLEMENT("تسوية حساب (ذهب ونقدية)", isGold = true, isCash = true);

    companion object {
        fun fromString(value: String): TransactionType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: GOLD_GIVEN
        }
    }
}

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val type: ContactType = ContactType.CUSTOMER,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contactId"), Index("timestamp")]
)
data class GoldTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val type: TransactionType,
    val goldGrams: Double = 0.0,
    val goldKarat: Int = 21,
    val goldGrams21Equivalent: Double = 0.0,
    val cashAmount: Double = 0.0,
    val notes: String = ""
) {
    companion object {
        fun calculate21Equivalent(grams: Double, karat: Int): Double {
            if (grams <= 0.0) return 0.0
            return (grams * karat) / 21.0
        }
    }
}

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contactId"), Index("dueDate")]
)
data class PaymentReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long = 0,
    val contactName: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val amountCash: Double = 0.0,
    val amountGoldGrams: Double = 0.0,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ContactBalanceSummary(
    val contact: Contact,
    // Gold: positive means shop is owed gold (ليا عنده ذهب), negative means shop owes contact gold (عليا ذهب)
    val netGoldGrams21: Double = 0.0,
    // Cash: positive means shop is owed cash (ليا عنده فلوس), negative means shop owes contact cash (عليا فلوس)
    val netCash: Double = 0.0,
    val totalGoldGiven21: Double = 0.0,
    val totalGoldReceived21: Double = 0.0,
    val totalCashGiven: Double = 0.0,
    val totalCashReceived: Double = 0.0,
    val transactionCount: Int = 0,
    val lastTransactionDate: Long? = null
)

data class ShopOverallStats(
    val totalGoldOwedToMe21: Double = 0.0, // ليا في السوق (جرام عيار 21)
    val totalGoldOwedByMe21: Double = 0.0, // عليا للسوق (جرام عيار 21)
    val netGoldBalance21: Double = 0.0,     // صافي الذهب
    val totalCashOwedToMe: Double = 0.0,   // ليا بره (فلوس)
    val totalCashOwedByMe: Double = 0.0,   // عليا بره (فلوس)
    val netCashBalance: Double = 0.0,      // صافي النقدية
    val totalContacts: Int = 0,
    val totalCustomers: Int = 0,
    val totalSuppliers: Int = 0,
    val estimatedNetValueWithGoldPrice: Double = 0.0 // القيمة الإجمالية الصافية بعد تقييم الذهب بالنقدية
)

data class GoldMarketPrice(
    val price24k: Double = 4150.0,
    val price21k: Double = 3630.0,
    val price18k: Double = 3110.0,
    val goldPoundPrice: Double = 29040.0, // 8 grams 21k
    val currency: String = "ج.م",
    val lastUpdated: Long = System.currentTimeMillis(),
    val isAutoFetched: Boolean = false
)
