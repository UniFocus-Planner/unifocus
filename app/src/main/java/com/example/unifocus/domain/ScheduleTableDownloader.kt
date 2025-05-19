package com.example.unifocus.domain

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*
import java.net.URL
import android.util.Log

class ScheduleTableDownloader {

    fun downloadToTempFile(
        context: Context,
        fileUrl: String,
        tempFileName: String,
        onDownloadComplete: (success: Boolean, file: File?) -> Unit
    ) {
        val tempFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), tempFileName)

        if (tempFile.exists()) tempFile.delete()

        Thread {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection()
                connection.connect()
                val input = connection.getInputStream()
                val output = FileOutputStream(tempFile)

                input.copyTo(output)

                output.flush()
                output.close()
                input.close()

                onDownloadComplete(true, tempFile)
            } catch (e: Exception) {
                Log.e("ManualDownload", "Ошибка загрузки файла", e)
                onDownloadComplete(false, null)
            }
        }.start()
    }

    fun replaceFile(
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

    fun downloadAndReplace(
        context: Context,
        fileUrl: String,
        fileName: String,
        onComplete: (success: Boolean) -> Unit
    ): Long {
        val tempFileName = "${fileName}.tmp_${System.currentTimeMillis()}"

        downloadToTempFile(context, fileUrl, tempFileName) { success, _ ->
            if (success) {
                replaceFile(context, tempFileName, fileName, onComplete)
            } else {
                onComplete(false)
            }
        }

        return -1L
    }
}
