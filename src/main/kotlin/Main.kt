import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import model.*
import util.*
import java.io.File

fun main(args: Array<String>) {
    val work = Work()
    val ficList = skrape(BrowserFetcher) {
        request {
            url = "https://archiveofourown.org/users/itsOnNetflix/readings"
        }

        response {
            htmlDocument(File("C:\\Users\\emili\\IdeaProjects\\AOMO\\src\\main\\resources\\History_AO3.html")) {
                BODY {
                     OUTER_WRAPPER {
                         INNER_WRAPPER {
                             MAIN_READINGS_DASHBOARD {
                                 LIST_OF_READINGS {
                                     READINGS_ELEMENT {
                                        findAll{ this }
                                     }.map{ work.processFic(it.toString()) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    print(ficList[0])
}