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

import android.annotation.SuppressLint
import android.app.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.AnyRes
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

val Context.notificationManager: NotificationManager? get() = getSystemService()
val Context.clipboardManager: ClipboardManager? get() = getSystemService()
val Context.telephonyManager: TelephonyManager? get() = getSystemService()
val Context.activityManager: ActivityManager? get() = getSystemService()

val Context.appName: CharSequence get() = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
val Context.versionName: String get() = packageManager.getPackageInfo(packageName, 0).versionName
val Context.versionCode get() = versionCode(packageName)
fun Context.isInstall(packageName: String) = versionCode(packageName) > 0L
fun Context.versionCode(packageName: String) = runCatching { PackageInfoCompat.getLongVersionCode(packageManager.getPackageInfo(packageName, 0)) }.getOrDefault(-1L)

infix fun Context.copy(text: String) {
    val clip = ClipData.newPlainText("label", text)
    clipboardManager?.run {
        setPrimaryClip(clip)
        toast(clip.getItemAt(0).text.toString() + " 복사되었습니다.")
    }
}


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) isDeviceSecure else isKeyguardSecure
    } ?: false

val Context.line1Number: String
    @SuppressLint("MissingPermission", "HardwareIds")
    get() = telephonyManager?.line1Number ?: ""

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

infix fun Context.toast(text: CharSequence) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()
infix fun Fragment.toast(text: CharSequence) = Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
infix fun Context.toast(@StringRes textRes: Int) = toast(getString(textRes))
infix fun Fragment.toast(@StringRes textRes: Int) = toast(getString(textRes))

fun Context.getRawString(@RawRes rawResId: Int) = resources.openRawResource(rawResId).text
fun Context.getDrawableId(drawable_name: String): Int = getResId(drawable_name, "drawable", packageName)
fun Context.getResId(name: String, defType: String, defPackage: String): Int = resources.getIdentifier(name, defType, defPackage)
fun Context.getResourceEntryName(@AnyRes resId: Int): String = resources.getResourceEntryName(resId)
fun Fragment.getRawString(@RawRes rawResId: Int) = resources.openRawResource(rawResId).text
fun Fragment.getDrawableId(drawable_name: String): Int = getResId(drawable_name, "drawable", requireContext().packageName)
fun Fragment.getResId(name: String, defType: String, defPackage: String): Int = resources.getIdentifier(name, defType, defPackage)
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
val Context.isForeground2: Boolean get() = topActivity?.packageName == packageName
val Context.topActivity: ComponentName? get() = activityManager?.getRunningTasks(1)?.firstOrNull()?.topActivity

@SuppressLint("MissingPermission")
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager: ConnectivityManager? = getSystemService()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val capabilities = connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        capabilities ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> true
            else -> false
        }
    } else {
        connectivityManager?.activeNetworkInfo?.isConnected ?: false
    }
}

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
