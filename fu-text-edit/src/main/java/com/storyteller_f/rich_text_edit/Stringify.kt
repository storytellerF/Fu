package com.storyteller_f.rich_text_edit

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter

@JsonClass(generateAdapter = true)
data class StyleData(val type: String, val start: Int, val end: Int, val data: Int)

fun Spanned.richFormatPlain(): String {
    val adapter = jsonAdapter()
    return adapter.toJson(richFormat())
}

@OptIn(ExperimentalStdlibApi::class)
private fun jsonAdapter() = Moshi.Builder().build().adapter<List<StyleData>>()

fun Context.parseRichFormatPlain(context: String, richFormat: String): SpannableString {
    val adapter = jsonAdapter()
    val data = adapter.fromJson(richFormat)
    val c = this
    return SpannableString(context).apply {
        data?.forEach {
            val value = it.data
            val style = when (val type = it.type) {
                RichSpan.PRESET_STYLE_BOLD -> BoldStyle()
                RichSpan.PRESET_STYLE_ALIGN -> AlignmentStyle(value)
                RichSpan.PRESET_STYLE_HEADLINE -> HeadlineStyle(value, context = c)
                RichSpan.PRESET_STYLE_QUOTA -> QuotaStyle()
                RichSpan.PRESET_STYLE_BACKGROUND -> BackgroundStyle(value)
                RichSpan.PRESET_STYLE_FOREGROUND -> ColorStyle(value)
                RichSpan.PRESET_STYLE_ITALIC -> ItalicStyle()
                RichSpan.PRESET_STYLE_UNDERLINE -> UnderlineStyle()
                RichSpan.PRESET_STYLE_STRIKETHROUGH -> StrikethroughStyle()
                else -> throw Exception("unrecognized type $type")
            }
            setSpan(style, it.start, it.end, 0)
        }
    }
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