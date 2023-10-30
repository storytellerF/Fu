package com.storyteller_f.rich_text_edit

import android.content.Context
import android.graphics.Typeface
import android.graphics.fonts.FontStyle
import android.text.Layout
import android.text.TextPaint
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ParagraphStyle
import android.text.style.QuoteSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
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
}

interface RichTextStyle : RichSpan

interface RichParagraphStyle : RichSpan

class Break(val style: Class<RichSpan>)

interface MultiValueStyle<T> {
    val value: T
}

class BoldStyle : StyleSpan(Typeface.BOLD), RichTextStyle

class ItalicStyle : StyleSpan(Typeface.ITALIC), RichTextStyle

class UnderlineStyle : UnderlineSpan(), RichTextStyle

class StrikethroughStyle : StrikethroughSpan(), RichTextStyle

class QuotaStyle : QuoteSpan(), RichTextStyle

class ColorStyle(override val value: Int) : ForegroundColorSpan(value), RichTextStyle,
    MultiValueStyle<Int>

class BackgroundStyle(override val value: Int) : BackgroundColorSpan(value), RichTextStyle,
    MultiValueStyle<Int>

class HeadlineStyle(override val value: Int, proportion: Float, private val context: Context) :
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
}

class AlignmentStyle(val align: Layout.Alignment) : AlignmentSpan.Standard(align),
    RichParagraphStyle,
    MultiValueStyle<Layout.Alignment> {
    override val value: Layout.Alignment
        get() = align

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AlignmentStyle

        return align == other.align
    }

    override fun hashCode(): Int {
        return align.hashCode()
    }
}

class Paragraph(val start: Int, val end: Int)
