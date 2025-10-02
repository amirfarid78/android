package com.coheser.app.simpleclasses

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.coheser.app.Constants
import com.simform.videooperations.CallBackOfQuery
import com.simform.videooperations.Common
import com.simform.videooperations.FFmpegCallBack
import com.simform.videooperations.LogMessage
import java.io.File


object FFMPEGFunctions {

    @JvmStatic
    fun getFilePath(activity: Activity): String {
        return Common.getFilePath(activity, Common.VIDEO)
    }

    fun createImageVideo(
        activity: Activity, photoPaths: ArrayList<String>,
        callback: com.coheser.app.interfaces.FragmentCallBack
    ) {
        var output = Common.getFilePath(activity, Common.VIDEO)

        val query = combineImagesToVideo(activity, photoPaths, output)
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                var message = logMessage.text
                Log.d("FFMPEG_", "process: ${message}")
                if (message.contains("size=") && message.contains("time=")) {
                    message = decodeFFMPEGMessage(message)
                    val bundle = Bundle()
                    bundle.putString("action", "process")
                    bundle.putString("message", message)
                    callback.onResponce(bundle)
                }

            }

            override fun success() {
                Log.d(Constants.tag, "success: ")
                val bundle = Bundle()
                bundle.putString("action", "success")
                bundle.putString("path", output)
                callback.onResponce(bundle)
            }

            override fun cancel() {
                Log.d(Constants.tag, "cancel: ")
                val bundle = Bundle()
                bundle.putString("action", "cancel")
                callback.onResponce(bundle)
            }

