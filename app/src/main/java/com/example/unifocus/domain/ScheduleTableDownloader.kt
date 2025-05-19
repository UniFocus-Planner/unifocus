package com.example.unifocus.domain

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

class ScheduleTableDownloader {

    private val isCancelled = AtomicBoolean(false)

    fun cancelAllDownloads() {
        isCancelled.set(true)
    }

    fun downloadToTempFile(
        context: Context,
        fileUrl: String,
        tempFileName: String,
        timeoutMillis: Long = 60000,
        progressCallback: (progress: Int) -> Unit,
        onDownloadComplete: (success: Boolean, file: File?, error: String?) -> Unit
    ) {
        isCancelled.set(false)
        val tempFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), tempFileName)
        if (tempFile.exists()) tempFile.delete()

        val startTime = System.currentTimeMillis()
        val handler = Handler(Looper.getMainLooper())

        Thread {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection().apply {
                    connectTimeout = timeoutMillis.toInt()
                    readTimeout = timeoutMillis.toInt()
                }

                // отмена перед подключением
                if (isCancelled.get()) {
                    handler.post { onDownloadComplete(false, null, "Download cancelled") }
                    return@Thread
                }

                connection.connect()

                // таймаут
                if (System.currentTimeMillis() - startTime > timeoutMillis) {
                    handler.post { onDownloadComplete(false, null, "Connection timeout") }
                    return@Thread
                }

                val contentLength = connection.contentLengthLong
                var downloadedBytes = 0L
                val input = connection.getInputStream()
                val output = FileOutputStream(tempFile)

                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    if (isCancelled.get()) {
                        input.close()
                        output.close()
                        tempFile.delete()
                        handler.post { onDownloadComplete(false, null, "Download cancelled") }
                        return@Thread
                    }

                    if (System.currentTimeMillis() - startTime > timeoutMillis) {
                        input.close()
                        output.close()
                        tempFile.delete()
                        handler.post { onDownloadComplete(false, null, "Download timeout") }
                        return@Thread
                    }

                    output.write(buffer, 0, bytesRead)
                    downloadedBytes += bytesRead

                    if (contentLength > 0) {
                        val progress = (downloadedBytes * 100 / contentLength).toInt()
                        handler.post { progressCallback(progress) }
                    }
                }

                output.flush()
                output.close()
                input.close()

                handler.post { onDownloadComplete(true, tempFile, null) }
            } catch (e: Exception) {
                Log.e("ManualDownload", "Download error", e)
                tempFile.delete()
                handler.post { onDownloadComplete(false, null, e.message ?: "Download error") }
            }
        }.start()
    }

    fun downloadAndReplace(
        context: Context,
        fileUrl: String,
        fileName: String,
        timeoutMillis: Long = 60000,
        progressCallback: (progress: Int) -> Unit,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        val tempFileName = "${fileName}.tmp_${System.currentTimeMillis()}"

        downloadToTempFile(context, fileUrl, tempFileName, timeoutMillis, progressCallback) { success, file, error ->
            if (success && file != null) {
                replaceFile(context, tempFileName, fileName) { replaceSuccess ->
                    onComplete(replaceSuccess, if (!replaceSuccess) "File replace failed" else null)
                }
            } else {
                onComplete(false, error)
            }
        }
    }

    private fun replaceFile(
        context: Context,
        tempFileName: String,
        targetFileName: String,
        onComplete: (success: Boolean) -> Unit
    ) {
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val tempFile = File(downloadsDir, tempFileName)
        val targetFile = File(downloadsDir, targetFileName)

        try {
            if (!tempFile.exists() || tempFile.length() == 0L) {
                onComplete(false)
                return
            }

            if (targetFile.exists()) {
                targetFile.delete()
            }

            val success = tempFile.renameTo(targetFile)

            if (!success) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            onComplete(true)
        } catch (e: Exception) {
            Log.e("FileReplace", "Error replacing file", e)
            onComplete(false)
        }
    }
}