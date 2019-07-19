package net.asqueados.wvscrap

object Main extends App {
    val url = args.toList.headOption.fold("file:///home/pablo/practicas/wvscrap/in/offtopic.html")(identity)
    val scrapper = new WVScrapper(JSoupPageDownloader, HtmlCleanerPageBrowser)
    val posts = scrapper.getPosts(url)
    import io.circe.syntax._
    println(posts.asJson)
}

