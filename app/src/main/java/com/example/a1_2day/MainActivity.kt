package com.example.a1_2day

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a1_2day.data.DatabaseHelper
import com.example.a1_2day.repository.NoteRepository
import com.example.a1_2day.ui.NotesAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Главный экран приложения: поиск, просмотр списка заметок и кнопка создания.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var repository: NoteRepository
    private lateinit var adapter: NotesAdapter

    private lateinit var etSearch: EditText
    private lateinit var rvNotes: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var fabAddNote: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dbHelper = DatabaseHelper(this)
        repository = NoteRepository(dbHelper)

        initViews()
        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем список заметок при возврате на главный экран
        loadNotes()
    }

    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        rvNotes = findViewById(R.id.rvNotes)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        fabAddNote = findViewById(R.id.fabAddNote)
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter { note ->
            // Открываем экран редактирования при клике на заметку
            NoteDetailActivity.start(this, note.id)
        }
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.adapter = adapter
    }

    private fun setupListeners() {
        // Кнопка добавления новой заметки
        fabAddNote.setOnClickListener {
            NoteDetailActivity.start(this)
        }

        // Поиск по мере ввода текста
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadNotes(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadNotes(query: String = etSearch.text.toString()) {
        val notes = repository.searchNotes(query)
        adapter.submitList(notes)

        if (notes.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = if (query.isBlank()) {
                getString(R.string.empty_notes)
            } else {
                getString(R.string.empty_search)
            }
        } else {
            tvEmptyState.visibility = View.GONE
        }
    }
}
