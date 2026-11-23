package com.example.a1_2day

import com.example.a1_2day.data.Note
import com.example.a1_2day.data.NoteDataSource
import com.example.a1_2day.repository.NoteRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Фейковый источник данных в памяти для независимого и прямого Unit-тестирования бизнес-логики.
 */
class InMemoryNoteDataSource : NoteDataSource {
    private val notes = mutableListOf<Note>()
    private var nextId = 1L

    override fun getAllNotes(): List<Note> = notes.toList()

    override fun getNoteById(id: Long): Note? = notes.find { it.id == id }

    override fun insertNote(note: Note): Long {
        val inserted = note.copy(id = nextId++)
        notes.add(inserted)
        return inserted.id
    }

    override fun updateNote(note: Note): Boolean {
        val index = notes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            notes[index] = note
            return true
        }
        return false
    }

    override fun deleteNote(id: Long): Boolean {
        return notes.removeIf { it.id == id }
    }
}

/**
 * Модульные unit-тесты (JUnit4) для проверки бизнес-логики NoteRepository:
 * 1. Успешное создание заметки
 * 2. Валидация пустой заметки
 * 3. Редактирование заметки
 * 4. Удаление заметки
 * 5. Поиск заметок (без учёта регистра)
 */
class NoteRepositoryTest {

    private lateinit var repository: NoteRepository
    private lateinit var dataSource: InMemoryNoteDataSource

    @Before
    fun setUp() {
        dataSource = InMemoryNoteDataSource()
        repository = NoteRepository(dataSource)
    }

    @Test
    fun testAddNote_success() {
        val note = repository.addNote("Покупки", "Купить молоко и хлеб")
        assertNotNull(note)
        assertTrue(note.id > 0)
        assertEquals("Покупки", note.title)
        assertEquals("Купить молоко и хлеб", note.content)

        val allNotes = repository.getAllNotes()
        assertEquals(1, allNotes.size)
        assertEquals("Покупки", allNotes[0].title)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAddNote_emptyValidation_throwsException() {
        // Заметка из одних пробелов не должна сохраняться
        repository.addNote("   ", "  \n  ")
    }

    @Test
    fun testUpdateNote_success() {
        val originalNote = repository.addNote("Старый заголовок", "Старый текст")
        val updatedSuccess = repository.updateNote(originalNote.id, "Новый заголовок", "Новый текст")

        assertTrue(updatedSuccess)
        val fetchedNote = repository.getNoteById(originalNote.id)
        assertNotNull(fetchedNote)
        assertEquals("Новый заголовок", fetchedNote?.title)
        assertEquals("Новый текст", fetchedNote?.content)
    }

    @Test
    fun testDeleteNote_success() {
        val note = repository.addNote("Удаляемая заметка", "Содержимое")
        assertEquals(1, repository.getAllNotes().size)

        val deleted = repository.deleteNote(note.id)
        assertTrue(deleted)
        assertEquals(0, repository.getAllNotes().size)
        assertNull(repository.getNoteById(note.id))
    }

    @Test
    fun testSearchNotes_caseInsensitive() {
        repository.addNote("Купить продуктов", "Молоко, Хлеб, Сырок")
        repository.addNote("Планы на день", "Сделать ДЗ по Котлин")
        repository.addNote("Важная заметка", "Молоко очень полезно")

        // Ищем "молоко" в нижнем регистре
        val searchResult = repository.searchNotes("молоко")
        assertEquals(2, searchResult.size)

        // Ищем "ДЗ"
        val searchDz = repository.searchNotes("дз")
        assertEquals(1, searchDz.size)
        assertEquals("Планы на день", searchDz[0].title)
    }
}
