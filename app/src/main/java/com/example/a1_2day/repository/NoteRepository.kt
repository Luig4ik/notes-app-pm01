package com.example.a1_2day.repository

import com.example.a1_2day.data.Note
import com.example.a1_2day.data.NoteDataSource

/**
 * Репозиторий бизнес-логики управления заметками.
 * Отвечает за валидацию данных, сортировку и фильтрацию заметок.
 */
class NoteRepository(private val dataSource: NoteDataSource) {

    /**
     * Валидация: заметка считается невалидной (пустой), если и заголовок, и текст состоят только из пробелов.
     */
    fun isValidNote(title: String, content: String): Boolean {
        return title.trim().isNotEmpty() || content.trim().isNotEmpty()
    }

    /**
     * Добавлениеใหม่ заметки с валидацией.
     * @throws IllegalArgumentException если заметка полностью пустая.
     */
    fun addNote(title: String, content: String): Note {
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()

        if (!isValidNote(trimmedTitle, trimmedContent)) {
            throw IllegalArgumentException("Заметка не может быть пустой!")
        }

        val newNote = Note(
            title = trimmedTitle,
            content = trimmedContent,
            updatedAt = System.currentTimeMillis()
        )
        val generatedId = dataSource.insertNote(newNote)
        return newNote.copy(id = generatedId)
    }

    /**
     * Получение всех заметок (сортировка: сначала свежие/новые).
     */
    fun getAllNotes(): List<Note> {
        return dataSource.getAllNotes().sortedByDescending { it.updatedAt }
    }

    /**
     * Получение конкретной заметки по ID.
     */
    fun getNoteById(id: Long): Note? {
        return dataSource.getNoteById(id)
    }

    /**
     * Редактирование существующей заметки с обновлением даты.
     * @throws IllegalArgumentException если отредактированный текст/заголовок пустой.
     */
    fun updateNote(id: Long, title: String, content: String): Boolean {
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()

        if (!isValidNote(trimmedTitle, trimmedContent)) {
            throw IllegalArgumentException("Заметка не может быть пустой!")
        }

        val updatedNote = Note(
            id = id,
            title = trimmedTitle,
            content = trimmedContent,
            updatedAt = System.currentTimeMillis()
        )
        return dataSource.updateNote(updatedNote)
    }

    /**
     * Удаление заметки по ID.
     */
    fun deleteNote(id: Long): Boolean {
        return dataSource.deleteNote(id)
    }

    /**
     * Поиск заметок по ключевой строке в заголовке или содержании (без учёта регистра).
     * Результат отсортирован (сначала новые).
     */
    fun searchNotes(query: String): List<Note> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return getAllNotes()
        }

        return getAllNotes().filter { note ->
            note.title.contains(trimmedQuery, ignoreCase = true) ||
                    note.content.contains(trimmedQuery, ignoreCase = true)
        }
    }
}
