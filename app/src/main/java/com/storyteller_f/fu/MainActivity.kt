package com.storyteller_f.fu

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
        val quota = findViewById<ImageButton>(R.id.quota)
        val alignRight = findViewById<ImageButton>(R.id.align_right)
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
        h1.setOnClickListener {
            text.toggle(HeadlineStyle::class.java) {
                HeadlineStyle(1)
            }
        }
    }
}