package net.asqueados.wvscrap

object Main extends App {
    args.toList.headOption.fold {
        println("ERROR: You need to specify an URL")
        System.exit(-1)
    } { url =>
        val scrapper = new WVScrapper(JSoupPageDownloader, HtmlCleanerPageBrowser)
        val posts = scrapper.getPosts(url)
        import io.circe.syntax._
        println(posts.asJson)
    }
}

