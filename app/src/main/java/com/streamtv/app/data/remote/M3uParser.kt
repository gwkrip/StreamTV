package com.streamtv.app.data.remote

import com.streamtv.app.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3uParser @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    suspend fun parseFromUrl(url: String, playlistId: Long): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                }

                val content = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response"))

                Result.success(parseM3uContent(content, playlistId))
            } catch (e: Exception) {
                Timber.e(e, "Error parsing M3U from URL: $url")
                Result.failure(e)
            }
        }

    suspend fun parseFromContent(content: String, playlistId: Long): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(parseM3uContent(content, playlistId))
            } catch (e: Exception) {
                Timber.e(e, "Error parsing M3U content")
                Result.failure(e)
            }
        }

    private fun parseM3uContent(content: String, playlistId: Long): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lines()

        if (lines.isEmpty() || !lines.first().startsWith("#EXTM3U")) {
            Timber.w("Invalid M3U format - missing #EXTM3U header")
        }

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()

            if (line.startsWith("#EXTINF:")) {
                val channel = parseExtInfLine(line, playlistId)

                // Look for the URL on the next non-empty, non-comment line
                var j = i + 1
                while (j < lines.size) {
                    val nextLine = lines[j].trim()
                    if (nextLine.isNotEmpty() && !nextLine.startsWith("#")) {
                        val finalChannel = channel.copy(url = nextLine)
                        if (finalChannel.url.isNotBlank()) {
                            channels.add(finalChannel)
                        }
                        i = j
                        break
                    }
                    j++
                }
            }
            i++
        }

        Timber.d("Parsed ${channels.size} channels from M3U")
        return channels
    }

    private fun parseExtInfLine(line: String, playlistId: Long): Channel {
        // Extract duration and attributes from #EXTINF line
        // Format: #EXTINF:-1 tvg-id="..." tvg-name="..." tvg-logo="..." group-title="...",Channel Name

        val commaIndex = line.lastIndexOf(',')
        val name = if (commaIndex >= 0 && commaIndex < line.length - 1) {
            line.substring(commaIndex + 1).trim()
        } else ""

        val attributesPart = if (commaIndex >= 0) line.substring(8, commaIndex) else line.substring(8)

        val tvgId = extractAttribute(attributesPart, "tvg-id")
        val tvgName = extractAttribute(attributesPart, "tvg-name")
        val tvgLogo = extractAttribute(attributesPart, "tvg-logo")
        val groupTitle = extractAttribute(attributesPart, "group-title")
        val tvgLanguage = extractAttribute(attributesPart, "tvg-language")
        val tvgCountry = extractAttribute(attributesPart, "tvg-country")

        return Channel(
            name = tvgName?.takeIf { it.isNotBlank() } ?: name,
            url = "", // Will be set after parsing
            logoUrl = tvgLogo,
            group = groupTitle,
            language = tvgLanguage,
            country = tvgCountry,
            epgId = tvgId,
            playlistId = playlistId
        )
    }

    private fun extractAttribute(text: String, attributeName: String): String? {
        val pattern = Regex("""$attributeName="([^"]*)"""")
        return pattern.find(text)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
    }
}
