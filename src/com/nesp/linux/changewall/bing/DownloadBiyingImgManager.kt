package com.nesp.linux.changewall.bing

import com.nesp.linux.changewall.bing.model.ImagePage
import com.nesp.linux.changewall.model.Settings
import com.nesp.linux.changewall.net.DownloadManager
import com.nesp.linux.changewall.utils.FileUtils
import java.io.File
import java.util.*
import java.util.concurrent.Executors


class DownloadBiyingImgManager(
        private val settings: Settings
) {


    /**
     * 下载模式：
     */
    private val downloadMode: Int = settings.dl_biying_img_mode
    private val maxDownloadNums: Int = settings.dl_biying_img_max_count
    /**
     * 保存的文件夹
     */
    private val saveDirPath: String = settings.dl_biying_img_dir

    companion object {
        /**
         * 最新模式
         */
        const val DOWNLOAD_MODE_NEWER = 0
        /**
         * 最受欢迎的模式
         */
        const val DOWNLOAD_MODE_FAVORITE = 1

    }

    var currentUrl = if (downloadMode == DOWNLOAD_MODE_FAVORITE)
        "https://bing.ioliu.cn/ranking"
    else
        "https://bing.ioliu.cn/"


    var lastDownloadTime = Date().time


    val dlDelayTime = if (settings.dl_delay_time.contains("h", ignoreCase = true) && !settings.dl_delay_time.contains("d", ignoreCase = true)) {
        settings.dl_delay_time.split("h", ignoreCase = true)[0].toInt() * 60 * 60 * 1000
    } else if (settings.dl_delay_time.contains("d", ignoreCase = true) && !settings.dl_delay_time.contains("h", ignoreCase = true)) {
        settings.dl_delay_time.split("d", ignoreCase = true)[0].toInt() * 24 * 60 * 60 * 1000
    } else if (settings.dl_delay_time.contains("d", ignoreCase = true) && settings.dl_delay_time.contains("h", ignoreCase = true)) {
        settings.dl_delay_time.split("d", ignoreCase = true)[0].toInt() * 24 * 60 * 60 * 1000
        +settings.dl_delay_time.split("d", ignoreCase = true)[1].split("h", ignoreCase = true)[0].toInt() * 60 * 60 * 1000
    } else {
        24 * 60 * 60 * 1000
    }

    fun start() {
        val imgPage = ImagePage(currentUrl)
        val saveDir = File(saveDirPath)

        if (settings.delete_files_before_dl_biying_img) {
            FileUtils.delAllFile(saveDir)
            saveDir.deleteOnExit()
            saveDir.mkdirs()
        } else {
            if (!saveDir.exists()) saveDir.mkdirs()
        }

        downloadAllPage(imgPage)
        println("dlDelayTime $dlDelayTime")
        if (dlDelayTime < 1) return
        while (true) {
            Thread.sleep(30 * 1000)
            if (Date().time - lastDownloadTime >= dlDelayTime) {
                System.err.println("redownload wallpaper")
                start()
                lastDownloadTime = Date().time
            }
        }
    }

    private var downloadedCount = 0

    private fun downloadAllPage(imgPage: ImagePage) {
        var currentImgPage = imgPage
        downloadImgPage(currentImgPage, { fileName, isSuccess ->

            if (maxDownloadNums > 0) {
                if (downloadedCount >= maxDownloadNums) {
                    System.err.println("=================Download file num is max================")
                    return@downloadImgPage
                }
            }

        }, {
            if (imgPage.getNextPageUrl().isEmpty()) {
                System.err.println("=================Not found next page================")
                return@downloadImgPage
            }

            currentImgPage = ImagePage(imgPage.getNextPageUrl())
            downloadAllPage(currentImgPage)
        })

    }


    private fun downloadImgPage(imgPage: ImagePage, itemDownloadCallBack: (fileName: String, isSuccess: Boolean) -> Unit, pageDownloadCallBack: () -> Unit) {

        Thread(Runnable {
            for (itemDownloadUrl in imgPage.getImageItemUrlsPerPage()) {
                val filename = getFileName(itemDownloadUrl)
                val result = DownloadManager.downloadFile(itemDownloadUrl, "$saveDirPath/${getFileName(itemDownloadUrl)}.dl")
                File("$saveDirPath/${getFileName(itemDownloadUrl)}.dl").renameTo(File("$saveDirPath/${getFileName(itemDownloadUrl)}"))
                if (result) downloadedCount++ else {
                    System.err.println("$filename====Download failed========")
                    System.err.println("====Delete it======${File("$saveDirPath/$filename").delete()}========")
                }
                println("downloadedCount $downloadedCount\n")
                println("maxDownloadNums $maxDownloadNums")
                if (downloadedCount >= maxDownloadNums) {
                    System.err.println("=================Download file num is max================")
                    return@Runnable
                }
                itemDownloadCallBack.invoke(filename, result)
            }
            pageDownloadCallBack.invoke()
        }).start()


    }

    private fun downloadImgPageMuiltiThread(imgPage: ImagePage, itemDownloadCallBack: (fileName: String, isSuccess: Boolean) -> Unit, pageDownloadCallBack: () -> Unit) {
        val threadPool = Executors.newFixedThreadPool(5)
        val downloadUrlCount = imgPage.getImageItemUrlsPerPage().size
        val downloadCountPerThread = Math.ceil((downloadUrlCount / 5).toDouble()).toInt()
        var currentDownloadUrlCount = downloadUrlCount
        var currentDownloadLastIndex = 0

        while (currentDownloadUrlCount >= 0) {
            threadPool.submit(Runnable {
                for (index in currentDownloadLastIndex until downloadCountPerThread + currentDownloadLastIndex) {
                    val itemDownloadUrl = imgPage.getImageItemUrlsPerPage()[index]
                    val result = DownloadManager.downloadFile(itemDownloadUrl, "$saveDirPath/${getFileName(itemDownloadUrl)}")
                    if (result) downloadedCount++
                    if (downloadedCount >= maxDownloadNums) {
                        threadPool.shutdownNow()
                        return@Runnable
                    }
                    itemDownloadCallBack.invoke(getFileName(itemDownloadUrl), result)
                }
                pageDownloadCallBack.invoke()
            })
            currentDownloadUrlCount -= downloadCountPerThread
            currentDownloadLastIndex += downloadCountPerThread
        }


    }

    private fun getFileName(downloadUrl: String): String {
        return downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1)
    }

}
