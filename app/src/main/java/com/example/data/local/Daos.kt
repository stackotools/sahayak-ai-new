package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger_entries ORDER BY date DESC, id DESC")
    fun getAllEntries(): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE khataId = :khataId ORDER BY id ASC")
    fun getEntriesForKhata(khataId: Long): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE isConfirmed = 0 ORDER BY id DESC")
    fun getPendingEntries(): Flow<List<LedgerEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: LedgerEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<LedgerEntry>)

    @Update
    suspend fun updateEntry(entry: LedgerEntry)

    @Delete
    suspend fun deleteEntry(entry: LedgerEntry)

    @Query("DELETE FROM ledger_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface SchemeDao {
    @Query("SELECT * FROM schemes")
    fun getAllSchemes(): Flow<List<Scheme>>

    @Query("SELECT * FROM schemes WHERE category = :category")
    fun getSchemesByCategory(category: SchemeCategory): Flow<List<Scheme>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchemes(schemes: List<Scheme>)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllReminders(): Flow<List<BusinessReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: BusinessReminder): Long

    @Update
    suspend fun updateReminder(reminder: BusinessReminder)

    @Query("UPDATE reminders SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Delete
    suspend fun deleteReminder(reminder: BusinessReminder)
}

@Dao
interface CommunityDao {
    @Query("SELECT * FROM community_posts ORDER BY id DESC")
    fun getAllPosts(): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CommunityPost>)

    @Query("UPDATE community_posts SET isLikedByUser = :isLiked, likesCount = :newCount WHERE id = :id")
    suspend fun updateLike(id: Long, isLiked: Boolean, newCount: Int)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY id ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM chat_conversations ORDER BY time ASC")
    fun getAllConversations(): Flow<List<ChatConversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ChatConversation>)
}

@Dao
interface ThreadMessageDao {
    @Query("SELECT * FROM chat_thread_messages ORDER BY id ASC")
    fun getAllMessages(): Flow<List<ChatThreadMessage>>

    @Query("SELECT * FROM chat_thread_messages WHERE conversationId = :conversationId ORDER BY id ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatThreadMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatThreadMessage>)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM digital_documents ORDER BY id DESC")
    fun getAllDocuments(): Flow<List<DigitalDocument>>

    @Query("SELECT * FROM digital_documents WHERE category = :category ORDER BY id DESC")
    fun getDocumentsByCategory(category: DocumentCategory): Flow<List<DigitalDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DigitalDocument): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(docs: List<DigitalDocument>)

    @Delete
    suspend fun deleteDocument(doc: DigitalDocument)

    @Query("DELETE FROM digital_documents WHERE id = :id")
    suspend fun deleteById(id: Long)
}

