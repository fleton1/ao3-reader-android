package com.ao3reader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ao3reader.domain.models.Work
import com.ao3reader.ui.theme.TagCharacter
import com.ao3reader.ui.theme.TagRelationship
import java.text.NumberFormat

/**
 * Card component displaying work summary information.
 * Used in search results, bookmarks, and other work lists.
 */
@Composable
fun WorkCard(
    work: Work,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
    progress: Float = 0f
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and author
            Text(
                text = work.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "by ${work.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fandoms
            if (work.fandoms.isNotEmpty()) {
                Text(
                    text = work.fandoms.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Summary
            if (work.summary.isNotBlank()) {
                Text(
                    text = work.summary.replace(Regex("<[^>]*>"), ""), // Strip HTML
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tags (relationships and characters)
            if (work.relationships.isNotEmpty() || work.characters.isNotEmpty()) {
                TagRow(
                    relationships = work.relationships.take(2),
                    characters = work.characters.take(2)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating
                Text(
                    text = work.rating.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Chapters
                Text(
                    text = "${work.currentChapters}/${work.totalChapters}",
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Words
                Text(
                    text = "${NumberFormat.getInstance().format(work.words)} words",
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.weight(1f))

                // Status indicators
                if (work.isBookmarked) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Bookmarked",
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (work.isDownloaded) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Downloaded",
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                // Kudos
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Kudos",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = NumberFormat.getInstance().format(work.kudos),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Progress indicator if applicable
            if (showProgress && progress > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TagRow(
    relationships: List<String>,
    characters: List<String>
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        relationships.forEach { relationship ->
            TagChip(
                text = relationship,
                color = TagRelationship,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        characters.forEach { character ->
            TagChip(
                text = character,
                color = TagCharacter,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}
