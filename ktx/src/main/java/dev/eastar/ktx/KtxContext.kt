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
import android.content.*
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.AnyRes
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

class KKContext

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

fun View.setIntent(clz: Class<out Activity>, vararg extras: Pair<String, Any?>) = setOnClickListener { it.context.startActivity(clz, *extras) }
fun View.setIntent(intent: Intent) = setOnClickListener { it.context.startActivity(intent) }

fun String?.toIntent(): Intent? = kotlin.runCatching { Intent.parseUri(this, Intent.URI_INTENT_SCHEME) }.getOrNull()
fun Intent?.toText(): String? = kotlin.runCatching { this?.toUri(Intent.URI_INTENT_SCHEME) }.getOrNull()
fun Context.toIntent(clz: Class<out Activity>, vararg extras: Pair<String, Any?>): Intent = Intent(this, clz).putExtras(bundleOf(*extras))
fun Fragment.toIntent(clz: Class<out Activity>, vararg extras: Pair<String, Any?>): Intent = Intent(requireContext(), clz).putExtras(bundleOf(*extras))

fun Context.startSetting() = startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
fun Fragment.startSetting() = startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireContext().packageName}")).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

fun Context.startMain() = startActivity(mainIntent)
fun Fragment.startMain() = startActivity(mainIntent)

val Context.mainIntent get() = packageManager.getLaunchIntentForPackage(packageName)!!.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
val Fragment.mainIntent get() = requireContext().packageManager.getLaunchIntentForPackage(requireContext().packageName)!!.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

fun Context.startPackage(packageName: String) {
    kotlin.runCatching { startActivity(packageManager.getLaunchIntentForPackage(packageName)) }
}

fun Fragment.startPackage(packageName: String) {
    kotlin.runCatching { startActivity(requireContext().packageManager.getLaunchIntentForPackage(packageName)) }
}

fun Context.startMarket() = startMarket(packageName)
fun Fragment.startMarket() = startMarket(requireContext().packageName)

fun Context.startMarket(packageName: String) {
    kotlin.runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")!!))
    }.recoverCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
    }
}

fun Fragment.startMarket(packageName: String) {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")!!))
    }.recoverCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
    }
}

//-------------------------------------------------------------------------------------
fun Fragment.startActivity(text: String?) {
    kotlin.runCatching { startActivity(text.toIntent()) }
}

fun Context.startActivity(text: String?) {
    kotlin.runCatching { startActivity(text.toIntent()) }
}

fun Fragment.startActivity(clz: Class<out Activity>, vararg extras: Pair<String, Any?>) = startActivity(toIntent(clz, *extras))
fun Context.startActivity(clz: Class<out Activity>, vararg extras: Pair<String, Any?>) = startActivity(toIntent(clz, *extras))

fun Fragment.startActivityOrMarket(text: String?) = startActivityOrMarket(text.toIntent())
fun Fragment.startActivityOrMarket(intent: Intent?) = kotlin.runCatching { startActivity(intent) }.getOrElse { intent?.`package`?.takeUnless { it.isBlank() }?.let { startMarket(it) } }
fun Context.startActivityOrMarket(text: String?) = startActivityOrMarket(text.toIntent())
fun Context.startActivityOrMarket(intent: Intent?) = kotlin.runCatching { startActivity(intent) }.getOrElse { intent?.`package`?.takeUnless { it.isBlank() }?.let { startMarket(it) } }

//-------------------------------------------------------------------------------------

//-------------------------------------------------------------------------------------

fun Activity.startActivityForResult(clz: Class<out Activity>, requestCode: Int = -1, vararg extras: Pair<String, Any?>) {
    startActivityForResult(toIntent(clz, *extras), requestCode)
}

fun Fragment.startActivityForResult(clz: Class<out Activity>, requestCode: Int = -1, vararg extras: Pair<String, Any?>) {
    startActivityForResult(toIntent(clz, *extras), requestCode)
}

//-------------------------------------------------------------------------------------
fun Application.lifecycleObserver() = ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeground() {
        android.util.Log.e("lifecycleObserver", "onForeground")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onBackground() {
        android.util.Log.w("lifecycleObserver", "onForeground")
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
