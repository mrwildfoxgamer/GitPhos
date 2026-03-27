package com.example.gitphos.ui.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gitphos.data.local.db.entity.UploadQueueEntity
import com.example.gitphos.ui.theme.GitPhosTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SyncScreen(
    state: SyncState,
    onEvent: (SyncEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SyncEvent.NavigateBack) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.pendingItems.any { it.status == "COMPLETED" }) {
                        TextButton(onClick = { onEvent(SyncEvent.ClearCompleted) }) {
                            Text("Clear Done")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                if (state.isSyncing) {
                    OutlinedButton(
                        onClick = { onEvent(SyncEvent.CancelSync) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancel Sync") }
                } else {
                    Button(
                        onClick = { onEvent(SyncEvent.StartSync) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.noActiveRepo && state.pendingItems.any { it.status == "PENDING" }
                    ) { Text("Start Sync") }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            when {
                state.noActiveRepo -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active repository configured.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> {
                    StatusBanner(state.workStatus)
                    RepoInfoRow(state.activeRepoName, state.remoteUrl)
                    if (state.isSyncing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    HorizontalDivider()
                    Text(
                        "Queue (${state.pendingItems.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (state.pendingItems.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Queue is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.pendingItems, key = { it.id }) { item ->
                                QueueItemRow(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBanner(workStatus: WorkStatus) {
    when (workStatus) {
        WorkStatus.Idle -> Unit
        WorkStatus.Running -> Unit // progress bar handles this
        is WorkStatus.Success -> {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Sync complete — ${workStatus.uploaded} file(s) uploaded")
                }
            }
        }
        is WorkStatus.PartialSuccess -> {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Text("${workStatus.uploaded} uploaded, ${workStatus.failed} failed")
                }
            }
        }
        is WorkStatus.Failed -> {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text("Sync failed: ${workStatus.reason}", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

@Composable
private fun RepoInfoRow(name: String, remoteUrl: String) {
    Column {
        Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(remoteUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun QueueItemRow(item: UploadQueueEntity) {
    val fileName = item.filePath.substringAfterLast("/")
    val (statusColor, statusLabel) = when (item.status) {
        "PENDING" -> MaterialTheme.colorScheme.onSurfaceVariant to "Pending"
        "IN_PROGRESS" -> MaterialTheme.colorScheme.primary to "Uploading…"
        "COMPLETED" -> MaterialTheme.colorScheme.primary to "Done"
        "FAILED" -> MaterialTheme.colorScheme.error to "Failed"
        else -> MaterialTheme.colorScheme.onSurface to item.status
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(fileName, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (item.status == "FAILED" && item.errorMessage != null) {
                Text(item.errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Text(statusLabel, style = MaterialTheme.typography.bodySmall, color = statusColor, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true, name = "Idle with queue")
@Composable
private fun SyncPreviewIdle() {
    GitPhosTheme {
        SyncScreen(
            state = SyncState(
                activeRepoName = "my-photos",
                remoteUrl = "https://github.com/user/my-photos",
                pendingItems = listOf(
                    UploadQueueEntity(id = 1, repoId = 1, filePath = "/storage/img1.jpg", status = "PENDING"),
                    UploadQueueEntity(id = 2, repoId = 1, filePath = "/storage/img2.jpg", status = "COMPLETED"),
                    UploadQueueEntity(id = 3, repoId = 1, filePath = "/storage/img3.jpg", status = "FAILED", errorMessage = "Push rejected")
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Syncing")
@Composable
private fun SyncPreviewRunning() {
    GitPhosTheme {
        SyncScreen(
            state = SyncState(
                activeRepoName = "my-photos",
                remoteUrl = "https://github.com/user/my-photos",
                isSyncing = true,
                workStatus = WorkStatus.Running,
                pendingItems = listOf(
                    UploadQueueEntity(id = 1, repoId = 1, filePath = "/storage/img1.jpg", status = "IN_PROGRESS")
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "No repo")
@Composable
private fun SyncPreviewNoRepo() {
    GitPhosTheme {
        SyncScreen(state = SyncState(noActiveRepo = true), onEvent = {})
    }
}