package com.example.data

import com.example.model.ActiveMockExamSession
import com.example.model.HskMockExamMode
import com.example.model.HskMockLevel
import com.example.model.MockExamQuestion
import com.example.model.MockExamSection
import com.example.model.MockQuestionKind
import com.example.model.PastMockExamRecord
import com.example.model.VocabWord
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates level-appropriate 3-section HSK 3.0 Mock Exams (Listening, Reading, Writing)
 * directly from Room vocabulary + curated HSK sentences, plus offline fallback writing scorer
 * and SharedPreferences JSON serialization for past exam score history.
 */
object HskMockExamGenerator {

    private data class FallbackTileSentence(
        val hskLevel: Int,
        val promptEn: String,
        val correctHanzi: String,
        val correctPinyin: String,
        val tiles: List<Pair<String, String>>,
        val note: String
    )

    private val EXTRA_HSK13_TILE_SENTENCES = listOf(
        FallbackTileSentence(
            hskLevel = 1,
            promptEn = "I drink tea every morning.",
            correctHanzi = "我每天早上喝茶",
            correctPinyin = "Wǒ měitiān zǎoshang hē chá",
            tiles = listOf("我" to "wǒ", "每天" to "měitiān", "早上" to "zǎoshang", "喝" to "hē", "茶" to "chá"),
            note = "Time words (每天早上) come after the subject and before the verb."
        ),
        FallbackTileSentence(
            hskLevel = 1,
            promptEn = "He is my Chinese teacher.",
            correctHanzi = "他是我的汉语老师",
            correctPinyin = "Tā shì wǒ de Hànyǔ lǎoshī",
            tiles = listOf("他" to "tā", "是" to "shì", "我的" to "wǒ de", "汉语" to "Hànyǔ", "老师" to "lǎoshī"),
            note = "Use 是 (shì) to link the subject and noun predicate."
        ),
        FallbackTileSentence(
            hskLevel = 1,
            promptEn = "Today the weather is very good.",
            correctHanzi = "今天天气很好",
            correctPinyin = "Jīntiān tiānqì hěn hǎo",
            tiles = listOf("今天" to "jīntiān", "天气" to "tiānqì", "很" to "hěn", "好" to "hǎo"),
            note = "Adjective predicates in Mandarin use 很 (hěn) instead of 是."
        ),
        FallbackTileSentence(
            hskLevel = 2,
            promptEn = "I went to Beijing yesterday with friends.",
            correctHanzi = "我昨天跟朋友去了北京",
            correctPinyin = "Wǒ zuótiān gēn péngyou qù le Běijīng",
            tiles = listOf("我" to "wǒ", "昨天" to "zuótiān", "跟朋友" to "gēn péngyou", "去了" to "qù le", "北京" to "Běijīng"),
            note = "Order: Subject + Time + 跟 (with) + Verb + 了 + Place."
        ),
        FallbackTileSentence(
            hskLevel = 2,
            promptEn = "This cup of coffee is hotter than tea.",
            correctHanzi = "这杯咖啡比茶热",
            correctPinyin = "Zhè bēi kāfēi bǐ chá rè",
            tiles = listOf("这杯" to "zhè bēi", "咖啡" to "kāfēi", "比" to "bǐ", "茶" to "chá", "热" to "rè"),
            note = "Comparison pattern: A + 比 (bǐ) + B + Adjective."
        ),
        FallbackTileSentence(
            hskLevel = 2,
            promptEn = "Please scan the QR code to pay.",
            correctHanzi = "请用手机扫码付款",
            correctPinyin = "Qǐng yòng shǒujī sǎomǎ fùkuǎn",
            tiles = listOf("请" to "qǐng", "用手机" to "yòng shǒujī", "扫码" to "sǎomǎ", "付款" to "fùkuǎn"),
            note = "Tool/means phrase (用手机) comes right before the main action."
        ),
        FallbackTileSentence(
            hskLevel = 3,
            promptEn = "Because it rained, we stayed home.",
            correctHanzi = "因为下雨所以我们留在家里",
            correctPinyin = "Yīnwèi xiàyǔ suǒyǐ wǒmen liú zài jiālǐ",
            tiles = listOf("因为" to "yīnwèi", "下雨" to "xiàyǔ", "所以" to "suǒyǐ", "我们" to "wǒmen", "留在家里" to "liú zài jiālǐ"),
            note = "Paired conjunction: 因为 (because) ... 所以 (therefore)."
        ),
        FallbackTileSentence(
            hskLevel = 3,
            promptEn = "I finished reading that Chinese book.",
            correctHanzi = "我把那本中文书看完了",
            correctPinyin = "Wǒ bǎ nà běn Zhōngwén shū kàn wán le",
            tiles = listOf("我" to "wǒ", "把" to "bǎ", "那本" to "nà běn", "中文书" to "Zhōngwén shū", "看完了" to "kàn wán le"),
            note = "把 structure places the specific object before the resultative verb."
        )
    )

