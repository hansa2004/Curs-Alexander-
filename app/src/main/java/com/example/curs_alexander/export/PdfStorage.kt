package com.example.curs_alexander.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.OutputStream

object PdfStorage {

    /**
     * Сохраняет PDF в Downloads через MediaStore.
     * Возвращает Uri сохранённого файла или null.
     */
    fun saveToDownloads(context: Context, fileName: String, write: (OutputStream) -> Unit): Uri? {
        return try {
            val resolver = context.contentResolver

            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            }

            val uri = resolver.insert(collection, values) ?: return null
            resolver.openOutputStream(uri)?.use { out ->
                write(out)
            } ?: return null

            uri
        } catch (t: Throwable) {
            Log.e("PdfStorage", "Failed to save pdf", t)
            null
        }
    }
}

