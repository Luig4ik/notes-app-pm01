package com.example.a1_2day.data

/**
 * Интерфейс источника данных для абстрагирования работы с БД.
 * Облегчает тестирование (позволяет подменять реальную БД на InMemory-версию в Unit-тестах).
 */
interface NoteDataSource {
    fun getAllNotes(): List<Note>
    fun getNoteById(id: Long): Note?
    fun insertNote(note: Note): Long
    fun updateNote(note: Note): Boolean
    fun deleteNote(id: Long): Boolean
}
