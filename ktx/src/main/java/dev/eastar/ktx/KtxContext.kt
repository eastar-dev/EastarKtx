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
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.AnyRes
import androidx.annotation.RawRes
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

class KKContext

val Context.notificationManager: NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
val Context.clipboardManager: ClipboardManager get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
val Context.telephonyManager: TelephonyManager get() = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
val Context.activityManager: ActivityManager get() = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

val Context.appName: CharSequence get() = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
val Context.versionName: String get() = packageManager.getPackageInfo(packageName, 0).versionName
val Context.versionCode get() = versionCode(packageName)
fun Context.isInstall(packageName: String) = versionCode(packageName) > 0L
fun Context.versionCode(packageName: String) = runCatching { PackageInfoCompat.getLongVersionCode(packageManager.getPackageInfo(packageName, 0)) }.getOrDefault(-1L)

infix fun Context.toast(text: CharSequence) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()
fun Context.pendingIntent(intent_text: String?): PendingIntent? = runCatching { PendingIntent.getActivity(this, 0, intent_text.intent, PendingIntent.FLAG_UPDATE_CURRENT) }.getOrNull()
fun Context.getStringFromRaw(@RawRes resId: Int) = resources.openRawResource(resId).text
fun Context.toString(@RawRes raw_resId: Int): String = resources.openRawResource(raw_resId).text
fun Context.getDrawableId(drawable_name: String): Int = getResId(drawable_name, "drawable", packageName)
fun Context.getResId(name: String, defType: String, defPackage: String): Int = resources.getIdentifier(name, defType, defPackage)
fun Context.getResourceEntryName(@AnyRes resId: Int): String = resources.getResourceEntryName(resId)

infix fun Context.copy(text: String) {
    val clip = ClipData.newPlainText("label", text)
    clipboardManager.setPrimaryClip(clip)
    toast(clip.getItemAt(0).text.toString() + " 복사되었습니다.")
}

val Context.isDeviceLock: Boolean
    get() = (getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager)?.run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) isDeviceSecure else isKeyguardSecure
    } ?: false

val Context.line1Number: String
    @SuppressLint("MissingPermission", "HardwareIds")
    get() = telephonyManager.line1Number ?: ""

/** SKT("45005"), LG("45006"), KT("45008") */
val Context.networkOperator: String get() = telephonyManager.networkOperator ?: ""

val Context.networkOperatorName: String
    get() = runCatching {
        when (telephonyManager.networkOperator) {
            "45005" -> "SKT"
            "45006" -> "LGT"
            "45008" -> "KT"
            else -> telephonyManager.networkOperatorName.urlEncodeEuckr
        }
    }.getOrDefault("")

//-----------------------------------------------------------
val Context.isForeground1: Boolean
    get() = activityManager.runningAppProcesses?.any {
        it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    } ?: false

fun Activity.keepScreenOn() = window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
fun Activity.keepScreenOff() = window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

fun Context.getThemeString(attr_id: Int): CharSequence {
    val outValue = TypedValue()
    theme.resolveAttribute(attr_id, outValue, true)
    return outValue.string
}

fun Context.startSetting() = startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
fun Context.startMain() = runCatching { getMainIntent(packageName)?.let { startActivity(it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)) } }.getOrElse { }
fun Context.getMainIntent(): Intent? = getMainIntent(packageName)
fun Context.getMainIntent(packageName: String) = runCatching { packageManager.getLaunchIntentForPackage(packageName) }.getOrNull()
fun Context.startMarket() = startMarket(packageName)

//-------------------------------------------------------------------------------------
fun Fragment.startActivity(intent_text: String?) = runCatching { startActivity(intent_text.intent) }.getOrElse {}

fun <T : Activity> Fragment.startActivity(clz: Class<T>) = runCatching { startActivity(Intent(requireContext(), clz)) }.getOrElse {}

fun Fragment.startActivityOrMarket(intent_text: String?) = startActivityOrMarket(intent_text.intent)
fun Fragment.startActivityOrMarket(intent: Intent?) = runCatching { startActivity(intent) }.getOrElse { intent?.`package`?.takeUnless { it.isBlank() }?.let { startMarket(it) } }
fun Fragment.startMarket(packageName: String) {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")!!))
    }.recoverCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
    }.getOrElse { }
}

//-------------------------------------------------------------------------------------
fun Context.startActivity(intent_text: String?) = runCatching { startActivity(intent_text.intent) }.getOrElse { }

fun <T : Activity> Context.startActivity(clz: Class<T>) = runCatching { startActivity(Intent(this, clz)) }.getOrElse {}
fun Context.startActivityOrMarket(intent_text: String?) = startActivityOrMarket(intent_text.intent)
fun Context.startActivityOrMarket(intent: Intent?) = runCatching { startActivity(intent) }.getOrElse { intent?.`package`?.takeUnless { it.isBlank() }?.let { startMarket(it) } }
fun Context.startMarket(packageName: String) {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")!!))
    }.recoverCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
    }.getOrElse { }
}

//-------------------------------------------------------------------------------------
fun Context.getIntent(clz: Class<out Activity>, vararg extras: Pair<String, Any?>): Intent = Intent(this, clz).putExtras(bundleOf(*extras))

fun Fragment.getIntent(clz: Class<out Activity>, vararg extras: Pair<String, Any?>): Intent = requireContext().getIntent(clz, *extras)

//-------------------------------------------------------------------------------------
fun View.setIntent(clz: Class<out Activity>, vararg extras: Pair<String, Any?>) {
    return context.startActivity(context.getIntent(clz, *extras))
}

fun Activity.startActivity(clz: Class<out Activity>, vararg extras: Pair<String, Any?>) {
    startActivityForResult(getIntent(clz, *extras), -1)
}

fun Fragment.startActivity(clz: Class<out Activity>, vararg extras: Pair<String, Any?>) {
    startActivityForResult(getIntent(clz, *extras), -1)
}

fun Activity.setIntentForResult(clz: Class<out Activity>, requestCode: Int = -1, vararg extras: Pair<String, Any?>) {
    startActivityForResult(getIntent(clz, *extras), requestCode)
}

fun Fragment.setIntentForResult(clz: Class<out Activity>, requestCode: Int = -1, vararg extras: Pair<String, Any?>) {
    startActivityForResult(getIntent(clz, *extras), requestCode)
}

//-------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------

val Context.isForeground2: Boolean get() = topActivity?.packageName == packageName
val Context.topActivity: ComponentName? get() = activityManager.getRunningTasks(1)?.firstOrNull()?.topActivity

//<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
@SuppressLint("MissingPermission")
fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        capabilities?.run {
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
        }
    } else {
        runCatching {
            connectivityManager.activeNetworkInfo?.run {
                return isConnected
            }
        }.getOrDefault(false)
    }
    return false
}