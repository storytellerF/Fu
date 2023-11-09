package com.storyteller_f.rich_text_edit

import android.text.SpannableStringBuilder
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

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
        editable.toggle(intRange, BoldStyle::class.java, {
            BoldStyle()
        }) {
            val spans = editable.getSpans(intRange, BoldStyle::class.java)
            assertEquals(1, spans.size)
            assert(editable.getSpanRange(spans.first()) == intRange)
        }
        editable.toggle(intRange, BoldStyle::class.java, {
            BoldStyle()
        }) {
            val spans = editable.getSpans(intRange, BoldStyle::class.java)
            assert(spans.isEmpty())
        }
    }
}