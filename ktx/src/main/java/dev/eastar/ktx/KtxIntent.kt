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

typealias KtxIntent = Unit

inline fun <reified T : Activity> View.setActivity(vararg extras: Pair<String, Any?>) = setOnClickListener { it.context.startActivity(it.context.toIntent<T>(*extras)) }
inline fun <reified T : Activity> Fragment.setActivity(vararg extras: Pair<String, Any?>) = context?.let { startActivity(it.toIntent<T>(*extras)) }
inline fun <reified T : Activity> Context.setActivity(vararg extras: Pair<String, Any?>) = startActivity(toIntent<T>(*extras))

inline fun <reified T : Activity> Fragment.toIntent(vararg extras: Pair<String, Any?>): Intent = Intent(requireContext(), T::class.java).putExtras(bundleOf(*extras))
inline fun <reified T : Activity> Context.toIntent(vararg extras: Pair<String, Any?>): Intent = Intent(this, T::class.java).putExtras(bundleOf(*extras))


fun View.setActivity(intent: Intent?) = if (intent != null) setOnClickListener { it.context.startActivity(intent) } else setOnClickListener(null)
fun String?.toIntent(): Intent? = kotlin.runCatching { Intent.parseUri(this, Intent.URI_INTENT_SCHEME) }.getOrNull()
fun Intent?.toText(): String? = kotlin.runCatching { this?.toUri(Intent.URI_INTENT_SCHEME) }.getOrNull()
//-------------------------------------------------------------------------------------
fun Fragment.startActivity(text: String?) = startActivity(text.toIntent())
fun Context.startActivity(text: String?) = startActivity(text.toIntent())
fun Context.startPackage(packageName: String) = kotlin.runCatching { startActivity(packageManager.getLaunchIntentForPackage(packageName)) }.getOrDefault(Unit)
fun Fragment.startPackage(packageName: String) = kotlin.runCatching { startActivity(requireContext().packageManager.getLaunchIntentForPackage(packageName)) }.getOrDefault(Unit)
val Context.mainIntent get() = packageManager.getLaunchIntentForPackage(packageName)!!.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
val Fragment.mainIntent get() = requireContext().packageManager.getLaunchIntentForPackage(requireContext().packageName)!!.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
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
