package com.storyteller_f.rich_text_edit

import android.text.Spannable
import android.text.Spanned
import android.widget.TextView

val TextView.selectionRange get() = selectionStart..selectionEnd

fun <T> Spannable.setSpan(instance: T, selectionRange: IntRange, flag: Int) {
    setSpan(instance, selectionRange.first, selectionRange.last, flag)
}

fun <T> Spanned.getSpans(selectionRange: IntRange, java: Class<T>): Array<out T> {
    return getSpans(selectionRange.first, selectionRange.last, java)
}