    fun generateExam(
        level: HskMockLevel,
        mode: HskMockExamMode,
        levelWords: List<VocabWord>,
        distractorPool: List<VocabWord>
    ): ActiveMockExamSession {
        val safePrimary = levelWords
            .filter { it.hanzi.isNotBlank() && it.english.isNotBlank() }
            .distinctBy { it.hanzi }
            .ifEmpty { HskCurriculumData.builtInVocab }

        val safePool = (safePrimary + distractorPool + HskCurriculumData.builtInVocab)
            .filter { it.hanzi.isNotBlank() && it.english.isNotBlank() }
            .distinctBy { it.hanzi }

        val listeningQs = buildListeningSection(
            level = level,
            count = mode.listeningCount,
            primaryWords = safePrimary.shuffled(),
            pool = safePool
        )

        val readingQs = buildReadingSection(
            level = level,
            count = mode.readingCount,
            primaryWords = safePrimary.shuffled(),
            pool = safePool
        )

        val writingQs = buildWritingSection(
            level = level,
            count = mode.writingCount,
            primaryWords = safePrimary.shuffled()
        )

        val allQuestions = listeningQs + readingQs + writingQs
        return ActiveMockExamSession(
            examId = "mock_${level.id}_${System.currentTimeMillis()}",
            level = level,
            mode = mode,
            questions = allQuestions,
            durationSeconds = mode.durationMinutes * 60
        )
    }

