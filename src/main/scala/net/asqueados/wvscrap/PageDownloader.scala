package net.asqueados.wvscrap

import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.util.Try

trait PageDownloader {
    def getHtml(url: String): String
}

object JSoupPageDownloader extends PageDownloader {
    private val retries = 10
    private val timeToRetry = 1000

    val userAgent = "Mozilla/5.0"
    val browser = new JsoupBrowser(userAgent = userAgent)

    override def getHtml(url: String): String = retryGetHtml(url, retries)

    private def retryGetHtml(url: String, retries: Int): String = {
        Try(browser.get(url).toString).recover {
            case _ if retries > 0 =>
                java.lang.Thread sleep timeToRetry
                retryGetHtml(url, retries-1)
            case e => throw e
        }.get
    }
}

