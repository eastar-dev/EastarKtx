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

import android.content.Intent
import android.content.res.Resources
import android.telephony.PhoneNumberUtils
import android.util.TypedValue
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.URISyntaxException
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class KKText

val Number.dpf: Float get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), Resources.getSystem().displayMetrics)
val Number.dp: Int get() = dpf.roundToInt()
val Number.i: Int get() = toInt()
val Number.d: Double get() = toDouble()

val Long.toTimeText: String get() = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(this)

val CharSequence?.intent: Intent?
    get() = try {
        Intent.parseUri(toString(), Intent.URI_INTENT_SCHEME)
    } catch (e: URISyntaxException) {
        null
    }

fun String?.isNotNullOrBlank() = !this.isNullOrBlank()
fun String?.isNotNullOrEmpty() = !this.isNullOrEmpty()

val CharSequence.lower get() = toString().toLowerCase(Locale.getDefault())
//val String.lower get() = toLowerCase(Locale.getDefault())

fun CharSequence.takePad(length: Int = 10, padChar: Char = ' ') = toString().take(length).padEnd(length, padChar)

fun String.takePadKr(length: Int = 10, padChar: Char = ' '): String = takeKr(length).padEndKr(length, padChar)

fun String.takeKr(length: Int = 10): String {
    var lengthKr = 0
    return toString()
            .takeWhile {
                val count = if (it.toInt() in 0x00..0x7f) 1 else 2
                if (lengthKr + count <= length) {
                    lengthKr += count
                    true
                } else
                    false
            }
}

fun String.padEndKr(length: Int = 10, padChar: Char = ' '): String = padEnd(length - (lengthKr - this.length), padChar)
val CharSequence.lengthKr: Int get() = toString().sumBy { if (it.toInt() in 0x00..0x7f) 1 else 2 }

val CharSequence?.urlEncode: String get() = URLEncoder.encode(this?.toString() ?: "", "UTF-8")

@Suppress("FunctionName", "FunctionName", "NonAsciiCharacters", "NonAsciiCharacters", "LocalVariableName", "LocalVariableName")
infix fun CharSequence.조사(이가: String): CharSequence {
    var 이 = 이가[0]
    var 가 = 이가[1]
    val JT = 28
    val M = 21
    if ((이 - '가') / JT / M != 11/*ㅇ*/) {
        val t = 이
        이 = 가
        가 = t
    }

    val lastName = get(length - 1) // 한글의 제일 처음과 끝의 범위밖일 경우는 오류
    return if (lastName < '가' || lastName > '힣')
        this
    else
        toString() + if ((lastName - '가') % JT > 0) 이 else 가
}

val String?.formatAccount get() = this?.let { if (length == 11) replace("^(\\d{3})(\\d{2})(\\d{5})(\\d)$".toRegex(), "$1-$2-$3-$4") else replace("^(\\d{3})(\\d{6})(\\d{3}|\\d{5})$".toRegex(), "$1-$2-$3") }

//----------------------------------------------------------------------------------------------------
/** 10000 -> 만원*/
val String.numberText: String
    get() {
        val unit4 = arrayOf("", "십", "백", "천")
        val unit = arrayOf("", "만", "억", "조", "경", "해", "자", "양", "구", "간", "정", "재", "극", "항하사", "아승기", "나유타", "불가사의", "무량대수")
        val number = arrayOf("", "", "이", "삼", "사", "오", "육", "칠", "팔", "구")

        val numberRemoved = this.replace("\\D".toRegex(), "")
        if (numberRemoved.length == 1 && numberRemoved[0] == '0')
            return "영"

//        val text = StringBuilder(numberRemoved)
        val length = numberRemoved.length
        var numSum = 0
        val result = StringBuilder()
        numberRemoved.forEachIndexed { index, ch ->
            val po = length - index - 1
            val num = ch - '0'

            numSum += num
            result.append((if (po == 0 && num == 1) "일" else number[num])
                    + (if (num > 0) unit4[po % 4] else "")
                    + if (numSum > 0 && po % 4 == 0) unit[po / 4] + "" else ""
            )
            if (po % 4 == 0)
                numSum = 0
        }
        return result.toString()
    }

val InputStream.text get() = bufferedReader().use { it.readText() }

val String.formatPhoneNumber: String get() = PhoneNumberUtils.formatNumber(this, Locale.getDefault().country)

val CharSequence.md5: String
    get() = try {
        val digest = MessageDigest.getInstance("MD5")
        digest.update(toString().toByteArray())
        val messageDigest = digest.digest()
        messageDigest.hexString
    } catch (e: NoSuchAlgorithmException) {
        ""
    }

val ByteArray.hexString: String get() = joinToString("") { "%02x".format(it) }

val CharSequence.sha256: String
    get() = try {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.digest(toString().toByteArray()).hexString
    } catch (e: Exception) {
        ""
    }

val String?.urlDecode: String
    get() = try {
        URLDecoder.decode(this, "UTF-8")
    } catch (e: UnsupportedEncodingException) {
        ""
    }
val String?.urlEncode: String
    get() = try {
        URLEncoder.encode(this, "UTF-8")
    } catch (e: UnsupportedEncodingException) {
        ""
    }

val String?.urlDecodeEuckr: String
    get() = try {
        URLDecoder.decode(this, "euc-kr")
    } catch (e: UnsupportedEncodingException) {
        ""
    }
val String?.urlEncodeEuckr: String
    get() = try {
        URLEncoder.encode(this, "euc-kr")
    } catch (e: UnsupportedEncodingException) {
        ""
    }

val String?.toPrettyJson: String
    get() = kotlin.runCatching {
        when (this?.firstOrNull()) {
            '{' -> JSONObject(this).toString(2)
            '[' -> JSONArray(this).toString(2)
            null -> ""
            else -> this
        }
    }.getOrDefault("")