    private fun buildListeningSection(
        level: HskMockLevel,
        count: Int,
        primaryWords: List<VocabWord>,
        pool: List<VocabWord>
    ): List<MockExamQuestion> {
        val result = mutableListOf<MockExamQuestion>()
        val usedHanzi = mutableSetOf<String>()

        // Mix in curated official-style listening questions that fit the chosen level band
        val matchingCurated = HskCurriculumData.listeningQuestions
            .filter { q ->
                if (level == HskMockLevel.HSK_7_9) q.hskLevel >= 6
                else kotlin.math.abs(q.hskLevel - level.targetNumericLevel) <= 1
            }
            .shuffled()
            .take((count / 3).coerceAtLeast(1))

        matchingCurated.forEachIndexed { idx, cq ->
            val linkedWord = pool.firstOrNull { it.key == cq.targetWordKey }
            result.add(
                MockExamQuestion(
                    id = "l_cur_$idx",
                    section = MockExamSection.LISTENING,
                    kind = MockQuestionKind.LISTENING_AUDIO_MCQ,
                    promptText = cq.promptText,
                    hanziPrompt = cq.audioScript,
                    pinyinPrompt = cq.audioPinyin,
                    audioScript = cq.audioScript,
                    options = cq.options,
                    correctOptionIndex = cq.correctIndex.coerceIn(0, cq.options.lastIndex.coerceAtLeast(0)),
                    targetWords = listOfNotNull(linkedWord),
                    explanation = "${cq.audioScript} (${cq.audioPinyin})"
                )
            )
            linkedWord?.let { usedHanzi.add(it.hanzi) }
        }

        // Fill remaining listening slots dynamically from Room HSK vocabulary
        val candidates = (primaryWords + pool).filter { it.hanzi !in usedHanzi }
        var cursor = 0
        while (result.size < count && cursor < candidates.size) {
            val word = candidates[cursor++]
            usedHanzi.add(word.hanzi)

            val hasExample = word.exampleHanzi.isNotBlank() && word.exampleEnglish.isNotBlank()
            val askMeaning = result.size % 2 == 0 || !hasExample

            val distractors = pool
                .filter { it.hanzi != word.hanzi && !it.english.equals(word.english, ignoreCase = true) }
                .shuffled()
                .take(3)

            if (distractors.size < 3) continue

            if (askMeaning) {
                val rawOptions = (listOf(word) + distractors).shuffled()
                val correctIdx = rawOptions.indexOfFirst { it.hanzi == word.hanzi }
                val script = if (hasExample) "${word.hanzi}。${word.exampleHanzi}" else word.hanzi
                result.add(
                    MockExamQuestion(
                        id = "l_voc_${result.size}",
                        section = MockExamSection.LISTENING,
                        kind = MockQuestionKind.LISTENING_AUDIO_MCQ,
                        promptText = if (hasExample) {
                            "Listen to the Mandarin word and sentence. Which English meaning matches the target word?"
                        } else {
                            "Listen carefully to the Mandarin audio and select the matching meaning."
                        },
                        hanziPrompt = word.hanzi,
                        pinyinPrompt = word.pinyin,
                        audioScript = script,
                        options = rawOptions.map { it.english },
                        correctOptionIndex = correctIdx,
                        targetWords = listOf(word),
                        explanation = "${word.hanzi} (${word.pinyin}) means \"${word.english}\"."
                    )
                )
            } else {
                // Sentence listening -> select matching Hanzi word heard in clip
                val rawOptions = (listOf(word) + distractors).shuffled()
                val correctIdx = rawOptions.indexOfFirst { it.hanzi == word.hanzi }
                result.add(
                    MockExamQuestion(
                        id = "l_sen_${result.size}",
                        section = MockExamSection.LISTENING,
                        kind = MockQuestionKind.LISTENING_AUDIO_MCQ,
                        promptText = "Listen to the Mandarin clip. Which word or phrase was featured in the sentence?",
                        hanziPrompt = word.exampleHanzi,
                        pinyinPrompt = word.examplePinyin,
                        audioScript = word.exampleHanzi,
                        options = rawOptions.map { "${it.hanzi}  (${it.english})" },
                        correctOptionIndex = correctIdx,
                        targetWords = listOf(word),
                        explanation = "Sentence: ${word.exampleHanzi} — \"${word.exampleEnglish}\""
                    )
                )
            }
        }

        return result.take(count)
    }

