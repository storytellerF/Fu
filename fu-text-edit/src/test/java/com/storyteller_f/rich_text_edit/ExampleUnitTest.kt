package com.storyteller_f.rich_text_edit

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(Paragraph(0, 4), "test\nhello".currentParagraph(1))
        assertEquals(Paragraph(5, 10), "test\nhello".currentParagraph(6))
    }
}