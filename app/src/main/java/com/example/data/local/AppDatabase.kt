package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.BusinessReminder
import com.example.data.model.ChatMessage
import com.example.data.model.CommunityPost
import com.example.data.model.DigitalDocument
import com.example.data.model.LedgerEntry
import com.example.data.model.Scheme

@Database(
    entities = [
        LedgerEntry::class,
        Scheme::class,
        BusinessReminder::class,
        CommunityPost::class,
        ChatMessage::class,
        DigitalDocument::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ledgerDao(): LedgerDao
    abstract fun schemeDao(): SchemeDao
    abstract fun reminderDao(): ReminderDao
    abstract fun communityDao(): CommunityDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sahayakai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
