package com.example.gitphos.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gitphos.data.local.db.entity.RepoMetadataEntity
import com.example.gitphos.data.local.db.entity.SyncHistoryEntity
import com.example.gitphos.ui.theme.GitPhosTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardState,
    onEvent: (DashboardEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GitPhos") },
                actions = {
                    IconButton(onClick = { onEvent(DashboardEvent.Logout) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RepoCard(repo = state.activeRepo, onManageRepo = { onEvent(DashboardEvent.ManageRepo) })
                SyncStatusCard(pendingCount = state.pendingCount, lastSync = state.lastSync)
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onEvent(DashboardEvent.PickImages) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Images")
                    }
                    Button(
                        onClick = { onEvent(DashboardEvent.SyncNow) },
                        modifier = Modifier.weight(1f),
                        enabled = state.pendingCount > 0
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Sync Now")
                    }
                }
            }
        }
    }
}

@Composable
private fun RepoCard(
    repo: RepoMetadataEntity?,
    onManageRepo: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Repository", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onManageRepo) {
                    Text(if (repo == null) "Set up" else "Change")
                }
            }
            if (repo != null) {
                Spacer(Modifier.height(8.dp))
                Text(repo.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(repo.remoteUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text("Branch: ${repo.branch}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Spacer(Modifier.height(8.dp))
                Text("No repository configured", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SyncStatusCard(
    pendingCount: Int,
    lastSync: SyncHistoryEntity?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sync Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pending uploads", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "$pendingCount",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (pendingCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            if (lastSync != null) {
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Last sync", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(lastSync.syncedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Status", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        lastSync.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (lastSync.status == "SUCCESS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
                if (lastSync.filesChanged > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Files synced", style = MaterialTheme.typography.bodyMedium)
                        Text("${lastSync.filesChanged}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Text("No syncs yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview(showBackground = true, name = "Idle — repo + sync")
@Composable
private fun DashboardPreviewIdle() {
    GitPhosTheme {
        DashboardScreen(
            state = DashboardState(
                activeRepo = RepoMetadataEntity(id = 1, name = "my-photos", localPath = "/data/repo", remoteUrl = "https://github.com/user/my-photos", isActive = true),
                pendingCount = 3,
                lastSync = SyncHistoryEntity(repoId = 1, commitHash = "abc123", filesChanged = 5, status = "SUCCESS", triggeredBy = "MANUAL"),
                isLoading = false
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "No repo configured")
@Composable
private fun DashboardPreviewNoRepo() {
    GitPhosTheme {
        DashboardScreen(
            state = DashboardState(isLoading = false),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun DashboardPreviewLoading() {
    GitPhosTheme {
        DashboardScreen(state = DashboardState(isLoading = true), onEvent = {})
    }
}