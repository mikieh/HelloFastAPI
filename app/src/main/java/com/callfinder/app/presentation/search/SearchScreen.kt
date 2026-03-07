package com.callfinder.app.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.callfinder.app.domain.model.CallType
import com.callfinder.app.presentation.common.EmptyState
import com.callfinder.app.presentation.common.RecordingCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onNavigateToPlayer: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFilters() }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = uiState.textQuery,
                    onValueChange = { viewModel.updateTextQuery(it) },
                    placeholder = { Text("Search recordings…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.textQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateTextQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Expandable filters
            item {
                AnimatedVisibility(visible = uiState.showFilters) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Phone number
                        OutlinedTextField(
                            value = uiState.phoneNumber,
                            onValueChange = { viewModel.updatePhoneNumber(it) },
                            label = { Text("Phone number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Contact name
                        OutlinedTextField(
                            value = uiState.contactName,
                            onValueChange = { viewModel.updateContactName(it) },
                            label = { Text("Contact name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Min duration
                        OutlinedTextField(
                            value = uiState.minDurationSeconds,
                            onValueChange = { viewModel.updateMinDuration(it) },
                            label = { Text("Min duration (seconds)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Call type selector
                        Text(
                            text = "Call Type",
                            style = MaterialTheme.typography.labelMedium
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val options = listOf(null to "All", CallType.INCOMING to "Incoming", CallType.OUTGOING to "Outgoing")
                            options.forEachIndexed { index, (type, label) ->
                                SegmentedButton(
                                    selected = uiState.callType == type,
                                    onClick = { viewModel.updateCallType(type) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = options.size
                                    )
                                ) {
                                    Text(label)
                                }
                            }
                        }

                        // Keywords
                        if (uiState.availableKeywords.isNotEmpty()) {
                            Text(
                                text = "Keywords",
                                style = MaterialTheme.typography.labelMedium
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                uiState.availableKeywords.forEach { keyword ->
                                    FilterChip(
                                        selected = keyword in uiState.selectedKeywords,
                                        onClick = { viewModel.toggleKeyword(keyword) },
                                        label = { Text(keyword) }
                                    )
                                }
                            }
                        }

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { viewModel.clearFilters() }) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            }

            // Search button
            item {
                androidx.compose.material3.Button(
                    onClick = { viewModel.search() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSearching
                ) {
                    if (uiState.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text(
                        text = if (uiState.isSearching) "Searching…" else "Search",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Results
            if (uiState.hasSearched && !uiState.isSearching) {
                item {
                    Text(
                        text = "${uiState.results.size} results",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (uiState.results.isEmpty()) {
                    item {
                        EmptyState(
                            icon = {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            title = "No results found",
                            subtitle = "Try adjusting your search criteria"
                        )
                    }
                } else {
                    items(uiState.results, key = { it.id }) { recording ->
                        RecordingCard(
                            recording = recording,
                            onClick = { onNavigateToPlayer(recording.id) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
