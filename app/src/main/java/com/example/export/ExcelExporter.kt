package com.example.export

import com.example.data.Session
import com.example.utils.TimeUtils
import org.dhatim.fastexcel.Workbook
import java.io.OutputStream

fun generateLogSheetXlsx(
    outputStream: OutputStream,
    sessions: List<Session>,
    targetMinutes: Int,
    liveActiveSeconds: Long,
    todayStr: String
) {
    val wb = Workbook(outputStream, "WorkTimeCalc", "1.0")
    val ws = wb.newWorksheet("Work Log")

    ws.width(0, 22.0)
    ws.width(1, 18.0)
    ws.width(2, 18.0)
    ws.width(3, 18.0)
    ws.width(4, 15.0)

    ws.value(0, 0, "WORK TIME LOG SHEET")
    ws.range(0, 0, 0, 4).merge()
    ws.range(0, 0, 0, 4).style()
        .bold()
        .fontSize(14)
        .horizontalAlignment("center")
        .fillColor("D1FAE5")
        .set()

    ws.value(2, 0, "Date")
    ws.style(2, 0).bold().set()
    ws.value(2, 1, todayStr)

    ws.value(3, 0, "Target Work")
    ws.style(3, 0).bold().set()
    ws.value(3, 1, TimeUtils.fmtDur(targetMinutes))

    val totalSeconds = sessions.sumOf { session ->
        val outVal = session.outTime
        val durationMs = if (outVal == null) {
            liveActiveSeconds * 1000
        } else {
            outVal - session.inTime
        }
        durationMs / 1000
    }

    val workStartMs = sessions.last().inTime
    val workEndMs = sessions.first().outTime

    val startStr = TimeUtils.fmtTimestamp(workStartMs)
    val endStr = if (workEndMs != null) TimeUtils.fmtTimestamp(workEndMs) else "Ongoing"

    var totalBreakSeconds = 0L
    val reversed = sessions.reversed()
    reversed.forEachIndexed { index, session ->
        if (index < reversed.size - 1) {
            val nextSession = reversed[index + 1]
            val outsideSec = (nextSession.inTime - (session.outTime ?: session.inTime)) / 1000
            if (outsideSec > 0) {
                totalBreakSeconds += outsideSec
            }
        }
    }

    ws.value(4, 0, "Total Work Time")
    ws.style(4, 0).bold().set()
    ws.value(4, 1, TimeUtils.fmtDurSeconds(totalSeconds))
    val targetSeconds = targetMinutes * 60L
    val workRatio = if (targetSeconds > 0) totalSeconds.toDouble() / targetSeconds.toDouble() else 1.0
    val workColorHex = when {
        workRatio >= 1.0 -> "10B981"
        workRatio >= 0.95 -> "D97706"
        else -> "DC2626"
    }
    ws.style(4, 1).bold().fontColor(workColorHex).set()

    ws.value(5, 0, "Total Break/Outside")
    ws.style(5, 0).bold().set()
    ws.value(5, 1, TimeUtils.fmtDurSeconds(totalBreakSeconds))

    ws.value(6, 0, "Work Start")
    ws.style(6, 0).bold().set()
    ws.value(6, 1, startStr)

    ws.value(7, 0, "Work End")
    ws.style(7, 0).bold().set()
    ws.value(7, 1, endStr)

    ws.value(9, 0, "TIMELINE BREAKDOWN")
    ws.range(9, 0, 9, 4).merge()
    ws.range(9, 0, 9, 4).style()
        .bold()
        .fontSize(11)
        .fillColor("F1F5F9")
        .set()

    val headers = arrayOf("Activity", "Start/In Time", "End/Out Time", "Duration", "Status")
    for (i in headers.indices) {
        ws.value(10, i, headers[i])
        ws.style(10, i)
            .bold()
            .horizontalAlignment("center")
            .fillColor("E2E8F0")
            .set()
    }

    var currentRow = 11
    reversed.forEachIndexed { index, session ->
        val sessionNumber = index + 1
        val inStr = TimeUtils.fmtTimestamp(session.inTime)
        val outStr = session.outTime?.let { TimeUtils.fmtTimestamp(it) } ?: "Ongoing"

        val isLastOngoing = session.outTime == null
        val durationSec = if (isLastOngoing) {
            liveActiveSeconds
        } else {
            (session.outTime!! - session.inTime) / 1000
        }

        val status = if (isLastOngoing) "Ongoing" else "Completed"

        ws.value(currentRow, 0, "Session $sessionNumber")
        ws.value(currentRow, 1, inStr)
        ws.value(currentRow, 2, outStr)
        ws.value(currentRow, 3, TimeUtils.fmtDurSeconds(durationSec))
        ws.value(currentRow, 4, status)

        for (i in 0..4) {
            ws.style(currentRow, i).horizontalAlignment("center").set()
        }
        ws.style(currentRow, 0).bold().set()
        if (isLastOngoing) {
            ws.style(currentRow, 4).bold().fontColor("10B981").set()
        }

        currentRow++

        if (index < reversed.size - 1) {
            val nextSession = reversed[index + 1]
            val outsideSec = (nextSession.inTime - (session.outTime ?: session.inTime)) / 1000
            if (outsideSec > 0) {
                val breakStart = session.outTime?.let { TimeUtils.fmtTimestamp(it) } ?: inStr
                val breakEnd = TimeUtils.fmtTimestamp(nextSession.inTime)

                val breakType = TimeUtils.classifyOutsideBreak(session.outTime!!, outsideSec)
                ws.value(currentRow, 0, breakType.excelTitle())
                ws.value(currentRow, 1, breakStart)
                ws.value(currentRow, 2, breakEnd)
                ws.value(currentRow, 3, TimeUtils.fmtDurSeconds(outsideSec))
                ws.value(currentRow, 4, breakType.excelStatus())

                for (i in 0..4) {
                    ws.style(currentRow, i)
                        .italic()
                        .horizontalAlignment("center")
                        .fillColor("F8FAFC")
                        .set()
                }

                currentRow++
            }
        }
    }

    wb.finish()
}
