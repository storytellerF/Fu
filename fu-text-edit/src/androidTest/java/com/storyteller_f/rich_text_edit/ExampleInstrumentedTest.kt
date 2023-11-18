package com.storyteller_f.rich_text_edit

import android.text.SpannableStringBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.storyteller_f.rich_text_edit.test", appContext.packageName)
    }

    @Test
    fun testApplyTextStyle() {
        val editable = SpannableStringBuilder().apply {
            append("hello")
        }
        val intRange = 0..4
        editable.toggle(intRange, BoldStyle::class.java)
        run {
            val spans = editable.getSpans(intRange, BoldStyle::class.java)
            assertEquals(1, spans.size)
            assert(editable.getSpanRange(spans.first()) == intRange)
        }
        editable.toggle(intRange, BoldStyle::class.java)
        run {
            val spans = editable.getSpans(intRange, BoldStyle::class.java)
            assert(spans.isEmpty())
        }
    }

    @Test
    fun testApplyTextStyleWhenTwoExists() {
        val editable = SpannableStringBuilder().apply {
            append("hello")
        }
        editable.toggle(0..1, BoldStyle::class.java)
        editable.toggle(2..3, BoldStyle::class.java)
        editable.toggle(0..3, BoldStyle::class.java)
        run {
            val styles = editable.getSpans(0..3, BoldStyle::class.java)
            assertEquals(1, styles.size)
            assertEquals(0..3, editable.getSpanRange(styles.first()))
        }
    }

    @Test
    fun testAutoApplyTextStyle() {
        val editable = SpannableStringBuilder().apply {
            append("hello!")
        }
        editable.toggle(0..6, BoldStyle::class.java)
        val detectStyle = editable.detectStyle(6..6)
        assertEquals(0, detectStyle.size)
    }
}