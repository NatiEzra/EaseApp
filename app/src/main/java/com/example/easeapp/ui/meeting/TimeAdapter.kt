package com.example.easeapp.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R
import com.example.easeapp.model.DisplayableSlot

class TimeAdapter : RecyclerView.Adapter<TimeAdapter.TimeViewHolder>() {

    private var slots: List<DisplayableSlot> = emptyList()
    private var selectedPosition = RecyclerView.NO_POSITION

    fun submitList(list: List<DisplayableSlot>) {
        slots = list
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun getSelectedSlot(): DisplayableSlot? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            slots[selectedPosition]
        } else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return TimeViewHolder(view)
    }

    override fun getItemCount(): Int = slots.size

    override fun onBindViewHolder(holder: TimeViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val slot = slots[position]
        holder.bind(slot, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = position
            notifyItemChanged(previous)
            notifyItemChanged(position)
        }
    }

    class TimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(slot: DisplayableSlot, isSelected: Boolean) {
            timeText.text = slot.display
            itemView.isSelected = isSelected
        }
    }
}
