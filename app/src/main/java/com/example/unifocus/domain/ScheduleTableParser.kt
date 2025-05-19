package com.example.unifocus.domain

import android.util.Log
import org.apache.poi.ss.usermodel.*
import java.io.File
import java.io.FileInputStream

data class ScheduleItem(
    val group: String,
    val subgroup: String,
    val day: String,
    val lessonNum: String,
    val subject: String,
    val teachers: List<String>,
    val rooms: List<String>,
    val weekType: WeekType
) {
    enum class WeekType { ODD, EVEN }

    fun print() {
        Log.d(group, "Group: ${group}, subgroup: ${subgroup}, Day ${day}, Lesson Num ${lessonNum}, Subject ${subject}, WeekType ${weekType}, Teachers ${teachers}, Rooms ${rooms}")
    }
}

class ScheduleTableParser {

    fun parse(filePath: String): Map<String, List<ScheduleItem>> {
        val file = File(filePath)
        val inputStream = FileInputStream(file)
        val workbook = WorkbookFactory.create(inputStream)
        val scheduleData = mutableMapOf<String, MutableList<ScheduleItem>>()

        var firstSheet = 0
        var lastSheet = workbook.numberOfSheets

        //firstSheet = 2
        //lastSheet = 3

        try {
            for (sheetIndex in firstSheet until lastSheet) {
                val sheet = workbook.getSheetAt(sheetIndex)
                parseSheet(sheet, scheduleData)
            }
        } finally {
            workbook.close()
            inputStream.close()
        }

        return scheduleData
    }

