package com.example.unifocus.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", 0)
        val channelId = intent.getStringExtra("channelId") ?: "default_channel"
        val title = intent.getStringExtra("title") ?: "Title"
        val text = intent.getStringExtra("text") ?: "Text"

        NotificationService().sendNotification(context, notificationId, channelId, title, text)
    }
}