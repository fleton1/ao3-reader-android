package com.ao3reader.data.remote

import com.ao3reader.data.remote.models.ChapterDto
import com.ao3reader.data.remote.models.WorkDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scraper for AO3 website. Parses HTML to extract work and chapter data.
 * All network requests are rate-limited to comply with AO3's Terms of Service.
 */
@Singleton
class AO3Scraper @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val rateLimiter: RateLimiter
) {
    companion object {
        private const val BASE_URL = "https://archiveofourown.org"
        private const val USER_AGENT = "AO3Reader/1.0 (Educational Project)"
        private const val VIEW_ADULT_PARAM = "?view_adult=true"

        // Date formats used by AO3
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    /**
     * Searches for works matching the query and filters.
     * @param query Search query string
     * @param page Page number (1-indexed)
     * @return List of WorkDto objects
     */
    suspend fun searchWorks(
        query: String,
        page: Int = 1
    ): Result<List<WorkDto>> = withContext(Dispatchers.IO) {
        rateLimiter.throttle {
            try {
                val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                val url = "$BASE_URL/works/search?work_search[query]=$encodedQuery&page=$page"

                val document = fetchDocument(url)
                val works = parseSearchResults(document)

                Result.success(works)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Fetches detailed information about a work.
     * @param workId The AO3 work ID
     * @return WorkDto with complete work metadata
     */
    suspend fun getWork(workId: String): Result<WorkDto> = withContext(Dispatchers.IO) {
        rateLimiter.throttle {
            try {
                val url = "$BASE_URL/works/$workId$VIEW_ADULT_PARAM"
                val document = fetchDocument(url)
                val work = parseWorkDetail(document, workId)

                Result.success(work)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Fetches a specific chapter of a work.
     * @param workId The AO3 work ID
     * @param chapterNumber The chapter number (1-indexed)
     * @return ChapterDto with chapter content
     */
    suspend fun getChapter(
        workId: String,
        chapterNumber: Int
    ): Result<ChapterDto> = withContext(Dispatchers.IO) {
        rateLimiter.throttle {
            try {
                val url = if (chapterNumber == 1) {
                    "$BASE_URL/works/$workId$VIEW_ADULT_PARAM"
                } else {
                    "$BASE_URL/works/$workId/chapters/${getChapterId(workId, chapterNumber)}$VIEW_ADULT_PARAM"
                }

                val document = fetchDocument(url)
                val chapter = parseChapter(document, workId, chapterNumber)

                Result.success(chapter)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Fetches all chapters of a work at once.
     * Uses the "?view_full_work=true" parameter to get all chapters in one request.
     */
    suspend fun getAllChapters(workId: String): Result<List<ChapterDto>> = withContext(Dispatchers.IO) {
        rateLimiter.throttle {
            try {
                val url = "$BASE_URL/works/$workId?view_full_work=true&view_adult=true"
                val document = fetchDocument(url)
                val chapters = parseAllChapters(document, workId)

                Result.success(chapters)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Fetches an HTML document from the given URL.
     */
    private fun fetchDocument(url: String): Document {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()

        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }

        val html = response.body?.string() ?: throw Exception("Empty response body")
        return Jsoup.parse(html)
    }

    /**
     * Parses search results page to extract work summaries.
     */
    private fun parseSearchResults(document: Document): List<WorkDto> {
        val works = mutableListOf<WorkDto>()
        val workElements = document.select("li.work")

        workElements.forEach { element ->
            try {
                val workDto = parseWorkFromSearchResult(element)
                works.add(workDto)
            } catch (e: Exception) {
                // Skip works that fail to parse
                e.printStackTrace()
            }
        }

        return works
    }

    /**
     * Parses a single work element from search results.
     */
    private fun parseWorkFromSearchResult(element: Element): WorkDto {
        val id = element.attr("id").removePrefix("work_")
        val heading = element.selectFirst("h4.heading")
        val title = heading?.selectFirst("a[href*=/works/]")?.text() ?: "Unknown Title"
        val author = heading?.selectFirst("a[rel=author]")?.text() ?: "Anonymous"
        val authorId = heading?.selectFirst("a[rel=author]")?.attr("href")
            ?.substringAfter("/users/")?.substringBefore("/")

        // Tags and metadata
        val fandoms = element.select("h5.fandoms a").joinToString(", ") { it.text() }
        val warnings = element.select("li.warnings a").joinToString(", ") { it.text() }
        val rating = element.selectFirst("span.rating")?.text() ?: "Not Rated"
        val category = element.select("span.category").joinToString(", ") { it.text() }
        val relationships = element.select("li.relationships a").joinToString(", ") { it.text() }
        val characters = element.select("li.characters a").joinToString(", ") { it.text() }
        val freeformTags = element.select("li.freeforms a").joinToString(", ") { it.text() }

        // Summary
        val summary = element.selectFirst("blockquote.summary")?.text() ?: ""

        // Stats
        val stats = element.selectFirst("dl.stats")
        val language = stats?.selectFirst("dd.language")?.text() ?: "English"
        val words = stats?.selectFirst("dd.words")?.text()?.replace(",", "")?.toIntOrNull() ?: 0
        val chapters = stats?.selectFirst("dd.chapters")?.text() ?: "1/1"
        val chaptersParts = chapters.split("/")
        val currentChapters = chaptersParts[0].toIntOrNull() ?: 1
        val totalChapters = chaptersParts.getOrNull(1) ?: "1"
        val kudos = stats?.selectFirst("dd.kudos")?.text()?.replace(",", "")?.toIntOrNull() ?: 0
        val bookmarks = stats?.selectFirst("dd.bookmarks")?.text()?.replace(",", "")?.toIntOrNull() ?: 0
        val hits = stats?.selectFirst("dd.hits")?.text()?.replace(",", "")?.toIntOrNull() ?: 0

        // Dates
        val dateElement = element.selectFirst("p.datetime")
        val updatedDate = parseDate(dateElement?.text())
        val publishedDate = updatedDate // Approximate, search results don't show published date

        val isComplete = currentChapters.toString() == totalChapters && totalChapters != "?"

        return WorkDto(
            id = id,
            title = title,
            author = author,
            authorId = authorId,
            summary = summary,
            rating = rating,
            warnings = warnings,
            category = category,
            fandom = fandoms,
            relationships = relationships,
            characters = characters,
            additionalTags = freeformTags,
            language = language,
            publishedDate = publishedDate,
            updatedDate = updatedDate,
            words = words,
            currentChapters = currentChapters,
            totalChapters = totalChapters,
            kudos = kudos,
            bookmarksCount = bookmarks,
            hits = hits,
            isComplete = isComplete
        )
    }

    /**
     * Parses the work detail page.
     */
    private fun parseWorkDetail(document: Document, workId: String): WorkDto {
        val preface = document.selectFirst("div.preface") ?: throw Exception("Work not found")

        val title = document.selectFirst("h2.title")?.text()?.trim() ?: "Unknown Title"
        val author = document.selectFirst("a[rel=author]")?.text() ?: "Anonymous"
        val authorId = document.selectFirst("a[rel=author]")?.attr("href")
            ?.substringAfter("/users/")?.substringBefore("/")

        val summary = document.selectFirst("div.summary blockquote")?.html() ?: ""

        // Tags
        val rating = document.selectFirst("dd.rating a")?.text() ?: "Not Rated"
        val warnings = document.select("dd.warning a").joinToString(", ") { it.text() }
        val category = document.select("dd.category a").joinToString(", ") { it.text() }
        val fandoms = document.select("dd.fandom a").joinToString(", ") { it.text() }
        val relationships = document.select("dd.relationship a").joinToString(", ") { it.text() }
        val characters = document.select("dd.character a").joinToString(", ") { it.text() }
        val freeformTags = document.select("dd.freeform a").joinToString(", ") { it.text() }

        // Stats
        val stats = document.selectFirst("dl.stats")
        val language = stats?.selectFirst("dd.language")?.text() ?: "English"
        val words = stats?.selectFirst("dd.words")?.text()?.replace(",", "")?.toIntOrNull() ?: 0
        val chapters = stats?.selectFirst("dd.chapters")?.text() ?: "1/1"
        val chaptersParts = chapters.split("/")
        val currentChapters = chaptersParts[0].toIntOrNull() ?: 1
        val totalChapters = chaptersParts.getOrNull(1) ?: "1"
        val kudos = stats?.selectFirst("dd.kudos")?.text()?.replace(",", "")?.toIntOrNull() ?: 0
        val bookmarks = stats?.selectFirst("dd.bookmarks")?.text()?.replace(",", "")?.toIntOrNull() ?: 0
        val hits = stats?.selectFirst("dd.hits")?.text()?.replace(",", "")?.toIntOrNull() ?: 0

        // Dates
        val published = stats?.selectFirst("dd.published")?.text()
        val updated = stats?.selectFirst("dd.status")?.text()
        val publishedDate = parseDate(published)
        val updatedDate = parseDate(updated) ?: publishedDate

        val isComplete = currentChapters.toString() == totalChapters && totalChapters != "?"

        // Series info
        val seriesElement = document.selectFirst("dd.series")
        val isSeries = seriesElement != null
        val seriesName = seriesElement?.selectFirst("a")?.text()
        val seriesPart = seriesElement?.selectFirst("span.position")?.text()
            ?.filter { it.isDigit() }?.toIntOrNull()

        return WorkDto(
            id = workId,
            title = title,
            author = author,
            authorId = authorId,
            summary = summary,
            rating = rating,
            warnings = warnings,
            category = category,
            fandom = fandoms,
            relationships = relationships,
            characters = characters,
            additionalTags = freeformTags,
            language = language,
            publishedDate = publishedDate,
            updatedDate = updatedDate,
            words = words,
            currentChapters = currentChapters,
            totalChapters = totalChapters,
            kudos = kudos,
            bookmarksCount = bookmarks,
            hits = hits,
            isComplete = isComplete,
            isSeries = isSeries,
            seriesName = seriesName,
            seriesPart = seriesPart
        )
    }

    /**
     * Parses a chapter from the work page.
     */
    private fun parseChapter(document: Document, workId: String, chapterNumber: Int): ChapterDto {
        val chapterTitle = document.selectFirst("h3.title")?.text()?.trim()
        val summary = document.selectFirst("div.summary blockquote")?.html()
        val notes = document.selectFirst("div.notes blockquote")?.html()
        val endNotes = document.selectFirst("div.end.notes blockquote")?.html()

        val content = document.selectFirst("div[role=article]")?.html()
            ?: throw Exception("Chapter content not found")

        // Approximate word count
        val wordCount = content.split("\\s+".toRegex()).size

        return ChapterDto(
            workId = workId,
            chapterNumber = chapterNumber,
            title = chapterTitle,
            summary = summary,
            notes = notes,
            endNotes = endNotes,
            content = content,
            wordCount = wordCount
        )
    }

    /**
     * Parses all chapters from a full work view.
     */
    private fun parseAllChapters(document: Document, workId: String): List<ChapterDto> {
        val chapters = mutableListOf<ChapterDto>()
        val chapterElements = document.select("div.chapter")

        chapterElements.forEachIndexed { index, element ->
            val chapterNumber = index + 1
            val chapterTitle = element.selectFirst("h3.title")?.text()?.trim()
            val summary = element.selectFirst("div.summary blockquote")?.html()
            val notes = element.selectFirst("div.notes blockquote")?.html()
            val content = element.selectFirst("div[role=article]")?.html() ?: ""
            val endNotes = element.selectFirst("div.end.notes blockquote")?.html()

            val wordCount = content.split("\\s+".toRegex()).size

            chapters.add(
                ChapterDto(
                    workId = workId,
                    chapterNumber = chapterNumber,
                    title = chapterTitle,
                    summary = summary,
                    notes = notes,
                    endNotes = endNotes,
                    content = content,
                    wordCount = wordCount
                )
            )
        }

        return chapters
    }

    /**
     * Gets the chapter ID for a specific chapter number.
     * This is a simplified version - in reality, we'd need to parse the work page
     * to get the actual chapter IDs.
     */
    private suspend fun getChapterId(workId: String, chapterNumber: Int): String {
        // For now, we'll use chapter number directly
        // In a real implementation, we'd fetch the work page and extract chapter IDs
        return chapterNumber.toString()
    }

    /**
     * Parses a date string from AO3 format.
     */
    private fun parseDate(dateString: String?): Long {
        if (dateString == null) return System.currentTimeMillis()

        return try {
            DATE_FORMAT.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
