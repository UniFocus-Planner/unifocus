package com.example.unifocus

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.unifocus.domain.NotificationReceiver

import com.example.unifocus.ui.view.ProfileScreen
import com.example.unifocus.ui.view.ScheduleScreen
import com.example.unifocus.ui.view.TodayTasksScreen
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var todayButton: ImageButton
    private lateinit var scheduleButton: ImageButton
    private lateinit var profileButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)

        replaceFragment(ScheduleScreen())

        scheduleButton = findViewById<ImageButton>(R.id.schedule_screen).also { button ->
            button.setOnClickListener {
                replaceFragment(ScheduleScreen())
                updateButtonSelection(button)
            }
        }

        todayButton = findViewById<ImageButton>(R.id.today_button).also { button ->
            button.setOnClickListener {
                replaceFragment(TodayTasksScreen())
                updateButtonSelection(button)
            }
        }

        profileButton = findViewById<ImageButton>(R.id.profile_button).also { button ->
            button.setOnClickListener {
                replaceFragment(ProfileScreen())
                updateButtonSelection(button)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateButtonSelection(selectedButton:ImageButton) {
        profileButton.isSelected = false
        todayButton.isSelected = false
        scheduleButton.isSelected = false

        selectedButton.isSelected = true
    }

    fun CreateNotificationChannel(context: Context, channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = channelId
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                enableLights(true)
                enableVibration(true)

                // Sound settings
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                setSound(soundUri, audioAttributes)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(
        context: Context,
        targetTime: Calendar,
        notificationId: Int,
        channelId: String,
        title: String,
        text: String
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notificationId", notificationId)
            putExtra("channelId", channelId)
            putExtra("title", title)
            putExtra("text", text)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Android 6.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                targetTime.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                targetTime.timeInMillis,
                pendingIntent
            )
        }
    }

}
