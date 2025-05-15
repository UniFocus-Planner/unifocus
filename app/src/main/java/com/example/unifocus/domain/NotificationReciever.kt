package com.example.unifocus.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.unifocus.domain.NotificationService

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", 0)
        val channelId = intent.getStringExtra("channelId") ?: "default_channel"
        val title = intent.getStringExtra("title") ?: "Title"
        val text = intent.getStringExtra("text") ?: "Text"

        val notificationService = NotificationService()
        notificationService.sendNotification(context, notificationId, channelId, title, text)
    }
}
