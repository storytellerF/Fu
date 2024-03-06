package com.storyteller_f.rich_text_edit

import android.text.Spannable
import android.text.Spanned
import android.util.Log
import android.widget.EditText

/**
 * 标点符号会导致样式应用范围。
 */
val BREAK_CHARACTER = run {
    buildList {
        listOf(
            (' '..'/'),
            (':'..'@'),
            ('['..'`'),
            ('{'..'~')
        ).forEach { range ->
            range.forEach {
                add(it)
            }
        }
    }
}

/**
 * 切换样式。通过newInstance() 生成新的样式
 */
fun <T : RichSpan> Spannable.toggle(
    selectionRange: IntRange,
    spanType: Class<T>,
) =
    toggle(selectionRange, spanType, spanType.getConstructor().newInstance())

/**
 * 切换样式
 */
fun <T : RichSpan> Spannable.toggle(
    selectionRange: IntRange,
    spanType: Class<T>,
    span: T?,
) {
    if (spanType.isParagraphStyle) {
        val paragraph = paragraphAt(selectionRange.first)
        toggleParagraph(paragraph, spanType, span!!)
    } else if (spanType.isCharacterStyle) {
        toggleText(selectionRange, spanType, span)
    } else throw Exception("unrecognized ${spanType.javaClass}")
}

/**
 * 切换样式
 */
fun <T : RichSpan> EditText.toggle(span: Class<T>, factory: T) {
    editableText.toggle(selectionRange, span, factory)
}

/**
 * 清除指定样式
 */
fun <T> EditText.clear(klass: Class<T>) where T : MultiValueStyle<*>, T : RichSpan {
    editableText.toggle(selectionRange, klass, null)
}

/**
 * 检测指定区域完整填充的样式
 */
fun Spannable.detectStyle(
    range: IntRange,
): List<Pair<Class<out RichSpan>, RichSpan>> {
    val result = resolveStyleFillResult(range)
    val allFilled = detectCoveredStyle(result)
    Log.d(
        "Fu", "DetectCursorStyle run:\n ${
            result.map {
                "\t${it.key}-${it.value}"
            }.joinToString("\n")
        }\n\t$allFilled"
    )
    return allFilled
}

/**
 * 切换段落型样式
 */
private fun <T : RichSpan> Spannable.toggleParagraph(
    paragraph: Paragraph,
    spanType: Class<T>,
    instance: T
) {
    val spans = getSpans(paragraph.range, spanType)
    val sameStyleSpans = spans.filter {
        it.javaClass == spanType
    }
    val sameStyleButNotEqual = sameStyleSpans.all {
        it != instance
    }
    sameStyleSpans.forEach {
        removeSpan(it)
    }
    if (sameStyleButNotEqual) {
        setSpan(instance, paragraph.range)
    }
}

/**
 * 切换字符型样式
 */
private fun <T : RichSpan> Spannable.toggleText(
    selectionRange: IntRange,
    span: Class<T>,
    instance: T?
) {
    if (instance != null && instance.conflict.isNotEmpty() && instance.conflict.any {
            resolveStyleFillResult(selectionRange, span).isNotEmpty()
        }) {
        return
    }
    val result = resolveStyleFillResult(selectionRange, span)

    val (unfilled, filled) = result.separate {
        it.broken || !it.coverResult.covered
    }

    if (filled.isEmpty()) {
        /**
         * 没有完整覆盖，相当于此处没有此样式，需要打开样式
         */
        val clearCutStyle = clearCutStyle(
            selectionRange,
            span,
            unfilled
        )
        if (instance != null)
            setSpan(instance, clearCutStyle, 0)
    } else {
        /**
         * 完全覆盖选中区域，不过也有可能存在选中区域以外的部分。
         */
        val (atPartial, other) = result.separate {
            it.range partial selectionRange
        }
        other.forEach {
            removeSpan(it.span)
        }
        if (atPartial.isNotEmpty()) {
            clearCut(selectionRange, atPartial)
        }
        if (instance != null) {
            result.forEach {
                if (it.span is MultiValueStyle<*>) {
                    setSpan(instance, selectionRange)
                }
            }
        }
    }
}

/**
 * 裁切指定区域指定类型的样式清除。
 */
