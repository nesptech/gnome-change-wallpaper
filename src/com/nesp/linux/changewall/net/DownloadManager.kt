package com.nesp.linux.changewall.net

import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DownloadManager {

    companion object {

        fun downloadFile(fileUrl: String, localFileAbsPath: String): Boolean {

            var tag = true

            val localFile = File(localFileAbsPath)
            val localDirPath = localFile.parent
            val localDir = File(localDirPath)
            if (!localDir.exists()) localDir.mkdirs()

            val okHttpClient = OkHttpClient()
            val request = okhttp3.Request.Builder().get().url(fileUrl).build()
            var response: Response?
            try {
                response = okHttpClient.newCall(request).execute()
            } catch (e: Exception) {
                return false

            }
            if (!response.isSuccessful) {
                return false
            }

            val inStream = response.body?.byteStream()
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(localFile)
                val bytes = ByteArray(1024)
                var len: Int = inStream!!.read(bytes)
                while (len != -1) {
                    fos.write(bytes, 0, len)
                    len = inStream.read(bytes)
                }
            } catch (oue: IOException) {
                tag = false
            } finally {
                inStream?.close()
                fos?.close()
            }

            return tag
        }

    }

}