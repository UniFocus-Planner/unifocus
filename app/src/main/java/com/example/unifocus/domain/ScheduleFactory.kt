package com.example.unifocus.domain

import com.example.unifocus.data.models.schedule.Schedule

class ScheduleFactory {
    companion object {
        fun createSchedule(
            groupName: String
        ): Schedule {
            return Schedule(groupName)
        }
    }
}