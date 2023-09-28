package com.storyteller_f.fu

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Layout
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.storyteller_f.rich_text_edit.AlignmentStyle
import com.storyteller_f.rich_text_edit.BoldStyle
import com.storyteller_f.rich_text_edit.HeadlineStyle
import com.storyteller_f.rich_text_edit.ItalicStyle
import com.storyteller_f.rich_text_edit.QuotaStyle
import com.storyteller_f.rich_text_edit.RichEditText
import com.storyteller_f.rich_text_edit.StrikethroughStyle
import com.storyteller_f.rich_text_edit.UnderlineStyle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val text = findViewById<RichEditText>(R.id.text)
        val bold = findViewById<ImageButton>(R.id.bold)
        val italic = findViewById<ImageButton>(R.id.italic)
        val underline = findViewById<ImageButton>(R.id.underline)
        val strike = findViewById<ImageButton>(R.id.strike)
        val h1 = findViewById<Button>(R.id.h1)
        val h2 = findViewById<Button>(R.id.h2)
        val h3 = findViewById<Button>(R.id.h3)
        val h4 = findViewById<Button>(R.id.h4)
        val h5 = findViewById<Button>(R.id.h5)
        val quota = findViewById<ImageButton>(R.id.quota)
        val alignRight = findViewById<ImageButton>(R.id.align_right)
        val alignCenter = findViewById<ImageButton>(R.id.align_center)
        val alignLeft = findViewById<ImageButton>(R.id.align_left)
        bold.setOnClickListener {
            text.toggle(BoldStyle::class.java)
        }
        italic.setOnClickListener {
            text.toggle(ItalicStyle::class.java)
        }
        underline.setOnClickListener {
            text.toggle(UnderlineStyle::class.java)
        }
        strike.setOnClickListener {
            text.toggle(StrikethroughStyle::class.java)
        }
        quota.setOnClickListener {
            text.toggle(QuotaStyle::class.java)
        }
        alignRight.setOnClickListener {
            text.toggle(AlignmentStyle::class.java) {
                AlignmentStyle(Layout.Alignment.ALIGN_OPPOSITE)
            }
        }
        alignCenter.setOnClickListener {
            text.toggle(AlignmentStyle::class.java) {
                AlignmentStyle(Layout.Alignment.ALIGN_CENTER)
            }
        }
        alignLeft.setOnClickListener {
            text.toggle(AlignmentStyle::class.java) {
                AlignmentStyle(Layout.Alignment.ALIGN_NORMAL)
            }
        }
        val headlineTextViews = listOf(h1, h2, h3, h4, h5)
        headlineTextViews.forEachIndexed { index, button ->
            button.setOnClickListener {
                text.toggle(HeadlineStyle::class.java) {
                    HeadlineStyle(index + 1)
                }
            }
        }
        text.cursorStyle.observe(this) { spans ->
            underline.imageTintList = colorStateList(spans.any {
                it.first == UnderlineStyle::class.java
            })
            bold.imageTintList = colorStateList(spans.any {
                it.first == BoldStyle::class.java
            })
            italic.imageTintList = colorStateList(spans.any {
                it.first == ItalicStyle::class.java
            })
            strike.imageTintList = colorStateList(spans.any {
                it.first == StrikethroughStyle::class.java
            })
            val allHeadline = spans.filter {
                it.first == HeadlineStyle::class.java
            }.map {
                it.second
            }.filterIsInstance<HeadlineStyle>()
            headlineTextViews.forEachIndexed { index, button ->
                button.setTextColor(colorStateList(allHeadline.any {
                    it.head == index + 1
                }))
            }
        }
    }

    private fun colorStateList(b: Boolean) =
        if (b) ColorStateList.valueOf(Color.RED) else ColorStateList.valueOf(
            Color.BLACK
        )
}