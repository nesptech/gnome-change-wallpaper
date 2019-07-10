package com.nesp.linux.changewall

import com.nesp.linux.changewall.utils.FileUtils
import jdk.nashorn.internal.parser.JSONParser
import com.alibaba.fastjson.JSON
import com.nesp.linux.changewall.model.Settings
import java.io.File


class SettingsManager {
    private var settings: Settings? = null

    init {
        settings = if (!File("settings.json").exists()) {
            Settings()
        } else {
            val jsonSettings = FileUtils.readFileToString("settings.json")
            JSON.parseObject(jsonSettings, Settings::class.java)
        }
    }

    fun getSettings(): Settings? {
        return settings
    }

}