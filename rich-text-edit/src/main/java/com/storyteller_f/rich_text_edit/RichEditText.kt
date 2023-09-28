package com.storyteller_f.rich_text_edit

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpanWatcher
import android.text.Spannable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.MutableLiveData


class RichEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    val cursorStyle = MutableLiveData<List<Pair<Class<out RichSpan>, RichSpan>>>()

    inner class DetectCursorStyle : Runnable {
        override fun run() {
            val selectionStart = selectionStart
            val selectionEnd = selectionEnd
            val result = resolveStyleFill(selectionStart, selectionEnd)
            val allFilled = allFilled(result)
            Log.d(TAG, "run: $result $allFilled")
            cursorStyle.value = allFilled
        }

    }

    /**
     * 指定区域存在完整的样式。
     */
    private fun allFilled(styleFilled: Map<Class<out RichSpan>, List<FillResult>>) =
        styleFilled.mapNotNull { entry ->
            if (entry.value.any {
                    it.coverResult.covered() && !it.byBroken
                }) {
                entry.key to entry.value.first().span
            } else null
        }

    private val richEditHandler = Handler(Looper.getMainLooper())

    init {
        setEditableFactory(object : Editable.Factory() {
            override fun newEditable(source: CharSequence?): Editable {
                return super.newEditable(source).apply {
                    setSpan(object : SpanWatcher {
                        override fun onSpanAdded(
                            text: Spannable?, what: Any?, start: Int, end: Int
                        ) {
                            Log.d(
                                TAG,
                                "onSpanAdded() called with: text = $text, what = $what, start = $start, end = $end"
                            )
                        }

                        override fun onSpanRemoved(
                            text: Spannable?, what: Any?, start: Int, end: Int
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
                            detectStyleAtCursor()
                        }

                    }, 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
            }
        })

        gravity = Gravity.TOP
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(
                    TAG,
                    "beforeTextChanged() called with: s = $s ${s?.javaClass}, start = $start, count = $count, after = $after"
                )
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(
                    TAG,
                    "onTextChanged() called with: s = $s ${s?.javaClass}, start = $start, before = $before, count = $count"
                )
                autoApplyStyle(start, before, count)
            }

            override fun afterTextChanged(s: Editable?) {
                Log.d(TAG, "afterTextChanged() called with: s = $s ${s?.javaClass}")
            }

        })
    }

    /**
     * 对于在style 边缘新添加的字符自动应用样式。
     * 对于删除和在内部添加字符不符合此情景，会自动退出
     */
    private fun autoApplyStyle(start: Int, before: Int, count: Int) {
        if (count < before) return
        //actually，before is 0
        val styleFilled = resolveStyleFill(start, start + before)
        val allFilled = allFilled(styleFilled)
        allFilled.forEach {
            editableText.removeSpan(it.second)
            editableText.setSpan(it.second, start, start + count, 0)
        }
    }

    private fun detectStyleAtCursor() {
        richEditHandler.removeCallbacksAndMessages(null)
        richEditHandler.postDelayed(DetectCursorStyle(), 200)
    }

    class Paragraph(val start: Int, val end: Int)

    fun <T : RichSpan> toggle(
        span: Class<T>,
        factory: () -> T = { span.newInstance() },
    ) {
        val interfaces = span.interfaces
        if (interfaces.any {
                it == RichParagraphStyle::class.java
            }) {
            val paragraph = currentParagraph(selectionStart)
            toggleParagraph(span, paragraph, factory)
        } else if (interfaces.any {
                it == RichTextStyle::class.java
            }) {
            toggleText(span, factory)
        } else throw Exception("unrecognized ${span.javaClass}")
        detectStyleAtCursor()
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

    private fun <T : RichSpan> toggleParagraph(
        span: Class<T>,
        paragraph: Paragraph,
        factory: () -> T
    ) {
        val start = paragraph.start
        val end = paragraph.end
        val instance = factory()
        val spans = editableText.getSpans(start, end, span)
        val sameStyleSpans = spans.filter {
            it.javaClass == span
        }
        val sameStyleButNotEqual = sameStyleSpans.all {
            it != instance
        }
        sameStyleSpans.forEach {
            editableText.removeSpan(it)
        }
        if (sameStyleButNotEqual) {
            editableText.setSpan(instance, start, end, 0)
        }
    }

    private fun <T : RichSpan> toggleText(span: Class<T>, factory: () -> T) {
        val instance = factory()
        val result = resolveStyleFill(selectionStart, selectionEnd)
        val current = result[span].orEmpty()
        val unFilled = current.filter {
            it.byBroken || !it.coverResult.covered()
        }
        val filled = current.minus(unFilled.toSet())
        //todo 处理cover的情况
        if (filled.isEmpty()) {
            unFilled.forEach {
                editableText.removeSpan(it.span)
            }
            editableText.setSpan(instance, selectionStart, selectionEnd, 0)
        } else {
            current.forEach {
                editableText.removeSpan(it.span)
            }
        }
    }

    /**
     * 获取选定范围内的所有样式。同一种style 可能对应多个Result，因为在更长范围，存在两个不相连的区块，否则应该就是一个Result。
     */
    private fun resolveStyleFill(
        selectionStart: Int,
        selectionEnd: Int
    ): Map<Class<out RichSpan>, List<FillResult>> {
        //选中区域所有的样式
        val spans =
            editableText.getSpans(selectionStart, selectionEnd, RichSpan::class.java)
        val allBreaks =
            editableText.getSpans(selectionStart, selectionEnd, Break::class.java).groupBy {
                it.style
            }
        //选中区域被填充的样式
        return spans.map {
            val start = editableText.getSpanStart(it)
            val end = editableText.getSpanEnd(it)
            val beCovered = start < selectionStart && selectionEnd < end
            val equaled = start == selectionStart && end == selectionEnd
            val currentStyleBreaks = allBreaks[it::class.java]
            val spanScopeStart = start.coerceAtLeast(selectionStart)
            val spanScopeEnd = end.coerceAtMost(selectionEnd)
            val byBroken = if (currentStyleBreaks.isNullOrEmpty()) {
                false
            } else {
                currentStyleBreaks.any {
                    val breakStart = editableText.getSpanStart(it)
                    val breakEnd = editableText.getSpanEnd(it)
                    //break 需要至少需要占据选中范围内的style
                    breakStart <= spanScopeStart && breakEnd <= spanScopeEnd
                }
            }
            val coverResult =
                when {
                    beCovered -> CoverResult.Covered
                    equaled -> CoverResult.Equaled
                    else -> CoverResult.None
                }
            FillResult(it, coverResult, byBroken, start, end)
        }.groupBy {
            it.span.javaClass
        }
    }

    companion object {
        private const val TAG = "RichEditText"
    }

}
