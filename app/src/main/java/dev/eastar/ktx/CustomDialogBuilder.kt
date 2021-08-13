package dev.eastar.ktx

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import dev.eastar.ktx.databinding.CustomDialogBinding

class CustomDialogBuilder @JvmOverloads constructor(
    context: Context,
    themeResId: Int = R.style.Theme_AppCompat_Dialog_Alert
) : AlertDialog.Builder(context, themeResId) {
    var title: CharSequence? = null
    var message: CharSequence? = null
    var positiveText: CharSequence? = null
    var negativeText: CharSequence? = null
    private var positiveListener: DialogInterface.OnClickListener? = null
    private var negativeListener: DialogInterface.OnClickListener? = null

    override fun setTitle(titleId: Int): AlertDialog.Builder {
        title = context.getString(titleId)
        return this
    }

    override fun setTitle(title: CharSequence?): AlertDialog.Builder {
        this.title = title
        return this
    }

    override fun setMessage(messageId: Int): AlertDialog.Builder {
        message = context.getString(messageId)
        return this
    }

    override fun setMessage(message: CharSequence?): AlertDialog.Builder {
        this.message = message
        return this
    }

    override fun setPositiveButton(textId: Int, listener: DialogInterface.OnClickListener): AlertDialog.Builder {
        positiveText = context.getText(textId)
        positiveListener = listener
        return this
    }

    override fun setPositiveButton(text: CharSequence, listener: DialogInterface.OnClickListener): AlertDialog.Builder {
        positiveText = text
        positiveListener = listener
        return this
    }

    override fun setNegativeButton(textId: Int, listener: DialogInterface.OnClickListener): AlertDialog.Builder {
        negativeText = context.getText(textId)
        negativeListener = listener
        return this
    }

    override fun setNegativeButton(text: CharSequence, listener: DialogInterface.OnClickListener): AlertDialog.Builder {
        negativeText = text
        negativeListener = listener
        return this
    }

    override fun create(): AlertDialog {
        val bb = CustomDialogBinding.inflate(LayoutInflater.from(context))
        setView(bb.root)
        val dlg = super.create()
        bb.title.isGone = title.isNullOrBlank()
        bb.title.text = title

        bb.close.isGone = title.isNullOrBlank()
        bb.close.setOnClickListener { dlg.cancel() }

        bb.message.text = message

        bb.positive.isGone = positiveText.isNullOrEmpty()
        bb.negative.isGone = negativeText.isNullOrEmpty()

        bb.positive.text = positiveText
        bb.negative.text = negativeText

        bb.positive.setOnClickListener { positiveListener?.onClick(dlg, BUTTON_POSITIVE)?.apply { dlg.dismiss() } }
        bb.negative.setOnClickListener { negativeListener?.onClick(dlg, BUTTON_NEGATIVE)?.apply { dlg.dismiss() } }
        return dlg
    }
}
