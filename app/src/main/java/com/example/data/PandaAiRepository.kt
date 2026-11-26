package com.example.data

import android.util.Log
import com.example.data.local.AiCharacterNote
import com.example.data.local.CharacterAnatomy
import com.example.data.local.RelatedFamilyWord
import com.example.data.local.WordEtymologyStory
import com.example.model.AiChatMessage
import com.example.model.AiSentenceGrade
import com.example.model.GradedStory
import com.example.model.GroundedSource
import com.example.model.LessonQuestion
import com.example.model.StorySentenceDoc
import com.example.model.TutorMode
import com.example.model.VocabWord
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.UUID

private const val AI_TAG = "PandaAiRepo"
private const val MODEL_GENERAL = "gemini-4-hydrogen"
private const val MODEL_DEEP_REASONING = "gemini-4-carbon"
private const val MODEL_INTERACTIVE = "gemini-4-neon"
private const val MODEL_FAST = "gemini-3.1-flash-lite"

data class TeacherStreamChunk(
    val textDelta: String = "",
    val sources: List<GroundedSource> = emptyList(),
    val searchQueries: List<String> = emptyList(),
    val searchEntryPointHtml: String? = null
)

class PandaAiRepository {

    private data class TutorModelKey(
        val hskLevel: Int,
        val mode: TutorMode,
        val useGoogleSearch: Boolean
    )

    private val tutorModelCache = mutableMapOf<TutorModelKey, GenerativeModel>()