private fun <T : RichSpan> Spannable.clearCutStyle(
    selectionRange: IntRange,
    spanType: Class<T>,
    unfilled: List<FillResult>
): IntRange {
    val (atInner, atPartial) = unfilled.separate {
        it.range inner selectionRange
    }
    atInner.forEach {
        removeSpan(it.span)
    }
    return if (spanType.interfaces.any {
            it == MultiValueStyle::class.java
        }) {
        /**
         * 需要注意新添加的span 可能与现有的不完全相同，
         * 存在MultiValueStyle 的问题。如果存在，需要切割原有的style
         */
        clearCut(selectionRange, atPartial)
        selectionRange
    } else {
        //原有的style 全部移除
        removeAndCalcMaxRange(atPartial, selectionRange)
    }
}

/**
 * 后续需要重新设置一个覆盖完整区域的样式，
 * 完整区域指当前所有同类型在指定范围存在过的最大范围
 */
private fun Spannable.removeAndCalcMaxRange(
    atPartial: List<FillResult>,
    selectionRange: IntRange
): IntRange {
    val left = atPartial.filter {
        it.range leftPartial selectionRange
    }.minOfOrNull {
        it.range.first
    } ?: selectionRange.first
    val right = atPartial.filter {
        it.range rightPartial selectionRange
    }.minOfOrNull {
        it.range.last
    } ?: selectionRange.last
    atPartial.forEach {
        removeSpan(it.span)
    }
    return left..right
}

/**
 * 裁切指定的style，缩短范围。
 */
private fun Spannable.clearCut(
    selectionRange: IntRange,
    atPartial: List<FillResult>
) {
    atPartial.filter {
        it.range leftPartial selectionRange
    }.forEach {
        setSpan(it.span, it.range.first, selectionRange.first, 0)
    }
    atPartial.filter {
        it.range rightPartial selectionRange
    }.forEach {
        setSpan(it.span, selectionRange.last, it.range.last, 0)
    }
}

/**
 * 获取选中区域指定样式的填充结果。
 */
private fun Spannable.resolveStyleFillResult(
    selectionRange: IntRange,
    span: Class<out RichSpan>
): List<FillResult> {
    //选中区域所有的样式
    val spans = getSpans(selectionRange, span)
    val allBreaks =
        getSpans(selectionRange, Break::class.java).filter {
            it.style == span
        }.groupBy {
            it.style
        }
    //选中区域被填充的样式
    return spans.map {
        spanToResult(it, selectionRange, allBreaks)
    }
}

/**
 * 获取选定范围内的所有样式。同一种style 可能对应多个Result，
 * 因为在更长范围，可能存在两个不相连的区块，否则应该就是一个Result。
 */
fun Spannable.resolveStyleFillResult(
    selectionRange: IntRange
): Map<Class<out RichSpan>, List<FillResult>> {
    //选中区域所有的样式
    val spans =
        getSpans(selectionRange, RichSpan::class.java)
    val allBreaks =
        getSpans(selectionRange, Break::class.java).groupBy {
            it.style
        }
    //选中区域被填充的样式
    return spans.map { span ->
        spanToResult(span, selectionRange, allBreaks)
    }.groupBy {
        it.span.javaClass
    }
}

/**
 * 获取span 在指定区域的填充结果
 */
private fun Spanned.spanToResult(
    span: RichSpan,
    selectionRange: IntRange,
    allBreaks: Map<Class<RichSpan>, List<Break>>
): FillResult {
    val spanRange = getSpanRange(span)
    val covered = spanRange cover selectionRange
    val equaled = spanRange == selectionRange
    val currentStyleBreaks = allBreaks[span::class.java]
    val spanScopeRange = spanRange.coerce(selectionRange)
    val broken = if (currentStyleBreaks.isNullOrEmpty()) {
        false
    } else {
        currentStyleBreaks.any {
            val breakStart = getSpanStart(it)
            val breakEnd = getSpanEnd(it)
            val breakRange = breakStart..breakEnd
            //break 需要至少需要占据选中范围内的style
            breakRange cover spanScopeRange
        }
    }
    val coverResult =
        when {
            covered -> CoverResult.Covered
            equaled -> CoverResult.Equaled
            else -> CoverResult.None
        }
    return FillResult(span, coverResult, broken, spanRange)
}

/**
 * 获取选中位置所在的段落。
 */
fun CharSequence.paragraphAt(selection: Int): Paragraph {
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
fun Spannable.detectCoveredStyle(map: Map<Class<out RichSpan>, List<FillResult>>) =
    map.mapNotNull { entry ->
        if (entry.value.any {
                val range = it.range
                val lastCharacter = get(range.last - 1)
                it.coverResult.covered && !it.broken && !BREAK_CHARACTER.contains(lastCharacter)
            }) {
            entry.key to entry.value.first().span
        } else null
    }
