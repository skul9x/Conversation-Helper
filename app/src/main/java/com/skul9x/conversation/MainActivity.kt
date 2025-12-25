package com.skul9x.conversation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skul9x.conversation.databinding.ActivityMainBinding
import com.skul9x.conversation.databinding.DialogAddEditSentenceBinding
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as ConversationApplication).repository)
    }
    private lateinit var sentenceAdapter: SentenceAdapter
    private lateinit var tts: TextToSpeech
    private var isTtsInitialized = false

    private val backupLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                backupDataToUri(uri)
            }
        }
    }

    private val restoreLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                restoreDataFromUri(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        tts = TextToSpeech(this, this)
        setupUI()
        observeViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_backup -> {
                launchBackup()
                true
            }
            R.id.menu_restore -> {
                launchRestore()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupUI() {
        sentenceAdapter = SentenceAdapter { action ->
            when (action) {
                is SentenceAction.Click -> speakSentence(action.sentence.chineseText)
                is SentenceAction.Delete -> showDeleteConfirmationDialog(action.sentence)
                is SentenceAction.Edit -> showAddEditDialog(action.sentence)
                is SentenceAction.MoveUp -> viewModel.moveSentenceUp(action.sentence.id)
                is SentenceAction.MoveDown -> viewModel.moveSentenceDown(action.sentence.id)
            }
        }
        binding.recyclerViewSentences.adapter = sentenceAdapter

        binding.fabAddSentence.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sentences.collect { sentences ->
                    sentenceAdapter.submitList(sentences)

                    if (sentences.isEmpty()) {
                        binding.recyclerViewSentences.visibility = View.GONE
                        binding.emptyStateLayout.visibility = View.VISIBLE
                    } else {
                        binding.recyclerViewSentences.visibility = View.VISIBLE
                        binding.emptyStateLayout.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showAddEditDialog(sentence: Sentence?) {
        val dialogBinding = DialogAddEditSentenceBinding.inflate(LayoutInflater.from(this))
        val isEditing = sentence != null

        if (isEditing) {
            dialogBinding.editTextChinese.setText(sentence?.chineseText)
            dialogBinding.editTextVietnamese.setText(sentence?.vietnameseNote)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (isEditing) getString(R.string.edit_sentence) else getString(R.string.add_new_sentence))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.save)) { dialog, _ ->
                val chineseText = dialogBinding.editTextChinese.text.toString().trim()
                val vietnameseNote = dialogBinding.editTextVietnamese.text.toString().trim()

                if (chineseText.isNotEmpty()) {
                    if (isEditing) {
                        viewModel.updateSentence(sentence!!.id, chineseText, vietnameseNote)
                    } else {
                        viewModel.addSentence(chineseText, vietnameseNote)
                    }
                } else {
                    Toast.makeText(this, getString(R.string.chinese_sentence_cannot_be_empty), Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(sentence: Sentence) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_sentence))
            .setMessage(getString(R.string.are_you_sure_delete))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteSentence(sentence)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun launchBackup() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "conversation_backup.json")
        }
        backupLauncher.launch(intent)
    }

    private fun backupDataToUri(uri: Uri) {
        try {
            val sentences = viewModel.sentences.value
            if (sentences.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu để sao lưu.", Toast.LENGTH_SHORT).show()
                return
            }
            val jsonString = sentencesToJson(sentences)
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { fos ->
                    fos.write(jsonString.toByteArray())
                }
            }
            Toast.makeText(this, "Sao lưu thành công!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("Backup", "Lỗi khi sao lưu dữ liệu", e)
            Toast.makeText(this, "Sao lưu thất bại.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchRestore() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        restoreLauncher.launch(intent)
    }

    private fun restoreDataFromUri(uri: Uri) {
        try {
            val stringBuilder = StringBuilder()
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line)
                        line = reader.readLine()
                    }
                }
            }
            val restoredSentences = jsonToSentences(stringBuilder.toString())
            viewModel.restoreSentences(restoredSentences)
            Toast.makeText(this, "Phục hồi thành công!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("Restore", "Lỗi khi phục hồi dữ liệu", e)
            Toast.makeText(this, "Phục hồi thất bại. File có thể không hợp lệ.", Toast.LENGTH_LONG).show()
        }
    }

    private fun sentencesToJson(sentences: List<Sentence>): String {
        val jsonArray = JSONArray()
        sentences.forEach { sentence ->
            val jsonObject = JSONObject()
            jsonObject.put("id", sentence.id)
            jsonObject.put("chineseText", sentence.chineseText)
            jsonObject.put("vietnameseNote", sentence.vietnameseNote)
            // orderIndex không cần backup vì sẽ được tái tạo khi restore
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString(4) // pretty print
    }

    private fun jsonToSentences(jsonString: String): List<Sentence> {
        val sentences = mutableListOf<Sentence>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            sentences.add(
                Sentence(
                    id = jsonObject.optString("id", UUID.randomUUID().toString()),
                    chineseText = jsonObject.getString("chineseText"),
                    vietnameseNote = jsonObject.getString("vietnameseNote"),
                    orderIndex = i.toLong() // Gán thứ tự khi restore
                )
            )
        }
        return sentences
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
                Toast.makeText(this, getString(R.string.chinese_language_not_supported), Toast.LENGTH_LONG).show()
            } else {
                // SỬA LỖI Ở ĐÂY: isTtrInit -> isTtsInitialized
                isTtsInitialized = true
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    private fun speakSentence(text: String) {
        if (isTtsInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        } else {
            Toast.makeText(this, getString(R.string.tts_not_ready), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}