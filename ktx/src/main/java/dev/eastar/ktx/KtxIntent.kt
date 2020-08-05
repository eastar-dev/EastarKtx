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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment


//@Suppress("UNCHECKED_CAST")
//private fun Array<*>.toPair(): Array<kotlin.Pair<String, Any?>> {
//    require(count() % 2 != 1) { "!!key value must pair" }
//    return toList().zipWithNext { a, b ->
//        (a to b)
//    }.filterIndexed { index, _ ->
//        index % 2 == 0
//    }.map {
//        it as kotlin.Pair<String, Any?>
//    }.toTypedArray()
//}

fun View.setIntent(clz: Class<out Activity>, vararg extras: Pair<String, Any?>) = setOnClickListener { it.context.startActivity(clz, *extras) }
fun View.setIntent(intent: Intent) = setOnClickListener { it.context.startActivity(intent) }

fun String?.toIntent(): Intent? = kotlin.runCatching { Intent.parseUri(this, Intent.URI_INTENT_SCHEME) }.getOrNull()
fun Intent?.toText(): String? = kotlin.runCatching { this?.toUri(Intent.URI_INTENT_SCHEME) }.getOrNull()

fun Context.toIntent(clz: Class<out Activity>, vararg extras: Pair<String, Any?>): Intent = Intent(this, clz).putExtras(bundleOf(*extras))
fun Fragment.toIntent(clz: Class<out Activity>, vararg extras: Pair<String, Any?>): Intent = Intent(requireContext(), clz).putExtras(bundleOf(*extras))

val Context.mainIntent get() = packageManager.getLaunchIntentForPackage(packageName)!!.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
val Fragment.mainIntent get() = requireContext().packageManager.getLaunchIntentForPackage(requireContext().packageName)!!.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

//-------------------------------------------------------------------------------------
fun Fragment.startActivity(text: String?) = kotlin.runCatching { startActivity(text.toIntent()) }.getOrDefault(Unit)
fun Context.startActivity(text: String?) = kotlin.runCatching { startActivity(text.toIntent()) }.getOrDefault(Unit)
fun Fragment.startActivity(clz: Class<out Activity>, vararg extras: Pair<String, Any?>) = startActivity(toIntent(clz, *extras))
fun Context.startActivity(clz: Class<out Activity>, vararg extras: Pair<String, Any?>) = startActivity(toIntent(clz, *extras))
fun Activity.startActivityForResult(clz: Class<out Activity>, requestCode: Int = -1, vararg extras: Pair<String, Any?>) = startActivityForResult(toIntent(clz, *extras), requestCode)
fun Fragment.startActivityForResult(clz: Class<out Activity>, requestCode: Int = -1, vararg extras: Pair<String, Any?>) = startActivityForResult(toIntent(clz, *extras), requestCode)
fun Context.startPackage(packageName: String) = kotlin.runCatching { startActivity(packageManager.getLaunchIntentForPackage(packageName)) }.getOrDefault(Unit)
fun Fragment.startPackage(packageName: String) = kotlin.runCatching { startActivity(requireContext().packageManager.getLaunchIntentForPackage(packageName)) }.getOrDefault(Unit)

fun Context.startMain() = startActivity(mainIntent)
fun Fragment.startMain() = startActivity(mainIntent)
fun Context.startSetting() = startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
fun Fragment.startSetting() = startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${requireContext().packageName}")).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

fun Context.startMarket() = startMarket(packageName)
fun Fragment.startMarket() = startMarket(requireContext().packageName)
fun Context.startMarket(packageName: String) = kotlin.runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")!!)) }.getOrDefault(Unit)
fun Fragment.startMarket(packageName: String) = runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")!!)) }.getOrDefault(Unit)
fun Fragment.startActivityOrMarket(text: String?) = startActivityOrMarket(text.toIntent())
fun Fragment.startActivityOrMarket(intent: Intent?) = kotlin.runCatching { startActivity(intent) }.getOrElse { intent?.`package`?.takeUnless { it.isBlank() }?.let { startMarket(it) } }
fun Context.startActivityOrMarket(text: String?) = startActivityOrMarket(text.toIntent())
fun Context.startActivityOrMarket(intent: Intent?) = kotlin.runCatching { startActivity(intent) }.getOrElse { intent?.`package`?.takeUnless { it.isBlank() }?.let { startMarket(it) } }
