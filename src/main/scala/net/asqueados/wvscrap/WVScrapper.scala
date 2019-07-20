package net.asqueados.wvscrap

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.htmlcleaner.{HtmlCleaner, TagNode}

import scala.util.Try

trait PageBrowser {
    def getPosts(html: String): List[Post]
    def getThreads(html: String): List[Link]
    def getNextPageUrl(html: String): Option[String]
    def getTitle(html: String): String
}

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

object HtmlCleanerPageBrowser extends PageBrowser {
    private val MsgIdPattern = "e_msg_[0-9]*".r
    private val NextTexts = List("Siguiente", "última")
    private val MessageClass = "contenido_msg"
    private val ThreadClass = "topicMsg"
    private val TopicIndexId = "ForoIndiceTemas"
    private val TitleClass = "texto_big"

    private val cleaner = new HtmlCleaner

    import TagNodeExtend.tagNodeWrapper

    override def getPosts(html: String): List[Post] = {
        val rootNode = cleaner.clean(html)

        val divs = getPostsDivs(rootNode)
        val msgDivs = divs.flatMap(getMsgDivs)
        val userNames = divs.map(getUserNameFromPostDiv)
        val tuples = msgDivs zip userNames

        tuples.map {
            case (div: TagNode, userName: String) => Post(userName, div.innerHtml)
        }

    }

    override def getThreads(html: String): List[Link] = {
        val rootNode = cleaner.clean(html)

        val divs = getThreadDivs(rootNode)
        val links = divs.map(_.getAllElements(false)(0))

        links.map { link => Link(link.getText.toString, link.getAttributeByName("href")) }
    }

    override def getNextPageUrl(html: String): Option[String] = {
        val rootNode = cleaner.clean(html)
        val nextNode = rootNode.getElementsByName("a", true).find(n => NextTexts.contains(n.getText.toString))
        nextNode.map(_.getAttributeByName("href"))
    }

    override def getTitle(html: String): String = {
        val rootNode = cleaner.clean(html)
        val titleDiv = rootNode.findElementByAttValue("class", TitleClass, true, true)
        titleDiv.getElementsByName("a", true)(0).getText.toString
    }

    private def getPostsDivs(node: TagNode): List[TagNode] = node.findAllByAtt("class", MessageClass)

    private def getUserNameFromPostDiv(postDiv: TagNode): String = {
        val postRow = postDiv.getParent.getParent
        postRow.findElementByName("a", true).getText.toString
    }

    private def getMsgDivs(node: TagNode): List[TagNode] = node.findAllByAtt("id", MsgIdPattern)

    private def getThreadDivs(node: TagNode): List[TagNode] = {
        val topicsNode = node.findElementByAttValue("id", TopicIndexId, true, true)
        topicsNode.findAllByAtt("class", ThreadClass)
    }
}

class WVScrapper(downloader: PageDownloader, browser: PageBrowser) {
    private val baseUrl = "https://webvampiro.mforos.com"

    /**
      * Get a thread
      *
      * @param url url to one page of the thread
      * @return thread
      */
    def getThread(url: String): Thread = {
        val title = getTitle(cleanUrl(url))
        val posts = getRemainingPages(cleanUrl(url))
        Thread(title, posts)
    }


    /**
      * Get a subforum
      * @param url url to one page of the subforum
      * @return subforum
      */
    def getSubforum(url: String): Subforum = {
        val title = getTitle(cleanUrl(url))
        val threads = getRemainingThreads(cleanUrl(url))
        Subforum(title, threads)
    }

    private def getRemainingPages(url: String): List[Post] = {
        val html = downloader.getHtml(url)
        browser.getPosts(html) ++
            browser.getNextPageUrl(html).fold(List.empty[Post])(nextUrl => getRemainingPages(addBaseUrl(nextUrl)))
    }

    private def getRemainingThreads(url: String): List[Thread] = {
        val html = downloader.getHtml(url)

        browser.getThreads(html).map(getThread) ++
            browser.getNextPageUrl(html).fold(List.empty[Thread])(nextUrl => getRemainingThreads(addBaseUrl(nextUrl)))
    }

    private def getThread(link: Link): Thread = getThread(link.url)

    private def cleanUrl(url: String): String = addBaseUrl(url).split("\\?")(0)
    private def addBaseUrl(url:String): String = (if(!url.startsWith(baseUrl)) baseUrl else "") + url
    private def getTitle(url: String): String = browser.getTitle(downloader.getHtml(url))
}

case class Link(title: String, url: String)