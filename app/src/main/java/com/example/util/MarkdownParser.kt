package com.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em

object MarkdownParser {
    private val WIKI_LINK_REGEX = Regex("\\[\\[(.*?)\\]\\]")
    private val LINK_REGEX = Regex("\\[(.*?)\\]\\((.*?)\\)")
    private val BOLD_REGEX = Regex("\\*\\*(.*?)\\*\\*")
    private val ITALIC_REGEX = Regex("\\*(.*?)\\*")
    private val HEADING_REGEX = Regex("^(#{1,6})\\s+(.*)$", RegexOption.MULTILINE)
    private val CODE_REGEX = Regex("`(.*?)`")

    fun parse(text: String, primaryColor: Color): AnnotatedString {
        return buildAnnotatedString {
            append(text)
            
            // Highlight Headings
            HEADING_REGEX.findAll(text).forEach { match ->
                val level = match.groupValues[1].length
                val scale = 1.0f + (0.1f * (6 - level))
                addStyle(
                    style = SpanStyle(fontSize = scale.em, fontWeight = FontWeight.Bold),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }

            // Bold
            BOLD_REGEX.findAll(text).forEach { match ->
                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }

            // Italic
            ITALIC_REGEX.findAll(text).forEach { match ->
                addStyle(
                    style = SpanStyle(fontStyle = FontStyle.Italic),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }

            // Code
            CODE_REGEX.findAll(text).forEach { match ->
                addStyle(
                    style = SpanStyle(fontFamily = FontFamily.Monospace, background = Color.Gray.copy(alpha = 0.2f)),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }

            // Links
            LINK_REGEX.findAll(text).forEach { match ->
                addStyle(
                    style = SpanStyle(color = primaryColor, textDecoration = TextDecoration.Underline),
                    start = match.range.first,
                    end = match.range.last + 1
                )
                // Add annotation for click handling
                val url = match.groupValues[2]
                addStringAnnotation(tag = "URL", annotation = url, start = match.range.first, end = match.range.last + 1)
            }

            // Wiki Links
            WIKI_LINK_REGEX.findAll(text).forEach { match ->
                addStyle(
                    style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Medium),
                    start = match.range.first,
                    end = match.range.last + 1
                )
                val wikiTitle = match.groupValues[1]
                addStringAnnotation(tag = "WIKI", annotation = wikiTitle, start = match.range.first, end = match.range.last + 1)
            }
        }
    }
}
