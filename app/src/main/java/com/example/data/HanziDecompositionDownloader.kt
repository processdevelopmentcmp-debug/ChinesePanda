package com.example.data

import android.util.Log
import com.example.data.local.HanziCharacterEntity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

private const val HANZI_SYNC_TAG = "HanziDecompSync"

/**
 * Streams and parses the open-source Make Me a Hanzi dictionary (`skishore/makemeahanzi/dictionary.txt`,
 * ~9,574 Chinese characters with Kangxi radicals, IDS structural decomposition trees, and
 * pictographic/ideographic/pictophonetic semantic & phonetic etymology metadata).
 */
object HanziDecompositionDownloader {

    private val DICTIONARY_URLS = listOf(
        "https://cdn.jsdelivr.net/gh/skishore/makemeahanzi@master/dictionary.txt",
        "https://raw.githubusercontent.com/skishore/makemeahanzi/master/dictionary.txt"
    )

    fun downloadAndParseAllCharacters(): List<HanziCharacterEntity> {
        var lastError: Exception? = null
        for (urlStr in DICTIONARY_URLS) {
            try {
                val parsed = fetchFromUrl(urlStr)
                if (parsed.isNotEmpty()) {
                    return parsed
                }
            } catch (e: Exception) {
                Log.w(HANZI_SYNC_TAG, "Failed fetching character decomposition from $urlStr", e)
                lastError = e
            }
        }
        throw lastError ?: IllegalStateException("Could not download character decomposition dictionary.")
    }

    private fun fetchFromUrl(urlStr: String): List<HanziCharacterEntity> {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = true
            connectTimeout = 15_000
            readTimeout = 40_000
            setRequestProperty("Accept", "text/plain, application/json")
            setRequestProperty("User-Agent", "PandaChinese-Android/1.0")
        }

        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code downloading MakeMeAHanzi dictionary")
            }

            val results = ArrayList<HanziCharacterEntity>(9_800)
            BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8), 32 * 1024).use { reader ->
                reader.forEachLine { line ->
                    val entity = parseDictionaryLine(line)
                    if (entity != null) {
                        results += entity
                    }
                }
            }
            return results
        } finally {
            conn.disconnect()
        }
    }

    internal fun parseDictionaryLine(line: String): HanziCharacterEntity? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || !trimmed.startsWith("{")) return null
        return try {
            val obj = JSONObject(trimmed)
            val character = obj.optString("character", "").trim()
            if (character.isEmpty()) return null

            val definition = obj.optString("definition", "").trim()
            val pinyin = extractPinyin(obj)
            val radical = obj.optString("radical", "").trim()
            val decomposition = obj.optString("decomposition", "").trim()

            val etymObj = obj.optJSONObject("etymology")
            val etymType = etymObj?.optString("type", "").orEmpty().trim()
            val semantic = etymObj?.optString("semantic", "").orEmpty().trim()
            val phonetic = etymObj?.optString("phonetic", "").orEmpty().trim()
            val hint = etymObj?.optString("hint", "").orEmpty().trim()

            HanziCharacterEntity(
                character = character,
                definition = definition,
                pinyin = pinyin,
                radical = radical,
                decomposition = decomposition,
                etymologyType = etymType,
                semanticComponent = semantic,
                phoneticComponent = phonetic,
                etymologyHint = hint
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun extractPinyin(obj: JSONObject): String {
        val arr = obj.optJSONArray("pinyin")
        if (arr != null && arr.length() > 0) {
            val list = (0 until arr.length())
                .mapNotNull { arr.optString(it)?.trim()?.takeIf { s -> s.isNotEmpty() } }
            if (list.isNotEmpty()) return list.take(2).joinToString(" / ")
        }
        return obj.optString("pinyin", "").trim()
    }
}
