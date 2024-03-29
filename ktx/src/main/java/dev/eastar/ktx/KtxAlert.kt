/*
 * Copyright 2020 copyright eastar Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("unused")

package dev.eastar.ktx

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment

typealias KtxAlert = Unit

/** alert("message") { positiveButton("OK") { ... } } */
interface IOnAlertBuilder {
    fun onCreateAlertBuilder(): Builder
}

//application
/** newBuilder = object : NewBuilder { override fun invoke(context: Context): AlertDialog.Builder = AlertDialog.Builder(context) } */
typealias NewBuilder = (context: Context) -> Builder

var newBuilder: NewBuilder? = null

//@formatter:off
@JvmOverloads fun AppCompatActivity.alert(           message: CharSequence,            title: CharSequence? = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun AppCompatActivity.alert(@StringRes message: Int         , @StringRes title: Int?          = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun Fragment         .alert(           message: CharSequence,            title: CharSequence? = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun Fragment         .alert(@StringRes message: Int         , @StringRes title: Int?          = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun View             .alert(           message: CharSequence,            title: CharSequence? = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun View             .alert(@StringRes message: Int         , @StringRes title: Int?          = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun IOnAlertBuilder  .alert(           message: CharSequence,            title: CharSequence? = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun IOnAlertBuilder  .alert(@StringRes message: Int         , @StringRes title: Int?          = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun Builder          .alert(           message: CharSequence,            title: CharSequence? = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun Builder          .alert(@StringRes message: Int         , @StringRes title: Int?          = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)

@JvmOverloads fun Builder.onPositive (           text: CharSequence, cb: ((Int) -> Unit)? = null): Builder = setPositiveButton(text) { _, which -> cb?.invoke(which) }
@JvmOverloads fun Builder.onNegative (           text: CharSequence, cb: ((Int) -> Unit)? = null): Builder = setNegativeButton(text) { _, which -> cb?.invoke(which) }
@JvmOverloads fun Builder.onNeutral  (           text: CharSequence, cb: ((Int) -> Unit)? = null): Builder = setNeutralButton (text) { _, which -> cb?.invoke(which) }
@JvmOverloads fun Builder.onPositive (@StringRes text: Int         , cb: ((Int) -> Unit)? = null): Builder = setPositiveButton(text) { _, which -> cb?.invoke(which) }
@JvmOverloads fun Builder.onNegative (@StringRes text: Int         , cb: ((Int) -> Unit)? = null): Builder = setNegativeButton(text) { _, which -> cb?.invoke(which) }
@JvmOverloads fun Builder.onNeutral  (@StringRes text: Int         , cb: ((Int) -> Unit)? = null): Builder = setNeutralButton (text) { _, which -> cb?.invoke(which) }
//@formatter:on
fun Builder.onDismiss(cb: (AlertDialog) -> Unit): Builder = setOnDismissListener { dialog -> cb(dialog as AlertDialog) }
val Builder.unCancelable: Builder get() = setCancelable(false)
val Builder.onDismissFinish: Builder get() = onDismiss { (context as? AppCompatActivity)?.finish() }

/////////////////////////////////////////////////////////////////////////////////
private fun <CLZ, MSG, TIT> CLZ.showAlertDialog(message: MSG, title: TIT, block: Builder.() -> Unit): AlertDialog {
    return createBuilder().apply {
        setMessage(message?.toCharSequence(context))
        setTitle(title?.toCharSequence(context))
        block()
    }.create().apply {
        if (NoMore.getNoMore(this.context, message.toString()))
            return@apply
        setCanceledOnTouchOutside(false)
        show()
    }
}

private fun <T> T.createBuilder(): androidx.appcompat.app.AlertDialog.Builder {
    return when (this) {
        is androidx.appcompat.app.AlertDialog.Builder -> this
        is IOnAlertBuilder -> (asContext as IOnAlertBuilder).onCreateAlertBuilder()
        newBuilder != null -> newBuilder!!.invoke(asContext)
        else -> Builder(asContext)
    }
}

private val <T> T.asContext: Context
    get() = when (this) {
        is Dialog -> context
        is Context -> this
        is Fragment -> requireActivity()
        is View -> context
        is IOnAlertBuilder -> onCreateAlertBuilder().context
        else -> throw IllegalAccessException()
    }

private fun <T> T.toCharSequence(context: Context): CharSequence? = when (this) {
    is Int -> {
        if (this <= 0) null
        else context.getString(this)
    }
    is CharSequence -> this
    else -> null
}

object NoMore {
    private const val NAME = "NoMoreSharedPreferences"

    class OnNoMoreClickListener(private val key: Any) : DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface?, which: Int) {
            val context = dialog.asContext
            val key = key.toCharSequence(context) ?: return
            setNoMore(context, key.toString())
        }
    }

    //다시보지않기를 눌렀는지 확인
    fun getNoMore(context: Context, key: String): Boolean {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getBoolean(key.md5, false)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setNoMore(context: Context, key: String) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit(true) {
                putBoolean(key.md5, true)
            }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit(true) {
                clear()
            }
    }
}
