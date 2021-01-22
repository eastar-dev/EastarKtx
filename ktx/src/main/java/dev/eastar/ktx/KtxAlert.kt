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
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment

typealias KtxAlert = Unit

/**
 * Example:
 * <pre><code>
 * fun Fragment.test() {
 *   alert("message") {
 *     negativeButton("")
 *     negativeButton(android.R.string.yes) { startActivity(Intent()) }
 *     positiveButton("") { startActivity(Intent()) }
 *     neutralButton("") { activity.finish() }
 *     unCancelable
 *     setOnDismissListener {
 *         activity.finish()
 *     }
 *   }
 * }
 * </code></pre>
 */
interface IOnAlertBuilder {
    fun onCreateAlertBuilder(): Builder
}

//@formatter:off
@JvmOverloads fun AppCompatActivity.alert(           message: CharSequence,            title: CharSequence? = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun Fragment         .alert(           message: CharSequence,            title: CharSequence? = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun View             .alert(           message: CharSequence,            title: CharSequence? = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun IOnAlertBuilder  .alert(           message: CharSequence,            title: CharSequence? = null , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun AppCompatActivity.alert(@StringRes message: Int         , @StringRes title: Int = -1             , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun Fragment         .alert(@StringRes message: Int         , @StringRes title: Int = -1             , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun View             .alert(@StringRes message: Int         , @StringRes title: Int = -1             , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)
@JvmOverloads fun IOnAlertBuilder  .alert(@StringRes message: Int         , @StringRes title: Int = -1             , block: Builder.() -> Unit): AlertDialog = showAlertDialog(message, title, block)

@JvmOverloads fun Builder.positiveButton (           text: CharSequence, cb: ((Int) -> Unit)? = null): Builder = setPositiveButton(text) { _, which -> cb?.invoke(which) }
@JvmOverloads fun Builder.negativeButton (           text: CharSequence, cb: ((Int) -> Unit)? = null): Builder = setNegativeButton(text) { _, which -> cb?.invoke(which) }
@JvmOverloads fun Builder.neutralButton  (           text: CharSequence, cb: ((Int) -> Unit)? = null): Builder = setNeutralButton (text) { _, which -> cb?.invoke(which) }
@JvmOverloads fun Builder.positiveButton (@StringRes text: Int         , cb: ((Int) -> Unit)? = null): Builder = setPositiveButton(text) { _, which -> cb?.invoke(which) }
@JvmOverloads fun Builder.negativeButton (@StringRes text: Int         , cb: ((Int) -> Unit)? = null): Builder = setNegativeButton(text) { _, which -> cb?.invoke(which) }
@JvmOverloads fun Builder.neutralButton  (@StringRes text: Int         , cb: ((Int) -> Unit)? = null): Builder = setNeutralButton (text) { _, which -> cb?.invoke(which) }
//@formatter:on
fun Builder.onDismiss(cb: (AlertDialog) -> Unit): Builder = setOnDismissListener { dialog -> cb(dialog as AlertDialog) }
val Builder.unCancelable: Builder get() = setCancelable(false)
val Builder.onDismissFinish: Builder get() = onDismiss { (context as? AppCompatActivity)?.finish() }
//val Builder.onDismissExit: Builder get() = setOnDismissListener { }

/////////////////////////////////////////////////////////////////////////////////
private fun <CLZ, MSG, TIT> CLZ.showAlertDialog(message: MSG, title: TIT, block: Builder.() -> Unit): AlertDialog =
    createBuilder().apply {
        setMessage(message?.toCharSequence(context))
        setTitle(title?.toCharSequence(context))
        block()
    }.create().apply {
        setCanceledOnTouchOutside(false)
        show()
    }

private fun <T> T.createBuilder(): Builder = when (this) {
    is IOnAlertBuilder -> onCreateAlertBuilder()
    is Context -> Builder(this)
    is Fragment -> Builder(requireContext())
    is View -> Builder(context)
    else -> throw IllegalAccessException()
}

private fun <T> T.toCharSequence(context: Context): CharSequence? = when (this) {
    is Int -> context.getString(this)
    is CharSequence -> this
    else -> null
}

object NoMore {
    private const val NAME = "NoMoreSharedPreferences"

    val onNoMoreClickListener = DialogInterface.OnClickListener { dlg, _ ->
        val key = (dlg as Dialog).findViewById<TextView>(android.R.id.message).text.toString()
        setNoMore(dlg.context, key)
    }

    //다시보지않기를 눌렀는지 확인
    fun getNoMore(context: Context, key: String): Long = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        .getLong(key.md5, 0L)

    @Suppress("MemberVisibilityCanBePrivate")
    fun setNoMore(context: Context, key: String) = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        .edit(true) { putLong(key.md5, System.currentTimeMillis()) }

    fun clear(context: Context) = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit(true) { clear() }
}

