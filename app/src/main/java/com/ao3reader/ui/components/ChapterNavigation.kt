package com.ao3reader.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Navigation buttons for moving between chapters.
 */
@Composable
fun ChapterNavigation(
    currentChapter: Int,
    totalChapters: Int,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        TextButton(
            onClick = onPreviousChapter,
            enabled = currentChapter > 1
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Chapter"
            )
            Text(
                text = "Previous",
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Chapter indicator
        Text(
            text = "Chapter $currentChapter of $totalChapters",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Next button
        TextButton(
            onClick = onNextChapter,
            enabled = currentChapter < totalChapters
        ) {
            Text(
                text = "Next",
                modifier = Modifier.padding(end = 4.dp)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Chapter"
            )
        }
    }
}
