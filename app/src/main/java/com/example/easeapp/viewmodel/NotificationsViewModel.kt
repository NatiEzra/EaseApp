package com.example.easeapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ease.data.models.Notification
import com.example.easeapp.repositories.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for maintaining the “number of unread notifications” as LiveData.
 * Any Activity/Fragment can observe `unreadCount` and react the moment it changes.
 */
class NotificationsViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    // 1) Backing MutableLiveData for the unread count
    private val _unreadCount = MutableLiveData<Int>(0)
    val unreadCount: LiveData<Int> = _unreadCount

    /**
     * 2) Call this once—e.g. from Activity.onCreate()—to load the current unread count.
     */
    fun loadInitialUnreadCount() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allNotifications: List<Notification> = repository.fetchAll()
                // Count how many are unread
                val count = allNotifications.count { !it.isRead }
                _unreadCount.postValue(count)
            } catch (e: Exception) {
                // On error, just set to zero (or handle however you want)
                _unreadCount.postValue(0)
            }
        }
    }

    /**
     * 3) Call this whenever a notification is newly received (e.g. via WebSocket or a push).
     *    It will increment the existing count by 1 (or more, if you pass >1).
     */
    fun incrementBy(newItems: Int = 1) {
        val current = _unreadCount.value ?: 0
        _unreadCount.value = current + newItems
    }

    /**
     * 4) Call this whenever a specific notification is marked read—so we decrement by 1.
     *    If you mark multiple at once, pass that number to subtract.
     */
    fun decrementBy(readItems: Int = 1) {
        val current = _unreadCount.value ?: 0
        val updated = (current - readItems).coerceAtLeast(0)
        _unreadCount.value = updated
    }

    /**
     * 5) If you clear all notifications, call this to reset the count to zero.
     */
    fun clearAll() {
        _unreadCount.value = 0
    }
}
