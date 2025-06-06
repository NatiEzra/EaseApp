package com.example.easeapp.ui.activities

import android.app.AlertDialog
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R
import com.example.ease.data.models.Notification
import com.example.ease.ui.activities.MainActivity
import com.example.easeapp.model.RetrofitProvider.RetrofitProvider
import com.example.easeapp.model.requests.NotificationApi
import com.example.easeapp.repositories.NotificationRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class NotificationsFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var repository: NotificationRepository
    private lateinit var noNotificationsTextView: TextView
    private lateinit var clearAllButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable options menu if you want “Clear All” in the toolbar
        setHasOptionsMenu(true)

        // 1) Create the Retrofit-based NotificationApi and repository.
        //    Replace tokenProvider() with your actual logic for obtaining a JWT, if needed.
        val retrofit = RetrofitProvider.provideRetrofit(requireContext())
        val api: NotificationApi = retrofit.create(NotificationApi::class.java)

        repository = NotificationRepository(api)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.recycler_notifications)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        noNotificationsTextView = view.findViewById(R.id.notification_empty_text)
        clearAllButton = view.findViewById(R.id.clear_all_button)

        adapter = NotificationAdapter(emptyList(),
            onItemClick = { notification ->
                // 2) Mark it as read when clicked:
                markAsRead(notification.id)

            },

        )
        clearAllButton.setOnClickListener {
            promptClearAll()
        }

        recycler.adapter = adapter

        attachSwipeToDelete()

        loadNotifications()
    }

    /** Inflate a small menu with a “Clear All” option (optional) */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_notification, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_all -> {
                promptClearAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** 1) Load from server and submit to adapter */
    private fun loadNotifications() {
        lifecycleScope.launch {
            try {
                val list = repository.fetchAll()
                Log.d("NotificationsDebug", "notifications = $list")
                if (list.isEmpty()) {
                    noNotificationsTextView.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                } else {
                    noNotificationsTextView.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                }
                adapter.updateList(list)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 2) Mark a notification as read */
    private fun markAsRead(notificationId: String?) {
        if (notificationId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "cant mark notification without id", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val success = repository.markAsRead(notificationId)
            if (success) {
                loadNotifications()
                (activity as? MainActivity)?.updateUnreadBadge()
            } else {
                Toast.makeText(requireContext(), "Failed to mark as read", Toast.LENGTH_SHORT).show()
            }
        }
    }


    /** 3) Delete a notification by ID */
    private fun deleteNotification(notificationId: String) {
        lifecycleScope.launch {
            val success = repository.deleteById(notificationId)
            if (success) {
                loadNotifications()
            } else {
                Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Confirm before deleting via a dialog */
    private fun confirmDeletion(notificationId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete notification")
            .setMessage("Are you sure you want to delete this notification?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                deleteNotification(notificationId)
            }
            .show()
    }

    /** 4) Prompt to clear all notifications */
    private fun promptClearAll() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear all notifications")
            .setMessage("This action will delete all of your notifications. Continue?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                clearAll()
            }
            .show()
    }

    private fun clearAll() {
        lifecycleScope.launch {
            val success = repository.clearAll()
            if (success) {
                adapter.updateList(emptyList())
                noNotificationsTextView.visibility = View.VISIBLE
                recycler.visibility = View.GONE
                if (adapter.itemCount == 0) {
                    noNotificationsTextView.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                } else {
                    noNotificationsTextView.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                }
                (activity as? MainActivity)?.updateUnreadBadge()
            } else {
                Toast.makeText(requireContext(), "Failed to clear notifications", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun attachSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val notification = adapter.getItemAt(position)

                val notificationId = notification.id
                if (notificationId.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "לא ניתן למחוק התראה ללא מזהה תקף", Toast.LENGTH_SHORT).show()
                    adapter.notifyItemChanged(position)
                    return
                }

                lifecycleScope.launch {
                    val deleted = repository.deleteById(notificationId)
                    if (deleted) {
                        val currentList = adapter.run {
                            val temp = items.toMutableList()
                            temp.removeAt(position)
                            temp.toList()
                        }
                        adapter.updateList(currentList)
                        if (adapter.itemCount == 0) {
                            noNotificationsTextView.visibility = View.VISIBLE
                            recycler.visibility = View.GONE
                        }
                        (activity as? MainActivity)?.updateUnreadBadge()
                    } else {
                        adapter.notifyItemChanged(position)
                        if (adapter.itemCount == 0) {
                            noNotificationsTextView.visibility = View.VISIBLE
                            recycler.visibility = View.GONE
                        }
                        Toast.makeText(requireContext(), "Could not delete", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(recycler)
    }
}

/** Adapter updated with click listeners for read/delete: */
class NotificationAdapter(
    var items: List<Notification>,
    private val onItemClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage = itemView.findViewById<TextView>(R.id.tv_notification_message)
        private val tvDate    = itemView.findViewById<TextView>(R.id.tv_notification_date)

        fun bind(item: Notification) {
            tvMessage.text = item.message
            tvDate.text    = dateFormat.format(item.createdAt)
            itemView.alpha = if (item.isRead) 0.4f else 1.0f

            itemView.setOnClickListener {
                onItemClick(item)
            }
            // No long-click listener here anymore
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    /** Replace the entire list and refresh. */
    fun updateList(newList: List<Notification>) {
        items = newList
        notifyDataSetChanged()
    }

    /** Return the Notification at a given adapter position. */
    fun getItemAt(position: Int): Notification = items[position]

    /** Remove the item at `position` and notify the adapter. */
    fun removeAt(position: Int) {
        val mutable = items.toMutableList()
        mutable.removeAt(position)
        items = mutable.toList()
        notifyItemRemoved(position)
    }
}
