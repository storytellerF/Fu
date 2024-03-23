package com.storyteller_f.fu

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Layout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.distinctUntilChanged
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.storyteller_f.fu.databinding.ActivityMainBinding
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
import getAlignmentStyleState
import getHeadlineStyleState
import getStyleState
import getStyleValueState

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val richEditText = binding.text
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
            // Kotlin Code
            ColorPickerDialog
                .Builder(this)                        // Pass Activity Instance
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

    }

    private fun colorStateList(b: Boolean) =
        if (b) ColorStateList.valueOf(Color.RED) else ColorStateList.valueOf(
            Color.BLACK
        )
}