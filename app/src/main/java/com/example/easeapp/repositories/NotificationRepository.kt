package com.example.easeapp.repositories

import com.example.easeapp.model.requests.NotificationApi
import com.example.ease.data.models.Notification

class NotificationRepository(
    private val api: NotificationApi
) {
    /**
     * 1) Fetch all notifications (up to 100) for the current user.
     *    Returns a List<Notification> or throws an exception if network fails.
     */
    suspend fun fetchAll(): List<Notification> {
        return api.getNotifications()
    }

    /**
     * 2) Mark one notification as read. Returns true if successful.
     */
    suspend fun markAsRead(notificationId: String): Boolean {
        val response = api.markNotificationRead(notificationId)
        return response.isSuccessful
    }

    /**
     * 3) Delete one notification by ID. Returns true if successful.
     */
    suspend fun deleteById(notificationId: String): Boolean {
        val response = api.deleteNotification(notificationId)
        return response.isSuccessful
    }

    /**
     * 4) Clear all notifications for the current user. Returns true if successful.
     */
    suspend fun clearAll(): Boolean {
        val response = api.clearAllNotifications()
        return response.isSuccessful
    }
}
