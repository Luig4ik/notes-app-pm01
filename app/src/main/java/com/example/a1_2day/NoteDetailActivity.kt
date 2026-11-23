package com.example.a1_2day

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.a1_2day.data.DatabaseHelper
import com.example.a1_2day.repository.NoteRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

/**
 * Экран создания, редактирования и экспорта заметки.
 */
class NoteDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_NOTE_ID = "extra_note_id"

        /**
         * Удобный метод для запуска экрана редактирования или создания заметки.
         */
        fun start(context: Context, noteId: Long = -1L) {
            val intent = Intent(context, NoteDetailActivity::class.java).apply {
                putExtra(EXTRA_NOTE_ID, noteId)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var repository: NoteRepository
    private var noteId: Long = -1L

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnSave: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var btnExport: MaterialButton

    /**
     * Системный контракт Storage Access Framework (SAF) для экспорта без разрешений в манифесте.
     */
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let { exportContentToUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        val dbHelper = DatabaseHelper(this)
        repository = NoteRepository(dbHelper)

        noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)

        initViews()
        setupListeners()
        loadNoteData()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbarNoteDetail)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
        btnExport = findViewById(R.id.btnExport)

        if (noteId == -1L) {
            toolbar.title = getString(R.string.new_note_title)
            btnDelete.visibility = View.GONE
        } else {
            toolbar.title = getString(R.string.edit_note_title)
            btnDelete.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener { finish() }

        btnSave.setOnClickListener { saveNote() }

        btnDelete.setOnClickListener { showDeleteConfirmationDialog() }

        btnExport.setOnClickListener { startExportProcess() }
    }

    private fun loadNoteData() {
        if (noteId != -1L) {
            val note = repository.getNoteById(noteId)
            note?.let {
                etTitle.setText(it.title)
                etContent.setText(it.content)
            }
        }
    }

    private fun saveNote() {
        val title = etTitle.text.toString()
        val content = etContent.text.toString()

        try {
            if (noteId == -1L) {
                repository.addNote(title, content)
            } else {
                repository.updateNote(noteId, title, content)
            }
            finish()
        } catch (_: IllegalArgumentException) {
            Toast.makeText(this, getString(R.string.error_empty_note), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(R.string.delete_confirm_msg)
            .setPositiveButton(R.string.yes_delete) { _, _ ->
                repository.deleteNote(noteId)
                finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun startExportProcess() {
        val titleText = etTitle.text.toString().trim()
        val defaultFileName = if (titleText.isNotEmpty()) {
            "${titleText.take(20).replace(Regex("[^a-zA-Z0-9а-яА-Я_]"), "_")}.txt"
        } else {
            "note.txt"
        }
        createDocumentLauncher.launch(defaultFileName)
    }

    private fun exportContentToUri(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val title = etTitle.text.toString()
                val content = etContent.text.toString()
                val textToExport = "=== $title ===\n\n$content"
                outputStream.write(textToExport.toByteArray(Charsets.UTF_8))
            }
            Toast.makeText(this, getString(R.string.export_success), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "${getString(R.string.export_error)}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
