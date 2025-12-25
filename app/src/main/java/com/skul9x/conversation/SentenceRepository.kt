package com.skul9x.conversation

import kotlinx.coroutines.flow.Flow

class SentenceRepository(private val sentenceDao: SentenceDao) {

    val allSentences: Flow<List<Sentence>> = sentenceDao.getAllSentences()

    suspend fun insert(sentence: Sentence) {
        sentenceDao.insert(sentence)
    }

    suspend fun update(sentence: Sentence) {
        sentenceDao.update(sentence)
    }

    suspend fun delete(sentence: Sentence) {
        sentenceDao.delete(sentence)
    }

    suspend fun getCount(): Int {
        return sentenceDao.getCount()
    }

    suspend fun updateAll(sentences: List<Sentence>) {
        sentenceDao.updateAll(sentences)
    }
}