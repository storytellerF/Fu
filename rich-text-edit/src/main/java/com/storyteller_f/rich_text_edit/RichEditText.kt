package com.storyteller_f.rich_text_edit

import android.content.Context
import android.graphics.Typeface
import android.os.Parcel
import android.text.*
import android.text.style.*
import android.util.AttributeSet
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toIcon
import androidx.core.net.toFile
import androidx.core.text.getSpans
import androidx.core.text.toSpannable
import androidx.core.text.toSpanned
import androidx.core.widget.doOnTextChanged
import androidx.documentfile.provider.DocumentFile
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.io.StringWriter

class RichEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatEditText(context, attrs) {
    init {
        setEditableFactory(object : Editable.Factory() {
            override fun newEditable(source: CharSequence?): Editable {
                return super.newEditable(source).apply {
                    setSpan(object : SpanWatcher {
                        override fun onSpanAdded(text: Spannable?, what: Any?, start: Int, end: Int) {
                        }

                        override fun onSpanRemoved(text: Spannable?, what: Any?, start: Int, end: Int) {
                        }

                        override fun onSpanChanged(text: Spannable?, what: Any?, ostart: Int, oend: Int, nstart: Int, nend: Int) {
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
    fun toggle(span: Class<out RichEditTextSpan>) {
        if (selectionStart != selectionEnd) {
            val spans = editableText.getSpans(selectionStart, selectionEnd, span)
            if (spans.isEmpty()) {
                editableText.setSpan(span.newInstance(), selectionStart, selectionEnd, 0)
            } else {
                spans.forEach {
                    editableText.removeSpan(it)
                }
            }
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

}

interface RichEditTextSpan

class BoldStyle : StyleSpan(Typeface.BOLD), RichEditTextSpan

class ItalicStyle : StyleSpan(Typeface.ITALIC), RichEditTextSpan

class UnderlineStyle : UnderlineSpan(), RichEditTextSpan

class StrikethroughStyle : StrikethroughSpan(), RichEditTextSpan