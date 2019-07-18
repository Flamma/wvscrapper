package net.asqueados.wvscrap

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.htmlcleaner.{HtmlCleaner, TagNode}

trait PageScrapper {
    def getPosts(url: String): List[Post]
}

trait PageDownloader {
    def getHtml(url: String): String
}

object JSoupPageDownloader extends PageDownloader {
    val userAgent = "Mozilla/5.0"
    val browser = new JsoupBrowser(userAgent = userAgent)

    override def getHtml(url: String): String = browser.get(url).toString
}

class HtmlCleanerPageScrapper(downloader: PageDownloader) extends PageScrapper {
    import TagNodeExtend.tagNodeWrapper

    override def getPosts(url: String): List[Post] = {
        val cleaner = new HtmlCleaner
        val html = downloader.getHtml(url)
        val rootNode = cleaner.clean(html)

        val divs = getPostsDivs(rootNode)
        val postIds = divs.map(node => node.getAttributeByName("id"))
        val userNames = divs.map(getUserNameFromPostDiv)
        val tuples = postIds zip divs zip userNames

        tuples.map {
            case ((postId: String, div: TagNode), userName: String) => Post(userName, div.innerHtml)
        }

    }

    private def getPostsDivs(node: TagNode) = node.findAllByAtt("class", "contenido_msg")

    private def getUserNameFromPostDiv(postDiv: TagNode): String = {
        val postRow = postDiv.getParent.getParent
        postRow.findElementByName("a", true).getText.toString
    }
}