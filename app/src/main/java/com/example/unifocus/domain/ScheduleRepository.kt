package com.example.unifocus.domain

import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.models.task.TaskType
import com.example.unifocus.ui.viewmodels.UniFocusViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class ScheduleRepository(private val parsedSchedules: Map<String, List<ScheduleItem>>) {

    fun getAllTeachers() : List<String> {
        val result = mutableListOf<String>()

        getAllScheduleItems().forEach { scheduleItem ->
            scheduleItem.teachers.forEach { teacher ->
                if(!result.contains(teacher)) result.add(teacher)
            }
        }

        return result
    }

    fun getAllGroups() : List<String> {
        val result = mutableListOf<String>()

        getAllScheduleItems().forEach { scheduleItem ->
            if(!result.contains(scheduleItem.group)) result.add(scheduleItem.group)
        }

        return result
    }

    fun getAllScheduleItems(): List<ScheduleItem> {
        return parsedSchedules.flatMap { it.value }
    }

    fun filterByGroup(groupName: String): List<ScheduleItem> {
        return getAllScheduleItems().filter {
            it.group == groupName || it.group.startsWith("${groupName}_")
        }
    }

    // с подгруппой (например не БПИ-22-РП-3, а БПИ-22-РП-3_1)
    fun filterByFullGroupName(fullGroupName: String): List<ScheduleItem> {
        return parsedSchedules[fullGroupName] ?: emptyList()
    }

    fun filterByTeacher(teacherName: String): List<ScheduleItem> {
        return getAllScheduleItems().filter {
            it.teachers.any { teacher ->
                teacher.contains(teacherName, ignoreCase = true)
            }
        }
    }

    fun filterByWeekDay(day: String): List<ScheduleItem> {
        return getAllScheduleItems().filter { it.day.equals(day, ignoreCase = true) }
    }

    fun filterByWeekType(weekType: ScheduleItem.WeekType): List<ScheduleItem> {
        return getAllScheduleItems().filter { it.weekType == weekType }
    }

    // несколько фильтров
    fun filter(
        groupName: String? = null,
        fullGroupName: String? = null,
        teacherName: String? = null,
        day: String? = null,
        weekType: ScheduleItem.WeekType? = null
    ): List<ScheduleItem> {
        var result = getAllScheduleItems()

        groupName?.let { name ->
            result = result.filter { item ->
                item.group == name || item.group.startsWith("${name}_")
            }
        }

        fullGroupName?.let { name ->
            result = result.filter { item ->
                "${item.group}_${item.subgroup}" == name
            }
        }

        teacherName?.let { name ->
            result = result.filter { item ->
                item.teachers.any { teacher ->
                    teacher.contains(name, ignoreCase = true)
                }
            }
        }

        day?.let { dayName ->
            result = result.filter { item ->
                item.day.equals(dayName, ignoreCase = true)
            }
        }

        weekType?.let { type ->
            result = result.filter { item ->
                item.weekType == type
            }
        }

        return result
    }

    // Функция для преобразования дня недели из строки в DayOfWeek
    fun parseDayOfWeek(dayString: String): DayOfWeek {
        return when (dayString) {
            "Понедельник" -> DayOfWeek.MONDAY
            "Вторник" -> DayOfWeek.TUESDAY
            "Среда" -> DayOfWeek.WEDNESDAY
            "Четверг" -> DayOfWeek.THURSDAY
            "Пятница" -> DayOfWeek.FRIDAY
            "Суббота" -> DayOfWeek.SATURDAY
            "Воскресенье" -> DayOfWeek.SUNDAY
            else -> throw IllegalArgumentException("Неизвестный день недели: $dayString")
        }
    }

    fun calculateLessonDates(
        startDate: LocalDate,
        dayOfWeek: DayOfWeek,
        weekType: ScheduleItem.WeekType?,
        totalWeeks: Int = 1
    ): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()

        var currentDate = startDate.with(TemporalAdjusters.nextOrSame(dayOfWeek))

        for (week in 0 until totalWeeks) {
            if (weekType == null ||
                (weekType == ScheduleItem.WeekType.EVEN && week % 2 == 0) ||
                (weekType == ScheduleItem.WeekType.ODD && week % 2 == 1)) {
                dates.add(currentDate)
            }
            currentDate = currentDate.plusWeeks(1)
        }

        return dates
    }

    fun parseScheduleItemToTask(scheduleItem: ScheduleItem, viewModel: UniFocusViewModel, schedule: String): Task {
        val startDate = LocalDate.of(2025, 2, 5)
        val dayOfWeek = parseDayOfWeek(scheduleItem.day)

        val lessonDates = calculateLessonDates(startDate, dayOfWeek, scheduleItem.weekType)
        val firstLessonDate = lessonDates.firstOrNull()


        val deadline = firstLessonDate?.atTime(9, 0)

        var task = viewModel.createTask(
            name = scheduleItem.subject,
            description = "",
            deadline = deadline,
            taskType = TaskType.CLASS,
            room = scheduleItem.rooms.joinToString(", "),
            teacher = scheduleItem.teachers.joinToString(", "),
            weekType = scheduleItem.weekType,
            number = scheduleItem.lessonNum.toInt(),
            schedule = schedule,
            selected = false,
            additionalInformation = "Группа: ${scheduleItem.group}, Подгруппа: ${scheduleItem.subgroup}"
        )

        return task
    }
}