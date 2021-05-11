package dev.eastar.ktx

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone

class HanaDialog : AlertDialog {
    constructor(context: Context) : super(context)
    constructor(context: Context, themeResId: Int) : super(context, themeResId)
    constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : super(context, cancelable, cancelListener)

    class Builder : AlertDialog.Builder {
        var title: CharSequence? = null
        var message: CharSequence? = null
        var positiveButtonText: CharSequence? = null
        var negativeButtonText: CharSequence? = null
        var checkBoxListener: CompoundButton.OnCheckedChangeListener? = null
        var checkBoxText: String? = null
        var messageHasHtml = false
        private var positiveButtonListener: DialogInterface.OnClickListener? = null
        private var negativeButtonListener: DialogInterface.OnClickListener? = null

        constructor(context: Context) : super(context)
        constructor(context: Context, themeResId: Int) : super(context, themeResId)

        override fun setMessage(messageId: Int): AlertDialog.Builder {
            message = context.getString(messageId)
            return this
        }

        override fun setMessage(message: CharSequence?): AlertDialog.Builder {
            this.message = message
            return this
        }

        override fun setPositiveButton(textId: Int, listener: DialogInterface.OnClickListener): AlertDialog.Builder {
            positiveButtonText = context.getText(textId)
            positiveButtonListener = listener
            return super.setPositiveButton(null, listener)
        }

        override fun setPositiveButton(text: CharSequence, listener: DialogInterface.OnClickListener): AlertDialog.Builder {
            positiveButtonText = text
            positiveButtonListener = listener
            return this
        }

        override fun setNegativeButton(textId: Int, listener: DialogInterface.OnClickListener): AlertDialog.Builder {
            negativeButtonText = context.getText(textId)
            negativeButtonListener = listener
            return this
        }

        override fun setNegativeButton(text: CharSequence, listener: DialogInterface.OnClickListener): AlertDialog.Builder {
            negativeButtonText = text
            negativeButtonListener = listener
            return this
        }

        override fun create(): AlertDialog {
            val view = LayoutInflater.from(context).inflate(R.layout.custom_dialog, null, false)
            setView(view)

            val dlg = super.create()

            val btnPositive = view.findViewById<Button>(R.id.button1)
            btnPositive.isGone = positiveButtonText.isNullOrEmpty()
            btnPositive.text = positiveButtonText
            btnPositive.setOnClickListener {
                positiveButtonListener?.onClick(dlg, BUTTON_POSITIVE)?.apply { dlg.dismiss() }

            }
            val btnNegative = view.findViewById<Button>(R.id.button2)
            btnNegative.isGone = negativeButtonText.isNullOrEmpty()
            btnNegative.text = negativeButtonText
            btnNegative.setOnClickListener {
                negativeButtonListener?.onClick(dlg, BUTTON_POSITIVE)?.apply { dlg.dismiss() }
            }
            return dlg
        }
    }
}