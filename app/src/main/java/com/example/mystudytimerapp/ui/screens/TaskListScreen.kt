package com.example.mystudytimerapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mystudytimerapp.data.StudyTask
import com.example.mystudytimerapp.ui.viewmodel.TaskFilter
import com.example.mystudytimerapp.ui.viewmodel.TaskListUiState
import com.example.mystudytimerapp.ui.viewmodel.TaskListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onNavigateToTimer: (Int) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Confirmation dialog state managed via ViewModel's taskToDelete
    uiState.taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = {
                Text(
                    text = "削除の確認",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = "「${task.title}」を削除しますか？")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDeleteTask() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("削除", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("キャンセル")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "集中学習タイマー",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToHistory,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "学習履歴"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Section 4.1: Task Addition Component
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "学習内容を入力",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.inputText,
                            onValueChange = { viewModel.onInputTextChanged(it) },
                            placeholder = { Text("例: Kotlinの復習") },
                            isError = uiState.errorMessage != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    viewModel.addTask()
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                viewModel.addTask()
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("追加", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Validation Error Message Display
                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                        )
                    }
                }
            }

            // Section 8 (Advanced 2): Filter Chips and Bulk Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TaskFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = uiState.currentFilter == filter,
                            onClick = { viewModel.setFilter(filter) },
                            label = { Text(filter.label, fontSize = 12.sp) }
                        )
                    }
                }

                // Advanced 1: Delete completed tasks button
                if (uiState.completedCount > 0) {
                    IconButton(
                        onClick = { viewModel.deleteCompletedTasks() },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = "完了済みを削除"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Section 4.2 & 4.3: Task List using LazyColumn
            if (uiState.filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.totalCount == 0) "学習項目がありません。\n上の入力欄から追加してください。" else "該当する学習項目がありません。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.filteredTasks,
                        key = { it.id }
                    ) { task ->
                        TaskItemRow(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                            onStartTimer = { onNavigateToTimer(task.id) },
                            onDelete = { viewModel.requestDeleteTask(task) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 4.5: Count Display & Completion Message
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // All Completed Message (only if totalCount > 0 and completed == total)
                    if (uiState.totalCount > 0 && uiState.completedCount == uiState.totalCount) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "すべての学習が完了しました！",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Count text: 完了：X件 / 全体：Y件
                    Text(
                        text = "完了：${uiState.completedCount}件 / 全体：${uiState.totalCount}件",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItemRow(
    task: StudyTask,
    onToggleComplete: () -> Unit,
    onStartTimer: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox for completion state
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )

            // Task title with strikethrough if completed
            Text(
                text = task.title,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                fontSize = 16.sp,
                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                color = if (task.isCompleted)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.onSurface,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Timer Start Button
            FilledTonalButton(
                onClick = onStartTimer,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text("開始", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // Delete Button
            OutlinedButton(
                onClick = onDelete,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("削除", fontSize = 13.sp)
            }
        }
    }
}