    private fun buildReadingSection(
        level: HskMockLevel,
        count: Int,
        primaryWords: List<VocabWord>,
        pool: List<VocabWord>
    ): List<MockExamQuestion> {
        val result = mutableListOf<MockExamQuestion>()
        val usedHanzi = mutableSetOf<String>()
        val wordsWithExamples = (primaryWords + pool).filter {
            it.exampleHanzi.isNotBlank() && it.exampleEnglish.isNotBlank() && it.exampleHanzi.contains(it.hanzi)
        }.shuffled()

        val targetClozeCount = (count / 3).coerceAtLeast(1)
        val targetCompCount = (count / 3).coerceAtLeast(1)

        // 1. Cloze Fill-in-the-Blank Questions from Room examples (or MandarinDrillsData cloze cards)
        for (word in wordsWithExamples) {
            if (result.size >= targetClozeCount) break
            if (!usedHanzi.add(word.hanzi)) continue
            val distractors = pool
                .filter { it.hanzi != word.hanzi && it.hanzi.length in (word.hanzi.length - 1)..(word.hanzi.length + 1) }
                .ifEmpty { pool.filter { it.hanzi != word.hanzi } }
                .shuffled()
                .take(3)
            if (distractors.size < 3) continue

            val blanked = word.exampleHanzi.replaceFirst(word.hanzi, " ___ ")
            val optionsList = (listOf(word.hanzi) + distractors.map { it.hanzi }).shuffled()
            val correctIdx = optionsList.indexOf(word.hanzi)
            result.add(
                MockExamQuestion(
                    id = "r_clz_${result.size}",
                    section = MockExamSection.READING,
                    kind = MockQuestionKind.READING_CLOZE,
                    promptText = "Choose the best word to complete the sentence:",
                    hanziPrompt = blanked,
                    pinyinPrompt = word.examplePinyin.replaceFirst(word.pinyin, " ___ ", ignoreCase = true),
                    options = optionsList,
                    correctOptionIndex = correctIdx,
                    targetWords = listOf(word),
                    explanation = "Complete sentence: ${word.exampleHanzi} (${word.exampleEnglish})."
                )
            )
        }

        // Top up cloze with curated cloze cards if needed
        if (result.size < targetClozeCount) {
            MandarinDrillsData.clozeContextCards.shuffled().forEach { cc ->
                if (result.size >= targetClozeCount) return@forEach
                val opts = cc.options.shuffled()
                val correctIdx = opts.indexOf(cc.missingWord).coerceAtLeast(0)
                val linked = pool.firstOrNull { it.hanzi == cc.missingWord } ?: VocabWord(
                    key = "clz_${cc.id}",
                    hanzi = cc.missingWord,
                    pinyin = cc.missingPinyin,
                    english = cc.englishTranslation,
                    hskLevel = cc.hskLevel,
                    topicId = 1,
                    exampleHanzi = cc.fullSentence,
                    examplePinyin = cc.fullPinyin,
                    exampleEnglish = cc.englishTranslation
                )
                result.add(
                    MockExamQuestion(
                        id = "r_clz_cur_${result.size}",
                        section = MockExamSection.READING,
                        kind = MockQuestionKind.READING_CLOZE,
                        promptText = "Choose the best word to complete the sentence:",
                        hanziPrompt = cc.sentenceWithBlank,
                        pinyinPrompt = cc.fullPinyin,
                        options = opts,
                        correctOptionIndex = correctIdx,
                        targetWords = listOf(linked),
                        explanation = "${cc.fullSentence} — ${cc.grammarNote}"
                    )
                )
            }
        }

        // 2. Reading Comprehension (Pinyin-free by default; toggleable for HSK 1–2)
        val compCandidates = wordsWithExamples.filter { it.hanzi !in usedHanzi }
        var compAdded = 0
        for (word in compCandidates) {
            if (compAdded >= targetCompCount || result.size >= count) break
            if (!usedHanzi.add(word.hanzi)) continue
            val distractors = wordsWithExamples
                .filter { it.hanzi != word.hanzi && it.exampleEnglish != word.exampleEnglish }
                .shuffled()
                .take(3)
            if (distractors.size < 3) continue
            val rawOptions = (listOf(word.exampleEnglish) + distractors.map { it.exampleEnglish }).shuffled()
            val correctIdx = rawOptions.indexOf(word.exampleEnglish)
            result.add(
                MockExamQuestion(
                    id = "r_comp_${result.size}",
                    section = MockExamSection.READING,
                    kind = MockQuestionKind.READING_COMPREHENSION,
                    promptText = "Read the Chinese sentence carefully and choose the accurate meaning:",
                    hanziPrompt = word.exampleHanzi,
                    pinyinPrompt = word.examplePinyin,
                    options = rawOptions,
                    correctOptionIndex = correctIdx,
                    targetWords = listOf(word),
                    explanation = "Key word: ${word.hanzi} (${word.pinyin}) = ${word.english}."
                )
            )
            compAdded++
        }

        // 3. Hanzi-to-Meaning Recognition for all remaining Reading slots
        val meaningCandidates = (primaryWords + pool).filter { it.hanzi !in usedHanzi }
        for (word in meaningCandidates) {
            if (result.size >= count) break
            if (!usedHanzi.add(word.hanzi)) continue
            val distractors = pool
                .filter { it.hanzi != word.hanzi && !it.english.equals(word.english, ignoreCase = true) }
                .shuffled()
                .take(3)
            if (distractors.size < 3) continue
            val rawOptions = (listOf(word) + distractors).shuffled()
            val correctIdx = rawOptions.indexOfFirst { it.hanzi == word.hanzi }
            result.add(
                MockExamQuestion(
                    id = "r_mean_${result.size}",
                    section = MockExamSection.READING,
                    kind = MockQuestionKind.READING_HANZI_MEANING,
                    promptText = "Select the correct English meaning for this HSK ${level.label} character/word:",
                    hanziPrompt = word.hanzi,
                    pinyinPrompt = word.pinyin,
                    options = rawOptions.map { it.english },
                    correctOptionIndex = correctIdx,
                    targetWords = listOf(word),
                    explanation = "${word.hanzi} (${word.pinyin}) means \"${word.english}\"."
                )
            )
        }

        return result.take(count)
    }

