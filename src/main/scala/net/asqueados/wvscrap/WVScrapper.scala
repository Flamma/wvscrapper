package net.asqueados.wvscrap

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.htmlcleaner.{HtmlCleaner, TagNode}

trait PageBrowser {
    def getPosts(url: String): List[Post]
    def getNextPageUrl(html: String): Option[String]
}

trait PageDownloader {
    def getHtml(url: String): String
}

object JSoupPageDownloader extends PageDownloader {
    val userAgent = "Mozilla/5.0"
    val browser = new JsoupBrowser(userAgent = userAgent)

    override def getHtml(url: String): String = browser.get(url).toString
}

object HtmlCleanerPageBrowser extends PageBrowser {
    private val MsgIdPattern = "e_msg_[0-9]*".r
    private val cleaner = new HtmlCleaner
    private val NextTexts = List("Siguiente", "última")

    import TagNodeExtend.tagNodeWrapper

    override def getPosts(html: String): List[Post] = {
        val rootNode = cleaner.clean(html)

        val divs = getPostsDivs(rootNode)
        val msgDivs = divs.flatMap(getMsgDivs)
        val postIds = divs.map(node => node.getAttributeByName("id"))
        val userNames = divs.map(getUserNameFromPostDiv)
        val tuples = msgDivs zip userNames

        tuples.map {
            case (div: TagNode, userName: String) => Post(userName, div.innerHtml)
        }

    }

    override def getNextPageUrl(html: String): Option[String] = {
        val rootNode = cleaner.clean(html)
        val tableNode = rootNode.findElementByAttValue("id", "ForoMensajes", true, true)
        val nextNode = tableNode.getElementsByName("a", true).find(n => NextTexts.contains(n.getText.toString))
        nextNode.map(_.getAttributeByName("href"))
    }

    private def getPostsDivs(node: TagNode) = node.findAllByAtt("class", "contenido_msg")

    private def getUserNameFromPostDiv(postDiv: TagNode): String = {
        val postRow = postDiv.getParent.getParent
        postRow.findElementByName("a", true).getText.toString
    }

    private def getMsgDivs(node: TagNode) = node.findAllByAtt("id", MsgIdPattern)
}

class WVScrapper(downloader: PageDownloader, browser: PageBrowser) {
    private val baseUrl = "https://webvampiro.mforos.com"

    /**
      * Get all posts from one thread
      *
      * @param url url to one page of the thread
      */
    def getPosts(url: String): List[Post] = {
        getRemainingPages(cleanUrl(url))
    }

    private def getRemainingPages(url: String): List[Post] = {
        val html = downloader.getHtml(url)
        browser.getPosts(html) ++
            browser.getNextPageUrl(html).fold(List.empty[Post])(nextUrl => getRemainingPages(addBaseUrl(nextUrl)))
    }

    private def cleanUrl(url: String): String = addBaseUrl(url).split("\\?")(0)
    private def addBaseUrl(url:String): String = (if(!url.startsWith(baseUrl)) baseUrl else "") + url
}