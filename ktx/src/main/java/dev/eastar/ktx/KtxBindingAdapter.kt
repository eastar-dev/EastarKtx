package dev.eastar.ktx


import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter

typealias KtxBindingAdapter = Unit

@BindingAdapter("gone")
fun View.setGone(@NonNull gone: Boolean) {
    isGone = gone
}

@BindingAdapter("visible")
fun View.setVisible(@NonNull visible: Boolean) {
    isVisible = visible
}

@BindingAdapter("level")
fun ImageView.setLevel(@NonNull level: Int) {
    setImageLevel(level)
}

@BindingAdapter("select")
fun View.isSelect(@NonNull isSelect: Boolean) {
    isSelected = isSelect
}

@BindingAdapter("underline")
fun TextView.setUnderline(@NonNull b: Boolean) {
    text = if (b)
        SpannableString(text).apply { setSpan(UnderlineSpan(), 0, text.length, 0) }
    else
        text.toString()
}
