package com.nesp.linux.changewall.utils

import java.io.*
import java.io.File
import java.nio.charset.Charset


class FileUtils {

    companion object {

        @Throws(IOException::class)
        fun copyFile(fromDir: String, toDir: String) {
            copyFile(File(fromDir), File(toDir))
        }

        @Throws(IOException::class)
        fun copyFile(fromFile: File, toFile: File) {
            var input: InputStream? = null
            var output: OutputStream? = null
            try {
                input = FileInputStream(fromFile)
                output = FileOutputStream(toFile)
                val buf = ByteArray(1024)
                var bytesRead: Int = input.read(buf)
                while (bytesRead > 0) {
                    output.write(buf, 0, bytesRead)
                    bytesRead = input.read(buf)
                }
            } finally {
                input!!.close()
                output!!.close()
            }
        }

        fun readFileToString(fileName: String): String? {
            return readFileToString(File(fileName))
        }

        fun readFileToString(file: File): String? {
            val encoding: Charset = Charset.defaultCharset()
            val filelength = file.length()
            val filecontent = ByteArray(filelength.toInt())
            try {
                val `in` = FileInputStream(file)
                `in`.read(filecontent)
                `in`.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return try {
                String(filecontent, encoding)
            } catch (e: UnsupportedEncodingException) {
                System.err.println("The OS does not support $encoding")
                e.printStackTrace()
                null
            }

        }

        //删除文件夹
        //param folderPath 文件夹完整绝对路径
        fun delFolder(file: File) {
            try {
                delAllFile(file) //删除完里面所有内容
                file.delete() //删除空文件夹
            } catch (e: Exception) {
            }
        }

        //删除指定文件夹下所有文件
        //param path 文件夹完整绝对路径
        fun delAllFile(file: File): Boolean {
            val path = file.absolutePath
            var flag = false

            if (!file.exists()) {
                return flag
            }

            if (!file.isDirectory) {
                return flag
            }

            val tempList = file.list()
            var temp: File?
            for (i in 0 until tempList.size) {
                if (path.endsWith(File.separator)) {
                    temp = File(path + tempList[i])
                } else {
                    temp = File(path + File.separator + tempList[i])
                }
                if (temp.isFile) {
                    temp.delete()
                }
                if (temp.isDirectory) {
                    delAllFile(File(path + "/" + tempList[i]))//先删除文件夹里面的文件
                    delFolder(File(path + "/" + tempList[i]))//再删除空文件夹
                    flag = true
                }
            }
            return flag
        }
    }
}
