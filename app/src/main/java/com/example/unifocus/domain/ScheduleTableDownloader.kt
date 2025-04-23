package com.example.unifocus.domain

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileNotFoundException
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log

class ScheduleTableDownloader {

    fun checkAndRequestStoragePermission(activity: Activity, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                requestCode
            )
        }
    }

    fun downloadToTempFile(
        context: Context,
        fileUrl: String,
        tempFileName: String,
        onProgress: ((bytesDownloaded: Long, totalBytes: Long) -> Unit)? = null
    ): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val tempFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), tempFileName)

        if (tempFile.exists()) {
            tempFile.delete()
        }

        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle("Temp download")
            .setDescription("Downloading temporary file")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setDestinationUri(Uri.fromFile(tempFile))
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        return downloadManager.enqueue(request)
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

        val downloadId = downloadToTempFile(context, fileUrl, tempFileName)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val status = getDownloadStatus(context, downloadId)
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        replaceFile(context, tempFileName, fileName, onComplete)
                    } else {
                        onComplete(false)
                    }
                    context.unregisterReceiver(this)
                }
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Context.RECEIVER_NOT_EXPORTED
            } else {
                ContextCompat.RECEIVER_VISIBLE_TO_INSTANT_APPS
            }
        )

        return downloadId
    }

    private fun getDownloadStatus(context: Context, downloadId: Long): Int {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            } else {
                DownloadManager.STATUS_FAILED
            }
        }
    }

    fun readDownloadedFile(context: Context, fileName: String): String {
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        return if (file.exists()) {
            file.readText()
        } else {
            throw FileNotFoundException("File not found: ${file.absolutePath}")
        }
    }
}