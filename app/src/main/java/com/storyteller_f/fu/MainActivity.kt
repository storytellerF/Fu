package com.storyteller_f.fu

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ReplacementSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.lifecycle.distinctUntilChanged
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.storyteller_f.fu.databinding.ActivityMainBinding
import com.storyteller_f.fu.databinding.LayoutPostRefBinding
import com.storyteller_f.rich_text_edit.AlignmentStyle
import com.storyteller_f.rich_text_edit.BackgroundStyle
import com.storyteller_f.rich_text_edit.BoldStyle
import com.storyteller_f.rich_text_edit.ColorStyle
import com.storyteller_f.rich_text_edit.HeadlineStyle
import com.storyteller_f.rich_text_edit.ItalicStyle
import com.storyteller_f.rich_text_edit.MultiValueStyle
import com.storyteller_f.rich_text_edit.QuotaStyle
import com.storyteller_f.rich_text_edit.StrikethroughStyle
import com.storyteller_f.rich_text_edit.UnderlineStyle
import com.storyteller_f.rich_text_edit.ViewStyle
import getAlignmentStyleState
import getHeadlineStyleState
import getStyleState
import getStyleValueState
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val group = binding.group
        val richEditText = group.editText
        val padding = resources.getDimension(R.dimen.edit_text_padding_horizontal)
        richEditText.updatePadding(left = padding.roundToInt(), right = padding.roundToInt())
        binding.bold.setOnClickListener {
            richEditText.toggleAndFlush(BoldStyle::class.java)
        }
        binding.italic.setOnClickListener {
            richEditText.toggleAndFlush(ItalicStyle::class.java)
        }
        binding.underline.setOnClickListener {
            richEditText.toggleAndFlush(UnderlineStyle::class.java)
        }
        binding.strike.setOnClickListener {
            richEditText.toggleAndFlush(StrikethroughStyle::class.java)
        }
        binding.quota.setOnClickListener {
            richEditText.toggleAndFlush(QuotaStyle::class.java)
        }
        binding.alignRight.setOnClickListener {
            richEditText.toggleAndFlush(
                AlignmentStyle::class.java,
                AlignmentStyle(Layout.Alignment.ALIGN_OPPOSITE)
            )
        }
        binding.alignCenter.setOnClickListener {
            richEditText.toggleAndFlush(
                AlignmentStyle::class.java,
                AlignmentStyle(Layout.Alignment.ALIGN_CENTER)
            )
        }
        binding.alignLeft.setOnClickListener {
            richEditText.toggleAndFlush(
                AlignmentStyle::class.java,
                AlignmentStyle(Layout.Alignment.ALIGN_NORMAL)
            )
        }
        binding.foreground.setOnClickListener {
            richEditText.clearAndFlush(ColorStyle::class.java)
        }
        binding.changeTextColor.setOnClickListener {
            ColorPickerDialog
                .Builder(this)
                .setColorListener { color, _ ->
                    richEditText.toggleAndFlush(ColorStyle::class.java, ColorStyle(color))
                }
                .show()
        }

        binding.background.setOnClickListener {
            richEditText.clearAndFlush(BackgroundStyle::class.java)
        }
        binding.changeBackgroundColor.setOnClickListener {
            // Kotlin Code
            ColorPickerDialog
                .Builder(this)                        // Pass Activity Instance
                .setColorListener { color, _ ->
                    richEditText.toggleAndFlush(BackgroundStyle::class.java, BackgroundStyle(color))
                }
                .show()
        }
        val headline = listOf(3f, 2.5f, 2f, 1.5f, 1.2f)
        val headlineTextViews = listOf(binding.h1, binding.h2, binding.h3, binding.h4, binding.h5)
        headlineTextViews.forEachIndexed { index, button ->
            button.setOnClickListener {
                richEditText.toggleAndFlush(
                    HeadlineStyle::class.java,
                    HeadlineStyle(index + 1, headline[index], this)
                )
            }
        }
        listOf(
            UnderlineStyle::class.java to binding.underline,
            BoldStyle::class.java to binding.bold,
            ItalicStyle::class.java to binding.italic,
            StrikethroughStyle::class.java to binding.strike,
            QuotaStyle::class.java to binding.quota
        ).forEach { (clazz, view) ->
            richEditText.getStyleState(clazz).distinctUntilChanged().observe(this) {
                view.imageTintList = colorStateList(it)
            }
        }
        listOf(
            Layout.Alignment.ALIGN_CENTER to binding.alignCenter,
            Layout.Alignment.ALIGN_NORMAL to binding.alignLeft,
            Layout.Alignment.ALIGN_OPPOSITE to binding.alignRight
        ).forEach { (alignment, view) ->
            richEditText.getAlignmentStyleState(alignment).distinctUntilChanged().observe(this) {
                view.imageTintList = colorStateList(it)
            }
        }
        val listOf: List<Pair<Pair<Class<out MultiValueStyle<Int>>, Int>, TextView>> = listOf(
            ColorStyle::class.java to Color.BLACK to binding.foreground,
            BackgroundStyle::class.java to Color.WHITE to binding.background
        )
        listOf.forEach { (clazz, view) ->
            richEditText.getStyleValueState<Int>(
                clazz.first
            ).distinctUntilChanged().observe(this) { value ->
                view.setTextColor(value ?: clazz.second)
            }
        }
        headlineTextViews.forEachIndexed { index, button ->
            val headlineValue = index + 1
            richEditText.getHeadlineStyleState(headlineValue).distinctUntilChanged().observe(this) {
                button.setTextColor(colorStateList(it))
            }

        }
        richEditText.text = SpannableStringBuilder().apply {
            appendLine("Hello World!")
            appendLine(" ")
            append(
                "Exception in thread \"main\" kotlinx.serialization.SerializationException: Serializer for subclass 'OwnedProject' is not found in the polymorphic scope of 'Project'.\n" +
                        "Check if class with serial name 'OwnedProject' exists and serializer is registered in a corresponding SerializersModule.\n" +
                        "To be registered automatically, class 'OwnedProject' has to be '@Serializable', and the base class 'Project' has to be sealed and '@Serializable'.\nKotlin Serialization is fully static with respect to types by default. The structure of encoded objects is determined by compile-time types of objects. Let's examine this aspect in more detail and learn how to serialize polymorphic data structures, where the type of data is determined at runtime.\n" +
                        "\n" +
                        "To show the static nature of Kotlin Serialization let us make the following setup. An open class Project has just the name property, while its derived class OwnedProject adds an owner property. In the below example, we serialize data variable with a static type of Project that is initialized with an instance of OwnedProject at runtime."
            )
        }
        val width = ((if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.width()
        } else {
            windowManager.defaultDisplay.width
        }) - padding * 2).roundToInt()
        richEditText.text?.run {
            setSpan(PostAttachmentStyle(layoutInflater, width), 13, 15, 0)
        }

    }

    private fun colorStateList(b: Boolean) =
        if (b) ColorStateList.valueOf(Color.RED) else ColorStateList.valueOf(
            Color.BLACK
        )
}

