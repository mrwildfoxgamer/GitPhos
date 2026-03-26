package com.example.gitphos.ui.picker

import android.net.Uri

data class PickerState(
    val selectedUris: List<Uri> = emptyList(),
    val isAdding: Boolean = false,
    val noActiveRepo: Boolean = false
)

sealed interface PickerEvent {
    data class ImagesSelected(val uris: List<Uri>) : PickerEvent
    data class RemoveImage(val uri: Uri) : PickerEvent
    data object ConfirmAdd : PickerEvent
    data object NavigateBack : PickerEvent
}

sealed interface PickerEffect {
    data object NavigateBack : PickerEffect
    data class ShowMessage(val message: String) : PickerEffect
}