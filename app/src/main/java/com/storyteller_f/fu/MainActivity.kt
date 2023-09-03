package com.storyteller_f.fu

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.style.QuoteSpan
import android.widget.Button
import android.widget.ImageButton
import com.storyteller_f.rich_text_edit.BoldStyle
import com.storyteller_f.rich_text_edit.ItalicStyle
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
        val i = findViewById<Button>(R.id.h1)
        val quota = findViewById<ImageButton>(R.id.quota)
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
            val quoteSpan = QuoteSpan(Color.CYAN)
            text.editableText.setSpan(quoteSpan, text.selectionStart, text.selectionEnd, 0)
        }
    }
}