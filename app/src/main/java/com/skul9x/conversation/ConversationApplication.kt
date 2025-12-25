package com.skul9x.conversation

import android.app.Application

class ConversationApplication : Application() {
    // Khởi tạo database và repository một cách "lười biếng" (lazy)
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { SentenceRepository(database.sentenceDao()) }
}