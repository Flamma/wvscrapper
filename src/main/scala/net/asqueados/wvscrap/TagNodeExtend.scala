package net.asqueados.wvscrap

import java.util.regex.Pattern

import org.htmlcleaner.conditional.ITagNodeCondition
import org.htmlcleaner.{CommentNode, ContentNode, TagNode}

import scala.collection.JavaConverters._
import scala.util.matching.Regex

class TagNodeExtend(self: TagNode) {
    def innerHtml: String = privInnerHtml(self, false)
    def findAllByAtt(name:String, pattern: Regex): List[TagNode] = privFindAllByAtt(self, name, pattern)
    def findAllByAtt(name:String, value: String): List[TagNode] = privFindAllByAtt(self, name, value.r)

    private def privInnerHtml(node: TagNode, printOuter: Boolean = true): String = {

        outerStart(node, printOuter) +  (if(!node.hasChildren)
            node.getText.toString
        else
            node.getAllChildren.asScala.map {
                case child: TagNode => privInnerHtml(child)
                case child: ContentNode => child.getContent
                case _: CommentNode => ""
            }.mkString("")
        ) + outerEnd(node, printOuter)
    }

    private def outerStart(node: TagNode, printOuter: Boolean) =
        if(printOuter) s"<${node.getName}${attString(node.getAttributes.asScala.toMap)}>"
        else ""

    private def outerEnd(node: TagNode, printOuter: Boolean) =
        if(printOuter) s"</${node.getName}>"
        else ""

    private def attString(attributes: Map[String, String]): String = attributes.foldLeft(""){
        case (acc: String, (name: String, value: String)) => acc + s""" $name = "$value""""
    }

    private def privFindAllByAtt(node: TagNode, name: String, pattern: Regex): List[TagNode] = {
        node.getAttributes.asScala.get(name).filter(pattern.findFirstIn(_).isDefined).fold(List.empty[TagNode])(_ => List(node)) ++
        node.getAllChildren.asScala.toList.flatMap {
            case child: TagNode => privFindAllByAtt(child, name, pattern)
            case _ => List.empty[TagNode]
        }
    }
}

object TagNodeExtend {
    implicit def tagNodeWrapper(node: TagNode) = new TagNodeExtend(node)
}

class TagNodeAttNameValueRegexIdNotInListCondition(attNameRegex: Pattern, attValueRegex: Pattern, idList: List[String]) extends ITagNodeCondition {
    override def satisfy(tagNode: TagNode): Boolean =
        ( tagNode != null) &&
            (!idList.contains(tagNode.getAttributeByName("id"))) &&
            tagNode.getAttributes().asScala.exists {
                case (name, value) => attNameRegex.matcher(name).find && attValueRegex.matcher(value).find
            }
}