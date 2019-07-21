package net.asqueados.wvscrap

object Main extends App {
    val scrapper = new WVScrapper(JSoupPageDownloader, HtmlCleanerPageBrowser)

    args.toList match {
        case "thread" :: url :: _ =>
            val entity = scrapper.getThread(url)
            WVExporter.export[Thread](entity)
        case "subforum" :: url :: _ =>
            val entity = scrapper.getSubforum(url)
            WVExporter.export[Subforum](entity)

        case _ =>
            println("ERROR: You need to specify an order [thread|subforum] and an url")
            System.exit(-1)
    }
}

