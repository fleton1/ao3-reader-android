package com.ao3reader.ui.screens.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ao3reader.ui.components.ChapterNavigation
import com.ao3reader.ui.components.ErrorView
import com.ao3reader.ui.components.LoadingIndicator

/**
 * Reader screen for reading chapter content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    workId: String,
    chapterNumber: Int,
    onNavigateBack: () -> Unit,
    onNavigateToChapter: (Int) -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.work?.title ?: "Loading...",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        if (uiState.chapter?.title != null) {
                            Text(
                                text = uiState.chapter!!.title!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.decreaseFontSize() }) {
                        Icon(
                            imageVector = Icons.Default.TextDecrease,
                            contentDescription = "Decrease Font Size"
                        )
                    }
                    IconButton(onClick = { viewModel.increaseFontSize() }) {
                        Icon(
                            imageVector = Icons.Default.TextIncrease,
                            contentDescription = "Increase Font Size"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Loading chapter...")
            }
            uiState.error != null -> {
                ErrorView(
                    message = uiState.error!!,
                    onRetry = { viewModel.retry() }
                )
            }
            uiState.chapter != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    state = scrollState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Chapter navigation top
                    item {
                        ChapterNavigation(
                            currentChapter = uiState.currentChapter,
                            totalChapters = uiState.totalChapters,
                            onPreviousChapter = { viewModel.previousChapter() },
                            onNextChapter = { viewModel.nextChapter() }
                        )
                    }

                    item {
                        HorizontalDivider()
                    }

                    // Chapter title
                    if (uiState.chapter?.title != null) {
                        item {
                            Text(
                                text = uiState.chapter!!.title!!,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Chapter summary
                    if (!uiState.chapter?.summary.isNullOrBlank()) {
                        item {
                            Column {
                                Text(
                                    text = "Summary",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val summaryText = HtmlCompat.fromHtml(
                                    uiState.chapter!!.summary!!,
                                    HtmlCompat.FROM_HTML_MODE_COMPACT
                                ).toString()
                                Text(
                                    text = summaryText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = uiState.fontSize.sp,
                                        lineHeight = (uiState.fontSize * uiState.lineHeight).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Chapter notes
                    if (!uiState.chapter?.notes.isNullOrBlank()) {
                        item {
                            Column {
                                Text(
                                    text = "Notes",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val notesText = HtmlCompat.fromHtml(
                                    uiState.chapter!!.notes!!,
                                    HtmlCompat.FROM_HTML_MODE_COMPACT
                                ).toString()
                                Text(
                                    text = notesText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = uiState.fontSize.sp,
                                        lineHeight = (uiState.fontSize * uiState.lineHeight).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        HorizontalDivider()
                    }

                    // Chapter content
                    item {
                        val contentText = HtmlCompat.fromHtml(
                            uiState.chapter!!.content,
                            HtmlCompat.FROM_HTML_MODE_COMPACT
                        ).toString()

                        Text(
                            text = contentText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = uiState.fontSize.sp,
                                lineHeight = (uiState.fontSize * uiState.lineHeight).sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // End notes
                    if (!uiState.chapter?.endNotes.isNullOrBlank()) {
                        item {
                            HorizontalDivider()
                        }

                        item {
                            Column {
                                Text(
                                    text = "End Notes",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val endNotesText = HtmlCompat.fromHtml(
                                    uiState.chapter!!.endNotes!!,
                                    HtmlCompat.FROM_HTML_MODE_COMPACT
                                ).toString()
                                Text(
                                    text = endNotesText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = uiState.fontSize.sp,
                                        lineHeight = (uiState.fontSize * uiState.lineHeight).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        HorizontalDivider()
                    }

                    // Chapter navigation bottom
                    item {
                        ChapterNavigation(
                            currentChapter = uiState.currentChapter,
                            totalChapters = uiState.totalChapters,
                            onPreviousChapter = { viewModel.previousChapter() },
                            onNextChapter = { viewModel.nextChapter() }
                        )
                    }
                }
            }
        }
    }
}
