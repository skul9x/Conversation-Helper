package com.skul9x.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Collections

// ViewModel giờ đây sẽ phụ thuộc vào Repository
class MainViewModel(private val repository: SentenceRepository) : ViewModel() {

    // Lấy dữ liệu trực tiếp từ database dưới dạng StateFlow
    val sentences: StateFlow<List<Sentence>> = repository.allSentences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun addSentence(chineseText: String, vietnameseNote: String) = viewModelScope.launch {
        if (chineseText.isBlank()) return@launch
        // Tạo orderIndex dựa trên số lượng item hiện có
        val newIndex = (repository.getCount()).toLong()
        val newSentence = Sentence(
            chineseText = chineseText,
            vietnameseNote = vietnameseNote,
            orderIndex = newIndex
        )
        repository.insert(newSentence)
    }

    fun updateSentence(id: String, newChineseText: String, newVietnameseNote: String) = viewModelScope.launch {
        val sentenceToUpdate = sentences.value.find { it.id == id }
        sentenceToUpdate?.let {
            val updatedSentence = it.copy(chineseText = newChineseText, vietnameseNote = newVietnameseNote)
            repository.update(updatedSentence)
        }
    }

    fun deleteSentence(sentence: Sentence) = viewModelScope.launch {
        repository.delete(sentence)
        // Cập nhật lại orderIndex cho các item còn lại
        val remainingSentences = sentences.value.filterNot { it.id == sentence.id }
        updateOrder(remainingSentences)
    }

    private fun moveSentence(id: String, direction: Int) {
        val currentList = sentences.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }

        if (index != -1) {
            val newIndex = index + direction
            if (newIndex in currentList.indices) {
                Collections.swap(currentList, index, newIndex)
                updateOrder(currentList)
            }
        }
    }

    fun moveSentenceUp(id: String) {
        moveSentence(id, -1)
    }

    fun moveSentenceDown(id: String) {
        moveSentence(id, 1)
    }

    private fun updateOrder(newList: List<Sentence>) = viewModelScope.launch {
        val updatedListWithNewIndices = newList.mapIndexed { index, sentence ->
            sentence.copy(orderIndex = index.toLong())
        }
        repository.updateAll(updatedListWithNewIndices)
    }

    fun restoreSentences(restoredSentences: List<Sentence>) = viewModelScope.launch {
        // Gán orderIndex trước khi insert vào DB
        val correctlyOrderedSentences = restoredSentences.mapIndexed { index, sentence ->
            sentence.copy(orderIndex = index.toLong())
        }
        repository.updateAll(correctlyOrderedSentences)
    }
}

// ViewModelFactory để "tiêm" Repository vào ViewModel
class MainViewModelFactory(private val repository: SentenceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}