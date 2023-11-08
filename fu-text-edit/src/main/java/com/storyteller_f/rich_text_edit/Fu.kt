package com.storyteller_f.rich_text_edit

import android.text.Spannable
import android.text.Spanned

fun <T : RichSpan> Spannable.toggleParagraph(
    span: Class<T>,
    paragraph: Paragraph,
    factory: () -> T
) {
    val start = paragraph.start
    val end = paragraph.end
    val instance = factory()
    val spans = getSpans(start, end, span)
    val sameStyleSpans = spans.filter {
        it.javaClass == span
    }
    val sameStyleButNotEqual = sameStyleSpans.all {
        it != instance
    }
    sameStyleSpans.forEach {
        removeSpan(it)
    }
    if (sameStyleButNotEqual) {
        setSpan(instance, start, end, 0)
    }
}

/**
 * 对于在style 边缘新添加的字符自动应用样式。
 * 对于删除和在内部添加字符不符合此情景，会自动退出
 */
fun Spannable.autoApplyStyle(start: Int, before: Int, count: Int) {
    if (count < before) return
    //actually，before is 0
    val styleFilled = resolveStyleFilled(start..start + before)
    val allFilled = styleFilled.allFilled()
    allFilled.forEach {
        removeSpan(it.second)
        setSpan(it.second, start, start + count, 0)
    }
}

fun <T : RichSpan> Spannable.toggleText(
    span: Class<T>,
    selectionRange: IntRange,
    factory: () -> T
) {
    val instance = factory()
    if (instance.conflict.isNotEmpty()) {
        instance.conflict.map {
            resolveStyleFilled(selectionRange, span)
        }
    }
    val result = resolveStyleFilled(selectionRange, span)

    val (unfilled, filled) = result.separate {
        it.byBroken || !it.coverResult.covered()
    }

    if (filled.isEmpty()) {
        /**
         * 没有完整覆盖，相当于此处没有此样式，需要打开样式
         */
        val clearCutStyle = clearCutStyle(
            span,
            selectionRange,
            unfilled
        )
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
        clearCut(atPartial, selectionRange)
    }
}

/**
 * 填充样式。
 */
private fun <T : RichSpan> Spannable.clearCutStyle(
    span: Class<T>,
    selectionRange: IntRange,
    unfilled: List<FillResult>
): IntRange {
    val (atInner, atPartial) = unfilled.separate {
        it.range inner selectionRange
    }
    atInner.forEach {
        removeSpan(it.span)
    }
    return if (span.interfaces.any {
            it == MultiValueStyle::class.java
        }) {
        /**
         * 需要注意新添加的span 可能与现有的不完全相同，
         * 存在MultiValueStyle 的问题。如果存在，需要切割原有的style
         */
        clearCut(atPartial, selectionRange)
        selectionRange
    } else {
        //原有的style 全部移除
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
        left..right
    }
}

private fun Spannable.clearCut(
    atPartial: List<FillResult>,
    selectionRange: IntRange
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

private fun Spannable.resolveStyleFilled(
    selectionRange: IntRange,
    span: Class<out RichSpan>
): List<FillResult> {
    //选中区域所有的样式
    val spans =
        getSpans(selectionRange, span)
    val allBreaks =
        getSpans(selectionRange, Break::class.java).filter {
            it.style == span
        }.groupBy {
            it.style
        }
    //选中区域被填充的样式
    return groupAtSelection(spans, selectionRange, allBreaks)[span].orEmpty()
}

/**
 * 获取选定范围内的所有样式。同一种style 可能对应多个Result，
 * 因为在更长范围，存在两个不相连的区块，否则应该就是一个Result。
 */
fun Spannable.resolveStyleFilled(
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
    return groupAtSelection(spans, selectionRange, allBreaks)
}

private fun Spanned.groupAtSelection(
    spans: Array<out RichSpan>,
    selectionRange: IntRange,
    allBreaks: Map<Class<RichSpan>, List<Break>>
): Map<Class<out RichSpan>, List<FillResult>> {
    return spans.map { span ->
        val spanRange = getSpanRange(span)
        val beCovered = spanRange cover selectionRange
        val equaled = spanRange == selectionRange
        val currentStyleBreaks = allBreaks[span::class.java]
        val spanScopeRange = spanRange.coerce(selectionRange)
        val byBroken = if (currentStyleBreaks.isNullOrEmpty()) {
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
                beCovered -> CoverResult.Covered
                equaled -> CoverResult.Equaled
                else -> CoverResult.None
            }
        FillResult(span, coverResult, byBroken, spanRange)
    }.groupBy {
        it.span.javaClass
    }
}