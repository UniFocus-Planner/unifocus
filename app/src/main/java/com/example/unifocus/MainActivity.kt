package com.example.unifocus

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.unifocus.data.database.UniFocusDatabase
import com.example.unifocus.data.models.task.TaskType
import com.example.unifocus.data.repository.UniFocusRepository
import com.example.unifocus.domain.NotificationReceiver
import com.example.unifocus.domain.ScheduleFactory
import com.example.unifocus.domain.TaskFactory
import com.example.unifocus.ui.adapter.TaskAdapter
import com.example.unifocus.ui.viewmodels.UniFocusViewModel
import com.example.unifocus.ui.viewmodels.UniFocusViewModelFactory
import com.example.unifocus.domain.NotificationService

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter

    private val viewModel: UniFocusViewModel by viewModels {
        UniFocusViewModelFactory(UniFocusRepository(UniFocusDatabase.getDatabase(this)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        adapter = TaskAdapter()
        recyclerView.adapter = adapter

        viewModel.classTasks.observe(this) { tasks ->
            println("Задач обновлено: ${tasks?.size}")
            adapter.submitList(tasks)
        }

        findViewById<Button>(R.id.addButton).also {
            it.setOnClickListener {
                viewModel.createTask("BYE", taskType = TaskType.CLASS)
            }
        }

        findViewById<Button>(R.id.deleteButton).also {
            it.setOnClickListener {
                viewModel.deleteAllTasks()
            }
        }

        addTestData()

        // Notifications
        val channel_id = "UniFocus"
        requestNotificationPermission()
        CreateNotificationChannel(this, channel_id)

        val notificationService: NotificationService = NotificationService()
        var notificationCounter = 0

        findViewById<Button>(R.id.notificationButton).also {
            it.setOnClickListener {
                val notificationID = notificationCounter++
                val channelID = channel_id
                val title = "Test Notification"
                val text = "This is a test notification"
                val delayInSeconds = 2 // Delay to test outside the app

                scheduleNotification(this, delayInSeconds, notificationID, channelID, title, text)
            }
        }
    }

    private fun addTestTask() {
        println("Creating hello task")
        println(viewModel.classTasks.value)
        viewModel.createTask(
            name = "HELLO",
            taskType = TaskType.CLASS)
    }

    private fun addTestData() {
        CoroutineScope(Dispatchers.IO).launch {
            val schedule = ScheduleFactory.createSchedule("БПИ-22-РП-3")

            val tasks = listOf(
                TaskFactory.createTask(
                    name = "Математика",
                    description = "Лекция по математике",
                    deadline = System.currentTimeMillis() + 86400000,
                    taskType = TaskType.CLASS
                ),
                TaskFactory.createTask(
                    name = "История",
                    description = "Семинар по истории",
                    deadline = System.currentTimeMillis() + 172800000,
                    taskType = TaskType.CLASS
                )
            )

            viewModel.addSchedule(schedule, tasks)
        }
    }

    private val REQUEST_CODE_POST_NOTIFICATIONS = 1
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_POST_NOTIFICATIONS
            )
        }
        else {
            Toast.makeText(this, "Notification permission request is not needed", Toast.LENGTH_SHORT).show()
        }
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

    fun scheduleNotification(context: Context, delayInSeconds: Int, notificationId: Int, channelId: String, title: String, text: String) {
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
        val timeInMillis = Calendar.getInstance().timeInMillis + delayInSeconds * 1000

        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )

    }

}
