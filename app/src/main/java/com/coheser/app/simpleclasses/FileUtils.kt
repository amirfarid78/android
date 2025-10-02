package com.coheser.app.simpleclasses

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.interfaces.GenrateBitmapCallback
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.util.concurrent.Executors

object FileUtils {

    @JvmStatic
    fun getAppFolder(context: Context): String {
        return try {
            context.getExternalFilesDir(null)!!.path + "/"
        } catch (e: Exception) {
            context.filesDir.path + "/"
        }
    }

    @JvmStatic
    @Throws(Exception::class)
    fun getFileFromUri(context: Context, uri: Uri): File {
        var path: String? = null

        // DocumentProvider
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) { // TODO: 2015. 11. 17. KITKAT


                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        path = Environment.getExternalStorageDirectory()
                            .toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    path =
                        getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) { // MediaProvider
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                        split[1]
                    )
                    path = getDataColumn(
                        context,
                        contentUri,
                        selection,
                        selectionArgs
                    )
                } else if (isGoogleDrive(uri)) { // Google Drive
                    val TAG = "isGoogleDrive"
                    path = TAG
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val acc = split[0]
                    val doc = split[1]

                    /*
                     * @details google drive document data. - acc , docId.
                     * */return saveFileIntoExternalStorageByUri(
                        context,
                        uri
                    )
                } // MediaStore (and general)
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                path = getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                path = uri.path
            }
            File(path)
        } else {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            File(cursor!!.getString(cursor.getColumnIndex("_data")))
        }
    }

    fun isGoogleDrive(uri: Uri): Boolean {
        return uri.authority.equals("com.google.android.apps.docs.storage", ignoreCase = true)
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = MediaStore.Images.Media.DATA
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndex(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun makeEmptyFileIntoExternalStorageWithTitle(title: String?): File {
        val root = Environment.getExternalStorageDirectory().absolutePath
        return File(root, title)
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    @Throws(Exception::class)
    fun saveFileIntoExternalStorageByUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalSize = inputStream!!.available()
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        val fileName = getFileName(context, uri)
        val file = makeEmptyFileIntoExternalStorageWithTitle(fileName)
        bis = BufferedInputStream(inputStream)
        bos = BufferedOutputStream(
            FileOutputStream(
                file, false
            )
        )
        val buf = ByteArray(originalSize)
        bis.read(buf)
        do {
            bos.write(buf)
        } while (bis.read(buf) != -1)
        bos.flush()
        bos.close()
        bis.close()
        return file
    }

    @JvmStatic
    fun clearFilesCacheBeforeOperation(vararg files: File) {
        if (files.size > 0) {
            for (file in files) {
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }


    @JvmStatic
    fun UrlToBitmapGenrator(imgUrl: String?, callback: GenrateBitmapCallback) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute { //Background work here
            val `in`: InputStream
            try {
                val url = URL(imgUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                `in` = connection.inputStream
                val myBitmap = BitmapFactory.decodeStream(`in`)
                callback.onResult(myBitmap)
                executor.shutdownNow()
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception: $e")
                executor.shutdownNow()
            }
        }
    }

    fun createNoMediaFile(context: Context) {
        val `in`: InputStream? = null
        val out: OutputStream? = null
        try {

            //create output directory if it doesn't exist
            val path = getAppFolder(context) + "videoCache"
            val dir = File(path)
            val newFile = File(dir, ".nomedia")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            if (!newFile.exists()) {
                newFile.createNewFile()
                MediaScannerConnection.scanFile(
                    context, arrayOf(path), null
                ) { path, uri ->
                    Log.i("ExternalStorage", "Scanned $path:")
                    Log.i("ExternalStorage", "-> uri=$uri")
                }
            }
        } catch (e: Exception) {
            Log.e(Constants.tag, "" + e)
        }
    }

    @JvmStatic
    fun showVideoDurationInSec(videoPath: String?): Int {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val bit = retriever.frameAtTime
            val width = bit!!.width
            val height = bit.height
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            Integer.valueOf(duration) / 1000
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
            10
        }
    }

    @JvmStatic
    fun getBitmapToUri(context: Context, bitmap: Bitmap, fileName: String): File? {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val file = File(getAppFolder(context) + Variables.APP_HIDED_FOLDER + fileName)
        try {
            val fo = FileOutputStream(file)
            fo.write(bytes.toByteArray())
            fo.flush()
            fo.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return if (file.exists()) {
            file
        } else {
            null
        }
    }


    //use to get Directory Storage Used Capacity
    @JvmStatic
    fun getDirectorySize(path: String?): String {
        val dir = File(path)
        if (dir.exists()) {
            val bytes = getFolderSize(dir)
            if (bytes < 1024) return "$bytes B"
            val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
            val pre = "KMGTPE"[exp - 1].toString() + ""
            return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
        }
        return "0"
    }

    private fun getFolderSize(dir: File): Long {
        if (dir.exists()) {
            var result: Long = 0
            val fileList = dir.listFiles()
            for (i in fileList.indices) {
                // Recursive call if it's a directory
                result += if (fileList[i].isDirectory) {
                    getFolderSize(fileList[i])
                } else {
                    // Sum the file size in bytes
                    fileList[i].length()
                }
            }
            return result // return the file size
        }
        return 0
    }

    @JvmStatic
    fun copyFile(sourceFile: File?, destFile: File) {

        if (sourceFile == null || !sourceFile.exists()) {
            Log.d(Constants.tag, "Source file does not exist: ${sourceFile?.path}")
            return
        }

        if (!destFile.parentFile.exists()) {
            if (!destFile.parentFile.mkdirs()) {
                Log.d(Constants.tag, "Failed to create destination directory: ${destFile.parentFile.path}")
                return
            }
        }

        if (!destFile.exists()) {
            try {
                if (!destFile.createNewFile()) {
                    Log.d(Constants.tag, "Failed to create destination file: ${destFile.path}")
                    return
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception while creating destination file: $e")
                return
            }
        }

        var source: FileChannel? = null
        var destination: FileChannel? = null
        try {
            source = FileInputStream(sourceFile).channel
            destination = FileOutputStream(destFile).channel
            destination.transferFrom(source, 0, source.size())
            Log.d(Constants.tag, "File copied successfully from ${sourceFile.path} to ${destFile.path}")
        } catch (e: Exception) {
            Log.d(Constants.tag, "copyFileException: $e")
        } finally {
            try {
                source?.close()
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception while closing source channel: $e")
            }
            try {
                destination?.close()
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception while closing destination channel: $e")
            }
        }
    }



    fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success =
                    deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }


    fun createAppNameVideoDirectory(context: Context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            val resolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DCIM + File.separator + context.getString(R.string.app_name) + File.separator + Variables.VideoDirectory
            )
            val path = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                .toString()
            val folder = File(path)
            val isCreada = folder.exists()
            if (!isCreada) {
                folder.mkdirs()
            }
        } else {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(
                    createDefultFolder(
                        Environment.DIRECTORY_DCIM,
                        context.getString(R.string.app_name) + File.separator + Variables.VideoDirectory
                    )
                ),
                null
            ) { path, uri -> }
        }
    }

    fun createDefultFolder(root: String?, folderName: String?): String {
        val defultFile = File(Environment.getExternalStoragePublicDirectory(root), folderName)
        if (!defultFile.exists()) {
            defultFile.mkdirs()
        }
        return defultFile.absolutePath
    }


    // make the directory on specific path
    @JvmStatic
    fun makeDirectry(path: String?) {
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    // make the directory on specific path
    @JvmStatic
    fun makeDirectryAndRefresh(context: Context, dirPath: String, filePath: String) {
        val dir = File(dirPath)
        if (!dir.exists()) {
            dir.mkdir()
        }
        val file = File(dirPath, filePath)
        var imageStream: InputStream? = null
        try {
            imageStream = context.contentResolver.openInputStream(Uri.fromFile(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        BitmapFactory.decodeStream(imageStream)
        MediaScannerConnection.scanFile(
            context, arrayOf(dir.absolutePath, file.absolutePath), null
        ) { path, uri ->
            Log.i("ExternalStorage", "Scanned $path:")
            Log.i("ExternalStorage", "-> uri=$uri")
        }
    }

    @JvmStatic
    fun getFileDuration(context: Context?, uri: Uri?): Long {
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(context, uri)
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val file_duration = Functions.parseInterger(durationStr)
            return file_duration.toLong()
        } catch (e: Exception) {
        }
        return 0
    }


    @JvmStatic
    fun bitmapToBase64(imagebitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        imagebitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val byteArray = baos.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun base64ToBitmap(base_64: String?): Bitmap? {
        var decodedByte: Bitmap? = null
        try {
            val decodedString = Base64.decode(base_64, Base64.DEFAULT)
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: Exception) {
        }
        return decodedByte
    }

    @JvmStatic
    fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        ) + " " + units[digitGroups]
    }

    @JvmStatic
    fun convertImage(imagePath: String?): Bitmap {
        // Load the original image from a file or any source
        var originalImage = BitmapFactory.decodeFile(imagePath)

        // Get the dimensions of the original image
        val originalWidth = originalImage.width
        val originalHeight = originalImage.height

        // Set the default dimensions
        val defaultWidth = 512
        val defaultHeight = 1024

        // Calculate the aspect ratio of the original image
        val aspectRatio = originalWidth.toFloat() / originalHeight

        // Check if the original image exceeds the default dimensions
        if (originalWidth > defaultWidth || originalHeight > defaultHeight) {
            // Determine the new dimensions to maintain the aspect ratio
            var newWidth = defaultWidth
            var newHeight = (newWidth / aspectRatio).toInt()

            // Check if the new height exceeds the default height
            if (newHeight > defaultHeight) {
                newHeight = defaultHeight
                newWidth = (newHeight * aspectRatio).toInt()
            }

            // Resize the original image to the new dimensions
            originalImage = Bitmap.createScaledBitmap(originalImage, newWidth, newHeight, true)
        }

        // Create a new Bitmap with the default dimensions
        val convertedImage =
            Bitmap.createBitmap(defaultWidth, defaultHeight, Bitmap.Config.ARGB_8888)

        // Create a Canvas object to draw on the new Bitmap
        val canvas = Canvas(convertedImage)
        canvas.drawColor(Color.BLACK) // Set the extra space to black color

        // Calculate the center position to draw the original image
        val centerX = (defaultWidth - originalImage.width) / 2
        val centerY = (defaultHeight - originalImage.height) / 2

        // Draw the resized original image onto the converted image
        canvas.drawBitmap(originalImage, centerX.toFloat(), centerY.toFloat(), Paint())
        return convertedImage
    }

    fun correctDownloadURL(url: String): String {
        var url = url
        if (!url.contains(Variables.http)) {
            url = Constants.BASE_URL + url
        }
        // Remove extra forward slashes
        val correctedURL = url.replace("(?<!http:|https:|ftp:)\\/\\/+".toRegex(), "/")
        Log.d(Constants.tag, "correctedURL: $correctedURL")
        return correctedURL
    }

    @JvmStatic
    fun isWidthGreaterThanHeight(videoFilePath: String): Boolean {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoFilePath)
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        return if (width != null && height != null) {
            val videoWidth = width.toInt()
            val videoHeight = height.toInt()
            videoWidth > videoHeight
        } else {
            false
        }
    }

    @JvmStatic
    fun isFileSizeLessThan50KB(filePath: String?): Boolean {
        // Create a File object with the specified file path
        val file = File(filePath)

        // Check if the file exists and is a file (not a directory)
        return if (file.exists() && file.isFile) {
            // Get the file size in bytes
            val fileSizeInBytes = file.length()
            // Convert file size to kilobytes
            val fileSizeInKB = fileSizeInBytes / 1024

            // Check if file size is less than 50KB
            fileSizeInKB < 50
        } else {
            // File doesn't exist or is not a file
            false
        }
    }

    @JvmStatic
    fun resizeVideo(targetWidth: Int, originalWidth: Int, originalHeight: Int): IntArray {

        // Calculate the ratio
        val ratio = targetWidth.toDouble() / originalWidth

        // Calculate the new height
        val newHeight = (originalHeight * ratio).toInt()
        return intArrayOf(targetWidth, newHeight)
    }


    @JvmStatic
    fun getTrimVideoFrameRate(videoPath: String?): String {
        val extractor = MediaExtractor()
        var frameRate = 24 //may be default
        try {
            //Adjust data source as per the requirement if file, URI, etc.
            extractor.setDataSource(videoPath!!)
            val numTracks = extractor.trackCount
            for (i in 0 until numTracks) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime!!.startsWith("video/")) {
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        } finally {
            extractor.release()
            return "" + frameRate
        }
    }


    fun getJsonFromRaw(context: Context, resourceId: Int): JsonObject? {
        try {
            val `is` = context.resources.openRawResource(resourceId)
            val reader = InputStreamReader(`is`, "UTF-8")
            val gson = Gson()
            return gson.fromJson(reader, JsonObject::class.java)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }


}