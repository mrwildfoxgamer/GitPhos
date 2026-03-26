package com.example.gitphos.ui.picker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.gitphos.ui.theme.GitPhosTheme
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickerScreen(
    state: PickerState,
    onEvent: (PickerEvent) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) onEvent(PickerEvent.ImagesSelected(uris))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Images") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(PickerEvent.NavigateBack) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Images")
                }
                Button(
                    onClick = { onEvent(PickerEvent.ConfirmAdd) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.selectedUris.isNotEmpty() && !state.isAdding
                ) {
                    if (state.isAdding) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (state.selectedUris.isEmpty()) "Add to Queue"
                        else "Add ${state.selectedUris.size} to Queue"
                    )
                }
            }
        }
    ) { padding ->
        when {
            state.noActiveRepo -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No active repository. Set one up first.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            state.selectedUris.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No images selected",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.selectedUris, key = { it.toString() }) { uri ->
                    ImageThumbnail(uri = uri, onRemove = { onEvent(PickerEvent.RemoveImage(uri)) })
                }
            }
        }
    }
}

@Composable
private fun ImageThumbnail(uri: Uri, onRemove: () -> Unit) {
    Box(modifier = Modifier.aspectRatio(1f)) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true, name = "Empty selection")
@Composable
private fun PickerPreviewEmpty() {
    GitPhosTheme {
        PickerScreen(state = PickerState(), onEvent = {})
    }
}

@Preview(showBackground = true, name = "No active repo")
@Composable
private fun PickerPreviewNoRepo() {
    GitPhosTheme {
        PickerScreen(state = PickerState(noActiveRepo = true), onEvent = {})
    }
}

@Preview(showBackground = true, name = "Adding")
@Composable
private fun PickerPreviewAdding() {
    GitPhosTheme {
        PickerScreen(state = PickerState(isAdding = true), onEvent = {})
    }
}