    private fun buildWritingSection(
        level: HskMockLevel,
        count: Int,
        primaryWords: List<VocabWord>
    ): List<MockExamQuestion> {
        val result = mutableListOf<MockExamQuestion>()

        if (level.targetNumericLevel <= 3) {
            // HSK 1–3: Scrambled Chinese word tiles to construct a valid sentence
            val builtInTiles = HskCurriculumData.tileSentences.map { ts ->
                FallbackTileSentence(
                    hskLevel = ts.hskLevel,
                    promptEn = ts.promptEn,
                    correctHanzi = ts.correctHanzi,
                    correctPinyin = ts.tiles.joinToString(" ") { it.second },
                    tiles = ts.tiles,
                    note = ts.grammarNote
                )
            }
            val combined = (builtInTiles + EXTRA_HSK13_TILE_SENTENCES)
                .sortedBy { kotlin.math.abs(it.hskLevel - level.targetNumericLevel) }
                .distinctBy { it.correctHanzi }

            val picked = combined.take(count).shuffled()
            picked.forEachIndexed { idx, item ->
                val shuffledTiles = item.tiles.shuffled().let { candidate ->
                    if (candidate.joinToString("") { it.first } == item.correctHanzi && candidate.size > 1) {
                        candidate.reversed()
                    } else candidate
                }
                val linkedWords = primaryWords.filter { item.correctHanzi.contains(it.hanzi) }.take(2)
                result.add(
                    MockExamQuestion(
                        id = "w_tile_$idx",
                        section = MockExamSection.WRITING,
                        kind = MockQuestionKind.WRITING_TILE_ORDER,
                        promptText = "Arrange the Chinese word tiles in natural Mandarin order:\n\"${item.promptEn}\"",
                        hanziPrompt = item.promptEn,
                        scrambledTiles = shuffledTiles,
                        expectedSentenceHanzi = item.correctHanzi,
                        expectedSentencePinyin = item.correctPinyin,
                        targetWords = linkedWords,
                        explanation = item.note
                    )
                )
            }
        } else {
            // HSK 4–9: Compose a Mandarin sentence using 2 target HSK words
            val usable = primaryWords.shuffled()
            var cursor = 0
            while (result.size < count && cursor + 1 < usable.size) {
                val w1 = usable[cursor++]
                val w2 = usable[cursor++]
                result.add(
                    MockExamQuestion(
                        id = "w_free_${result.size}",
                        section = MockExamSection.WRITING,
                        kind = MockQuestionKind.WRITING_FREE_SENTENCE,
                        promptText = "Write one natural Chinese sentence (${level.label} level) that uses BOTH target words below:",
                        hanziPrompt = "${w1.hanzi}  +  ${w2.hanzi}",
                        pinyinPrompt = "${w1.pinyin}  ·  ${w2.pinyin}",
                        expectedSentenceHanzi = "${w1.hanzi} / ${w2.hanzi}",
                        expectedSentencePinyin = "${w1.pinyin} / ${w2.pinyin}",
                        targetWords = listOf(w1, w2),
                        explanation = "Required words: ${w1.hanzi} (${w1.english}) and ${w2.hanzi} (${w2.english})."
                    )
                )
            }
        }

        return result.take(count)
    }

