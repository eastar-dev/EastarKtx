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

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.AnyRes
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

typealias KtxContext = Unit

val Context.notificationManager: NotificationManager? get() = getSystemService()
val Context.clipboardManager: ClipboardManager? get() = getSystemService()
val Context.telephonyManager: TelephonyManager? get() = getSystemService()
val Context.activityManager: ActivityManager? get() = getSystemService()

val Context.appName get() = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
val Context.versionName: String get() = packageManager.getPackageInfo(packageName, 0).versionName
val Context.versionCode get() = versionCode(packageName)
fun Context.getAppName(packageName: String) = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
fun Context.isInstall(packageName: String) = versionCode(packageName) > 0L
fun Context.versionCode(packageName: String) = runCatching { PackageInfoCompat.getLongVersionCode(packageManager.getPackageInfo(packageName, 0)) }.getOrDefault(-1L)

@Suppress("DEPRECATION")
fun Context.getInstallerPackageName(packageName: String): String? = if (VERSION.SDK_INT >= VERSION_CODES.R) packageManager.getInstallSourceInfo(packageName).installingPackageName else packageManager.getInstallerPackageName(packageName)

infix fun Context.copy(text: String) = clipboardManager?.setPrimaryClip(ClipData.newPlainText("label", text))

fun Application.registerActivityStartedLifecycleCallbacks(callback: Activity.() -> Unit) =
    registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
        override fun onActivityStarted(activity: Activity) {
            callback(activity)
        }

        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    })


fun Activity.hideKeyboard() = window.decorView.hideKeyboard()
fun Fragment.hideKeyboard() = requireActivity().hideKeyboard()
fun View.hideKeyboard() = context.getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(windowToken, 0)

fun Activity.showKeyboard() = window.decorView.showKeyboard()
fun Fragment.showKeyboard() = requireActivity().showKeyboard()
fun View.showKeyboard() = context.getSystemService<InputMethodManager>()?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)


val Context.isDeviceLock: Boolean
    get() = getSystemService<KeyguardManager>()?.run {
        if (VERSION.SDK_INT >= VERSION_CODES.M) isDeviceSecure else isKeyguardSecure
    } ?: false

val Context.line1Number: String
    @SuppressLint("MissingPermission", "HardwareIds")
    get() {
        when {
            VERSION.SDK_INT >= VERSION_CODES.O -> arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE)
            else -> arrayOf(Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE)
        }.any {
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this@line1Number, it)
        }.takeIf {
            it
        } ?: return ""
        return telephonyManager?.line1Number ?: ""
    }

