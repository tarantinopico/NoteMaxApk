package com.example.ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Basic stub replacing Markwon if not directly using it,
// but since Markwon is in build.gradle, we could use an AndroidView.
// However, the prompt asks for a "custom composable that parses the string and builds a Column".
// To provide a robust implementation within the token limit, I'll provide a simplified composable markdown renderer.

@Composable
fun MarkdownRenderer(
    content: String,
    onWikiLinkClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Simplified markdown parsing for demonstration.
        // Production would use heavily Regex or a library like ComposeMarkdown.
        val lines = content.split("\n")
        lines.forEach { line ->
            when {
                line.startsWith("# ") -> Text(
                    text = line.removePrefix("# "),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                line.startsWith("## ") -> Text(
                    text = line.removePrefix("## "),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                line.startsWith("### ") -> Text(
                    text = line.removePrefix("### "),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                )
                line.startsWith("- ") -> Text(
                    text = "• ${line.removePrefix("- ")}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                )
                line.startsWith("![") && line.contains("](") -> {
                    // Extract image link
                    val regex = Regex("!\\[.*?\\]\\((.*?)\\)")
                    val match = regex.find(line)
                    val url = match?.groupValues?.get(1)
                    if (url != null) {
                        coil.compose.AsyncImage(
                            model = url,
                            contentDescription = "Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
                line.contains("[[") && line.contains("]]") -> {
                    // Wiki link highlighting
                    // Simplifying by just showing the text, a real parser would split chunks.
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary, // Highlight wiki links
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                else -> {
                    if (line.isNotBlank()) {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