            override fun failed() {
                Log.d(Constants.tag, "failed: ")
                val bundle = Bundle()
                bundle.putString("action", "failed")
                callback.onResponce(bundle)
            }
        })
    }


    fun addImageProcess(
        stickerPath: String,
        videoFile: File, outputPath: String,
        callback: com.coheser.app.interfaces.FragmentCallBack
    ) {

        val query = addVideoWaterMark(videoFile.absolutePath, stickerPath, outputPath)
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                var message = logMessage.text
                Log.d("FFMPEG_", "process: ${message}")
                if (message.contains("size=") && message.contains("time=")) {
                    message = decodeFFMPEGMessage(message)
                    val bundle = Bundle()
                    bundle.putString("action", "process")
                    bundle.putString("message", message)
                    callback.onResponce(bundle)
                }

            }

            override fun success() {
                Log.d(Constants.tag, "success: ")
                val bundle = Bundle()
                bundle.putString("action", "success")
                bundle.putString("path", outputPath)
                callback.onResponce(bundle)
            }

            override fun cancel() {
                Log.d(Constants.tag, "cancel: ")
                val bundle = Bundle()
                bundle.putString("action", "cancel")
                callback.onResponce(bundle)
            }

            override fun failed() {
                Log.d(Constants.tag, "failed: ")
                val bundle = Bundle()
                bundle.putString("action", "failed")
                callback.onResponce(bundle)
            }
        })
    }


    fun trimVideoProcess(
        videoFile: File, outputPath: String,
        startTimeString: String, endTimeString: String,
        callback: com.coheser.app.interfaces.FragmentCallBack
    ) {

        val query = cutVideo(videoFile.absolutePath, startTimeString, endTimeString, outputPath)
        Log.d("FFMPEG_", query.toString())
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                var message = logMessage.text
                Log.d("FFMPEG_", "process: ${message}")
                if (message.contains("size=") && message.contains("time=")) {
                    message = decodeFFMPEGMessage(message)
                    val bundle = Bundle()
                    bundle.putString("action", "process")
                    bundle.putString("message", message)
                    callback.onResponce(bundle)
                }
            }

            override fun success() {
                Log.d("FFMPEG_", "success: ")
                val bundle = Bundle()
                bundle.putString("action", "success")
                bundle.putString("path", outputPath)
                callback.onResponce(bundle)
            }

            override fun cancel() {
                Log.d("FFMPEG_", "cancel: ")
                val bundle = Bundle()
                bundle.putString("action", "cancel")
                callback.onResponce(bundle)
            }

            override fun failed() {
                Log.d("FFMPEG_", "failed: ")
                val bundle = Bundle()
                bundle.putString("action", "failed")
                callback.onResponce(bundle)
            }
        })

    }


    fun compressVideoHighToLowProcess(
        activity: Context, videoFile: File,
        frameRate: Int,
        width: Int,
        height: Int,
        callback: com.coheser.app.interfaces.FragmentCallBack
    ) {
        val outputPath = Common.getFilePath(activity, Common.VIDEO)
        val query = highToLowCompressor(videoFile.absolutePath, outputPath, frameRate, width, height)
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                var message = logMessage.text
                Log.d("FFMPEG_", "process: ${message}")
                if (message.contains("size=") && message.contains("time=")) {
                    message = decodeFFMPEGMessage(message)
                    val bundle = Bundle()
                    bundle.putString("action", "process")
                    bundle.putString("message", message)
                    callback.onResponce(bundle)
                }

            }

            override fun success() {
                Log.d("FFMPEG_", "success: ")
                val bundle = Bundle()
                bundle.putString("action", "success")
                bundle.putString("path", outputPath)
                callback.onResponce(bundle)
            }

            override fun cancel() {
                Log.d("FFMPEG_", "cancel: ")
                val bundle = Bundle()
                bundle.putString("action", "cancel")
                callback.onResponce(bundle)
            }

            override fun failed() {
                Log.d("FFMPEG_", "failed: ")
                val bundle = Bundle()
                bundle.putString("action", "failed")
                callback.onResponce(bundle)
            }
        })

    }


    fun changeVideoFormat(
        activity: Context, videoFile: File,
        callback: com.coheser.app.interfaces.FragmentCallBack
    ) {
        val outputPath = Common.getFilePath(activity, Common.VIDEO)
        val query = changefileFormat(videoFile.absolutePath, outputPath)
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                var message = logMessage.text
                Log.d("FFMPEG_", "process: ${message}")
                if (message.contains("size=") && message.contains("time=")) {
                    message = decodeFFMPEGMessage(message)
                    val bundle = Bundle()
                    bundle.putString("action", "process")
                    bundle.putString("message", message)
                    callback.onResponce(bundle)
                }

            }

            override fun success() {
                Log.d("FFMPEG_", "success: ")
                val bundle = Bundle()
                bundle.putString("action", "success")
                bundle.putString("path", outputPath)
                callback.onResponce(bundle)
            }

            override fun cancel() {
                Log.d("FFMPEG_", "cancel: ")
                val bundle = Bundle()
                bundle.putString("action", "cancel")
                callback.onResponce(bundle)
            }

            override fun failed() {
                Log.d("FFMPEG_", "failed: ")
                val bundle = Bundle()
                bundle.putString("action", "failed")
                callback.onResponce(bundle)
            }
        })

    }



    fun compressVideoForAi(
        activity: Context, videoFile: File,
        width: Int,
        height: Int,
        callback: com.coheser.app.interfaces.FragmentCallBack
    ) {
        val outputPath = Common.getFilePath(activity, Common.VIDEO)
        val query = compressForAi(videoFile.absolutePath, outputPath, width, height)
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                var message = logMessage.text
                Log.d("FFMPEG_", "process: ${message}")
                if (message.contains("size=") && message.contains("time=")) {
                    message = decodeFFMPEGMessage(message)
                    val bundle = Bundle()
                    bundle.putString("action", "process")
                    bundle.putString("message", message)
                    callback.onResponce(bundle)
                }

            }

            override fun success() {
                Log.d("FFMPEG_", "success: ")
                val bundle = Bundle()
                bundle.putString("action", "success")
                bundle.putString("path", outputPath)
                callback.onResponce(bundle)
            }

            override fun cancel() {
                Log.d("FFMPEG_", "cancel: ")
                val bundle = Bundle()
                bundle.putString("action", "cancel")
                callback.onResponce(bundle)
            }

            override fun failed() {
                Log.d("FFMPEG_", "failed: ")
                val bundle = Bundle()
                bundle.putString("action", "failed")
                callback.onResponce(bundle)
            }
        })

    }


    fun videoSpeedProcess(
        context: Context, inputPath: String, speedTabPosition: Int,
        frameRate: Int,
        callback: com.coheser.app.interfaces.FragmentCallBack
    ) {
        val outputPath = Common.getFilePath(context, Common.VIDEO)
        var setpts: Double = 1.0
        var atempo: Double = 1.0
        Log.d(Constants.tag, "speedTabPosition: $speedTabPosition")
        when (speedTabPosition) {
            0 -> {
                setpts = 2.0
                atempo = 0.5
            }

            1 -> {
                setpts = 1.5
                atempo = 0.75
            }

            2 -> {
                setpts = 1.0
                atempo = 1.0
            }

            3 -> {
                setpts = 0.75
                atempo = 1.5
            }

            4 -> {
                setpts = 0.5
                atempo = 2.0
            }

            else -> {
                setpts = 1.0
                atempo = 1.0
            }

        }


        val query = videoMotion(inputPath, outputPath, setpts, atempo, frameRate)
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                var message = logMessage.text
                Log.d("FFMPEG_", "process: ${message}")
                if (message.contains("size=") && message.contains("time=")) {
                    message = decodeFFMPEGMessage(message)
                    val bundle = Bundle()
                    bundle.putString("action", "process")
                    bundle.putString("message", message)
                    callback.onResponce(bundle)
                }

            }

            override fun success() {
                Log.d("FFMPEG_", "success: ")
                val bundle = Bundle()
                bundle.putString("action", "success")
                try {
                    FileUtils.copyFile(File(outputPath), File(inputPath))
                    FileUtils.clearFilesCacheBeforeOperation(File(outputPath))
                } catch (e: Exception) {
                    Functions.printLog(
                        Constants.tag, "" + e
                    )
                }
                bundle.putString("path", inputPath)
                callback.onResponce(bundle)
            }

            override fun cancel() {
                Log.d("FFMPEG_", "cancel: ")
                val bundle = Bundle()
                bundle.putString("action", "cancel")
                callback.onResponce(bundle)
            }

            override fun failed() {
                Log.d("FFMPEG_", "failed: ")
                val bundle = Bundle()
                bundle.putString("action", "failed")
                callback.onResponce(bundle)
            }
        })


    }


    fun rotateVideoToPotrate(
        context: Context, inputPath: String,
        callback: com.coheser.app.interfaces.FragmentCallBack
    ) {
        val outputPath = Common.getFilePath(context, Common.VIDEO)


        val query = rotateVideo(inputPath, outputPath)
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                var message = logMessage.text
                Log.d("FFMPEG_", "process: ${message}")
                if (message.contains("size=") && message.contains("time=")) {
                    message = decodeFFMPEGMessage(message)
                    val bundle = Bundle()
                    bundle.putString("action", "process")
                    bundle.putString("message", message)
                    callback.onResponce(bundle)
                }

            }

            override fun success() {
                Log.d("FFMPEG_", "success: ")
                val bundle = Bundle()
                bundle.putString("action", "success")
                try {
                    FileUtils.copyFile(File(outputPath), File(inputPath))
                    FileUtils.clearFilesCacheBeforeOperation(File(outputPath))
                } catch (e: Exception) {
                    Functions.printLog(
                        Constants.tag, "" + e
                    )
                }
                bundle.putString("path", inputPath)
                callback.onResponce(bundle)
            }

            override fun cancel() {
                Log.d("FFMPEG_", "cancel: ")
                val bundle = Bundle()
                bundle.putString("action", "cancel")
                callback.onResponce(bundle)
            }

            override fun failed() {
                Log.d("FFMPEG_", "failed: ")
                val bundle = Bundle()
                bundle.putString("action", "failed")
                callback.onResponce(bundle)
            }
        })
    }


    fun ConcatenateMultipleVideos(
        activity: Activity, videoPaths: ArrayList<String>, output: String,
        callback: com.coheser.app.interfaces.FragmentCallBack
    ) {
        val query = ConcateVideos(videoPaths, output)
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                var message = logMessage.text
                Log.d("FFMPEG_", "process: ${message}")
                if (message.contains("size=") && message.contains("time=")) {
                    message = decodeFFMPEGMessage(message)
                    val bundle = Bundle()
                    bundle.putString("action", "process")
                    bundle.putString("message", message)
                    callback.onResponce(bundle)
                }

            }

            override fun success() {
                Log.d("FFMPEG_", "success: ")
                val bundle = Bundle()
                bundle.putString("action", "success")
                bundle.putString("path", output)
                callback.onResponce(bundle)
            }

            override fun cancel() {
                Log.d("FFMPEG_", "cancel: ")
                val bundle = Bundle()
                bundle.putString("action", "cancel")
                callback.onResponce(bundle)
            }

            override fun failed() {
                Log.d("FFMPEG_", "failed: ")
                val bundle = Bundle()
                bundle.putString("action", "failed")
                callback.onResponce(bundle)
            }
        })
    }


    fun combineImagesToVideo(
        activity: Activity,
        photoPaths: ArrayList<String>,
        output: String
    ): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        for (i in 0 until photoPaths.size) {
            val imagePath: String = photoPaths.get(i)
            val duration: Int = (Constants.MAX_TIME_FOR_VIDEO_PICS / photoPaths.size)
            //for input
            inputs.add("-loop")
            inputs.add("1")
            inputs.add("-t")
            inputs.add("$duration")
            inputs.add("-i")
            inputs.add("$imagePath")
        }

        var query: String = ""
        var queryAudio: String = ""
        for (i in 0 until photoPaths.size) {
            query = query.trim()
            query += "[" + i + ":v]scale=${Functions.getPhoneResolution(activity).widthPixels}x${
                Functions.getPhoneResolution(
                    activity
                ).heightPixels
            },setdar=${Functions.getPhoneResolution(activity).widthPixels}/${
                Functions.getPhoneResolution(
                    activity
                ).heightPixels
            }[" + i + "v];"

            queryAudio = queryAudio.trim()
            queryAudio += "[" + i + "v][" + photoPaths.size + ":a]"
        }
        return getCombineImagesToVideo(inputs, query, queryAudio, photoPaths, output)
    }

    private fun getCombineImagesToVideo(
        inputs: ArrayList<String>,
        query: String,
        queryAudio: String,
        paths: ArrayList<String>,
        output: String
    ): Array<String> {
        Log.d(Constants.tag, "inputsquery: ${inputs}")
        val width = 620
        val height = 1102
        inputs.apply {
            add("-f")
            add("lavfi")
            add("-t")
            add("0.1")
            add("-i")
            add("anullsrc")
            add("-filter_complex")
            add(query + queryAudio + "concat=n=" + paths.size + ":v=1:a=1 [v][a]")
            add("-s")
            add("${width}x${height}")
            add("-map")
            add("[v]")
            add("-map")
            add("[a]")
            add("-r")
            add("25")
            add("-vcodec")
            add("mpeg4")
            add("-b:v")
            add("25M")
            add("-b:a")
            add("64000")
            add("-ac")
            add("2")
            add("-ar")
            add("22050")
            add("-preset")
            add("ultrafast")
            add(output)
        }
        Log.d(Constants.tag, "inputsfinal: ${inputs}")
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }


    fun addVideoWaterMark(inputVideo: String, imageInput: String, output: String): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {
            add("-i")
            add(inputVideo)
            add("-i")
            add(imageInput)
            add("-filter_complex")
            add("[0:v][1:v]overlay=0:0")
            add("-r")
            add("25")
            add("-vcodec")
            add("mpeg4")
            add("-b:v")
            add("25M")
            add("-c:a")
            add("copy")
            add("-preset")
            add("ultrafast")
            add("-max_muxing_queue_size")
            add("9999")
            add(output)
        }
        Log.d(Constants.tag, "inputs AddImage: ${inputs}")
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }


    fun highToLowCompressor(
        inputVideo: String,
        outputVideo: String,
        frameRate: Int,
        width: Int,
        height: Int,
    ): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {
            add("-y")
            add("-i")
            add(inputVideo)
            add("-vf")
            add("scale=w='min(" + width + ",iw)':h='min(" + height + ",ih)', pad=" + width + ":" + height + ":(" + width + "-iw)/2:(" + height + "-ih)/2:black")
            add("-r")
            add("${if (frameRate >= 10) frameRate - 5 else frameRate}")
            add("-vcodec")
            add("mpeg4")
            add("-b:v")
            add("25M")
            add("-b:a")
            add("64000")
            add("-ac")
            add("2")
            add("-ar")
            add("22050")
            add("-preset")
            add("ultrafast")
            add(outputVideo)
        }
        Log.d(Constants.tag, "inputs Compression: ${inputs}")
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    fun changefileFormat(
        inputVideo: String,
        outputVideo: String,
    ): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {
            add("-y")
            add("-i")
            add(inputVideo)
            add("-vcodec")
            add("mpeg4")
            add("-c:a")
            add("aac")
            add("-preset")
            add("ultrafast")
            add(outputVideo)
        }
        Log.d(Constants.tag, "inputs changefileFormat: ${inputs}")
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    fun compressForAi(
        inputVideo: String,
        outputVideo: String,
        width: Int,
        height: Int,
    ): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {
            add("-y")
            add("-i")
            add(inputVideo)
            add("-vf")
            add("scale=" + width + ":" + height)
            add("-vcodec")
            add("mpeg4")
            add("-b:v")
            add("25M")
            add("-b:a")
            add("64000")
            add("-ac")
            add("2")
            add("-ar")
            add("22050")
            add("-preset")
            add("ultrafast")
            add(outputVideo)
        }
        Log.d(Constants.tag, "inputs Compression: ${inputs}")
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }



    fun cutVideo(
        inputVideoPath: String,
        startTime: String?,
        endTime: String?,
        output: String
    ): Array<String> {
        Common.getFrameRate(inputVideoPath)
        val inputs: ArrayList<String> = ArrayList()

        inputs.apply {
            add("-ss")
            add(startTime.toString())
            add("-i")
            add(inputVideoPath)
            add("-to")
            add(endTime.toString())
            add("-c:v")
            add("copy")
            add("-c:a")
            add("copy")
            add("-copyts")
            add(output)
        }

        Functions.printLog(Constants.tag, inputs.toString())

        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }


    fun videoMotion(
        inputVideo: String,
        output: String,
        setpts: Double,
        atempo: Double,
        frameRate: Int
    ): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {
            add("-y")
            add("-i")
            add(inputVideo)
            add("-filter_complex")
            add("[0:v]setpts=${setpts}*PTS[v];[0:a]atempo=${atempo}[a]")
            add("-map")
            add("[v]")
            add("-map")
            add("[a]")
            add("-b:v")
            add("25M")
            add("-b:a")
            add("64000")
            add("-r")
            add("$frameRate")
            add("-vcodec")
            add("mpeg4")
            add("-preset")
            add("ultrafast")
            add(output)
        }



        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }


    fun ConcateVideos(videos: ArrayList<String>, output: String): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        val inputStringBuilder = StringBuilder()
        for (i in 0..(videos.size - 1)) {

            inputStringBuilder.append("[${i}:v][${i}:a]")

        }

        inputs.apply {
            add("-y")
            for (i in 0..(videos.size - 1)) {
                add("-i")
                add(videos.get(i))
            }
            add("-filter_complex")

            add(inputStringBuilder.toString() + "concat=n=" + videos.size + ":v=1:a=1[outv][outa]")
            add("-map")
            add("[outv]")

            add("-map")
            add("[outa]")

            add("-b:v")
            add("25M")

            add("-r")
            add("25")

            add("-s")
            add("620x1102")

            add(output)
        }
        Log.d(Constants.tag, "inputsfinal: ${inputs}")
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }

    fun rotateVideo(inputVideo: String, output: String): Array<String> {
        val inputs: ArrayList<String> = ArrayList()
        inputs.apply {
            add("-i")
            add(inputVideo)
            add("-vf")
            add("transpose=1")
            add("-c:a")
            add("copy")
            add("-b:v")
            add("25M")
            add(output)
        }
        return inputs.toArray(arrayOfNulls<String>(inputs.size))
    }


    @JvmStatic
    fun CalculateFFMPEGTimeToPercentage(message: String, allowRecordingDuration: Int): Int {
        var preViousPercent = 0.0
        return try {
            val array = message.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val startTime = DateOprations.getTimeWithAdditionalSecond("HH:mm:ss", 0)
            val endTime = DateOprations.getTimeWithAdditionalSecond("HH:mm:ss", allowRecordingDuration)
            val currentTime = array[4]
            val start = DateOprations.getTimeInMilli("HH:mm:ss", startTime)
            val end = DateOprations.getTimeInMilli("HH:mm:ss", endTime)
            val cur = DateOprations.getTimeInMilli("HH:mm:ss", currentTime)
            val percent = (cur - start) / (end - start) * 100f
            preViousPercent = percent
            percent.toInt()
        } catch (e: Exception) {
            preViousPercent.toInt()
        }
    }

    @JvmStatic
    fun decodeFFMPEGMessage(message: String): String {
        var message = message
        try {
            message = message.replace("  ".toRegex(), " ")
            message = message.replace("  ".toRegex(), " ")
            message = message.replace("  ".toRegex(), " ")
            message = message.replace("fps= ".toRegex(), "")
            message = message.replace("fps=".toRegex(), "")
            message = message.replace("frame= ".toRegex(), "")
            message = message.replace("frame=".toRegex(), "")
            message = message.replace("size= ".toRegex(), "")
            message = message.replace("size=".toRegex(), "")
            message = message.replace("q= ".toRegex(), "")
            message = message.replace("q=".toRegex(), "")
            message = message.replace("time= ".toRegex(), "")
            message = message.replace("time=".toRegex(), "")
            return message
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        }
        return ""
    }



}

