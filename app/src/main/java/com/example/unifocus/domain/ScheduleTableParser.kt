package com.example.unifocus.domain

import android.util.Log
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
//import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream

class ScheduleTableParser {

    companion object {
        private const val TAG = "ScheduleTableParser"
    }

    fun parseSchedule(filePath: String): Map<String, List<ScheduleItem>> {
        val scheduleData = mutableMapOf<String, MutableList<ScheduleItem>>()

        try {
            FileInputStream(File(filePath)).use { inputStream ->
                val workbook = when {
                    filePath.endsWith(".xls") -> HSSFWorkbook(inputStream)
                    //filePath.endsWith(".xlsx") -> XSSFWorkbook(inputStream)
                    else -> {
                        Log.e(TAG, "Unsupported file format")
                        return scheduleData
                    }
                }

                for (i in 0 until workbook.numberOfSheets) {
                    workbook.getSheetAt(i)?.let { sheet ->
                        parseSheet(sheet, scheduleData)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing schedule file", e)
        }

        return scheduleData
    }

    private fun parseSheet(sheet: Sheet, scheduleData: MutableMap<String, MutableList<ScheduleItem>>) {
        if (sheet.physicalNumberOfRows == 0) return

        val groupMap = mutableMapOf<Int, String>() // columnIndex -> groupName
        val headerRow = sheet.getRow(0) ?: return

        // Определяем группы по первой строке
        for (cell in headerRow) {
            val groupName = getCellValueAsString(cell)
            if (groupName.isNotBlank() && cell.columnIndex >= 3) {
                groupMap[cell.columnIndex] = groupName
            }
        }

        var currentDay = ""

        for (rowIndex in 1 until sheet.physicalNumberOfRows) {
            val row = sheet.getRow(rowIndex) ?: continue

            val dayCell = row.getCell(0)
            val lessonNumCell = row.getCell(1)

            val rawDay = getCellValueAsString(dayCell)
            val lessonNum = getCellValueAsString(lessonNumCell)

            if (rawDay.isNotBlank()) currentDay = rawDay
            if (lessonNum.isBlank()) continue  // пропускаем строки без номера пары

            for ((colIndex, groupName) in groupMap) {
                val cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                val value = getCellValueAsString(cell)

                val item = parseCellContent(value, groupName, currentDay, lessonNum)
                scheduleData.getOrPut(groupName) { mutableListOf() }.add(item)
            }
        }

    }




    private fun parseCellContent(value: String, group: String, day: String, lessonNum: String): ScheduleItem {
        val trimmedValue = value.trim()

        return if (trimmedValue.isBlank()) {
            ScheduleItem(
                group = group,
                day = day,
                lessonNum = lessonNum,
                subject = "-",
                teachers = emptyList(),
                room = ""
            )
        } else {
            val parts = trimmedValue.split("\n")
            val subject = parts.firstOrNull()?.trim() ?: "-"
            val teachersRaw = parts.drop(1).joinToString(" ").trim()
            val teachers = teachersRaw
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            ScheduleItem(
                group = group,
                day = day,
                lessonNum = lessonNum,
                subject = subject,
                teachers = teachers,
                room = ""
            )
        }
    }



    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> if (DateUtil.isCellDateFormatted(cell)) {
                cell.dateCellValue.toString()
            } else {
                cell.numericCellValue.toInt().toString()
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> try {
                cell.stringCellValue.trim()
            } catch (e: Exception) {
                cell.numericCellValue.toInt().toString()
            }
            else -> ""
        }
    }

    data class ScheduleItem(
        var group: String = "",
        var day: String = "",
        var lessonNum: String = "",
        var subject: String = "",
        var teachers: List<String> = emptyList(),
        var room: String = ""
    ) {
        fun isValid() = group.isNotEmpty() && day.isNotEmpty() && lessonNum.isNotEmpty()

        override fun toString(): String {
            return "ScheduleItem(group='$group', day='$day', lessonNum='$lessonNum', " +
                    "subject='$subject', teachers=$teachers, room='$room')"
        }
    }
}
