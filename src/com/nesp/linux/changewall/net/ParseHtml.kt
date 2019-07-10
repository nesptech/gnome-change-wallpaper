/*
 *
 *   Copyright (c) 2019  NESP Technology Corporation. All rights reserved.
 *
 *   This program is free software; you can redistribute it and/or modify it
 *   under the terms and conditions of the GNU General Public License,
 *   version 2, as published by the Free Software Foundation.
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License.See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   If you have any questions or if you find a bug,
 *   please contact the author by email or ask for Issues.
 *
 *   Author:JinZhaolu <1756404649@qq.com>
 */

package com.nesp.linux.changewall.net


import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.util.ArrayList

/**
 * @Team: NESP Technology
 * @Author: 靳兆鲁
 * Email: 1756404649@qq.com
 * @Time: Created 2018/7/20 20:18
 * @Project qiushibaike
 */
class ParseHtml private constructor(private var url: String?) {

    private val TAG = "ParseHtml"
    private var userAgentType = UserAgentType.MOBILE

    val userNames: List<String>?
        @Throws(Exception::class)
        get() {
            val list = getElementsValue("div.author.clearfix > a:nth-child(2) > h2")
            return if (list.size > 0)
                list
            else
                null
        }

    val userContents: List<String>?
        @Throws(Exception::class)
        get() {
            val list = getElementsValue("a.contentHerf")
            return if (list.size > 0)
                list
            else
                null
        }

    val document: Document?
        @Throws(Exception::class)
        get() {
            val document: Document

            var userAgent = "Mozilla/5.0 (Linux; U; Android 8.0; en-us; Xoom Build/HRI39) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13"
            if (userAgentType == UserAgentType.MOBILE) {
                userAgent = "Mozilla/5.0 (Linux; U; Android 8.0; en-us; Xoom Build/HRI39) AppleWebKit/534.13 (KHTML, like Gecko) Version/4.0 Safari/534.13"
            } else if (userAgentType == UserAgentType.PC) {
                userAgent = "Mozilla/5.0 (Windows NT 6.1; rv:22.0) Gecko/20100101 Firefox/22.0"
            }
            document = Jsoup
                    .connect(url)
                    .userAgent(userAgent)
                    .ignoreContentType(true)
                    .timeout(30000)
                    .get()
            return document
        }

    fun setUserAgentType(userAgentType: UserAgentType): ParseHtml {
        this.userAgentType = userAgentType
        return this
    }

    fun setUrl(url: String): ParseHtml {
        this.url = url
        return this
    }

    @Throws(Exception::class)
    fun getElementsByClassName(className: String): Elements {
        return document!!.getElementsByClass(className)
    }

    @Throws(Exception::class)
    fun getElementsByTagName(tagName: String): Elements {
        return document!!.getElementsByTag(tagName)
    }

    @Throws(Exception::class)
    fun getElementByIdName(idName: String): Element {
        return document!!.getElementById(idName)
    }

    enum class UserAgentType {
        MOBILE, PC
    }

    @Throws(Exception::class)
    fun getElementsValue(cssQuary: String): List<String> {
        val listResult = ArrayList<String>()
        val document = document
        if (document != null) {
            for (element in document.select(cssQuary)) {
                listResult.add(element.text())
            }
        } else {
            listResult.add("")
        }
        return listResult
    }

    @Throws(Exception::class)
    fun getElementsBySelector(selector: String): Elements? {
        val document = document
        return document?.select(selector)
    }

    @Throws(Exception::class)
    fun getElementsAttrValue(cssQuary: String, attrKey: String): List<String> {
        val listResult = ArrayList<String>()
        val document = document
        if (document != null) {
            for (element in document.select(cssQuary)) {
                listResult.add(element.attr(attrKey))
            }
        } else {
        }
        return listResult
    }

    companion object {

        fun getInstance(url: String): ParseHtml {
            return ParseHtml(url)
        }
    }
}