class PostAttachmentStyle(layoutInflater: LayoutInflater, private val width: Int) : ViewStyle() {
    private val cached by lazy {
        LayoutPostRefBinding.inflate(layoutInflater)
    }

    override fun getView(): View {
        return cached.root
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        fm ?: return 0
        cached.root.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(1000, MeasureSpec.AT_MOST)
        )
        fm.descent += cached.root.measuredHeight - (fm.descent - fm.ascent)
        return width
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
    }

    override val type: String
        get() = "post"

    companion object
}

class Line4Style : ReplacementSpan() {

    private var cache: Float = 0f
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        Log.d(
            TAG,
            "getSize() called with: paint = $paint, text = $text, start = $start, end = $end, fm = $fm"
        )
        val result = paint.measureText(text, start, end)
        cache = result
        return result.toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        paint as TextPaint
        Log.d(
            TAG,
            "draw() called with: canvas = $canvas, text = $text, start = $start, end = $end, x = $x, top = $top, y = $y, bottom = $bottom, paint = ${paint.fontMetricsInt}"
        )
        drawLine(Color.MAGENTA, canvas, paint, x, paint.fontMetricsInt.top + y)
        drawLine(Color.CYAN, canvas, paint, x, (paint.ascent() + y).toInt())
        drawLine(Color.GREEN, canvas, paint, x, y)
        drawLine(Color.RED, canvas, paint, x, bottom)
    }

    private fun drawLine(color: Int, canvas: Canvas, paint: Paint, x: Float, y: Int) {
        paint.color = color
        canvas.drawLine(x, y.toFloat(), cache, (y + 1).toFloat(), paint)
    }

    companion object {
        private const val TAG = "Line4Style"
    }


}
