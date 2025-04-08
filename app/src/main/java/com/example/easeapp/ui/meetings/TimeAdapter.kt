package com.example.easeapp.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R

class TimeAdapter : RecyclerView.Adapter<TimeAdapter.TimeViewHolder>() {

    private var times: List<String> = emptyList()
    private var selectedPosition = RecyclerView.NO_POSITION

    fun submitList(list: List<String>) {
        times = list
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun getSelectedTime(): String? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            times[selectedPosition]
        } else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeViewHolder(view)
    }

    override fun getItemCount(): Int = times.size

    override fun onBindViewHolder(holder: TimeViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val time = times[position]
        holder.bind(time, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = position
            notifyItemChanged(previous)
            notifyItemChanged(position)
        }
    }

    class TimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(time: String, isSelected: Boolean) {
            timeText.text = time
            itemView.alpha = if (isSelected) 1f else 0.5f
        }
    }
}
