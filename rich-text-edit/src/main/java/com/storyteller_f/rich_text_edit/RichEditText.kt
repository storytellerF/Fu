package com.storyteller_f.rich_text_edit

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Layout.Alignment
import android.text.SpanWatcher
import android.text.Spannable
import android.text.TextPaint
import android.text.TextWatcher
import android.text.style.AlignmentSpan
import android.text.style.ParagraphStyle
import android.text.style.QuoteSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.view.Gravity
import androidx.core.text.toSpannable


class RichEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatEditText(context, attrs) {
    init {
        setEditableFactory(object : Editable.Factory() {
            override fun newEditable(source: CharSequence?): Editable {
                return super.newEditable(source).apply {
                    setSpan(object : SpanWatcher {
                        override fun onSpanAdded(
                            text: Spannable?,
                            what: Any?,
                            start: Int,
                            end: Int
                        ) {
                        }

                        override fun onSpanRemoved(
                            text: Spannable?,
                            what: Any?,
                            start: Int,
                            end: Int
                        ) {
                        }

                        override fun onSpanChanged(
                            text: Spannable?,
                            what: Any?,
                            ostart: Int,
                            oend: Int,
                            nstart: Int,
                            nend: Int
                        ) {
                        }

                    }, 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
            }
        })
        gravity = Gravity.TOP
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                println("${s?.javaClass} $start $before $count")
                s?.toSpannable()?.let {
                    if (tempRemoveSpan) {
                        tempRemoveSpan = false
                        return
                    }
                    if (count > before) {
                        val spans = it.getSpans(start, start, RichEditTextSpan::class.java)
                        spans.filter { span ->
                            it.getSpanEnd(span) == start
                        }.forEach { span ->
                            val st = it.getSpanStart(span)
                            val end = it.getSpanEnd(span)
                            editableText.removeSpan(span)
                            editableText.setSpan(span, st, end + count, 0)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    var tempRemoveSpan = false

    fun <T : RichEditTextSpan> toggle(
        span: Class<T>,
        factory: () -> T = { span.newInstance() },
    ) {
        if (ParagraphStyle::class.java.isAssignableFrom(span)) {
            val selection = selectionStart
            var paragraphStart = selection
            val text = text.toString()
            while (paragraphStart > 0 && text[paragraphStart - 1] != '\n') {
                paragraphStart--
            }

            var paragraphEnd = selection
            while (paragraphEnd < text.length && text[paragraphEnd] != '\n') {
                paragraphEnd++
            }
            toggle(span, paragraphStart, paragraphEnd, factory)
        } else if (selectionStart != selectionEnd) {
            toggle(span, selectionStart, selectionEnd, factory)
        } else {
            val selection = selectionStart
            val spans = editableText.getSpans(selection, selection, span)
            if (spans.isEmpty()) {
                editableText.setSpan(span.newInstance(), selection, selection, 0)
            } else {
                tempRemoveSpan = true
            }
        }

    }

    private fun <T : RichEditTextSpan> toggle(
        span: Class<T>,
        start: Int,
        end: Int,
        factory: () -> T,
    ) {
        val spans = editableText.getSpans(start, end, span)
        val instance = factory()
        if (spans.isEmpty()) {
            editableText.setSpan(instance, start, end, 0)
        } else {
            val all = spans.all {
                it != instance
            }
            spans.forEach {
                editableText.removeSpan(it)
            }
            if (all) {
                editableText.setSpan(instance, start, end, 0)
            }
        }
    }

}

interface RichEditTextSpan

class BoldStyle : StyleSpan(Typeface.BOLD), RichEditTextSpan

class ItalicStyle : StyleSpan(Typeface.ITALIC), RichEditTextSpan

class UnderlineStyle : UnderlineSpan(), RichEditTextSpan

class StrikethroughStyle : StrikethroughSpan(), RichEditTextSpan

class QuotaStyle : QuoteSpan(), RichEditTextSpan

class HeadlineStyle(val head: Int) : ParagraphStyle, RichEditTextSpan,
    RelativeSizeSpan((5 - head).toFloat()) {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isFakeBoldText = true
    }

    override fun updateMeasureState(ds: TextPaint) {
        super.updateMeasureState(ds)
        ds.isFakeBoldText = true
    }

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

class AlignmentStyle(val align: Alignment) : AlignmentSpan.Standard(align), RichEditTextSpan {
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