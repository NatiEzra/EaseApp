package com.example.easeapp.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R
import com.example.easeapp.model.AppointmentDate

class DateAdapter : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private var dates: List<AppointmentDate> = emptyList()
    private var selectedPosition = RecyclerView.NO_POSITION
    private var onDateSelectedListener: ((String) -> Unit)? = null

    fun submitList(list: List<AppointmentDate>) {
        dates = list
        notifyDataSetChanged()
    }

    fun getSelectedDate(): String? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            dates[selectedPosition].date
        } else null
    }

    fun setOnDateSelectedListener(listener: (String) -> Unit) {
        onDateSelectedListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
        return DateViewHolder(view)
    }

    override fun getItemCount(): Int = dates.size

    override fun onBindViewHolder(holder: DateViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = dates[position]
        holder.bind(item, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = position
            notifyItemChanged(previous)
            notifyItemChanged(position)
            onDateSelectedListener?.invoke(item.date)
        }
    }

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayLetter: TextView = itemView.findViewById(R.id.dayLetter)
        private val dayNumber: TextView = itemView.findViewById(R.id.dayNumber)

        fun bind(item: AppointmentDate, isSelected: Boolean) {
            dayLetter.text = item.day
            dayNumber.text = item.dayNum
            itemView.alpha = if (isSelected) 1f else 0.5f
        }
    }
}
