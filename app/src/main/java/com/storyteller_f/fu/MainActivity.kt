package com.storyteller_f.fu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    }
}