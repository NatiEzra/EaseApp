package com.example.easeapp.model.requests

import com.example.ease.data.models.Notification
import retrofit2.Response
import retrofit2.http.*

interface NotificationApi {

    @GET("api/notifications")
    suspend fun getNotifications(): List<Notification>

    @PATCH("api/notifications/{id}/read")
    suspend fun markNotificationRead(
        @Path("id") notificationId: String
    ): Response<Void>

    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") notificationId: String
    ): Response<Void>

    @DELETE("api/notifications")
    suspend fun clearAllNotifications(): Response<Void>
}
