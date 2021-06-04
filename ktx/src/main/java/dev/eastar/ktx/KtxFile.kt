package dev.eastar.ktx

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import java.io.*

typealias KtxFile = Unit

@Throws(IOException::class)
@WorkerThread
private fun readTextFromUri(contentResolver: ContentResolver, uri: Uri): String {
    val stringBuilder = StringBuilder()
    contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line: String? = reader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = reader.readLine()
            }
        }
    }
    return stringBuilder.toString()
}

@Throws(IOException::class)
@WorkerThread
fun ContentResolver.writeTextFromUri(uri: Uri, text: String) = kotlin.runCatching {
    openFileDescriptor(uri, "w")?.use {
        FileOutputStream(it.fileDescriptor).use { out ->
            out.write(text.toByteArray())
        }
    }
}.getOrThrow()

@Throws(IOException::class)
@WorkerThread
fun ContentResolver.readBitmapFromUri(uri: Uri) = kotlin.runCatching {
    openFileDescriptor(uri, "r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    }
}.getOrThrow()

@Throws(IOException::class)
@WorkerThread
fun ContentResolver.writeBitmapFromMediaStore(bitmap: Bitmap, filename: String) = kotlin.runCatching {
    val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    insert(contentUri, values)?.also { uri ->
        openFileDescriptor(uri, "w")?.use {
            FileOutputStream(it.fileDescriptor).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        }
        values.clear()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            update(uri, values, null, null)
        }
    }
}.getOrThrow()


fun Bitmap.toFile(file: File, compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100) = kotlin.runCatching {
    file.parentFile?.let {
        if (!it.exists())
            it.mkdirs()
    }
    file.outputStream().use { compress(compressFormat, quality, it) }
}.getOrDefault(false)