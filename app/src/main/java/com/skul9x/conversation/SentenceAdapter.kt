// java/com/skul9x/conversation/SentenceAdapter.kt

package com.skul9x.conversation

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skul9x.conversation.databinding.ItemSentenceBinding

// Define a sealed interface for all possible actions
sealed interface SentenceAction {
    data class Click(val sentence: Sentence) : SentenceAction
    data class Edit(val sentence: Sentence) : SentenceAction
    data class Delete(val sentence: Sentence) : SentenceAction
    data class MoveUp(val sentence: Sentence) : SentenceAction
    data class MoveDown(val sentence: Sentence) : SentenceAction
}

class SentenceAdapter(
    private val onAction: (SentenceAction) -> Unit
) : ListAdapter<Sentence, SentenceAdapter.SentenceViewHolder>(SentenceDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SentenceViewHolder {
        val binding = ItemSentenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SentenceViewHolder(binding, onAction)
    }

    override fun onBindViewHolder(holder: SentenceViewHolder, position: Int) {
        val sentence = getItem(position)
        // Pass list size and position to determine if move up/down should be enabled
        holder.bind(sentence, position, itemCount)
    }

    class SentenceViewHolder(
        private val binding: ItemSentenceBinding,
        private val onAction: (SentenceAction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(sentence: Sentence, position: Int, totalItems: Int) {
            binding.textViewNumber.text = (position + 1).toString()
            binding.textViewSentence.text = sentence.chineseText
            binding.textViewNote.text = sentence.vietnameseNote

            binding.clickableLayout.setOnClickListener {
                onAction(SentenceAction.Click(sentence))
            }

            binding.buttonOptions.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.inflate(R.menu.item_options_menu)

                // Conditionally show/hide move buttons
                popup.menu.findItem(R.id.menu_move_up).isVisible = position > 0
                popup.menu.findItem(R.id.menu_move_down).isVisible = position < totalItems - 1

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit -> onAction(SentenceAction.Edit(sentence)).let { true }
                        R.id.menu_delete -> onAction(SentenceAction.Delete(sentence)).let { true }
                        R.id.menu_move_up -> onAction(SentenceAction.MoveUp(sentence)).let { true }
                        R.id.menu_move_down -> onAction(SentenceAction.MoveDown(sentence)).let { true }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }
}

object SentenceDiffCallback : DiffUtil.ItemCallback<Sentence>() {
    override fun areItemsTheSame(oldItem: Sentence, newItem: Sentence): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Sentence, newItem: Sentence): Boolean {
        return oldItem == newItem
    }
}