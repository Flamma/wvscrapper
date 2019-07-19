package net.asqueados.wvscrap

import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.TagNode

import java.net.URL
import java.util.UUID

import scala.collection.mutable.ListBuffer
import scala.io.Source


object Main extends App {
    val url = args.toList.headOption.fold("file:///home/pablo/practicas/wvscrap/in/offtopic.html")(identity)
    val scrapper = new HtmlCleanerPageScrapper(JSoupPageDownloader)
    val posts = scrapper.getPosts(url)
    import io.circe.syntax._
    println(posts.asJson)

}

