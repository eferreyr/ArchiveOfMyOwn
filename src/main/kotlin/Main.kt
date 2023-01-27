import it.skrape.core.htmlDocument
import it.skrape.fetcher.*
import it.skrape.selects.ElementNotFoundException
import it.skrape.selects.eachText
import it.skrape.selects.html5.*
import java.io.File
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

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
const val FANDOM_HEADER = "h5.fandoms.heading"
const val TAG = "a.tag"
const val DATE_LAST_UPDATED = "p.datetime"
const val TAG_SECTION = "ul.tags.commas"
const val WARNING_TAG = "li.warnings"
const val RELATIONSHIP_TAG = "li.relationships"
const val CHARACTER_TAG = "li.characters"
const val FREEFORM_TAG = "li.freeforms"
const val SUMMARY_BLOCK = "blockquote.userstuff.summary"
const val SERIES_FOOTER = "ul.series"
const val STATS_FOOTER = "dl.stats"
const val LANGUAGE = "dd.language"
const val WORD_COUNT = "dd.words"
const val CHAPTER_COUNT = "dd.chapters"
const val COMMENTS_COUNT = "dd.comments"
const val KUDOS_COUNT = "dd.kudos"
const val BOOKMARKS_COUNT = "dd.bookmarks"
const val HITS_COUNT = "dd.hits"

data class Fic(
    var id: String = "",
    var header: Header = Header(),
    var tags: Tags = Tags(),
    var summary: String = "",
    var isSeries: Boolean = false,
    var series: Series? = null,
    var stats: Stats = Stats()
)

data class Header(
    var title: String = "",
    var author: List<String> = listOf(),
    var fandom: List<String> = listOf(),
    var dateLastUpdated: String = ""
)

data class Tags(
    var warningTag: String = "",
    var relationshipTags: List<String> = listOf(),
    var characterTags: List<String> = listOf(),
    var freeformTags: List<String> = listOf()
)

data class Series(
    var name: String = "",
    var part: Int = 0
)

data class Stats(
    var language: String = "",
    var wordCount: Int = 0,
    var chapterCount: Int = 0,
    var commentCount: Int = 0,
    var kudosCount: Int = 0,
    var bookmarkCount: Int = 0,
    var hitCount: Int = 0
)

fun main(args: Array<String>) {
    val ficList = skrape(BrowserFetcher) {
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
                                        findAll{ this }
                                     }.map{ processFic(it.toString()) }
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

fun processFic(rawFic: String): Fic {
//    println(rawFic)
    val fic = Fic()

    htmlDocument(rawFic) {
        li {
            fic.id = findFirst { id }.split("_").last()

            FIC_HEADER {
                fic.header = processHeader(findFirst { this }.toString(), fic.id)
            }

            TAG_SECTION {
                fic.tags = processTags(findFirst { this }.toString())
            }

            SUMMARY_BLOCK {
                p {
                    fic.summary = findFirst { text }
                }
            }

            try {
                SERIES_FOOTER {
                    fic.series = processSeries(findFirst { this }.toString())
                    fic.isSeries = true
                }
            } catch (e: ElementNotFoundException) {}

            STATS_FOOTER {
                fic.stats = processStats(findFirst { this }.toString())
            }
        }
    }

    return fic
}

fun processHeader(rawHeader: String, id: String): Header {
    val header = Header()

    htmlDocument(rawHeader) {
        TITLE_AND_AUTHOR_HEADER {
            val ficTitle = "a[href='https://archiveofourown.org/works/${id}']"

            ficTitle {
                header.title = findFirst { text }
            }

            FIC_AUTHOR {
                header.author = findAll { eachText }
            }
        }

        FANDOM_HEADER {
            TAG {
                header.fandom = findAll { eachText }
            }
        }

        DATE_LAST_UPDATED {
            header.dateLastUpdated = findFirst { text }
        }
    }

    return header
}

fun processTags(rawTagSection: String): Tags {
//    println(rawTagSection)
    val tags = Tags()

    htmlDocument(rawTagSection) {
        relaxed = true

        WARNING_TAG {
            strong {
                TAG {
                    tags.warningTag = findFirst { text }
                }
            }
        }

        RELATIONSHIP_TAG {
            TAG {
                tags.relationshipTags = findAll { eachText }
            }
        }

        CHARACTER_TAG {
            TAG {
                tags.characterTags = findAll { eachText }
            }
        }

        FREEFORM_TAG {
            TAG {
                tags.freeformTags = findAll { eachText }
            }
        }
    }

    return tags
}

fun processSeries(rawSeries: String): Series {
    val series = Series()

    htmlDocument(rawSeries) {
        strong {
            val numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH) as DecimalFormat
            series.part = numberFormat.parse(findFirst { text }).toInt()
        }

        a {
            series.name = findFirst { text }
        }
    }

    return series
}

fun processStats(rawStats: String): Stats {
    val stats = Stats()

    htmlDocument(rawStats) {
        val numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH) as DecimalFormat

        LANGUAGE {
            stats.language = findFirst { text }
        }

        WORD_COUNT {
            stats.wordCount = numberFormat.parse(findFirst { text }).toInt()
        }

        CHAPTER_COUNT {
            try {
                a {
                    stats.chapterCount = numberFormat.parse(findFirst { text }).toInt()
                }
            } catch (e: ElementNotFoundException) {
                stats.chapterCount = 1
            }

        }

        COMMENTS_COUNT {
            a {
                stats.commentCount = numberFormat.parse(findFirst { text }).toInt()
            }
        }

        KUDOS_COUNT {
            a {
                stats.kudosCount = numberFormat.parse(findFirst { text }).toInt()
            }
        }

        BOOKMARKS_COUNT {
            a {
                stats.bookmarkCount = numberFormat.parse(findFirst { text }).toInt()
            }
        }

        HITS_COUNT {
            stats.hitCount = numberFormat.parse(findFirst { text }).toInt()
        }
    }

    return stats
}