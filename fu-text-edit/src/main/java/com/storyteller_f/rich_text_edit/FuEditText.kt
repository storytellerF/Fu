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


class FuEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    val cursorStyle = MutableLiveData<List<Pair<Class<out RichSpan>, RichSpan>>>()

    private val richEditHandler = Handler(Looper.getMainLooper())

    inner class DetectCursorStyle : Runnable {
        override fun run() {
            cursorStyle.value = editableText.detectStyle(selectionRange)
        }

    }

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
                            sendDetectMessage()
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
                editableText.autoApplyStyle(start, before, count)
            }

            override fun afterTextChanged(s: Editable?) {
                Log.d(TAG, "afterTextChanged() called with: s = $s ${s?.javaClass}")
            }

        })
    }

    private fun sendDetectMessage() {
        richEditHandler.removeCallbacksAndMessages(null)
        richEditHandler.postDelayed(DetectCursorStyle(), 200)
    }

    fun <T : RichSpan> toggle(
        span: Class<T>,
        factory: T = span.getConstructor().newInstance(),
    ) {
        editableText.toggle(selectionRange, span, factory)
        sendDetectMessage()
    }

    companion object {
        private const val TAG = "FuEditText"
    }

}

