package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun CustomUrlDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (url: String, title: String, artist: String) -> Unit
) {
    if (!isOpen) return

    var urlText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }
    var artistText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(StudioSurface)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Import Stream / YouTube URL",
                color = StudioTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Enter any direct audio stream (FLAC, WAV, AAC, OGG) or YouTube Music URL to route through the 24-bit / 32-bit spatial engine.",
                color = StudioTextSecondary,
                fontSize = 12.sp
            )

            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                label = { Text("Audio Stream or YouTube URL") },
                placeholder = { Text("https://...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stream_url_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = StudioCardBorder,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary
                )
            )

            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("Track Title (Optional)") },
                placeholder = { Text("Hi-Res Master Track") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TubeAmber,
                    unfocusedBorderColor = StudioCardBorder,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary
                )
            )

            OutlinedTextField(
                value = artistText,
                onValueChange = { artistText = it },
                label = { Text("Artist / Channel (Optional)") },
                placeholder = { Text("Studio Artist") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TubeAmber,
                    unfocusedBorderColor = StudioCardBorder,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = StudioTextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (urlText.isNotBlank()) {
                            onConfirm(urlText, titleText, artistText)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    enabled = urlText.isNotBlank(),
                    modifier = Modifier.testTag("confirm_import_button")
                ) {
                    Text("Import & Play", color = StudioBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
