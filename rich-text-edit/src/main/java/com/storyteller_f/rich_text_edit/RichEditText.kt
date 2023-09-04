package com.storyteller_f.rich_text_edit

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
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
import android.util.Log
import android.view.Gravity
import androidx.core.text.toSpannable
import androidx.lifecycle.MutableLiveData


class RichEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatEditText(context, attrs) {

    val cursorStyle = MutableLiveData(false)

    inner class DetectCursorStyle : Runnable {
        override fun run() {
            val spans =
                editableText.getSpans(selectionStart, selectionEnd, UnderlineStyle::class.java)
            if (spans.size == 1) {
                val underlineStyle = spans.first()
                val spanStart = editableText.getSpanStart(underlineStyle)
                val spanEnd = editableText.getSpanEnd(
                    underlineStyle
                )
                cursorStyle.value = spanStart == selectionStart && spanEnd == selectionEnd
            } else cursorStyle.value = false
        }

    }

    private val richEditHandler = Handler(Looper.getMainLooper())

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
                            Log.d(
                                TAG,
                                "onSpanAdded() called with: text = $text, what = $what, start = $start, end = $end"
                            )
                        }

                        override fun onSpanRemoved(
                            text: Spannable?,
                            what: Any?,
                            start: Int,
                            end: Int
                        ) {
                            Log.d(
                                TAG,
                                "onSpanRemoved() called with: text = $text, what = $what, start = $start, end = $end"
                            )
                        }

                        override fun onSpanChanged(
                            text: Spannable?,
                            what: Any?,
                            ostart: Int,
                            oend: Int,
                            nstart: Int,
                            nend: Int
                        ) {
                            Log.d(
                                TAG,
                                "onSpanChanged() called with: text = $text, what = $what, ostart = $ostart, oend = $oend, nstart = $nstart, nend = $nend"
                            )
                            detectStyle()
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

    private fun detectStyle() {
        richEditHandler.removeCallbacksAndMessages(null)
        richEditHandler.postDelayed(DetectCursorStyle(), 500)
    }

    var tempRemoveSpan = false

    class Paragraph(val start: Int, val end: Int)

    fun <T : RichEditTextSpan> toggle(
        span: Class<T>,
        factory: () -> T = { span.newInstance() },
    ) {
        if (ParagraphStyle::class.java.isAssignableFrom(span)) {
            //todo 可能选中了多个段落
            val paragraph = currentParagraph(selectionStart)
            toggle(span, paragraph.start, paragraph.end, factory)
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
        detectStyle()
    }

    private fun currentParagraph(selection: Int): Paragraph {
        var paragraphStart = selection
        val text = text.toString()
        while (paragraphStart > 0 && text[paragraphStart - 1] != '\n') {
            paragraphStart--
        }

        var paragraphEnd = selection
        while (paragraphEnd < text.length && text[paragraphEnd] != '\n') {
            paragraphEnd++
        }
        return Paragraph(paragraphStart, paragraphEnd)
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
            //同类型，但是不想等。比如不同级别的标题
            val filterIsInstance = spans.filterIsInstance<MultiValueStyle>()
            val sameStyleButNotEqual = when {
                filterIsInstance.isNotEmpty() -> filterIsInstance.all {
                    it != instance
                }
                else -> false
            }
            spans.forEach {
                editableText.removeSpan(it)
            }
            if (sameStyleButNotEqual) {
                editableText.setSpan(instance, start, end, 0)
            }
        }
    }

    companion object {
        private const val TAG = "RichEditText"
    }

}

interface RichEditTextSpan

interface MultiValueStyle

class BoldStyle : StyleSpan(Typeface.BOLD), RichEditTextSpan

class ItalicStyle : StyleSpan(Typeface.ITALIC), RichEditTextSpan

class UnderlineStyle : UnderlineSpan(), RichEditTextSpan

class StrikethroughStyle : StrikethroughSpan(), RichEditTextSpan

class QuotaStyle : QuoteSpan(), RichEditTextSpan

class HeadlineStyle(val head: Int) : ParagraphStyle, RichEditTextSpan,
    RelativeSizeSpan((6 - head).toFloat()), MultiValueStyle {
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

class AlignmentStyle(val align: Alignment) : AlignmentSpan.Standard(align), RichEditTextSpan,
    MultiValueStyle {
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