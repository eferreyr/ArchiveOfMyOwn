import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import it.skrape.selects.html5.*
import java.io.File

/* CSS Selectors */
const val BODY = "body.logged-in.javascript"
const val OUTER_WRAPPER = "div.wrapper[id='outer']"
const val INNER_WRAPPER = "div.wrapper[id='inner']"
const val MAIN_DASHBOARD = "div.readings-index.dashboard.region[id='main']"
const val LIST_OF_FICS = "ol.reading.work.index.group"
const val FIC_ELEMENT = "li.reading.work.blurb.group"
const val FIC_HEADER = "div.header.module"
const val TITLE_AND_AUTHOR_HEADER = "h4.heading"
const val FIC_AUTHOR = "a[rel='author']"

data class Fic(
    var id: String = "",
    var title: String = "",
    var author: String = ""
)

fun main(args: Array<String>) {
    val ao3Client = skrape(BrowserFetcher) {
        request {
            url = "https://archiveofourown.org/users/itsOnNetflix/readings"
        }

        response {
            htmlDocument(File("C:\\Users\\emili\\IdeaProjects\\AOMO\\src\\main\\resources\\History_AO3.html")) {
                BODY {
                     OUTER_WRAPPER {
                         INNER_WRAPPER {
                             MAIN_DASHBOARD {
                                 LIST_OF_FICS {
                                     FIC_ELEMENT {
                                        findAll{ this } }.map{ processFic(it.toString()) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    print(ao3Client[0])
}

fun processFic(rawFic: String): Fic {
//    println(rawFic)
    val fic = Fic()

    htmlDocument(rawFic) {
        li {
            fic.id = findFirst { id }?.split("_")?.last() ?: "*"
             FIC_HEADER {
                 TITLE_AND_AUTHOR_HEADER {
                     val ficTitle = "a[href='https://archiveofourown.org/works/${fic.id}']"
                     
                     ficTitle {
                        fic.title = findFirst { text }
                    }

                     FIC_AUTHOR {
                        fic.author = findFirst { text }
                    }
                }
            }
        }
    }

    return fic
}