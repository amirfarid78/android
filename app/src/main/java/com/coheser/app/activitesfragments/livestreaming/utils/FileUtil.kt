package com.coheser.app.activitesfragments.livestreaming.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File

object FileUtil {
    private const val LOG_FOLDER_NAME = "log"
    private const val LOG_FILE_NAME = "agora-rtc.log"

    /**
     * Initialize the log folder
     *
     * @param context Context to find the accessible file folder
     * @return the absolute path of the log file
     */
    fun initializeLogFile(context: Context): String {
        var folder: File?
        if (Build.VERSION.SDK_INT >= 29) {
            folder =
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), LOG_FOLDER_NAME)
        } else {
            val path = Environment.getExternalStorageDirectory()
                .absolutePath + File.separator +
                    context.packageName + File.separator +
                    LOG_FOLDER_NAME
            folder = File(path)
            if (!folder.exists() && !folder.mkdir()) folder = null
        }

        return if (folder != null && !folder.exists() && !folder.mkdir()) ""
        else File(folder, LOG_FILE_NAME).absolutePath
    }
}
