package com.skul9x.conversation

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sentences")
data class Sentence(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val chineseText: String,
    val vietnameseNote: String,
    val orderIndex: Long // Thêm trường này để quản lý thứ tự sắp xếp
)