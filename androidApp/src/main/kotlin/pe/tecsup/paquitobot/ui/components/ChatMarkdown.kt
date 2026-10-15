package pe.tecsup.paquitobot.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/** Subconjunto de Markdown habitual en respuestas del asistente, sin dependencias HTML. */
internal fun formatChatMarkdown(markdown: String): AnnotatedString = buildAnnotatedString {
    var inCodeBlock = false
    var hasRenderedLine = false
    markdown.replace("\r\n", "\n").lines().forEach { rawLine ->
        val line = rawLine.trimEnd()
        if (line.trimStart().startsWith("```")) {
            inCodeBlock = !inCodeBlock
            return@forEach
        }
        if (hasRenderedLine) append('\n')
        hasRenderedLine = true
        if (inCodeBlock) {
            pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0xFFE8E8E8)))
            append(line)
            pop()
            return@forEach
        }

        val trimmed = line.trimStart()
        val heading = Regex("^(#{1,3})\\s+(.+)$").matchEntire(trimmed)
        val bullet = Regex("^[-*+]\\s+(.+)$").matchEntire(trimmed)
        val numbered = Regex("^(\\d+)[.)]\\s+(.+)$").matchEntire(trimmed)
        when {
            heading != null -> {
                val level = heading.groupValues[1].length
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = (19 - level).sp))
                appendInlineMarkdown(heading.groupValues[2])
                pop()
            }
            bullet != null -> {
                append("•  ")
                appendInlineMarkdown(bullet.groupValues[1])
            }
            numbered != null -> {
                append("${numbered.groupValues[1]}.  ")
                appendInlineMarkdown(numbered.groupValues[2])
            }
            trimmed.startsWith("> ") -> {
                pushStyle(SpanStyle(color = Color.DarkGray, fontStyle = FontStyle.Italic))
                append("│  ")
                appendInlineMarkdown(trimmed.removePrefix("> "))
                pop()
            }
            else -> appendInlineMarkdown(line)
        }
    }
}

private val inlineMarkers = listOf("***", "**", "__", "~~", "*", "_", "`")

private fun AnnotatedString.Builder.appendInlineMarkdown(value: String) {
    var index = 0
    while (index < value.length) {
        if (value[index] == '\\' && index + 1 < value.length && value[index + 1] in "*_`~\\") {
            append(value[index + 1])
            index += 2
            continue
        }
        val marker = inlineMarkers.firstOrNull { value.startsWith(it, index) &&
            value.indexOf(it, index + it.length) > index + it.length }
        if (marker == null) {
            append(value[index])
            index++
            continue
        }
        val closing = value.indexOf(marker, index + marker.length)
        val inner = value.substring(index + marker.length, closing)
        pushStyle(when (marker) {
            "***" -> SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
            "**", "__" -> SpanStyle(fontWeight = FontWeight.Bold)
            "*", "_" -> SpanStyle(fontStyle = FontStyle.Italic)
            "~~" -> SpanStyle(textDecoration = TextDecoration.LineThrough)
            else -> SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0xFFE8E8E8))
        })
        if (marker == "`") append(inner) else appendInlineMarkdown(inner)
        pop()
        index = closing + marker.length
    }
}
