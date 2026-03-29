package com.example.gitphos.ui.repo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gitphos.data.local.db.entity.RepoMetadataEntity
import com.example.gitphos.ui.theme.GitPhosTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoScreen(
    state: RepoState,
    onEvent: (RepoEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Repositories") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(RepoEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(RepoEvent.ShowAddDialog) }) {
                Icon(Icons.Default.Add, contentDescription = "Add repository")
            }
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.repos.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No repositories yet. Tap + to add one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.repos, key = { it.id }) { repo ->
                    RepoCard(
                        repo = repo,
                        isActive = repo.id == state.activeRepoId,
                        onSetActive = { onEvent(RepoEvent.SetActive(repo)) },
                        onDelete = { onEvent(RepoEvent.DeleteRepo(repo)) }
                    )
                }
            }
        }

        if (state.showAddDialog) {
            AddRepoDialog(state = state, onEvent = onEvent)
        }
    }
}

@Composable
private fun RepoCard(
    repo: RepoMetadataEntity,
    isActive: Boolean,
    onSetActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isActive) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(repo.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Row {
                    if (!isActive) {
                        TextButton(onClick = onSetActive) { Text("Set Active") }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Text(repo.remoteUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text("Branch: ${repo.branch}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Path: ${repo.localPath}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRepoDialog(
    state: RepoState,
    onEvent: (RepoEvent) -> Unit
) {
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val path = it.path ?: ""
            val absolutePath = if (path.startsWith("/tree/primary:")) {
                "/storage/emulated/0/" + path.substringAfter("/tree/primary:")
            } else {
                it.toString()
            }
            onEvent(RepoEvent.DialogLocalPathChanged(absolutePath))
        }
    }

    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onEvent(RepoEvent.DismissDialog) },
        title = { Text("Add Repository") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = state.dialogRemoteUrl,
                        onValueChange = { onEvent(RepoEvent.DialogRemoteUrlChanged(it)) },
                        label = { Text("Remote URL") },
                        placeholder = { Text("https://github.com/user/repo") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        when {
                            state.isFetchingRepos -> {
                                DropdownMenuItem(
                                    text = { Text("Fetching repositories...") },
                                    onClick = {}
                                )
                            }
                            state.availableRemoteRepos.isEmpty() -> {
                                DropdownMenuItem(
                                    text = { Text("No repositories found") },
                                    onClick = { expanded = false }
                                )
                            }
                            else -> {
                                state.availableRemoteRepos.forEach { repo ->
                                    DropdownMenuItem(
                                        text = { Text(repo.name) },
                                        onClick = {
                                            onEvent(RepoEvent.RemoteRepoSelected(repo))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = state.dialogName,
                    onValueChange = { onEvent(RepoEvent.DialogNameChanged(it)) },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.dialogLocalPath,
                        onValueChange = { onEvent(RepoEvent.DialogLocalPathChanged(it)) },
                        label = { Text("Local Path") },
                        placeholder = { Text("/storage/emulated/0/MyRepo") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { folderPickerLauncher.launch(null) }) {
                        Icon(Icons.Default.Folder, contentDescription = "Pick Folder")
                    }
                }

                OutlinedTextField(
                    value = state.dialogBranch,
                    onValueChange = { onEvent(RepoEvent.DialogBranchChanged(it)) },
                    label = { Text("Branch") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.dialogError != null) {
                    Text(
                        state.dialogError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(RepoEvent.ConfirmAdd) },
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(RepoEvent.DismissDialog) }) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun RepoPreviewEmpty() {
    GitPhosTheme { RepoScreen(state = RepoState(isLoading = false), onEvent = {}) }
}

@Preview(showBackground = true, name = "With repos")
@Composable
private fun RepoPreviewList() {
    GitPhosTheme {
        RepoScreen(
            state = RepoState(
                isLoading = false,
                activeRepoId = 1L,
                repos = listOf(
                    RepoMetadataEntity(id = 1, name = "my-photos", localPath = "/storage/emulated/0/repo", remoteUrl = "https://github.com/user/my-photos", isActive = true),
                    RepoMetadataEntity(id = 2, name = "backup", localPath = "/storage/emulated/0/backup", remoteUrl = "https://github.com/user/backup")
                )
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, name = "Add dialog")
@Composable
private fun RepoPreviewDialog() {
    GitPhosTheme {
        RepoScreen(
            state = RepoState(isLoading = false, showAddDialog = true, dialogRemoteUrl = "https://github.com/"),
            onEvent = {}
        )
    }
}