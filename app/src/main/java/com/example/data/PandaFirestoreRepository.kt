package com.example.data

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.CustomWordDoc
import com.example.model.GradedStory
import com.example.model.SavedStoryDoc
import com.example.model.Sm2Result
import com.example.model.SrsCardDoc
import com.example.model.UserProfile
import com.example.model.VocabWord
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PandaFirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun userDocRef(uid: String) = db.collection("users").document(uid)

    // --- 1. USER PROFILE ---

    fun observeProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user changed." }
        val path = "users/$uid"
        emitAll(
            userDocRef(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(userId: String, displayName: String): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user profile write." }
            val ref = userDocRef(uid)
            val snap = ref.get().await()
            if (!snap.exists()) {
                val safeName = displayName.trim().ifBlank { "Panda Learner" }.take(60)
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "xp" to 100L,
                    "streak" to 1L,
                    "hearts" to 5L,
                    "currentHskLevel" to 1L,
                    "completedUnitIds" to emptyList<String>(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                ref.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun awardXpAndUnit(
        userId: String,
        xpDelta: Long,
        completedUnitId: String? = null,
        newHskLevel: Long? = null
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user profile update." }
            val ref = userDocRef(uid)
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (xpDelta > 0L) {
                updates["xp"] = FieldValue.increment(xpDelta)
            }
            if (!completedUnitId.isNullOrBlank()) {
                updates["completedUnitIds"] = FieldValue.arrayUnion(completedUnitId)
            }
            if (newHskLevel != null && newHskLevel in 1L..9L) {
                updates["currentHskLevel"] = newHskLevel
            }
            ref.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateHearts(userId: String, hearts: Long): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user update." }
            val clamped = hearts.coerceIn(0L, 5L)
            userDocRef(uid).update(
                mapOf(
                    "hearts" to clamped,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- 2. CUSTOM & AI-GENERATED WORDS ---

    fun observeCustomWords(userId: String): Flow<List<CustomWordDoc>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user changed." }
        val path = "users/$uid/custom_words"
        emitAll(
            userDocRef(uid).collection("custom_words")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        CustomWordDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addCustomWords(userId: String, words: List<VocabWord>): Result<Int> {
        val basePath = "users/$userId/custom_words"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user write." }
            var added = 0
            val batch = db.batch()
            val col = userDocRef(uid).collection("custom_words")
            words.take(40).forEach { w ->
                val hanzi = w.hanzi.trim().take(30)
                val pinyin = w.pinyin.trim().take(100)
                val english = w.english.trim().take(200)
                if (hanzi.isNotEmpty() && pinyin.isNotEmpty() && english.isNotEmpty()) {
                    val docRef = col.document()
                    val payload = mapOf(
                        "hanzi" to hanzi,
                        "pinyin" to pinyin,
                        "english" to english,
                        "hskLevel" to w.hskLevel.coerceIn(1, 9).toLong(),
                        "topicId" to w.topicId.coerceIn(1, 14).toLong(),
                        "writable" to w.writable,
                        "exampleHanzi" to w.exampleHanzi.trim().take(200),
                        "examplePinyin" to w.examplePinyin.trim().take(250),
                        "exampleEnglish" to w.exampleEnglish.trim().take(250),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    batch.set(docRef, payload)
                    added++
                }
            }
            if (added > 0) {
                batch.commit().await()
            }
            Result.success(added)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, basePath)
            Result.failure(e)
        }
    }

    // --- 3. SM-2 SPACED REPETITION CARDS ---

    fun observeSrsCards(userId: String): Flow<List<SrsCardDoc>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user changed." }
        val path = "users/$uid/srs_cards"
        emitAll(
            userDocRef(uid).collection("srs_cards")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        SrsCardDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    private fun buildSafeSrsDocId(wordKey: String): String {
        val asciiPrefix = wordKey.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").trim('_').take(24)
        val needsHash = wordKey.any { it.code > 127 } || wordKey.length > 24
        return if (needsHash) {
            val hexHash = wordKey.hashCode().toUInt().toString(16)
            "card_${asciiPrefix.ifBlank { "w" }}_$hexHash".take(40)
        } else {
            "card_${asciiPrefix.ifBlank { "w" }}".take(40)
        }
    }

    suspend fun upsertSrsCard(userId: String, word: VocabWord): Result<Unit> {
        val safeDocId = buildSafeSrsDocId(word.key)
        val path = "users/$userId/srs_cards/$safeDocId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user SRS write." }
            val ref = userDocRef(uid).collection("srs_cards").document(safeDocId)
            val existing = ref.get().await()
            if (!existing.exists()) {
                val payload = mapOf(
                    "wordKey" to word.key.take(40).ifBlank { safeDocId },
                    "hanzi" to word.hanzi.trim().take(30),
                    "pinyin" to word.pinyin.trim().take(100),
                    "english" to word.english.trim().take(200),
                    "hskLevel" to word.hskLevel.coerceIn(1, 9).toLong(),
                    "exampleHanzi" to word.exampleHanzi.trim().take(200),
                    "repetitions" to 0L,
                    "intervalDays" to 1L,
                    "easeFactor" to 2.5,
                    "lapses" to 0L,
                    "nextReviewEpochMs" to System.currentTimeMillis(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                ref.set(payload).await()
            } else {
                // Make it due immediately so learner can review it right away
                ref.update(
                    mapOf(
                        "nextReviewEpochMs" to System.currentTimeMillis(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertSrsCardsBatch(userId: String, words: List<VocabWord>): Result<Int> {
        var added = 0
        for (word in words.distinctBy { it.hanzi }) {
            val res = upsertSrsCard(userId, word)
            if (res.isSuccess) added++
        }
        return Result.success(added)
    }

    suspend fun updateSrsReview(
        userId: String,
        cardId: String,
        sm2: Sm2Result,
        mnemonicHint: String? = null
    ): Result<Unit> {
        val path = "users/$userId/srs_cards/$cardId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user SRS update." }
            require(cardId.isNotBlank()) { "Card ID must not be blank." }
            val rawMap = mutableMapOf<String, Any?>(
                "repetitions" to sm2.repetitions.coerceIn(0, 1000).toLong(),
                "intervalDays" to sm2.intervalDays.coerceIn(0, 3650).toLong(),
                "easeFactor" to sm2.easeFactor.coerceIn(1.3, 5.0),
                "lapses" to sm2.lapses.coerceIn(0, 1000).toLong(),
                "nextReviewEpochMs" to sm2.nextReviewEpochMs.coerceAtLeast(0L),
                "mnemonicHint" to mnemonicHint?.trim()?.take(500),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            @Suppress("UNCHECKED_CAST")
            val filtered = rawMap.filterValues { it != null } as Map<String, Any>
            userDocRef(uid).collection("srs_cards").document(cardId).update(filtered).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- 4. SAVED AI GRADED STORIES ---

    fun observeSavedStories(userId: String): Flow<List<SavedStoryDoc>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user changed." }
        val path = "users/$uid/saved_stories"
        emitAll(
            userDocRef(uid).collection("saved_stories")
                .snapshots()
                .map { snap ->
                    snap.toObjects(
                        SavedStoryDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveGeneratedStory(userId: String, story: GradedStory): Result<Unit> {
        val basePath = "users/$userId/saved_stories"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user story write." }
            val ref = userDocRef(uid).collection("saved_stories").document()
            val sentenceMaps = story.sentences.take(20).map { s ->
                mapOf(
                    "zh" to s.zh.take(240),
                    "pinyin" to s.pinyin.take(300),
                    "en" to s.en.take(300)
                )
            }
            val options = story.quizOptions.take(4).ifEmpty { listOf("Option A", "Option B") }
            val payload = mapOf(
                "titleZh" to story.titleZh.trim().take(100).ifBlank { "故事" },
                "titleEn" to story.titleEn.trim().take(120).ifBlank { "Chinese Story" },
                "hskLevel" to story.hskLevel.coerceIn(1, 9).toLong(),
                "topicName" to story.topicName.trim().take(80).ifBlank { "General" },
                "sentences" to sentenceMaps,
                "quizQuestion" to story.quizQuestion.trim().take(250).ifBlank { "What happened in the story?" },
                "quizOptions" to options,
                "quizCorrectIndex" to story.quizCorrectIndex.coerceIn(0, options.lastIndex).toLong(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, basePath)
            Result.failure(e)
        }
    }
}
