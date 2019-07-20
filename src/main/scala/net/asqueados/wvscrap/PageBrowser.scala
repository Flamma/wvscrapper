package net.asqueados.wvscrap

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import org.htmlcleaner.{HtmlCleaner, TagNode}

trait PageBrowser {
    def getPosts(html: String): List[Post]
    def getThreads(html: String): List[Link]
    def getNextPageUrl(html: String): Option[String]
    def getTitle(html: String): String
}

object HtmlCleanerPageBrowser extends PageBrowser {
    private val MsgIdPattern = "e_msg_[0-9]*".r
    private val MiADateFormat = DateTimeFormatter.ofPattern("d/MMM/y, k:m").withLocale(new Locale("es"))
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
        val times = divs.map(getTimeFromPostDiv)
        val tuples = msgDivs zip userNames zip times

        tuples.map {
            case ((div: TagNode, userName: String), time: LocalDateTime) => Post(userName, time, div.innerHtml)
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

    private def getTimeFromPostDiv(postDiv: TagNode): LocalDateTime = {
        getDateTime(postDiv.findElementByName("time", true).getText.toString)
    }

    private def getDateTime(stringTime: String): LocalDateTime = LocalDateTime.parse(stringTime.toLowerCase, MiADateFormat)
    private def getMsgDivs(node: TagNode): List[TagNode] = node.findAllByAtt("id", MsgIdPattern)

    private def getThreadDivs(node: TagNode): List[TagNode] = {
        val topicsNode = node.findElementByAttValue("id", TopicIndexId, true, true)
        topicsNode.findAllByAtt("class", ThreadClass)
    }


}
