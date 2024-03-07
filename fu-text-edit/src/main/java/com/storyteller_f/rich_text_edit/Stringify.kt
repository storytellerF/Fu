package com.storyteller_f.rich_text_edit

import android.text.Spanned

data class StyleData(val type: String, val start: Int, val end: Int, val data: Int)

fun Spanned.richFormatPlain(): String {
    return richFormat().joinToString("\n")
}

private fun Spanned.richFormat(): List<StyleData> {
    return buildList {
        getSpans(allRange(), RichSpan::class.java).forEach {
            val spanRange = getSpanRange(it)
            val data = if (it is MultiValueStyle<*>) {
                (it.value as? Int) ?: 0
            } else {
                0
            }
            add(StyleData(it.type, spanRange.first, spanRange.last, data))
        }
    }
}

fun CharSequence.allRange(): IntRange {
    return 0..length
}