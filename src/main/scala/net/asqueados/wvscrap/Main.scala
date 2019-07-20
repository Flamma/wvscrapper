package net.asqueados.wvscrap

import io.circe.syntax._

object Main extends App {
    val scrapper = new WVScrapper(JSoupPageDownloader, HtmlCleanerPageBrowser)

    args.toList match {
        case "thread" :: url :: _ =>
            val posts = scrapper.getPosts(url)
            println(posts.asJson)
        case "subforum" :: url :: _ =>
            val threads = scrapper.getThreads(url)
            println(threads.asJson)

        case _ =>
            println("ERROR: You need to specify an order [thread|subforum] and an url")
            System.exit(-1)
    }
}

