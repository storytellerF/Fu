package com.storyteller_f.rich_text_edit

import android.text.Spanned
import android.util.Log


infix fun <T : Comparable<T>> ClosedRange<T>.cover(range: ClosedRange<T>) =
    start <= range.start && range.endInclusive <= endInclusive

/**
 * 限制范围最大为range 的范围
 */
infix fun <T : Comparable<T>> ClosedRange<T>.coerce(range: ClosedRange<T>) =
    start.coerceAtLeast(range.start)..endInclusive.coerceAtMost(range.endInclusive)

/**
 * @return 返回是否覆盖了range 的左半部分或者右半部分
 */
infix fun <T : Comparable<T>> ClosedRange<T>.partial(range: ClosedRange<T>): Boolean {
    return leftPartial(range) || rightPartial(range)
}

/**
 * @return 返回覆盖了range 的左半部分
 */
infix fun <T : Comparable<T>> ClosedRange<T>.leftPartial(range: ClosedRange<T>): Boolean {
    return start < range.start && range.start < endInclusive && endInclusive < range.endInclusive
}

/**
 * @return 返回是否覆盖了range 的右半部分
 */
infix fun <T : Comparable<T>> ClosedRange<T>.rightPartial(range: ClosedRange<T>): Boolean {
    return start < range.endInclusive && range.endInclusive < endInclusive
}

/**
 * @return 返回是否是range 中的一部分
 */
infix fun <T : Comparable<T>> ClosedRange<T>.inner(range: ClosedRange<T>): Boolean {
    return range.start < start && endInclusive < range.endInclusive
}

fun Spanned.getSpanRange(span: RichSpan): IntRange {
    val start = getSpanStart(span)
    val end = getSpanEnd(span)
    return start..end
}