    /**
     * Deterministic offline fallback scorer for HSK 4–9 free-writing prompts
     * when Gemini AI is unavailable or device is offline.
     */
    fun scoreFreeSentenceOffline(
        userSentence: String,
        targetWords: List<VocabWord>
    ): Pair<Int, String> {
        val trimmed = userSentence.trim()
        if (trimmed.isBlank()) {
            return 0 to "No sentence submitted."
        }
        val hanCount = trimmed.count { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.HAN }
        if (hanCount < 3) {
            return 20 to "Sentence is too short — write a complete Chinese sentence using both target words."
        }
        val matchedCount = targetWords.count { trimmed.contains(it.hanzi) }
        val targetRatio = if (targetWords.isEmpty()) 1f else matchedCount.toFloat() / targetWords.size.toFloat()

        var score = (targetRatio * 60f).toInt()
        score += when {
            hanCount >= 10 -> 30
            hanCount >= 6 -> 22
            else -> 12
        }
        if (trimmed.endsWith("。") || trimmed.endsWith("？") || trimmed.endsWith("！") || hanCount >= 8) {
            score += 10
        }
        val finalScore = score.coerceIn(0, 100)
        val note = when {
            matchedCount == targetWords.size && finalScore >= 80 ->
                "Offline check passed! Both target words were included in a full Mandarin sentence."
            matchedCount > 0 ->
                "Included $matchedCount of ${targetWords.size} target words. Try incorporating every required word naturally."
            else ->
                "Missing target words (${targetWords.joinToString(", ") { it.hanzi }})."
        }
        return finalScore to note
    }

    fun formatExamDate(epochMs: Long): String {
        return runCatching {
            SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(epochMs))
        }.getOrDefault("Today")
    }

    fun serializeHistory(records: List<PastMockExamRecord>): String {
        val arr = JSONArray()
        records.take(25).forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("levelLabel", r.levelLabel)
                put("modeLabel", r.modeLabel)
                put("dateLabel", r.dateLabel)
                put("totalScore", r.totalScore)
                put("listeningScore", r.listeningScore)
                put("readingScore", r.readingScore)
                put("writingScore", r.writingScore)
                put("passed", r.passed)
                put("timestampEpochMs", r.timestampEpochMs)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    fun deserializeHistory(rawJson: String?): List<PastMockExamRecord> {
        if (rawJson.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(rawJson)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(
                        PastMockExamRecord(
                            id = obj.optString("id", "rec_$i"),
                            levelLabel = obj.optString("levelLabel", "HSK 1"),
                            modeLabel = obj.optString("modeLabel", "Quick Mock"),
                            dateLabel = obj.optString("dateLabel", ""),
                            totalScore = obj.optInt("totalScore", 0),
                            listeningScore = obj.optInt("listeningScore", 0),
                            readingScore = obj.optInt("readingScore", 0),
                            writingScore = obj.optInt("writingScore", 0),
                            passed = obj.optBoolean("passed", false),
                            timestampEpochMs = obj.optLong("timestampEpochMs", 0L)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
