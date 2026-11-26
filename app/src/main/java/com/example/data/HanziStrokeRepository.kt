package com.example.data

import android.content.Context
import android.util.Log
import com.example.model.HanziPoint
import com.example.model.HanziStrokeData
import com.example.model.HanziStrokeParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

private const val STROKE_REPO_TAG = "HanziStrokeRepo"

/**
 * Loads exact Make Me a Hanzi / hanzi-writer-data stroke order sequences & SVG paths
 * for any HSK 1–9 Chinese character, with instant in-memory + persistent local disk cache
 * and built-in offline starter stroke medians.
 */
class HanziStrokeRepository(context: Context) {

    private val cacheDir: File = File(context.applicationContext.filesDir, "hanzi_stroke_cache").apply {
        if (!exists()) mkdirs()
    }
    private val memoryCache = ConcurrentHashMap<String, HanziStrokeData>()

    suspend fun getStrokeData(singleChar: String): HanziStrokeData? {
        val clean = singleChar.trim().take(1)
        if (clean.isEmpty()) return null

        memoryCache[clean]?.let { return it }

        return withContext(Dispatchers.IO) {
            // 1. Check persistent local disk cache first (0ms offline reuse)
            val cacheFile = File(cacheDir, "${clean[0].code.toString(16)}.json")
            if (cacheFile.exists() && cacheFile.length() > 20L) {
                val cachedText = runCatching { cacheFile.readText(Charsets.UTF_8) }.getOrNull()
                val parsed = cachedText?.let { HanziStrokeParser.parseHanziWriterJson(clean, it) }
                if (parsed != null) {
                    memoryCache[clean] = parsed
                    return@withContext parsed
                }
            }

            // 2. Fetch from MIT-licensed hanzi-writer-data CDN (covers 9,000+ simplified Hanzi)
            val downloadedJson = fetchFromCdn(clean)
            if (downloadedJson != null) {
                val parsed = HanziStrokeParser.parseHanziWriterJson(clean, downloadedJson)
                if (parsed != null) {
                    runCatching { cacheFile.writeText(downloadedJson, Charsets.UTF_8) }
                    memoryCache[clean] = parsed
                    return@withContext parsed
                }
            }

            // 3. Offline curated fallback for core starter HSK characters
            val builtIn = BUILT_IN_FALLBACK_MEDIANS[clean]
            if (builtIn != null) {
                val fallback = HanziStrokeData(
                    character = clean,
                    svgStrokePaths = emptyList(),
                    medians = builtIn,
                    isOfflineFallback = true
                )
                memoryCache[clean] = fallback
                return@withContext fallback
            }

            null
        }
    }

