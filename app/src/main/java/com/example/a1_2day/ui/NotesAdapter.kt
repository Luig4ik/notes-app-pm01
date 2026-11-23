package com.example.a1_2day.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a1_2day.R
import com.example.a1_2day.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Адаптер для отображения списка заметок в RecyclerView.
 */
class NotesAdapter(
    private val onItemClick: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private val notes = mutableListOf<Note>()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    /**
     * Обновление списка элементов адаптера.
     */
    fun submitList(newList: List<Note>) {
        notes.clear()
        notes.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(note: Note) {
            tvTitle.text = note.title.ifBlank { "Без заголовка" }
            tvContent.text = note.content.ifBlank { "..." }
            tvDate.text = dateFormat.format(Date(note.updatedAt))

            itemView.setOnClickListener { onItemClick(note) }
        }
    }
}
