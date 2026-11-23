package com.example.a1_2day.data

/**
 * Модель данных заметки.
 * @param id Уникальный идентификатор заметки в БД (0 по умолчанию для новых)
 * @param title Заголовок заметки
 * @param content Текст заметки
 * @param updatedAt Время последнего изменения (в миллисекундах UTC)
 */
data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)
