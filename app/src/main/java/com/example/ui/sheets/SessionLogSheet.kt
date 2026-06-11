package com.example.ui.sheets

import com.example.ui.theme.AppTheme
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Session
import com.example.export.generateLogSheetXlsx
import com.example.ui.components.SessionBreakDivider
import com.example.ui.components.SessionLogItem
import com.example.ui.dialogs.ClearDayConfirmDialog
import com.example.ui.dialogs.DeleteSessionConfirmDialog
import com.example.ui.dialogs.EditSessionDialog
import com.example.ui.viewmodel.WorkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionLogSheet(
    viewModel: WorkViewModel,
    sessions: List<Session>,
    targetMinutes: Int,
    liveActiveSeconds: Long,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showEditDialogForSession by remember { mutableStateOf<Session?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<Session?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        )
    ) { uri ->
        if (uri != null) {
            try {
                val todayStr = viewModel.getTodayString()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    generateLogSheetXlsx(
                        outputStream = outputStream,
                        sessions = sessions,
                        targetMinutes = targetMinutes,
                        liveActiveSeconds = liveActiveSeconds,
                        todayStr = todayStr
                    )
                }
                Toast.makeText(context, "Saved Excel log successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save Excel sheet: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    if (showClearConfirm) {
        ClearDayConfirmDialog(
            onDismiss = { showClearConfirm = false },
            onConfirm = { viewModel.clearAllSessions() }
        )
    }

    sessionToDelete?.let { session ->
        DeleteSessionConfirmDialog(
            onDismiss = { sessionToDelete = null },
            onConfirm = { viewModel.deleteSession(session) }
        )
    }

    showEditDialogForSession?.let { session ->
        EditSessionDialog(
            session = session,
            onDismiss = { showEditDialogForSession = null },
            onSave = { updated ->
                viewModel.updateSession(updated)
                showEditDialogForSession = null
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.sheetSurface,
        scrimColor = AppTheme.colors.sheetScrim
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Session Log",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (sessions.isEmpty()) {
                                Toast.makeText(context, "No sessions to download!", Toast.LENGTH_SHORT).show()
                            } else {
                                val todayStr = viewModel.getTodayString()
                                val fileName = "work_log_${todayStr.replace(" ", "_")}.xlsx"
                                createDocumentLauncher.launch(fileName)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Export Excel",
                            tint = AppTheme.colors.accentGreen
                        )
                    }
                    Text(
                        text = "Clear Day",
                        color = AppTheme.colors.accentGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable { showClearConfirm = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sessions.isEmpty()) {
                Text(
                    text = "No sessions yet. Punch in to start.",
                    color = AppTheme.colors.hintText,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                    sessions.forEachIndexed { index, session ->
                        val sessionNumber = sessions.size - index
                        val isOpen = session.outTime == null
                        val durationSec = if (isOpen) {
                            liveActiveSeconds
                        } else {
                            (session.outTime!! - session.inTime) / 1000
                        }

                        SessionLogItem(
                            session = session,
                            sessionNumber = sessionNumber,
                            durationSec = durationSec,
                            isOpen = isOpen,
                            onEdit = { showEditDialogForSession = session },
                            onDelete = { sessionToDelete = session }
                        )

                        if (index < sessions.size - 1) {
                            val nextSession = sessions[index + 1]
                            if (nextSession.outTime != null) {
                                val outsideSec = (session.inTime - nextSession.outTime) / 1000
                                SessionBreakDivider(outsideSec = outsideSec, outTime = nextSession.outTime!!)
                            }
                        }
                    }
                }
            }
        }
    }
}
