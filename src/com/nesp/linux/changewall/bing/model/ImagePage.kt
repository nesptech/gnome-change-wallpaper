package com.nesp.linux.changewall.bing.model

import com.nesp.linux.changewall.net.ParseHtml
import com.sun.org.apache.xpath.internal.operations.Bool

data class ImagePage(var currentUrl: String) {

    private var parseHtml: ParseHtml? = null

    init {
        parseHtml = ParseHtml.getInstance(currentUrl)
    }

    fun getImageItemUrlsPerPage(): MutableList<String> {
        val result = mutableListOf<String>()
        val items = parseHtml!!.getElementsBySelector("body > div.container")!![0]?.getElementsByClass("item")
        items!!.forEach { item ->
            result.add(item.getElementsByTag("img")[0].absUrl("src"))
        }
        return result
    }

    fun getNextPageUrl(): String {
        val page = parseHtml!!.getElementsByClassName("page")[0]
        val pages = page.getElementsByTag("span")[0].text()
        if (pages.split("/")[0].trim().toInt() / pages.split("/")[1].trim().toInt() < 1) {
            return page.getElementsByTag("a")[1].absUrl("href")
        }
        return ""
    }

}