    private val fastHelperModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_FAST,
            systemInstruction = content {
                text(
                    "You are Panda Laoshi (熊猫老师), a creative, supportive Mandarin Chinese mnemonic coach. " +
                        "Respond concisely and vividly with Hanzi, tone-marked Pinyin, and English."
                )
            }
        )
    }

    private fun getTutorModel(
        hskLevel: Int,
        mode: TutorMode,
        useGoogleSearch: Boolean
    ): GenerativeModel {
        val clampedLevel = hskLevel.coerceIn(1, 9)
        val cacheKey = TutorModelKey(clampedLevel, mode, useGoogleSearch)
        return tutorModelCache.getOrPut(cacheKey) {
            val selectedModelName = if (useGoogleSearch) {
                MODEL_GENERAL
            } else {
                when (mode) {
                    TutorMode.CONVERSATION -> MODEL_GENERAL
                    TutorMode.GRAMMAR -> MODEL_DEEP_REASONING
                    TutorMode.ROLEPLAY -> MODEL_INTERACTIVE
                    TutorMode.PRONUNCIATION -> MODEL_FAST
                }
            }

            val modeGuidance = when (mode) {
                TutorMode.CONVERSATION ->
                    "Engage in warm, encouraging bilingual conversation tailored to HSK 3.0 Level $clampedLevel. " +
                        "Respond naturally in Simplified Chinese with tone-marked Pinyin and English translation. " +
                        "If the learner tries Chinese and makes a mistake, praise their effort first, gently show the natural phrasing, and end with an inviting follow-up question."
                TutorMode.GRAMMAR ->
                    "Provide deep, crystal-clear Mandarin grammar reasoning for HSK 3.0 Level $clampedLevel. " +
                        "Explain SVO sentence patterns, particles (了, 过, 着, 把, 被), and measure words with 2-3 concise examples (Hanzi + tone-marked Pinyin + English) and a quick practice prompt."
                TutorMode.ROLEPLAY ->
                    "Act as a friendly native Chinese conversation partner in a rich, interactive everyday scene calibrated to HSK 3.0 Level $clampedLevel. " +
                        "Speak in-character in Simplified Chinese (with Pinyin and English beneath), give a brief supportive coaching tip on what the learner said, and prompt their next turn."
                TutorMode.PRONUNCIATION ->
                    "Focus on crisp, fast Mandarin pronunciation coaching: four tones + neutral tone, tone sandhi (3+3, 一, 不), and tricky sound pairs for HSK 3.0 Level $clampedLevel. " +
                        "Always show exact tone-marked Pinyin, highlight tone shifts clearly, and give a quick speaking drill."
            }

            val groundingGuidance = if (useGoogleSearch) {
                "You have Google Search enabled. Whenever the learner asks about current events, modern Chinese slang, " +
                    "recent HSK 3.0 syllabus news, China travel, food trends, or real-world facts, use Google Search to provide accurate, up-to-date answers."
            } else {
                ""
            }

            Firebase.ai.generativeModel(
                modelName = selectedModelName,
                tools = if (useGoogleSearch) listOf(Tool.googleSearch()) else null,
                systemInstruction = content {
                    text(
                        "You are Panda Laoshi (熊猫老师), a deeply supportive, patient, and expert Mandarin Chinese tutor " +
                            "for the official HSK 3.0 curriculum (Levels 1–9). " +
                            "Current learner target: HSK 3.0 Level $clampedLevel. " +
                            "$modeGuidance $groundingGuidance " +
                            "Formatting rules: Always include Simplified Hanzi, tone-marked Pinyin, and clear English. " +
                            "Keep replies warm, scannable, concise (usually 3–6 short lines unless asked for a full lesson), and encouraging. Never be harsh or overly dry."
                    )
                }
            )
        }
    }

    private val wordPackModel by lazy {
        val wordSchema = Schema.obj(
            properties = mapOf(
                "hanzi" to Schema.string("Simplified Chinese word or expression"),
                "pinyin" to Schema.string("Tone-marked Pinyin"),
                "english" to Schema.string("Concise English definition"),
                "writable" to Schema.boolean("True if suitable for handwriting practice"),
                "exampleHanzi" to Schema.string("Natural Chinese example sentence"),
                "examplePinyin" to Schema.string("Pinyin for the example sentence"),
                "exampleEnglish" to Schema.string("English translation of example sentence")
            )
        )
        val packSchema = Schema.obj(
            properties = mapOf(
                "words" to Schema.array(wordSchema, "List of 10 authentic HSK 3.0 vocabulary entries")
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_GENERAL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = packSchema
            }
        )
    }

    private val sentenceGraderModel by lazy {
        val gradeSchema = Schema.obj(
            properties = mapOf(
                "score" to Schema.integer("Score from 0 to 100"),
                "verdict" to Schema.string("Short headline verdict such as Excellent, Good Attempt, or Needs Grammar Fix"),
                "correctedHanzi" to Schema.string("Most natural corrected Simplified Chinese sentence"),
                "correctedPinyin" to Schema.string("Tone-marked Pinyin for corrected sentence"),
                "grammarAnalysis" to Schema.string("Clear explanation of SVO word order, particles, and usage"),
                "nativeTip" to Schema.string("One practical tip to sound like a native Mandarin speaker")
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_GENERAL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = gradeSchema
            }
        )
    }

    private val storyGeneratorModel by lazy {
        val sentenceSchema = Schema.obj(
            properties = mapOf(
                "zh" to Schema.string("Chinese sentence in Simplified Hanzi"),
                "pinyin" to Schema.string("Tone-marked Pinyin"),
                "en" to Schema.string("Natural English translation")
            )
        )
        val storySchema = Schema.obj(
            properties = mapOf(
                "titleZh" to Schema.string("Story title in Simplified Hanzi"),
                "titleEn" to Schema.string("Story title in English"),
                "sentences" to Schema.array(sentenceSchema, "5 to 7 graded sentences matching the HSK level"),
                "quizQuestion" to Schema.string("One comprehension question in Chinese with English in parentheses"),
                "quizOptions" to Schema.array(Schema.string(), "Exactly 3 answer options"),
                "quizCorrectIndex" to Schema.integer("Zero-based index (0, 1, or 2) of the correct option")
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_GENERAL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = storySchema
            }
        )
    }

    private val bonusExerciseModel by lazy {
        val exerciseSchema = Schema.obj(
            properties = mapOf(
                "prompt" to Schema.string("Question prompt in English"),
                "hanziHighlight" to Schema.string("Featured Chinese phrase or fill-in-the-blank"),
                "pinyinHint" to Schema.string("Short Pinyin or grammar hint"),
                "audioText" to Schema.string("Chinese sentence to read aloud"),
                "options" to Schema.array(Schema.string(), "Exactly 4 multiple-choice options"),
                "correctIndex" to Schema.integer("Zero-based index (0..3) of the correct option"),
                "explanation" to Schema.string("Why the correct answer works and why distractors fail")
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_GENERAL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = exerciseSchema
            }
        )
    }

    private val etymologyStoryModel by lazy {
        val charNoteSchema = Schema.obj(
            properties = mapOf(
                "character" to Schema.string("Single Simplified Chinese character"),
                "formationExplanation" to Schema.string("How this character is formed from its radical, semantic (meaning), and phonetic (sound) components in HanziCraft style"),
                "miniStory" to Schema.string("Short vivid 1-sentence CharacterPop-style visual story linking the components to the character's meaning")
            )
        )
        val relatedWordSchema = Schema.obj(
            properties = mapOf(
                "hanzi" to Schema.string("Related HSK word sharing a character or radical"),
                "pinyin" to Schema.string("Tone-marked Pinyin"),
                "english" to Schema.string("Concise English meaning")
            )
        )
        val etymologySchema = Schema.obj(
            properties = mapOf(
                "wordMakingLogic" to Schema.string("Clear equation and intuition showing how each character's literal meaning combines to create the whole word"),
                "characterPopStory" to Schema.string("Vivid, unforgettable 2-3 sentence CharacterPop-style mnemonic story weaving the word's radicals and components together"),
                "historicalOrigin" to Schema.string("Authentic HanziCraft / Shuowen-style etymology explaining the historical origin (pictograph, ideograph, or phono-semantic evolution from ancient script to modern Simplified Chinese)"),
                "characterNotes" to Schema.array(charNoteSchema, "Character-by-character breakdown for each unique Hanzi in the word"),
                "relatedWords" to Schema.array(relatedWordSchema, "3 to 4 practical HSK words built from the same character or radical family")
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_GENERAL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = etymologySchema
            }
        )
    }

    fun streamTeacherReply(
        history: List<AiChatMessage>,
        userPrompt: String,
        hskLevel: Int = 1,
        mode: TutorMode = TutorMode.CONVERSATION,
        useGoogleSearch: Boolean = true
    ): Flow<TeacherStreamChunk> = flow {
        val sanitizedHistory = buildAlternatingChatHistory(history)
        val model = getTutorModel(
            hskLevel = hskLevel,
            mode = mode,
            useGoogleSearch = useGoogleSearch
        )
        val chat = model.startChat(history = sanitizedHistory)
        withTimeout(60_000L) {
            chat.sendMessageStream(userPrompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                val grounding = chunk.candidates.firstOrNull()?.groundingMetadata
                val sources = grounding?.groundingChunks
                    ?.mapNotNull { gChunk ->
                        val web = gChunk.web
                        val uri = web?.uri
                        if (!uri.isNullOrBlank()) {
                            GroundedSource(
                                title = web.title?.takeIf { it.isNotBlank() } ?: uri,
                                uri = uri
                            )
                        } else null
                    }
                    .orEmpty()
                val queries = grounding?.webSearchQueries.orEmpty()
                val entryPointHtml = grounding?.searchEntryPoint?.renderedContent

                if (text.isNotEmpty() || sources.isNotEmpty() || queries.isNotEmpty() || !entryPointHtml.isNullOrBlank()) {
                    emit(
                        TeacherStreamChunk(
                            textDelta = text,
                            sources = sources,
                            searchQueries = queries,
                            searchEntryPointHtml = entryPointHtml
                        )
                    )
                }
            }
        }
    }

    /**
     * Gemini startChat history must strictly alternate user -> model turns and begin with "user".
     * Synthetic welcome banners, error cards, and blank placeholders are excluded.
     */
    private fun buildAlternatingChatHistory(messages: List<AiChatMessage>): List<Content> {
        val eligible = messages.filter { !it.isWelcome && !it.isError && !it.streaming && it.text.isNotBlank() }
        val alternating = mutableListOf<AiChatMessage>()
        for (msg in eligible) {
            if (alternating.isEmpty()) {
                if (msg.fromUser) {
                    alternating += msg
                }
            } else if (alternating.last().fromUser != msg.fromUser) {
                alternating += msg
            } else {
                // Replace consecutive same-role message with the latest one to preserve strict alternation
                alternating[alternating.lastIndex] = msg
            }
        }
        // History passed to startChat before sending a new user turn must end with a model turn
        if (alternating.isNotEmpty() && alternating.last().fromUser) {
            alternating.removeAt(alternating.lastIndex)
        }
        return alternating.takeLast(16).map { msg ->
            content(if (msg.fromUser) "user" else "model") { text(msg.text) }
        }
    }

    suspend fun generateTopicWordPack(
        hskLevel: Int,
        topicId: Int,
        topicPrompt: String
    ): Result<List<VocabWord>> {
        return try {
            val prompt = "Generate 10 authentic, practical HSK 3.0 Level $hskLevel Mandarin Chinese vocabulary words " +
                "for the theme '$topicPrompt'. Include accurate tone-marked Pinyin and natural modern example sentences."
            val response = withTimeout(60_000L) {
                wordPackModel.generateContent(prompt)
            }
            val jsonStr = response.text ?: throw IllegalStateException("Empty response from AI.")
            val root = JSONObject(jsonStr)
            val arr = root.getJSONArray("words")
            val words = mutableListOf<VocabWord>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                words += VocabWord(
                    key = "ai_${System.currentTimeMillis()}_$i",
                    hanzi = obj.getString("hanzi"),
                    pinyin = obj.getString("pinyin"),
                    english = obj.getString("english"),
                    hskLevel = hskLevel.coerceIn(1, 9),
                    topicId = topicId.coerceIn(1, 14),
                    writable = obj.optBoolean("writable", hskLevel <= 4),
                    exampleHanzi = obj.optString("exampleHanzi", ""),
                    examplePinyin = obj.optString("examplePinyin", ""),
                    exampleEnglish = obj.optString("exampleEnglish", "")
                )
            }
            Result.success(words)
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Word pack generation timed out", e)
            Result.failure(Exception("AI generation timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Word pack generation failed", e)
            Result.failure(e)
        }
    }

    suspend fun gradeFreeSentence(
        targetWord: VocabWord,
        userSentence: String
    ): Result<AiSentenceGrade> {
        return try {
            val prompt = "Grade this learner's Chinese sentence. Target word: ${targetWord.hanzi} " +
                "(${targetWord.pinyin} - ${targetWord.english}, HSK ${targetWord.hskLevel}). " +
                "Learner's sentence: \"$userSentence\". Check grammar, SVO word order, tone/particle accuracy, and whether the target word is used naturally."
            val response = withTimeout(60_000L) {
                sentenceGraderModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty grading response."))
            Result.success(
                AiSentenceGrade(
                    score = json.optInt("score", 80).coerceIn(0, 100),
                    verdict = json.optString("verdict", "Good effort"),
                    correctedHanzi = json.optString("correctedHanzi", userSentence),
                    correctedPinyin = json.optString("correctedPinyin", ""),
                    grammarAnalysis = json.optString("grammarAnalysis", ""),
                    nativeTip = json.optString("nativeTip", "")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Sentence grading timed out", e)
            Result.failure(Exception("Grading timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Sentence grading failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateGradedStory(hskLevel: Int, topicName: String): Result<GradedStory> {
        return try {
            val prompt = "Write an engaging, culturally authentic graded Chinese short story strictly calibrated " +
                "to HSK 3.0 Level $hskLevel about '$topicName'. Provide 5 to 6 sentences with Simplified Hanzi, " +
                "tone-marked Pinyin, and natural English, plus a 3-option comprehension question."
            val response = withTimeout(60_000L) {
                storyGeneratorModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty story response."))
            val sentencesArr = json.getJSONArray("sentences")
            val sentences = (0 until sentencesArr.length()).map { i ->
                val s = sentencesArr.getJSONObject(i)
                StorySentenceDoc(
                    zh = s.getString("zh"),
                    pinyin = s.getString("pinyin"),
                    en = s.getString("en")
                )
            }
            val optionsArr = json.getJSONArray("quizOptions")
            val options = (0 until optionsArr.length()).map { optionsArr.getString(it) }
            Result.success(
                GradedStory(
                    id = "ai_story_${UUID.randomUUID().toString().take(8)}",
                    titleZh = json.getString("titleZh"),
                    titleEn = json.getString("titleEn"),
                    hskLevel = hskLevel.coerceIn(1, 9),
                    topicName = topicName,
                    sentences = sentences,
                    quizQuestion = json.getString("quizQuestion"),
                    quizOptions = options,
                    quizCorrectIndex = json.optInt("quizCorrectIndex", 0).coerceIn(0, (options.size - 1).coerceAtLeast(0)),
                    aiGenerated = true
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Story generation timed out", e)
            Result.failure(Exception("Story generation timed out after 60 seconds."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Story generation failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateMnemonicRescue(hanzi: String, pinyin: String, english: String): Result<String> {
        return try {
            val prompt = "Create a vivid, unforgettable 2-sentence visual mnemonic breaking down the radicals and sound " +
                "of the Chinese word '$hanzi' ($pinyin — $english) so a learner who missed it 3 times will never forget it."
            val response = withTimeout(45_000L) {
                fastHelperModel.generateContent(prompt)
            }
            val text = response.text?.trim()
            if (text.isNullOrBlank()) {
                Result.failure(Exception("Empty mnemonic returned."))
            } else {
                Result.success(text)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Mnemonic rescue timed out", e)
            Result.failure(Exception("Mnemonic generation timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Mnemonic rescue failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateBonusExercise(hskLevel: Int): Result<LessonQuestion> {
        return try {
            val prompt = "Generate one fun, high-quality HSK 3.0 Level $hskLevel multiple-choice grammar or vocabulary " +
                "question in the style of HelloChinese/SuperChinese with 4 options and a concise explanation."
            val response = withTimeout(60_000L) {
                bonusExerciseModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty exercise response."))
            val optionsArr = json.getJSONArray("options")
            val options = (0 until optionsArr.length()).map { optionsArr.getString(it) }
            Result.success(
                LessonQuestion(
                    id = "ai_ex_${System.currentTimeMillis()}",
                    kind = "AI BONUS",
                    prompt = json.getString("prompt"),
                    hanziHighlight = json.optString("hanziHighlight", ""),
                    pinyinHint = json.optString("pinyinHint", ""),
                    audioText = json.optString("audioText", ""),
                    options = options,
                    correctIndex = json.optInt("correctIndex", 0).coerceIn(0, (options.size - 1).coerceAtLeast(0)),
                    explanation = json.optString("explanation", "")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Bonus exercise timed out", e)
            Result.failure(Exception("Bonus exercise generation timed out."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Bonus exercise failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateWordEtymologyStory(
        word: VocabWord,
        anatomy: List<CharacterAnatomy>
    ): Result<WordEtymologyStory> {
        return try {
            val structuralContext = if (anatomy.isNotEmpty()) {
                anatomy.joinToString("\n") { ch ->
                    val comps = ch.components.joinToString(" + ") { "${it.glyph}(${it.meaning.ifBlank { it.role.label }})" }
                    "- ${ch.character} (${ch.pinyin}): meaning='${ch.definition}', radical='${ch.radical}', " +
                        "layout='${ch.structureLabel}', type='${ch.formationType}', " +
                        "semantic='${ch.semanticComponent}', phonetic='${ch.phoneticComponent}', " +
                        "components=[$comps], hint='${ch.etymologyHint}'"
                }
            } else {
                "Analyze each character's official Kangxi radical, phonetic/semantic parts, and composition."
            }

            val prompt = """
                You are an expert Chinese etymology coach combining CharacterPop (vivid component stories) and HanziCraft (structural decomposition, radicals, phonetic/semantic cues, and word-building history).
                Word to teach: ${word.hanzi} (${word.pinyin}) — "${word.english}" (HSK 3.0 Level ${word.hskLevel}).

                Verified character decomposition facts:
                $structuralContext

                Provide:
                1. wordMakingLogic: Show literally how the characters combine to form "${word.hanzi}" (e.g. "电 (electricity) + 脑 (brain) = 'electric brain' → computer"). For single-character words, show how its radical + components combine.
                2. characterPopStory: A vivid, concrete 2-3 sentence memory story weaving the exact radicals/components into an unforgettable scene.
                3. historicalOrigin: Authentic linguistic & historical evolution (Oracle Bone / Bronze / Seal script origins, phono-semantic 形声, ideographic 会意, or pictographic 象形 roots).
                4. characterNotes: One entry per unique Hanzi character in "${word.hanzi}" explaining its formation and a 1-sentence mini-story.
                5. relatedWords: 4 useful HSK words sharing a key character or radical from "${word.hanzi}".
            """.trimIndent()

            val response = withTimeout(60_000L) {
                etymologyStoryModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty etymology response."))

            val notesArr = json.optJSONArray("characterNotes")
            val notes = mutableListOf<AiCharacterNote>()
            if (notesArr != null) {
                for (i in 0 until notesArr.length()) {
                    val item = notesArr.getJSONObject(i)
                    notes += AiCharacterNote(
                        character = item.optString("character", ""),
                        formationExplanation = item.optString("formationExplanation", ""),
                        miniStory = item.optString("miniStory", "")
                    )
                }
            }

            val relatedArr = json.optJSONArray("relatedWords")
            val related = mutableListOf<RelatedFamilyWord>()
            if (relatedArr != null) {
                for (i in 0 until relatedArr.length()) {
                    val item = relatedArr.getJSONObject(i)
                    val h = item.optString("hanzi", "").trim()
                    if (h.isNotEmpty()) {
                        related += RelatedFamilyWord(
                            hanzi = h,
                            pinyin = item.optString("pinyin", ""),
                            english = item.optString("english", "")
                        )
                    }
                }
            }

            Result.success(
                WordEtymologyStory(
                    hanzi = word.hanzi,
                    wordMakingLogic = json.optString("wordMakingLogic", ""),
                    characterPopStory = json.optString("characterPopStory", ""),
                    historicalOrigin = json.optString("historicalOrigin", ""),
                    characterNotes = notes,
                    relatedWords = related
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "Word etymology generation timed out", e)
            Result.failure(Exception("Story & etymology timed out after 60 seconds. Tap Retry."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Word etymology generation failed", e)
            Result.failure(e)
        }
    }
}
