package com.example.data

import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import com.example.data.local.HskBand
import com.example.data.local.HskVocabularyEntity
import java.io.BufferedInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

private const val SYNC_TAG = "Hsk30CatalogSync"

/**
 * Streams and parses the official open-source MIT-licensed Complete HSK 3.0 Vocabulary dataset
 * (drkameleon/complete-hsk-vocabulary — ~11,092 official HSK 3.0 words across Levels 1–9)
 * using Android's low-memory streaming [JsonReader].
 */
object Hsk30CatalogDownloader {

    private val DATASET_URLS = listOf(
        "https://cdn.jsdelivr.net/gh/drkameleon/complete-hsk-vocabulary@main/complete.min.json",
        "https://raw.githubusercontent.com/drkameleon/complete-hsk-vocabulary/main/complete.min.json",
        "https://cdn.jsdelivr.net/gh/drkameleon/complete-hsk-vocabulary@main/complete.json",
        "https://raw.githubusercontent.com/drkameleon/complete-hsk-vocabulary/main/complete.json"
    )

    fun downloadAndParseAllHsk30Words(
        skipHanzi: Set<String>,
        onProgress: (percent: Int, status: String) -> Unit
    ): List<HskVocabularyEntity> {
        var lastError: Exception? = null
        for (urlStr in DATASET_URLS) {
            try {
                onProgress(10, "Connecting to HSK 3.0 dictionary...")
                val parsed = fetchFromUrl(urlStr, skipHanzi, onProgress)
                if (parsed.isNotEmpty()) {
                    return parsed
                }
            } catch (e: Exception) {
                Log.w(SYNC_TAG, "Failed fetching HSK 3.0 catalog from $urlStr", e)
                lastError = e
            }
        }
        throw lastError ?: IllegalStateException("Could not download HSK 3.0 vocabulary catalog.")
    }

