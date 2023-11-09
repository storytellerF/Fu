package com.storyteller_f.rich_text_edit

import android.text.Spanned


infix fun <T : Comparable<T>> ClosedRange<T>.cover(range: ClosedRange<T>) =
    start <= range.start && range.endInclusive <= endInclusive

infix fun <T : Comparable<T>> ClosedRange<T>.coerce(range: ClosedRange<T>) =
    start.coerceAtLeast(range.start)..endInclusive.coerceAtLeast(range.endInclusive)

infix fun <T : Comparable<T>> ClosedRange<T>.partial(range: ClosedRange<T>): Boolean {
    val leftPartial =
        start < range.start && range.start < endInclusive && endInclusive < range.endInclusive
    val rightPartial = start < range.endInclusive && range.endInclusive < endInclusive
    return leftPartial || rightPartial
}

infix fun <T : Comparable<T>> ClosedRange<T>.leftPartial(range: ClosedRange<T>): Boolean {
    return start < range.start && range.start < endInclusive && endInclusive < range.endInclusive
}

infix fun <T : Comparable<T>> ClosedRange<T>.rightPartial(range: ClosedRange<T>): Boolean {
    return start < range.endInclusive && range.endInclusive < endInclusive
}

infix fun <T : Comparable<T>> ClosedRange<T>.inner(range: ClosedRange<T>): Boolean {
    return range.start < start && endInclusive < range.endInclusive
}

fun Spanned.getSpanRange(span: RichSpan): IntRange {
    val start = getSpanStart(span)
    val end = getSpanEnd(span)
    return start..end
}