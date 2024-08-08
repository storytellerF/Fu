package com.storyteller_f.rich_text_edit

import android.content.Context
import android.text.SpanWatcher
import android.text.Spannable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

class FuEditTextGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    val editText = FuEditText(context)
    val map = mutableMapOf<ViewStyle, View>()
    val reversedMap = mutableMapOf<View, ViewStyle>()

    init {
        editText.addSpanWatcher(object : SpanWatcher {
            override fun onSpanAdded(text: Spannable?, what: Any?, start: Int, end: Int) {
                Log.d(
                    TAG,
                    "onSpanAdded() called with: text = $text, what = $what, start = $start, end = $end"
                )
                if (what is ViewStyle) {
                    Log.i(TAG, "onSpanAdded: add view done")
                    if (map[what] == null) {
                        val view = what.getView()
                        reversedMap[view] = what
                        map[what] = view
                        addView(view)
                    }
                }
            }

            override fun onSpanRemoved(text: Spannable?, what: Any?, start: Int, end: Int) {
                Log.i(TAG, "onSpanRemoved: add view removed $what")
                if (what is ViewStyle) {
                    val view = map[what]
                    if (view != null) {
                        removeView(view)
                        map.remove(what)
                        reversedMap.remove(view)
                    }
                }
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

        })
        addView(
            editText,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )

        editText.viewTreeObserver.addOnPreDrawListener {
            styleChildren().forEach {
                it.translationY = -editText.scrollY.toFloat()
            }
            true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChild(editText, widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Log.d(TAG, "onLayout() called with: changed = $changed, l = $l, t = $t, r = $r, b = $b")
        Log.i(TAG, "onLayout: add view layout")
        editText.layout(
            paddingStart,
            paddingTop,
            paddingStart + measuredWidth,
            paddingTop + measuredHeight
        )
        styleChildren().forEach {
            val viewStyle = reversedMap[it]!!
            val range = editText.editableText.getSpanRange(viewStyle)
            val line = editText.layout.getLineForOffset(range.first)
            val top = editText.layout.getLineTop(line) + editText.paddingTop
            Log.i(TAG, "onLayout: add view $line $top")
            it.layout(
                l + editText.paddingStart + paddingStart,
                top + paddingTop,
                l + editText.paddingStart + paddingStart + it.measuredWidth,
                top + paddingTop + it.measuredHeight
            )
        }
    }


    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT, MarginLayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    private fun styleChildren() = children.filter {
        it !== editText
    }

    companion object {
        private const val TAG = "FuEditTextGroup"
    }
}