package com.storyteller_f.fu

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Layout
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.storyteller_f.fu.databinding.ActivityMainBinding
import com.storyteller_f.rich_text_edit.AlignmentStyle
import com.storyteller_f.rich_text_edit.BackgroundStyle
import com.storyteller_f.rich_text_edit.BoldStyle
import com.storyteller_f.rich_text_edit.ColorStyle
import com.storyteller_f.rich_text_edit.HeadlineStyle
import com.storyteller_f.rich_text_edit.ItalicStyle
import com.storyteller_f.rich_text_edit.QuotaStyle
import com.storyteller_f.rich_text_edit.StrikethroughStyle
import com.storyteller_f.rich_text_edit.UnderlineStyle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val richEditText = binding.text
        binding.bold.setOnClickListener {
            richEditText.toggle(BoldStyle::class.java)
        }
        binding.italic.setOnClickListener {
            richEditText.toggle(ItalicStyle::class.java)
        }
        binding.underline.setOnClickListener {
            richEditText.toggle(UnderlineStyle::class.java)
        }
        binding.strike.setOnClickListener {
            richEditText.toggle(StrikethroughStyle::class.java)
        }
        binding.quota.setOnClickListener {
            richEditText.toggle(QuotaStyle::class.java)
        }
        binding.alignRight.setOnClickListener {
            richEditText.toggle(AlignmentStyle::class.java) {
                AlignmentStyle(Layout.Alignment.ALIGN_OPPOSITE)
            }
        }
        binding.alignCenter.setOnClickListener {
            richEditText.toggle(AlignmentStyle::class.java) {
                AlignmentStyle(Layout.Alignment.ALIGN_CENTER)
            }
        }
        binding.alignLeft.setOnClickListener {
            richEditText.toggle(AlignmentStyle::class.java) {
                AlignmentStyle(Layout.Alignment.ALIGN_NORMAL)
            }
        }
        binding.changeTextColor.setOnClickListener {
            // Kotlin Code
            ColorPickerDialog
                .Builder(this)        				// Pass Activity Instance
                .setColorListener { color, _ ->
                    richEditText.toggle(ColorStyle::class.java) {
                        ColorStyle(color)
                    }
                }
                .show()
        }

        binding.changeBackgroundColor.setOnClickListener {
            // Kotlin Code
            ColorPickerDialog
                .Builder(this)        				// Pass Activity Instance
                .setColorListener { color, _ ->
                    richEditText.toggle(BackgroundStyle::class.java) {
                        BackgroundStyle(color)
                    }
                }
                .show()
        }
        val headline = listOf(3f, 2.5f, 2f, 1.5f, 1.2f)
        val headlineTextViews = listOf(binding.h1, binding.h2, binding.h3, binding.h4, binding.h5)
        headlineTextViews.forEachIndexed { index, button ->
            button.setOnClickListener {
                richEditText.toggle(HeadlineStyle::class.java) {
                    HeadlineStyle(index + 1, headline[index])
                }
            }
        }
        richEditText.cursorStyle.observe(this) { spans ->
            binding.underline.imageTintList = colorStateList(spans.any {
                it.first == UnderlineStyle::class.java
            })
            binding.bold.imageTintList = colorStateList(spans.any {
                it.first == BoldStyle::class.java
            })
            binding.italic.imageTintList = colorStateList(spans.any {
                it.first == ItalicStyle::class.java
            })
            binding.strike.imageTintList = colorStateList(spans.any {
                it.first == StrikethroughStyle::class.java
            })
            val allHeadline = spans.filter {
                it.first == HeadlineStyle::class.java
            }.map {
                it.second
            }.filterIsInstance<HeadlineStyle>()
            headlineTextViews.forEachIndexed { index, button ->
                button.setTextColor(colorStateList(allHeadline.any {
                    it.value == index + 1
                }))
            }
            binding.foreground.setTextColor((spans.firstOrNull {
                it.first == ColorStyle::class.java
            }?.second as? ColorStyle)?.foregroundColor ?: Color.BLACK)
            binding.background.setBackgroundColor((spans.firstOrNull {
                it.first == BackgroundStyle::class.java
            }?.second as? BackgroundStyle)?.backgroundColor ?: Color.WHITE)
        }
    }

    private fun colorStateList(b: Boolean) =
        if (b) ColorStateList.valueOf(Color.RED) else ColorStateList.valueOf(
            Color.BLACK
        )
}