package com.ulesson.chat.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.ulesson.chat.R

open class CustomFontTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        typeface = readTypeFace(context, attrs)
    }
}

class CustomFontButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {
    init {
        typeface = readTypeFace(context, attrs)
    }

}

private fun readTypeFace(context: Context, attrs: AttributeSet?): Typeface? {
    attrs ?: return null
    val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CustomFont, 0, 0)
    return try {
        Typeface.createFromAsset(context.assets, "fonts/MuliRegular.ttf")
        val fontName = a.getString(R.styleable.CustomFont_fontName) ?: return null
        Typeface.createFromAsset(context.assets, "fonts/$fontName.ttf")
    } catch (e: Exception) {
        null
    } finally {
        a?.recycle()
    }

}
