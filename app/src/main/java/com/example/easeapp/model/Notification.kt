package com.example.ease.data.models

import com.squareup.moshi.Json
import java.util.*

/**
 * Matches your MongoDB Notification schema:
 * {
 *   _id: ...,
 *   userId: ...,
 *   message: ...,
 *   appointmentId: ...,
 *   isRead: ...,
 *   createdAt: ...
 * }
 */
data class Notification(
    @Json(name = "_id")
    val id: String,

    val userId: String,
    val message: String,

    @Json(name = "appointmentId")
    val appointmentId: String,

    @Json(name = "isRead")
    val isRead: Boolean,

    @Json(name = "createdAt")
    val createdAt: Date
)

