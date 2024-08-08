package com.storyteller_f.rich_text_edit

import android.content.Context
import android.graphics.Typeface
import android.text.Layout
import android.text.TextPaint
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ParagraphStyle
import android.text.style.QuoteSpan
import android.text.style.RelativeSizeSpan
import android.text.style.ReplacementSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.graphics.TypefaceCompat

/**
 * 所有的style 都应该是独立的，不可以使用多个style 完成一个功能。
 */
interface RichSpan {
    /**
     * 无法共存的style。
     */
    val conflict: List<Class<out RichSpan>>
        get() = emptyList()

    val type: String

    companion object {
        const val PRESET_STYLE_BOLD = "bold"
        const val PRESET_STYLE_ITALIC = "italic"
        const val PRESET_STYLE_BACKGROUND = "background"
        const val PRESET_STYLE_HEADLINE = "headline"
        const val PRESET_STYLE_FOREGROUND = "color"
        const val PRESET_STYLE_UNDERLINE = "underline"
        const val PRESET_STYLE_STRIKETHROUGH = "strikethrough"
        const val PRESET_STYLE_QUOTA = "quota"
        const val PRESET_STYLE_ALIGN = "align"
    }
}

val <T : RichSpan> Class<T>.isCharacterStyle
    get() = interfaces.any {
        it == RichTextStyle::class.java
    }

val <T : RichSpan> Class<T>.isParagraphStyle
    get() = interfaces.any {
        it == RichParagraphStyle::class.java
    }

interface RichTextStyle : RichSpan

interface RichParagraphStyle : RichSpan

class Break(val style: Class<RichSpan>)

interface MultiValueStyle<T> {
    val value: T
}

class BoldStyle : StyleSpan(Typeface.BOLD), RichTextStyle {
    override val type: String
        get() = RichSpan.PRESET_STYLE_BOLD

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return true
    }

    override fun hashCode(): Int {
        return 0
    }
}

class ItalicStyle : StyleSpan(Typeface.ITALIC), RichTextStyle {
    override val type: String
        get() = RichSpan.PRESET_STYLE_ITALIC

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return true
    }

    override fun hashCode(): Int {
        return 0
    }
}

class UnderlineStyle : UnderlineSpan(), RichTextStyle {
    override val type: String
        get() = RichSpan.PRESET_STYLE_UNDERLINE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return true
    }

    override fun hashCode(): Int {
        return 0
    }
}

class StrikethroughStyle : StrikethroughSpan(), RichTextStyle {
    override val type: String
        get() = RichSpan.PRESET_STYLE_STRIKETHROUGH

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return true
    }

    override fun hashCode(): Int {
        return 0
    }
}

class QuotaStyle : QuoteSpan(), RichTextStyle {
    override val type: String
        get() = RichSpan.PRESET_STYLE_QUOTA

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return true
    }

    override fun hashCode(): Int {
        return 0
    }
}

class ColorStyle(override val value: Int) : ForegroundColorSpan(value), RichTextStyle,
    MultiValueStyle<Int> {
    override val type: String
        get() = RichSpan.PRESET_STYLE_FOREGROUND

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ColorStyle

        return value == other.value
    }

    override fun hashCode(): Int {
        return value
    }


}

class BackgroundStyle(override val value: Int) : BackgroundColorSpan(value), RichTextStyle,
    MultiValueStyle<Int> {
    override val type: String
        get() = RichSpan.PRESET_STYLE_BACKGROUND
}

class HeadlineStyle(
    override val value: Int,
    proportion: Float = PRESET_HEADLINE_PROPORTION[value - 1],
    private val context: Context
) :
    ParagraphStyle, RichParagraphStyle,
    RelativeSizeSpan(proportion), MultiValueStyle<Int> {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        updateTypeface(ds)
    }

    override fun updateMeasureState(ds: TextPaint) {
        super.updateMeasureState(ds)
        updateTypeface(ds)
    }

    /**
     * FontStyle.FONT_WEIGHT_BOLD 需要至少api 29
     */
    private fun updateTypeface(ds: TextPaint) {
        val typeface = ds.typeface
        val create = TypefaceCompat.create(context, typeface, 700, typeface.isItalic)
        ds.typeface = create
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeadlineStyle

        return value == other.value
    }

    override fun hashCode() = value

    override val conflict: List<Class<out RichSpan>>
        get() = listOf(BoldStyle::class.java)

    override val type: String
        get() = RichSpan.PRESET_STYLE_HEADLINE

    companion object {
        val PRESET_HEADLINE_PROPORTION = listOf(3f, 2.5f, 2f, 1.5f, 1.2f)
    }
}

class AlignmentStyle(align: Layout.Alignment) : AlignmentSpan.Standard(align),
    RichParagraphStyle,
    MultiValueStyle<Int> {
    override val value: Int
        get() = when (alignment) {
            Layout.Alignment.ALIGN_CENTER -> OUTPUT_ALIGN_CENTER
            Layout.Alignment.ALIGN_OPPOSITE -> OUTPUT_ALIGN_RIGHT
            else -> OUTPUT_ALIGN_LEFT
        }

    constructor(v: Int) : this(
        when (v) {
            OUTPUT_ALIGN_CENTER -> Layout.Alignment.ALIGN_CENTER
            OUTPUT_ALIGN_RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_NORMAL
        }
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AlignmentStyle

        return alignment == other.alignment
    }

    override fun hashCode(): Int {
        return alignment.hashCode()
    }

    override val type: String
        get() = RichSpan.PRESET_STYLE_ALIGN

    companion object {
        const val OUTPUT_ALIGN_LEFT = -1
        const val OUTPUT_ALIGN_CENTER = 0
        const val OUTPUT_ALIGN_RIGHT = 1
    }
}

data class Paragraph(val start: Int, val end: Int) {
    val range get() = start..end
}

abstract class ViewStyle : ReplacementSpan(), RichParagraphStyle {
    abstract fun getView(): View
}