package com.example.unifocus

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

import com.example.unifocus.ui.view.ProfileScreen
import com.example.unifocus.ui.view.ScheduleScreen
import com.example.unifocus.ui.view.TodayTasksScreen

class MainActivity : AppCompatActivity() {
    private lateinit var todayButton: Button
    private lateinit var scheduleButton: Button
    private lateinit var profileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_layout)

        replaceFragment(ScheduleScreen())

        scheduleButton = findViewById<Button>(R.id.schedule_screen).also { button ->
            button.setOnClickListener {
                replaceFragment(ScheduleScreen())
                updateButtonSelection(button)
            }
        }

        todayButton = findViewById<Button>(R.id.today_button).also { button ->
            button.setOnClickListener {
                replaceFragment(TodayTasksScreen())
                updateButtonSelection(button)
            }
        }

        profileButton = findViewById<Button>(R.id.profile_button).also { button ->
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

    private fun updateButtonSelection(selectedButton:Button) {
        profileButton.isSelected = false
        todayButton.isSelected = false
        scheduleButton.isSelected = false

        selectedButton.isSelected = true
    }
}
