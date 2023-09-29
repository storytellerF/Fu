package com.storyteller_f.rich_text_edit

import android.graphics.Typeface
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


interface RichSpan

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

class HeadlineStyle(val head: Int) : ParagraphStyle, RichParagraphStyle,
    RelativeSizeSpan((6 - head).toFloat()), MultiValueStyle<Int> {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isFakeBoldText = true
    }

    override fun updateMeasureState(ds: TextPaint) {
        super.updateMeasureState(ds)
        ds.isFakeBoldText = true
    }

    override val value: Int
        get() = head

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeadlineStyle

        return head == other.head
    }

    override fun hashCode(): Int {
        return head
    }
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
