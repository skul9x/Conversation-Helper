package com.skul9x.conversation

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SentenceDao {

    @Query("SELECT * FROM sentences ORDER BY orderIndex ASC")
    fun getAllSentences(): Flow<List<Sentence>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sentence: Sentence)

    @Update
    suspend fun update(sentence: Sentence)

    @Delete
    suspend fun delete(sentence: Sentence)

    @Query("SELECT COUNT(*) FROM sentences")
    suspend fun getCount(): Int

    // Dùng để cập nhật lại toàn bộ danh sách khi sắp xếp
    @Update
    suspend fun updateAll(sentences: List<Sentence>)
}