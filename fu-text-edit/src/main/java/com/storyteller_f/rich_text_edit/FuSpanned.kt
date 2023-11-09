package com.storyteller_f.rich_text_edit

import android.text.Spannable
import android.text.Spanned
import android.widget.TextView

fun CharSequence.currentParagraph(selection: Int): Paragraph {
    var paragraphStart = selection
    val text = this
    while (paragraphStart > 0 && text[paragraphStart - 1] != '\n') {
        paragraphStart--
    }

    var paragraphEnd = selection
    while (paragraphEnd < text.length && text[paragraphEnd] != '\n') {
        paragraphEnd++
    }
    return Paragraph(paragraphStart, paragraphEnd)
}

/**
 * 指定区域存在完整的样式。
 */
fun Map<Class<out RichSpan>, List<FillResult>>.allFilled() =
    mapNotNull { entry ->
        if (entry.value.any {
                it.coverResult.covered() && !it.byBroken
            }) {
            entry.key to entry.value.first().span
        } else null
    }

val TextView.selectionRange get() = selectionStart..selectionEnd

fun <T : RichSpan> Spannable.toggle(
    selectionRange: IntRange,
    span: Class<T>,
    factory: () -> T,
    detectStyleAtCursor: () -> Unit
) {
    val interfaces = span.interfaces
    if (interfaces.any {
            it == RichParagraphStyle::class.java
        }) {
        val paragraph = currentParagraph(selectionRange.first)
        toggleParagraph(span, paragraph, factory)
    } else if (interfaces.any {
            it == RichTextStyle::class.java
        }) {
        toggleText(span, selectionRange, factory)
    } else throw Exception("unrecognized ${span.javaClass}")
    detectStyleAtCursor()
}

fun <T> Spannable.setSpan(instance: T, selectionRange: IntRange, flag: Int) {
    setSpan(instance, selectionRange.first, selectionRange.last, flag)
}

fun <T> Spanned.getSpans(selectionRange: IntRange, java: Class<T>): Array<out T> {
    return getSpans(selectionRange.first, selectionRange.last, java)
}

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

fun <T> Iterable<T>.separate(block: (T) -> Boolean): Pair<List<T>, List<T>> {
    val groupBy = groupBy {
        block(it)
    }
    return groupBy[true].orEmpty() to groupBy[false].orEmpty()
}

enum class Third {
    NEGATIVE, NEUTRAL, POSITIVE
}

fun <T> Iterable<T>.separateTriple(block: (T) -> Third): Triple<List<T>, List<T>, List<T>> {
    val groupBy = groupBy {
        block(it)
    }
    return Triple(
        groupBy[Third.NEGATIVE].orEmpty(),
        groupBy[Third.NEUTRAL].orEmpty(),
        groupBy[Third.POSITIVE].orEmpty()
    )
}