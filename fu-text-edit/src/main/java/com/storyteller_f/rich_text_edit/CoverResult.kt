package com.storyteller_f.rich_text_edit

enum class CoverResult {
    /**
     * 无法填充指定范围
     */
    None,

    /**
     * 超过指定范围
     */
    Covered,

    /**
     * 和范围完全相同，与Covered 排斥
     */
    Equaled;

    val covered get() = this == Covered || this == Equaled
}


data class FillResult(
    val span: RichSpan,
    val coverResult: CoverResult,
    val broken: Boolean,
    val range: IntRange
)