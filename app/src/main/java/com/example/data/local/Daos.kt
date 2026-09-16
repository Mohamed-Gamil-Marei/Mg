package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Contact
import com.example.data.model.GoldTransaction
import com.example.data.model.PaymentReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: Long): Contact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<Contact>)

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("SELECT * FROM contacts")
    suspend fun getAllContactsSnapshot(): List<Contact>

    @Query("DELETE FROM contacts")
    suspend fun clearAll()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<GoldTransaction>>

    @Query("SELECT * FROM transactions WHERE contactId = :contactId ORDER BY timestamp ASC")
    fun getTransactionsForContact(contactId: Long): Flow<List<GoldTransaction>>

    @Query("SELECT * FROM transactions WHERE contactId = :contactId ORDER BY timestamp ASC")
    suspend fun getTransactionsForContactSnapshot(contactId: Long): List<GoldTransaction>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 30): Flow<List<GoldTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: GoldTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<GoldTransaction>)

    @Update
    suspend fun updateTransaction(transaction: GoldTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: GoldTransaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsSnapshot(): List<GoldTransaction>

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllReminders(): Flow<List<PaymentReminder>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getPendingReminders(): Flow<List<PaymentReminder>>

    @Query("SELECT * FROM reminders WHERE contactId = :contactId ORDER BY dueDate ASC")
    fun getRemindersForContact(contactId: Long): Flow<List<PaymentReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: PaymentReminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<PaymentReminder>)

    @Update
    suspend fun updateReminder(reminder: PaymentReminder)

    @Query("UPDATE reminders SET isCompleted = :completed WHERE id = :id")
    suspend fun setReminderCompleted(id: Long, completed: Boolean)

    @Delete
    suspend fun deleteReminder(reminder: PaymentReminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("SELECT * FROM reminders")
    suspend fun getAllRemindersSnapshot(): List<PaymentReminder>

    @Query("DELETE FROM reminders")
    suspend fun clearAll()
}
