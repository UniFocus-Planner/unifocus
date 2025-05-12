package com.example.unifocus.domain

class ScheduleRepository(private val parsedSchedules: Map<String, List<ScheduleItem>>) {

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
}