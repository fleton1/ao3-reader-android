package com.ao3reader.ui.screens.workdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ao3reader.domain.models.Work
import com.ao3reader.ui.components.ErrorView
import com.ao3reader.ui.components.LoadingIndicator
import com.ao3reader.ui.components.TagChip
import com.ao3reader.ui.theme.TagCharacter
import com.ao3reader.ui.theme.TagFreeform
import com.ao3reader.ui.theme.TagRelationship
import com.ao3reader.ui.theme.TagWarning
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Work detail screen showing full work metadata and action buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkDetailScreen(
    workId: String,
    onNavigateToReader: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: WorkDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Loading work...")
            }
            uiState.error != null -> {
                ErrorView(
                    message = uiState.error!!,
                    onRetry = { viewModel.refresh() }
                )
            }
            uiState.work != null -> {
                WorkDetailContent(
                    work = uiState.work!!,
                    isBookmarked = uiState.isBookmarked,
                    isDownloaded = uiState.isDownloaded,
                    isFollowing = uiState.isFollowing,
                    isDownloading = uiState.isDownloading,
                    onRead = { onNavigateToReader(1) },
                    onToggleBookmark = { viewModel.toggleBookmark() },
                    onDownload = { viewModel.downloadWork() },
                    onToggleFollow = { viewModel.toggleFollow() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun WorkDetailContent(
    work: Work,
    isBookmarked: Boolean,
    isDownloaded: Boolean,
    isFollowing: Boolean,
    isDownloading: Boolean,
    onRead: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDownload: () -> Unit,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = work.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Author
        item {
            Text(
                text = "by ${work.author}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Action buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRead,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Read")
                }

                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove Bookmark" else "Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isDownloaded) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Downloaded",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else if (isDownloading) {
                    CircularProgressIndicator()
                } else {
                    IconButton(onClick = onDownload) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download"
                        )
                    }
                }

                IconButton(onClick = onToggleFollow) {
                    Icon(
                        imageVector = if (isFollowing) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFollowing) "Unfollow" else "Follow",
                        tint = if (isFollowing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Stats
        item {
            WorkStats(work = work)
        }

        // Rating and warnings
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TagChip(text = work.rating.displayName)
                    work.categories.forEach { category ->
                        TagChip(text = category)
                    }
                }
                if (work.warnings.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(work.warnings) { warning ->
                            TagChip(text = warning, color = TagWarning)
                        }
                    }
                }
            }
        }

        // Fandoms
        if (work.fandoms.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Fandoms",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    work.fandoms.forEach { fandom ->
                        Text(
                            text = fandom,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Relationships
        if (work.relationships.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Relationships",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(work.relationships) { relationship ->
                            TagChip(text = relationship, color = TagRelationship)
                        }
                    }
                }
            }
        }

        // Characters
        if (work.characters.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Characters",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(work.characters) { character ->
                            TagChip(text = character, color = TagCharacter)
                        }
                    }
                }
            }
        }

        // Additional tags
        if (work.additionalTags.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Additional Tags",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(work.additionalTags) { tag ->
                            TagChip(text = tag, color = TagFreeform)
                        }
                    }
                }
            }
        }

        // Summary
        if (work.summary.isNotBlank()) {
            item {
                Column {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val summaryText = HtmlCompat.fromHtml(
                        work.summary,
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    ).toString()
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Series info
        if (work.isSeries && work.seriesName != null) {
            item {
                Column {
                    Text(
                        text = "Part of Series",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${work.seriesName} (Part ${work.seriesPart})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkStats(work: Work) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Chapters:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${work.currentChapters}/${work.totalChapters}${if (work.isComplete) " (Complete)" else ""}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Words:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = NumberFormat.getInstance().format(work.words),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Language:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = work.language,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Kudos:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = NumberFormat.getInstance().format(work.kudos),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Bookmarks:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = NumberFormat.getInstance().format(work.bookmarksCount),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hits:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = NumberFormat.getInstance().format(work.hits),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Published:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dateFormat.format(Date(work.publishedDate)),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Updated:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dateFormat.format(Date(work.updatedDate)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
