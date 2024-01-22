package model

import it.skrape.core.htmlDocument
import it.skrape.selects.ElementNotFoundException
import it.skrape.selects.eachText
import it.skrape.selects.html5.a
import it.skrape.selects.html5.li
import it.skrape.selects.html5.p
import it.skrape.selects.html5.strong
import util.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class Work{
    fun processFic(rawFic: String): Fic {
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
                } catch (_: ElementNotFoundException) {}

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

            REQUIRED_TAGS {
                li {
                    findAll{ this }
                }
            }

            DATE_LAST_UPDATED {
                header.dateLastUpdated = findFirst { text }
            }
        }

        return header
    }

    fun processTags(rawTagSection: String): Tags {
        val tags = Tags()

        htmlDocument(rawTagSection) {
            relaxed = true

            WARNING_TAG {
                strong {
                    TAG {
                        tags.warningTag = findAll { eachText }
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
}

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
    var dateLastUpdated: String = "",
    var requiredTags: RequiredTags = RequiredTags()
)

data class RequiredTags(
    var rating: String = "",
    var warning: List<String> = listOf(),
    var category: List<String> = listOf(),
    var isComplete: Boolean = false
)

data class Tags(
    var warningTag: List<String> = listOf(),
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