package com.example.easeapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R
import com.example.easeapp.model.DiaryModel
import java.text.SimpleDateFormat
import java.util.Locale

class DiaryAdapter(
    private val onDeleteClick: (DiaryModel) -> Unit
) : ListAdapter<DiaryModel, DiaryAdapter.DiaryViewHolder>(DIFF_CALLBACK) {

    inner class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val diaryText: TextView = itemView.findViewById(R.id.textDiary)
        private val diaryDate: TextView = itemView.findViewById(R.id.dateTextDiary)
        val dateFormat = SimpleDateFormat("EEEE, yyyy-MM-dd HH:mm", Locale.getDefault())
        private val deleteBtn: ImageView = itemView.findViewById(R.id.deleteDiaryIcon)

        fun bind(item: DiaryModel) {
            diaryText.text = item.context
            diaryDate.text = dateFormat.format(item.date)
            deleteBtn.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.diary_row, parent, false)
        return DiaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DiaryModel>() {
            override fun areItemsTheSame(oldItem: DiaryModel, newItem: DiaryModel): Boolean {
                return oldItem.date == newItem.date
            }

            override fun areContentsTheSame(oldItem: DiaryModel, newItem: DiaryModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
