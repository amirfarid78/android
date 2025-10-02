package com.coheser.app.simpleclasses.Downloading

import com.coheser.app.apiclasses.InterfaceFileUpload
import com.smusix.app.services.ProgressInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream

object DownloadFiles {

    public suspend fun downloadFileWithProgress(
        fileUrl: String,
        fileid:String,
        extention:String,
        outputDir: File,
        progressCallback: (Long, Long) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .addInterceptor(ProgressInterceptor(progressCallback))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://example.com/")
            .client(client)
            .build()

        val service = retrofit.create(InterfaceFileUpload::class.java)
        val response = service.downloadFile(fileUrl).execute()

        if (response.isSuccessful && response.body() != null) {
            val file = File(outputDir, fileid+"."+extention)
            val inputStream = response.body()!!.byteStream()
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return@withContext file
        }

        else {
            return@withContext null
        }
    }



}