package com.storyteller_f.rich_text_edit

enum class CoverResult {
    None, Covered, Equaled;

    fun covered(): Boolean {
        return this == Covered || this == Equaled
    }
}



class FillResult(
    val span: RichSpan,
    val coverResult: CoverResult,
    val byBroken: Boolean,
    val start: Int,
    val end: Int
)