    private fun fetchFromCdn(charStr: String): String? {
        val encoded = runCatching { URLEncoder.encode(charStr, "UTF-8") }.getOrNull() ?: return null
        val urls = listOf(
            "https://cdn.jsdelivr.net/npm/hanzi-writer-data@2.0/$encoded.json",
            "https://unpkg.com/hanzi-writer-data@2.0/$encoded.json"
        )
        for (urlStr in urls) {
            var conn: HttpURLConnection? = null
            try {
                conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 4500
                    readTimeout = 4500
                    setRequestProperty("Accept", "application/json")
                }
                if (conn.responseCode in 200..299) {
                    val body = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    if (body.contains("\"medians\"")) {
                        return body
                    }
                }
            } catch (e: Exception) {
                Log.d(STROKE_REPO_TAG, "CDN fetch failed for $charStr at $urlStr: ${e.message}")
            } finally {
                conn?.disconnect()
            }
        }
        return null
    }

    companion object {
        @Volatile
        private var INSTANCE: HanziStrokeRepository? = null

        fun getInstance(context: Context): HanziStrokeRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HanziStrokeRepository(context).also { INSTANCE = it }
            }
        }

        private fun p(x: Float, y: Float) = HanziPoint(x, y)

        // Curated offline normalized stroke medians (0..1) so starter HSK characters work even without internet
        private val BUILT_IN_FALLBACK_MEDIANS: Map<String, List<List<HanziPoint>>> = mapOf(
            "一" to listOf(
                listOf(p(0.16f, 0.50f), p(0.50f, 0.49f), p(0.84f, 0.48f))
            ),
            "二" to listOf(
                listOf(p(0.28f, 0.34f), p(0.72f, 0.33f)),
                listOf(p(0.16f, 0.68f), p(0.84f, 0.67f))
            ),
            "三" to listOf(
                listOf(p(0.26f, 0.26f), p(0.74f, 0.25f)),
                listOf(p(0.30f, 0.50f), p(0.70f, 0.49f)),
                listOf(p(0.16f, 0.74f), p(0.84f, 0.73f))
            ),
            "十" to listOf(
                listOf(p(0.16f, 0.46f), p(0.84f, 0.45f)),
                listOf(p(0.50f, 0.16f), p(0.50f, 0.86f))
            ),
            "人" to listOf(
                listOf(p(0.51f, 0.18f), p(0.42f, 0.52f), p(0.20f, 0.82f)),
                listOf(p(0.46f, 0.46f), p(0.64f, 0.68f), p(0.84f, 0.82f))
            ),
            "大" to listOf(
                listOf(p(0.18f, 0.42f), p(0.82f, 0.40f)),
                listOf(p(0.50f, 0.16f), p(0.46f, 0.52f), p(0.20f, 0.84f)),
                listOf(p(0.50f, 0.46f), p(0.68f, 0.70f), p(0.86f, 0.84f))
            ),
            "口" to listOf(
                listOf(p(0.26f, 0.28f), p(0.29f, 0.74f)),
                listOf(p(0.26f, 0.29f), p(0.74f, 0.28f), p(0.71f, 0.72f)),
                listOf(p(0.29f, 0.72f), p(0.71f, 0.71f))
            ),
            "日" to listOf(
                listOf(p(0.30f, 0.22f), p(0.30f, 0.80f)),
                listOf(p(0.30f, 0.22f), p(0.70f, 0.21f), p(0.70f, 0.80f)),
                listOf(p(0.31f, 0.50f), p(0.69f, 0.49f)),
                listOf(p(0.31f, 0.78f), p(0.69f, 0.77f))
            ),
            "木" to listOf(
                listOf(p(0.20f, 0.38f), p(0.80f, 0.37f)),
                listOf(p(0.50f, 0.14f), p(0.50f, 0.88f)),
                listOf(p(0.48f, 0.40f), p(0.34f, 0.62f), p(0.18f, 0.78f)),
                listOf(p(0.52f, 0.42f), p(0.68f, 0.64f), p(0.84f, 0.78f))
            ),
            "山" to listOf(
                listOf(p(0.50f, 0.18f), p(0.50f, 0.76f)),
                listOf(p(0.24f, 0.44f), p(0.23f, 0.77f), p(0.76f, 0.75f)),
                listOf(p(0.78f, 0.42f), p(0.77f, 0.80f))
            ),
            "中" to listOf(
                listOf(p(0.24f, 0.34f), p(0.28f, 0.62f)),
                listOf(p(0.25f, 0.35f), p(0.76f, 0.33f), p(0.72f, 0.60f)),
                listOf(p(0.28f, 0.60f), p(0.72f, 0.59f)),
                listOf(p(0.50f, 0.14f), p(0.50f, 0.88f))
            ),
            "好" to listOf(
                listOf(p(0.32f, 0.20f), p(0.22f, 0.58f), p(0.42f, 0.72f)),
                listOf(p(0.42f, 0.38f), p(0.32f, 0.64f), p(0.16f, 0.80f)),
                listOf(p(0.14f, 0.48f), p(0.46f, 0.42f)),
                listOf(p(0.54f, 0.24f), p(0.80f, 0.22f), p(0.67f, 0.38f)),
                listOf(p(0.67f, 0.38f), p(0.68f, 0.84f), p(0.56f, 0.76f)),
                listOf(p(0.48f, 0.54f), p(0.88f, 0.52f))
            ),
            "扫" to listOf(
                listOf(p(0.16f, 0.36f), p(0.42f, 0.34f)),
                listOf(p(0.30f, 0.16f), p(0.30f, 0.84f), p(0.20f, 0.76f)),
                listOf(p(0.14f, 0.60f), p(0.42f, 0.48f)),
                listOf(p(0.50f, 0.26f), p(0.84f, 0.25f), p(0.82f, 0.80f)),
                listOf(p(0.52f, 0.52f), p(0.81f, 0.51f)),
                listOf(p(0.48f, 0.78f), p(0.82f, 0.77f))
            ),
            "码" to listOf(
                listOf(p(0.14f, 0.28f), p(0.42f, 0.26f)),
                listOf(p(0.28f, 0.28f), p(0.14f, 0.62f)),
                listOf(p(0.22f, 0.52f), p(0.24f, 0.76f)),
                listOf(p(0.23f, 0.52f), p(0.40f, 0.50f), p(0.38f, 0.74f)),
                listOf(p(0.24f, 0.74f), p(0.38f, 0.73f)),
                listOf(p(0.52f, 0.24f), p(0.80f, 0.22f), p(0.76f, 0.52f)),
                listOf(p(0.52f, 0.28f), p(0.48f, 0.54f), p(0.84f, 0.52f), p(0.78f, 0.84f), p(0.66f, 0.78f)),
                listOf(p(0.46f, 0.68f), p(0.74f, 0.66f))
            )
        )
    }
}