fun Context.permissionsAll(vararg permissions: String): Boolean = permissions.all {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

fun Context.permissionsAny(vararg permissions: String): Boolean = permissions.any {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

/** SKT("45005"), LG("45006"), KT("45008") */
val Context.networkOperator: String get() = telephonyManager?.networkOperator ?: ""

val Context.networkOperatorName: String
    get() = runCatching {
        when (telephonyManager?.networkOperator) {
            "45005" -> "SKT"
            "45006" -> "LGT"
            "45008" -> "KT"
            else -> telephonyManager?.networkOperatorName.urlEncodeEuckr
        }
    }.getOrDefault("")

infix fun Context.toast(text: CharSequence) = Toast.makeText(asContext, text.toCharSequence(asContext), Toast.LENGTH_SHORT).show()
infix fun Fragment.toast(text: CharSequence) = Toast.makeText(asContext, text.toCharSequence(asContext), Toast.LENGTH_SHORT).show()
infix fun View.toast(text: CharSequence) = Toast.makeText(asContext, text.toCharSequence(asContext), Toast.LENGTH_SHORT).show()
infix fun Context.toast(@StringRes text: Int) = Toast.makeText(asContext, text.toCharSequence(asContext), Toast.LENGTH_SHORT).show()
infix fun Fragment.toast(@StringRes text: Int) = Toast.makeText(asContext, text.toCharSequence(asContext), Toast.LENGTH_SHORT).show()
infix fun View.toast(@StringRes text: Int) = Toast.makeText(asContext, text.toCharSequence(asContext), Toast.LENGTH_SHORT).show()

private val Context.asContext: Context get() = this
private val Fragment.asContext: Context get() = requireContext()
private val View.asContext: Context get() = context


private fun <T> T.toCharSequence(context: Context): CharSequence? = when (this) {
    is Int -> context.getString(this)
    is CharSequence -> this
    else -> null
}

fun Context.getRawString(@RawRes rawResId: Int) = resources.openRawResource(rawResId).text
fun Fragment.getRawString(@RawRes rawResId: Int) = resources.openRawResource(rawResId).text
fun Context.getDrawableId(drawable_name: String): Int = getResId(drawable_name, "drawable", asContext.packageName)
fun Fragment.getDrawableId(drawable_name: String): Int = getResId(drawable_name, "drawable", asContext.packageName)
fun Context.getResId(name: String, defType: String, defPackage: String): Int = resources.getIdentifier(name, defType, defPackage)
fun Fragment.getResId(name: String, defType: String, defPackage: String): Int = resources.getIdentifier(name, defType, defPackage)
fun Context.getResourceEntryName(@AnyRes resId: Int): String = resources.getResourceEntryName(resId)
fun Fragment.getResourceEntryName(@AnyRes resId: Int): String = resources.getResourceEntryName(resId)

fun Activity.keepScreenOn() = window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
fun Activity.keepScreenOff() = window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

fun Context.getThemeString(attr_id: Int): CharSequence {
    val outValue = TypedValue()
    theme.resolveAttribute(attr_id, outValue, true)
    return outValue.string
}


//-------------------------------------------------------------------------------------
fun Application.lifecycleObserver() = ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeground() {
        android.util.Log.e("lifecycleObserver", "onForeground")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onBackground() {
        android.util.Log.w("lifecycleObserver", "onBackground")
    }
})

//-------------------------------------------------------------------------------------
val Context.isForeground1: Boolean
    get() = activityManager?.runningAppProcesses?.any {
        it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    } ?: false


@SuppressLint("MissingPermission")
@RequiresApi(VERSION_CODES.LOLLIPOP)
fun Context.registerNetworkCallback() {
    val connectivityManager: ConnectivityManager? = getSystemService()
    connectivityManager?.registerNetworkCallback(NetworkRequest.Builder().build(), object : NetworkCallback() {
        override fun onLost(network: Network) {
            android.util.Log.e("registerNetworkCallback", "Network onLost")
        }

        override fun onAvailable(network: Network) {
            android.util.Log.e("registerNetworkCallback", "Network onAvailable")
        }
    })
}

val Context.widthPixels: Int get() = resources.displayMetrics.widthPixels
val Context.heightPixels: Int get() = resources.displayMetrics.heightPixels

//알림설정이동
fun Context.startNotificationSetting() {
    val intent = Intent()
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        else -> {
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", packageName)
            intent.putExtra("app_uid", applicationInfo?.uid)
        }
    }
    startActivity(intent)
}

/**
//@formatter:off
override fun onResume() {
    super.onResume()
    bb.msg.getClipboardText {
        val pasteText = it?.replace("\\D".toRegex(), "")
        if (!pasteText.isNullOrBlank()) {
            val text = SpannableString("붙여넣기\n$pasteText")
            text.setSpan(ForegroundColorSpan(0xffeade57.toInt()), text.lastIndexOf(pasteText), text.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            text.setSpan(RelativeSizeSpan(1.2F), text.lastIndexOf(pasteText), text.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            bb.msg.text = text
            bb.msg.setOnClickListener { ... }
        }
    }
}
//@formatter:on
 */
fun View.getClipboardText(callback: (CharSequence?) -> Unit) {
    requestFocus()
    viewTreeObserver.addOnWindowFocusChangeListener(object : ViewTreeObserver.OnWindowFocusChangeListener {
        override fun onWindowFocusChanged(hasFocus: Boolean) {
            val pasteText = context.getSystemService<ClipboardManager>()?.primaryClip?.getItemAt(0)?.text
            callback(pasteText)
            viewTreeObserver.removeOnWindowFocusChangeListener(this)
        }
    })
}