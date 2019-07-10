package com.nesp.linux.changewall

import com.nesp.linux.changewall.bing.DownloadBiyingImgManager
import com.nesp.linux.changewall.model.Settings
import com.nesp.linux.changewall.shell.ShellEngine
import com.nesp.linux.changewall.utils.FileUtils
import java.util.ArrayList
import java.io.File


class Main {

    private val supportImagePostfixs = arrayOf("png", "jpg", "jpeg", "svg")
    private val cacheDirPath = "cache"

    fun init() {
        val cacheDir = File(cacheDirPath)
        if (cacheDir.exists()) {
            cacheDir.delete()
        }
        ShellEngine.exec("mkdir $cacheDirPath")
    }


    private fun getSupportImageFiles(wallImageDir: String): List<File> {
        val files = ArrayList<File>()
        val fileWallDir = File(wallImageDir)
        if (!fileWallDir.exists()) return listOf()
        for (file in fileWallDir.listFiles()!!) {
            if (file.isDirectory) {
                files.addAll(getSupportImageFiles(file.absolutePath))
            } else {
                if (isSupportImage(file)) {
                    files.add(file)
                }
            }
        }
        return files
    }

    private fun isSupportImage(imageFile: File): Boolean {
        val fileName = imageFile.name
        return isSupportImage(fileName.substring(fileName.lastIndexOf(".") + 1))
    }

    private fun getPostfix(fileName: String): String {
        return fileName.substring(fileName.lastIndexOf(".") + 1)
    }

    private fun isSupportImage(imagePostfix: String): Boolean {
        for (supportImagePostfix in supportImagePostfixs) {
            if (supportImagePostfix.equals(imagePostfix, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun setWall(wallImageFile: File): ShellEngine.ExecResult? {
        return ShellEngine.exec("gsettings set org.gnome.desktop.background picture-uri file://${wallImageFile.absolutePath}")
    }

    private fun blurImage(settings: Settings, imageFile: File, callback: (blurImagePath: String) -> Unit) {
        Thread(Runnable {
            val blurImagePath = "/usr/share/backgrounds/gdmlock.jpg"
            ShellEngine.exec("pkexec convert -resize 1440 -quality 100 -brightness-contrast -10x-15 -blur 0x30 ${imageFile.absolutePath} ${imageFile.absolutePath}")
            Thread.sleep(3)
            callback.invoke(blurImagePath)
        }).start()
    }

    private fun setLockScreenBackground(imageFile: File) {
        ShellEngine.exec("gsettings set org.gnome.desktop.screensaver picture-uri \"file://${imageFile.absolutePath}\"")
    }

    private fun changeWall(imageFile: File) {
        FileUtils.copyFile(imageFile.absolutePath, "$cacheDirPath/wall.${getPostfix(imageFile.name)}")
        var copiedFile = File(cacheDirPath + "/wall." + getPostfix(imageFile.name))
        setWall(copiedFile)
//        ShellEngine.exec("echo jin | sudo -S ./exec.sh " + File(cacheDirPath + "/wall." + getPostfix(imageFile.name)).absolutePath)
    }


//    var settings: Settings? = null

    fun exec() {
        init()

        val settings = SettingsManager().getSettings()

        if (settings!!.dl_biying_img_max_count > 0)
            Thread(Runnable {
                DownloadBiyingImgManager(settings).start()
            }).start()
        val wallImageDir = settings.wall_img_dir

        Thread(Runnable {
            Thread.sleep(8000)
            if (settings.cycle_time < 1) {
                return@Runnable
            }

            while (true) {
                val listImage = getSupportImageFiles(wallImageDir)
                println("listImgSize  ${listImage.size}")
                if (listImage.isEmpty()) {
                    Thread.sleep(5000)
                    continue
                }
                for (index in 0 until listImage.size) {
                    print(listImage[index].name + "\n")
//                    if (settings.change_lock_background) {
//                        if (settings.blur_lock_background) {
//                            ShellEngine.exec("echo jin | sudo -S  shell/exec.sh ${listImage[index].absolutePath}")
//                        } else {
//                            changeWall(listImage[index])
//                            setLockScreenBackground(listImage[index])
//                        }
//                    } else {
                    changeWall(listImage[index])
                    Thread.sleep(settings.cycle_time)

//                    }
                }
            }

        }).start()
    }


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Main().exec()
        }


    }
}
