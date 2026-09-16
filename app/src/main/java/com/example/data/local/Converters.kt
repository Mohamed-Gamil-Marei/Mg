package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ContactType
import com.example.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromContactType(value: ContactType?): String {
        return value?.name ?: ContactType.CUSTOMER.name
    }

    @TypeConverter
    fun toContactType(value: String?): ContactType {
        return value?.let { ContactType.fromString(it) } ?: ContactType.CUSTOMER
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String {
        return value?.name ?: TransactionType.GOLD_GIVEN.name
    }

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType {
        return value?.let { TransactionType.fromString(it) } ?: TransactionType.GOLD_GIVEN
    }
}
