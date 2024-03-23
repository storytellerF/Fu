import android.text.Layout
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.storyteller_f.rich_text_edit.AlignmentStyle
import com.storyteller_f.rich_text_edit.FuEditText
import com.storyteller_f.rich_text_edit.HeadlineStyle
import com.storyteller_f.rich_text_edit.MultiValueStyle
import java.io.Serializable

fun FuEditText.getStyleState(
    clazz: Serializable
) = cursorStyle.map { spans ->
    spans.any { (spanClass) ->
        clazz == spanClass
    }
}

fun FuEditText.getAlignmentStyleState(
    alignment: Layout.Alignment
) = cursorStyle.map { spans ->
    spans.any { (_, span) ->
        span is AlignmentStyle && alignment == span.alignment
    }
}

fun FuEditText.getHeadlineStyleState(
    headlineValue: Int
): LiveData<Boolean> = cursorStyle.map { spans ->
    val allHeadline = spans.filter { (spanClass) ->
        spanClass == HeadlineStyle::class.java
    }.map { (_, span) ->
        span
    }.filterIsInstance<HeadlineStyle>()
    allHeadline.any {
        it.value == headlineValue
    }
}

fun<T> FuEditText.getStyleValueState(clazz: Class<out MultiValueStyle<Int>>): LiveData<T?> {
    return cursorStyle.map { spans ->
        @Suppress("UNCHECKED_CAST")
        (spans.firstOrNull { (spanClass) ->
            spanClass == clazz
        }?.second as? MultiValueStyle<*>)?.value as? T
    }
}