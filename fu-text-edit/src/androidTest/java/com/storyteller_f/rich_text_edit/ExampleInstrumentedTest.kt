package com.storyteller_f.rich_text_edit

import android.graphics.Color
import android.text.Layout
import android.text.SpannableStringBuilder
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
        val editable = builder {
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
        val editable = builder {
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
        val editable = builder {
            append("hello!")
        }
        editable.toggle(0..6, BoldStyle::class.java)
        val detectStyle = editable.detectStyle(6..6)
        assertEquals(0, detectStyle.size)
    }

    @Test
    fun testApplyTextColor() {
        val editable = builder {
            append("hello")
        }
        editable.toggle(0..2, ColorStyle::class.java, ColorStyle(Color.RED))
        assertEquals(1, editable.detectStyle(0..2).size)
        editable.toggle(0..2, ColorStyle::class.java, ColorStyle(Color.RED))
        assertEquals(1, editable.detectStyle(0..2).size)
    }

    @Test
    fun testApplyHeadlineColor() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val editable = builder {
            append("hello")
        }
        editable.toggle(0..2, HeadlineStyle::class.java, HeadlineStyle(1, 2f, appContext))
        assertEquals(1, editable.detectStyle(0..2).size)
        editable.toggle(0..2, HeadlineStyle::class.java, HeadlineStyle(2, 1.3f, appContext))
        assertEquals(1, editable.detectStyle(0..2).size)
    }

    @Test
    fun testCharSequenceStringify() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val editable = builder {
            append("hello")
        }
        editable.toggle(0..2, HeadlineStyle::class.java, HeadlineStyle(1, 2f, appContext))
        val richFormatStringify = editable.richFormatPlain()
        assertEquals("StyleData(type=headline, start=0, end=5, data=1)", richFormatStringify)
    }

    @Test
    fun testAlignmentCharSequenceStringify() {
        val editable = builder {
            append("hello")
        }
        editable.toggle(0..2, AlignmentStyle::class.java, AlignmentStyle(Layout.Alignment.ALIGN_OPPOSITE))
        val richFormatStringify = editable.richFormatPlain()
        assertEquals("StyleData(type=alignment, start=0, end=5, data=1)", richFormatStringify)
    }

    private fun builder(block: SpannableStringBuilder.() -> Unit): SpannableStringBuilder {
        return SpannableStringBuilder().apply {
            block()
        }
    }
}