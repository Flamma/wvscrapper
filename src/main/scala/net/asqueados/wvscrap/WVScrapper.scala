package net.asqueados.wvscrap

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