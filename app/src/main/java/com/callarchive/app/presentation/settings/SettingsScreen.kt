package com.callarchive.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.callarchive.app.domain.model.SyncFrequency
import com.callarchive.app.presentation.common.formatDate
import com.callarchive.app.presentation.theme.UploadedColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google Account section
            SectionHeader("Google Account")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.isSignedIn) {
                        Text(
                            text = uiState.googleAccountEmail ?: "Signed in",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.signOut() }) {
                            Text("Sign Out")
                        }
                    } else {
                        Text(
                            text = "Sign in to upload recordings to Google Drive",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { /* Launch Google Sign-In intent */ }) {
                            Text("Sign In with Google")
                        }
                    }
                }
            }

            HorizontalDivider()

            // Sync Schedule section
            SectionHeader("Sync Schedule")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Last sync info
                    if (uiState.lastSyncTime != null) {
                        Text(
                            text = "Last sync: ${formatDate(uiState.lastSyncTime!!)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Frequency dropdown
                    var frequencyExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = frequencyExpanded,
                        onExpandedChange = { frequencyExpanded = !frequencyExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.syncFrequency.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Frequency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = frequencyExpanded,
                            onDismissRequest = { frequencyExpanded = false }
                        ) {
                            SyncFrequency.entries.forEach { frequency ->
                                DropdownMenuItem(
                                    text = {
                                        Text(frequency.name.lowercase()
                                            .replaceFirstChar { it.uppercase() })
                                    },
                                    onClick = {
                                        viewModel.updateSyncFrequency(frequency)
                                        frequencyExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Time display
                    Text(
                        text = "Sync time: %02d:%02d".format(uiState.syncHour, uiState.syncMinute),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider()

            // Scan folder
            SectionHeader("Scan Folder")
            OutlinedTextField(
                value = uiState.scanFolderPath,
                onValueChange = { viewModel.updateScanFolderPath(it) },
                label = { Text("Scan folder path") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider()

            // Gemini API Key section
            SectionHeader("Gemini API Key")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = uiState.geminiApiKey,
                        onValueChange = { viewModel.updateGeminiApiKey(it) },
                        label = { Text("API Key") },
                        placeholder = { Text("Enter your Gemini API key") },
                        visualTransformation = if (uiState.isApiKeyVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.toggleApiKeyVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.isApiKeyVisible) {
                                            Icons.Default.VisibilityOff
                                        } else {
                                            Icons.Default.Visibility
                                        },
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveAndValidateApiKey() },
                            enabled = !uiState.isValidatingApiKey && uiState.geminiApiKey.isNotBlank()
                        ) {
                            if (uiState.isValidatingApiKey) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Save & Validate")
                        }

                        // Validation result
                        when (uiState.apiKeyValidationResult) {
                            true -> {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Valid",
                                    tint = UploadedColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Valid",
                                    color = UploadedColor,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            false -> {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Invalid",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Invalid key",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            null -> {}
                        }
                    }
                }
            }

            HorizontalDivider()

            // Transcription Options
            SectionHeader("Transcription Options")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Min duration slider
                    Text(
                        text = "Minimum call duration: ${uiState.minTranscriptionDuration}s",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = uiState.minTranscriptionDuration.toFloat(),
                        onValueChange = {
                            viewModel.updateMinTranscriptionDuration(it.toInt())
                        },
                        valueRange = 0f..300f,
                        steps = 29
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wi-Fi only toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Wi-Fi only",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Only transcribe when connected to Wi-Fi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.wifiOnly,
                            onCheckedChange = { viewModel.updateWifiOnly(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Language preference
                    var languageExpanded by remember { mutableStateOf(false) }
                    val languages = listOf("auto" to "Auto-detect", "he" to "Hebrew", "en" to "English")

                    ExposedDropdownMenuBox(
                        expanded = languageExpanded,
                        onExpandedChange = { languageExpanded = !languageExpanded }
                    ) {
                        OutlinedTextField(
                            value = languages.find { it.first == uiState.language }?.second ?: "Auto-detect",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Language") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = languageExpanded,
                            onDismissRequest = { languageExpanded = false }
                        ) {
                            languages.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        viewModel.updateLanguage(code)
                                        languageExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}