    private fun fetchFromUrl(
        urlStr: String,
        skipHanzi: Set<String>,
        onProgress: (percent: Int, status: String) -> Unit
    ): List<HskVocabularyEntity> {
        val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = true
            connectTimeout = 15_000
            readTimeout = 40_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "PandaChinese-Android/1.0")
        }

        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("HTTP $code downloading HSK 3.0 dataset")
            }

            onProgress(22, "Streaming 11,000+ HSK 3.0 entries...")
            return BufferedInputStream(conn.inputStream, 32 * 1024).use { bis ->
                parseJsonStream(InputStreamReader(bis, Charsets.UTF_8), skipHanzi, onProgress)
            }
        } finally {
            conn.disconnect()
        }
    }

    internal fun parseJsonStream(
        inputReader: java.io.Reader,
        skipHanzi: Set<String>,
        onProgress: (percent: Int, status: String) -> Unit = { _, _ -> }
    ): List<HskVocabularyEntity> {
        val results = ArrayList<HskVocabularyEntity>(11_500)
        val seenHanzi = HashSet<String>(skipHanzi)

        JsonReader(inputReader).use { reader ->
            reader.isLenient = true
            reader.beginArray()
            var count = 0
            while (reader.hasNext()) {
                val entry = parseEntryObject(reader)
                if (entry != null && seenHanzi.add(entry.hanzi)) {
                    results += entry
                }
                count++
                if (count % 1500 == 0) {
                    val pct = (25 + (count * 45 / 12_000)).coerceIn(25, 70)
                    onProgress(pct, "Parsed ${results.size} HSK 3.0 words...")
                }
            }
            reader.endArray()
        }
        return results
    }

    private fun parseEntryObject(reader: JsonReader): HskVocabularyEntity? {
        var simplified = ""
        val levels = mutableListOf<String>()
        val posList = mutableListOf<String>()
        var pinyin = ""
        val meanings = mutableListOf<String>()

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "s", "simplified" -> simplified = nextStringSafe(reader)
                "l", "level" -> readStringArray(reader, levels)
                "p", "pos" -> readStringArray(reader, posList)
                "f", "forms" -> {
                    if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            if ((pinyin.isBlank() || meanings.size < 2) && reader.peek() == JsonToken.BEGIN_OBJECT) {
                                val (formPinyin, formMeanings) = parseForm(reader)
                                if (pinyin.isBlank() && formPinyin.isNotBlank()) {
                                    pinyin = formPinyin
                                }
                                if (meanings.size < 2) {
                                    meanings.addAll(formMeanings)
                                }
                            } else {
                                reader.skipValue()
                            }
                        }
                        reader.endArray()
                    } else {
                        reader.skipValue()
                    }
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        if (simplified.isBlank()) return null

        // Determine HSK 3.0 ("new-X" or minified "nX") level; ignore entries that are only in legacy HSK 2.0
        val newLevels = levels.mapNotNull { parseNewHskLevel(it) }
        if (newLevels.isEmpty()) return null

        val hskLevel = newLevels.min()
        val hasOldLevel = levels.any { isOldHskLevel(it) }
        val isHsk30NewTerm = !hasOldLevel

        val cleanEnglish = meanings
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(2)
            .joinToString("; ")
            .take(120)
            .ifBlank { "HSK 3.0 Level $hskLevel term" }

        val posLabel = mapPartOfSpeech(posList.firstOrNull(), simplified)
        val topicId = classifyTopic(simplified, cleanEnglish, posList)

        return HskVocabularyEntity(
            wordKey = "hsk30_${hskLevel}_$simplified",
            hanzi = simplified,
            pinyin = pinyin.ifBlank { simplified },
            english = cleanEnglish,
            hskLevel = hskLevel,
            hskBand = HskBand.fromLevel(hskLevel).name,
            topicId = topicId,
            partOfSpeech = posLabel,
            writable = hskLevel <= 4 && simplified.length <= 4,
            isHsk30NewTerm = isHsk30NewTerm,
            exampleHanzi = "",
            examplePinyin = "",
            exampleEnglish = ""
        )
    }

    private fun parseForm(reader: JsonReader): Pair<String, List<String>> {
        var pinyin = ""
        val meanings = mutableListOf<String>()

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "i", "transcriptions" -> {
                    if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "y", "pinyin" -> pinyin = nextStringSafe(reader)
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()
                    } else {
                        reader.skipValue()
                    }
                }
                "m", "meanings" -> readStringArray(reader, meanings)
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return pinyin to meanings
    }

    private fun readStringArray(reader: JsonReader, target: MutableList<String>) {
        when (reader.peek()) {
            JsonToken.BEGIN_ARRAY -> {
                reader.beginArray()
                while (reader.hasNext()) {
                    val s = nextStringSafe(reader)
                    if (s.isNotEmpty()) target += s
                }
                reader.endArray()
            }
            JsonToken.STRING -> {
                val s = reader.nextString()
                if (s.isNotEmpty()) target += s
            }
            else -> reader.skipValue()
        }
    }

    private fun nextStringSafe(reader: JsonReader): String {
        return when (reader.peek()) {
            JsonToken.STRING -> reader.nextString()
            JsonToken.NUMBER -> reader.nextString()
            JsonToken.NULL -> {
                reader.nextNull()
                ""
            }
            else -> {
                reader.skipValue()
                ""
            }
        }
    }

    internal fun parseNewHskLevel(tag: String): Int? {
        val normalized = tag.trim().lowercase()
        val suffix = when {
            normalized.startsWith("new-") -> normalized.removePrefix("new-")
            normalized.length >= 2 && normalized[0] == 'n' && normalized[1].isDigit() ->
                normalized.removePrefix("n")
            else -> return null
        }
        return when {
            suffix.startsWith("7") -> 7
            suffix.startsWith("8") -> 8
            suffix.startsWith("9") -> 9
            else -> suffix.takeWhile { it.isDigit() }.toIntOrNull()?.coerceIn(1, 9)
        }
    }

    internal fun isOldHskLevel(tag: String): Boolean {
        val normalized = tag.trim().lowercase()
        return normalized.startsWith("old-") ||
            (normalized.length >= 2 && normalized[0] == 'o' && normalized[1].isDigit())
    }

    private fun mapPartOfSpeech(code: String?, hanzi: String): String {
        if (hanzi.length == 4 && code == "i") return "Idiom (Chengyu)"
        return when (code?.lowercase()) {
            "n", "nr", "ns", "nt", "nz" -> "Noun"
            "v", "vd", "vn" -> "Verb"
            "a", "ad", "an" -> "Adjective"
            "d" -> "Adverb"
            "c" -> "Connector"
            "p" -> "Preposition"
            "q" -> "Measure Word"
            "r" -> "Pronoun"
            "m" -> "Numeral"
            "t" -> "Time Word"
            "f" -> "Direction"
            "u" -> "Particle"
            "e", "y", "o" -> "Expression"
            "i", "l" -> "Idiom / Phrase"
            else -> if (hanzi.length == 4) "Phrase / Idiom" else "Vocabulary"
        }
    }

    /**
     * Classifies each HSK 3.0 entry into one of the 14 real-life topics so every word
     * is immediately browsable both by HSK Level (1–9) and by real-life theme.
     */
    private fun classifyTopic(hanzi: String, english: String, posList: List<String>): Int {
        val en = english.lowercase()
        val pos = posList.firstOrNull()?.lowercase().orEmpty()

        return when {
            pos == "r" || en.contains("hello") || en.contains("thank") || en.contains("sorry") || en.contains("goodbye") || en.contains("greet") -> 1
            pos == "q" || pos == "m" || en.contains("classifier") || en.contains("measure word") || en.contains("number") || en.contains("percent") -> 2
            pos == "t" || en.contains("today") || en.contains("tomorrow") || en.contains("year") || en.contains("month") || en.contains("week") || en.contains("hour") || en.contains("minute") || en.contains("clock") || en.contains("calendar") || en.contains("time") -> 3
            en.contains("father") || en.contains("mother") || en.contains("family") || en.contains("friend") || en.contains("teacher") || en.contains("child") || en.contains("husband") || en.contains("wife") || en.contains("person") || en.contains("people") || en.contains("marry") -> 4
            en.contains("eat") || en.contains("drink") || en.contains("food") || en.contains("rice") || en.contains("tea") || en.contains("coffee") || en.contains("restaurant") || en.contains("fruit") || en.contains("meat") || en.contains("cook") || en.contains("taste") || en.contains("vegetable") || en.contains("milk") -> 5
            pos == "f" || en.contains("north") || en.contains("south") || en.contains("east") || en.contains("west") || en.contains("left") || en.contains("right") || en.contains("street") || en.contains("city") || en.contains("hospital") || en.contains("school") || en.contains("building") || en.contains("location") -> 7
            en.contains("train") || en.contains("airport") || en.contains("flight") || en.contains("subway") || en.contains("bus") || en.contains("car") || en.contains("travel") || en.contains("ticket") || en.contains("passport") || en.contains("hotel") || en.contains("drive") || en.contains("traffic") -> 8
            en.contains("study") || en.contains("work") || en.contains("company") || en.contains("office") || en.contains("exam") || en.contains("university") || en.contains("business") || en.contains("economy") || en.contains("science") || en.contains("research") || en.contains("manage") || en.contains("meeting") -> 9
            en.contains("buy") || en.contains("sell") || en.contains("shop") || en.contains("price") || en.contains("money") || en.contains("pay") || en.contains("phone") || en.contains("internet") || en.contains("computer") || en.contains("network") || en.contains("digital") || en.contains("software") || en.contains("data") || en.contains("app") -> 10
            en.contains("room") || en.contains("house") || en.contains("table") || en.contains("chair") || en.contains("bed") || en.contains("door") || en.contains("window") || en.contains("clothes") || en.contains("wear") || en.contains("kitchen") || en.contains("furniture") -> 11
            en.contains("weather") || en.contains("rain") || en.contains("snow") || en.contains("wind") || en.contains("mountain") || en.contains("river") || en.contains("sea") || en.contains("tree") || en.contains("flower") || en.contains("animal") || en.contains("environment") || en.contains("climate") || en.contains("nature") -> 12
            pos == "c" || pos == "p" || pos == "u" || en.contains("because") || en.contains("although") || en.contains("therefore") || en.contains("however") || en.contains("particle") || en.contains("conjunction") -> 14
            pos == "a" || en.contains("happy") || en.contains("sad") || en.contains("angry") || en.contains("beautiful") || en.contains("feel") || en.contains("emotion") || en.contains("love") || en.contains("worry") -> 13
            pos == "v" -> 6
            else -> ((hanzi.hashCode() and 0x7FFFFFFF) % 14) + 1
        }
    }
}
