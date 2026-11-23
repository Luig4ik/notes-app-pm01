package com.example.a1_2day.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Локальное хранилище данных на базе стандартного SQLiteOpenHelper.
 * Работает полностью офлайн без внешних библиотек.
 */
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), NoteDataSource {

    companion object {
        private const val DATABASE_NAME = "notes_db.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NOTES = "notes"

        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"
        private const val COLUMN_UPDATED_AT = "updated_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NOTES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_CONTENT TEXT NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        onCreate(db)
    }

    override fun getAllNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NOTES,
            arrayOf(COLUMN_ID, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_UPDATED_AT),
            null, null, null, null,
            "$COLUMN_UPDATED_AT DESC"
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                val idIndex = c.getColumnIndexOrThrow(COLUMN_ID)
                val titleIndex = c.getColumnIndexOrThrow(COLUMN_TITLE)
                val contentIndex = c.getColumnIndexOrThrow(COLUMN_CONTENT)
                val updatedIndex = c.getColumnIndexOrThrow(COLUMN_UPDATED_AT)

                do {
                    val note = Note(
                        id = c.getLong(idIndex),
                        title = c.getString(titleIndex),
                        content = c.getString(contentIndex),
                        updatedAt = c.getLong(updatedIndex)
                    )
                    notes.add(note)
                } while (c.moveToNext())
            }
        }
        return notes
    }

    override fun getNoteById(id: Long): Note? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NOTES,
            arrayOf(COLUMN_ID, COLUMN_TITLE, COLUMN_CONTENT, COLUMN_UPDATED_AT),
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        return cursor.use { c ->
            if (c.moveToFirst()) {
                val idIndex = c.getColumnIndexOrThrow(COLUMN_ID)
                val titleIndex = c.getColumnIndexOrThrow(COLUMN_TITLE)
                val contentIndex = c.getColumnIndexOrThrow(COLUMN_CONTENT)
                val updatedIndex = c.getColumnIndexOrThrow(COLUMN_UPDATED_AT)

                Note(
                    id = c.getLong(idIndex),
                    title = c.getString(titleIndex),
                    content = c.getString(contentIndex),
                    updatedAt = c.getLong(updatedIndex)
                )
            } else null
        }
    }

    override fun insertNote(note: Note): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
            put(COLUMN_UPDATED_AT, note.updatedAt)
        }
        return db.insert(TABLE_NOTES, null, values)
    }

    override fun updateNote(note: Note): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
            put(COLUMN_UPDATED_AT, note.updatedAt)
        }
        val rows = db.update(TABLE_NOTES, values, "$COLUMN_ID = ?", arrayOf(note.id.toString()))
        return rows > 0
    }

    override fun deleteNote(id: Long): Boolean {
        val db = writableDatabase
        val rows = db.delete(TABLE_NOTES, "$COLUMN_ID = ?", arrayOf(id.toString()))
        return rows > 0
    }
}
