package com.storyteller_f.rich_text_edit

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

fun <T> Iterable<T>.separate(block: (T) -> Boolean): Pair<List<T>, List<T>> {
    val groupBy = groupBy {
        block(it)
    }
    return groupBy[true].orEmpty() to groupBy[false].orEmpty()
}