    private fun parseSheet(sheet: Sheet, scheduleData: MutableMap<String, MutableList<ScheduleItem>>) {
        if (sheet.physicalNumberOfRows < 3) return

        val groupHeaderRow = sheet.getRow(0) ?: return
        val subgroupHeaderRow = sheet.getRow(1) ?: return
        val mergedRegions = sheet.mergedRegions

        val groups = mutableListOf<GroupInfo>()
        var currentCol = 2

        while (currentCol < groupHeaderRow.lastCellNum.toInt()) {
            val groupCell = groupHeaderRow.getCell(currentCol)
            val groupName = getCellValueAsString(groupCell).takeIf { it.isNotBlank() }

            if (groupName == null) {
                currentCol++
                continue
            }

            if (groupName == "Время" || groupName.matches(Regex("\\d{2}:\\d{2}:\\d{2} - \\d{2}:\\d{2}:\\d{2}"))) {
                currentCol++
                continue
            }

            val mergedRegion = mergedRegions.find { it.isInRange(0, currentCol) }

            if (mergedRegion != null) {
                val startCol = mergedRegion.firstColumn
                val endCol = mergedRegion.lastColumn

                val subgroups = mutableListOf<SubgroupInfo>()
                var subgroupCol = startCol

                while (subgroupCol <= endCol) {
                    val subgroupCell = subgroupHeaderRow.getCell(subgroupCol)
                    val subgroupName = getCellValueAsString(subgroupCell).takeIf { it.isNotBlank() } ?: "1"

                    val subgroupMerged = mergedRegions.find { it.isInRange(1, subgroupCol) }

                    if (subgroupMerged != null) {
                        val subgroupNum = if (subgroupName.matches(Regex("\\d+"))) subgroupName else "1"
                        subgroups.add(SubgroupInfo(subgroupNum, subgroupCol))
                        subgroupCol = subgroupMerged.lastColumn + 1
                    } else {
                        val subgroupNum = if (subgroupName.matches(Regex("\\d+"))) subgroupName else "1"
                        subgroups.add(SubgroupInfo(subgroupNum, subgroupCol))
                        subgroupCol++
                    }
                }

                groups.add(GroupInfo(groupName, startCol, endCol, subgroups))
                currentCol = endCol + 1
            } else {
                val subgroupCell = subgroupHeaderRow.getCell(currentCol)
                val subgroupName = getCellValueAsString(subgroupCell).takeIf { it.isNotBlank() } ?: "1"
                val subgroupNum = if (subgroupName.matches(Regex("\\d+"))) subgroupName else "1"

                groups.add(GroupInfo(groupName, currentCol, currentCol,
                    listOf(SubgroupInfo(subgroupNum, currentCol))))
                currentCol++
            }
        }

        var currentDay = ""
        var rowIndex = 2

        while (rowIndex < sheet.physicalNumberOfRows) {
            val rowOdd = sheet.getRow(rowIndex) ?: break
            val rowEven = if (rowIndex + 1 < sheet.physicalNumberOfRows) sheet.getRow(rowIndex + 1) else null

            getCellValueAsString(rowOdd.getCell(0)).takeIf { it.isNotBlank() }?.let { currentDay = it }

            val lessonNum = getCellValueAsString(rowOdd.getCell(1))
            if (lessonNum.isBlank()) {
                rowIndex += 2
                continue
            }

            for (group in groups) {
                for (subgroup in group.subgroups) {
                    if (subgroup.name == "2") continue
                    fun createItem(cell: Cell?, roomCell: Cell?, weekType: ScheduleItem.WeekType): ScheduleItem {
                        val subjectText = getCellValueAsString(cell).trim()
                        val roomText = getCellValueAsString(roomCell).trim()

                        val (subject, teachers) = if (subjectText.isBlank()) {
                            "-" to emptyList()
                        } else {
                            val parts = subjectText.split("\n")
                            parts[0].trim() to parts.drop(1)
                                .joinToString(" ")
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                        }

                        val rooms = if (roomText.isBlank()) {
                            emptyList()
                        } else {
                            roomText.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                        }

                        return ScheduleItem(
                            group = group.name,
                            subgroup = subgroup.name,
                            day = currentDay,
                            lessonNum = lessonNum,
                            subject = subject,
                            teachers = teachers,
                            rooms = rooms,
                            weekType = weekType
                        )
                    }

                    val oddCell = rowOdd.getCell(subgroup.column)
                    val oddRoomCell = rowOdd.getCell(subgroup.column + 1)
                    val groupKey = "${group.name}_${subgroup.name}"

                    val hasOddEntry = scheduleData[groupKey]?.any {
                        it.day == currentDay && it.lessonNum == lessonNum && it.weekType == ScheduleItem.WeekType.ODD
                    } ?: false

                    if (!hasOddEntry) {
                        if (oddCell != null && getCellValueAsString(oddCell).isNotBlank()) {
                            scheduleData.getOrPut(groupKey) { mutableListOf() }
                                .add(createItem(oddCell, oddRoomCell, ScheduleItem.WeekType.ODD))
                        } else {
                            scheduleData.getOrPut(groupKey) { mutableListOf() }
                                .add(createItem(null, null, ScheduleItem.WeekType.ODD))
                        }
                    }

                    if (rowEven != null) {
                        val evenCell = rowEven.getCell(subgroup.column)
                        val evenRoomCell = rowEven.getCell(subgroup.column + 1)

                        val hasEvenEntry = scheduleData[groupKey]?.any {
                            it.day == currentDay && it.lessonNum == lessonNum && it.weekType == ScheduleItem.WeekType.EVEN
                        } ?: false

                        if (!hasEvenEntry) {
                            if (evenCell != null && getCellValueAsString(evenCell).isNotBlank()) {
                                scheduleData.getOrPut(groupKey) { mutableListOf() }
                                    .add(createItem(evenCell, evenRoomCell, ScheduleItem.WeekType.EVEN))
                            } else {
                                scheduleData.getOrPut(groupKey) { mutableListOf() }
                                    .add(createItem(null, null, ScheduleItem.WeekType.EVEN))
                            }
                        }
                    }
                }
            }

            rowIndex += 2
        }
    }

    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""

        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> if (DateUtil.isCellDateFormatted(cell)) {
                cell.dateCellValue.toString()
            } else {
                val numValue = cell.numericCellValue
                if (numValue == numValue.toInt().toDouble()) {
                    numValue.toInt().toString()
                } else {
                    numValue.toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> when (cell.cachedFormulaResultType) {
                CellType.STRING -> cell.stringCellValue.trim()
                CellType.NUMERIC -> cell.numericCellValue.toString()
                CellType.BOOLEAN -> cell.booleanCellValue.toString()
                else -> ""
            }
            else -> ""
        }
    }

    private data class GroupInfo(
        val name: String,
        val startColumn: Int,
        val endColumn: Int,
        val subgroups: List<SubgroupInfo>
    )

    private data class SubgroupInfo(
        val name: String,
        val column: Int
    )
}