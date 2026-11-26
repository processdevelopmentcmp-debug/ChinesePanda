const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_learner_1";
const BOB_UID = "bob_learner_2";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- 1. USER PROFILE TESTS ---

test("Unauthenticated user: denied reading or writing profile and subcollections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Hacker",
      xp: 0,
      streak: 1,
      hearts: 5,
      currentHskLevel: 1,
      completedUnitIds: [],
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("custom_words").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("srs_cards").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("saved_stories").get()
  );
});

test("UserProfile: owner create, partial update, immutable guards, and cross-user denial", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Valid create matching Kotlin UserProfileRepository
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      xp: 350,
      streak: 5,
      hearts: 5,
      currentHskLevel: 1,
      completedUnitIds: ["unit_1"],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Valid owner get
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Listing all users is forbidden
  await assertFails(aliceDb.collection("users").get());

  // Valid partial update (mutable fields only)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      xp: 370,
      completedUnitIds: ["unit_1", "unit_2"],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      xp: 380,
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Reject mutating immutable createdAt
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field (Shadow Update test)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject invalid hearts (> 5)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      hearts: 99,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user isolation
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      xp: 9999,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

// --- 2. CUSTOM WORDS SUBCOLLECTION TESTS ---

test("CustomWords: owner CRUD, list query, invalid payload & cross-user denial", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Empty subcollection list must succeed for owner
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("custom_words").get()
  );

  // Valid create (no `id` key, matches Kotlin CustomWord)
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("custom_words")
      .doc("word_saoma")
      .set({
        hanzi: "扫码",
        pinyin: "sǎo mǎ",
        english: "Scan QR Code",
        hskLevel: 2,
        topicId: 10,
        writable: true,
        exampleHanzi: "请扫码付款。",
        examplePinyin: "Qǐng sǎo mǎ fùkuǎn.",
        exampleEnglish: "Please scan the QR code to pay.",
        createdAt: created,
        updatedAt: created,
      })
  );

  // Reject unexpected @DocumentId `id` field in body
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("custom_words")
      .doc("word_bad")
      .set({
        id: "word_bad",
        hanzi: "测试",
        pinyin: "cè shì",
        english: "Test",
        hskLevel: 3,
        topicId: 9,
        writable: true,
        exampleHanzi: "",
        examplePinyin: "",
        exampleEnglish: "",
        createdAt: created,
        updatedAt: created,
      })
  );

  // Reject out-of-range hskLevel (10)
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("custom_words")
      .doc("word_lvl10")
      .set({
        hanzi: "测试",
        pinyin: "cè shì",
        english: "Test",
        hskLevel: 10,
        topicId: 9,
        writable: true,
        exampleHanzi: "",
        examplePinyin: "",
        exampleEnglish: "",
        createdAt: created,
        updatedAt: created,
      })
  );

  // Cross-user isolation
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("custom_words").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("custom_words")
      .doc("word_saoma")
      .delete()
  );
});

// --- 3. SM-2 SRS CARDS SUBCOLLECTION TESTS ---

test("SrsCards: owner create, SM-2 review update, optional mnemonicHint, and cross-user denial", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("srs_cards")
      .doc("card_101")
      .set({
        wordKey: "w_101",
        hanzi: "你好",
        pinyin: "nǐ hǎo",
        english: "Hello",
        hskLevel: 1,
        exampleHanzi: "你好！很高兴认识你。",
        repetitions: 0,
        intervalDays: 1,
        easeFactor: 2.5,
        lapses: 0,
        nextReviewEpochMs: Date.now() - 5000,
        createdAt: created,
        updatedAt: created,
      })
  );

  // Valid SM-2 partial update including AI mnemonicHint
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("srs_cards")
      .doc("card_101")
      .update({
        repetitions: 1,
        intervalDays: 1,
        easeFactor: 2.6,
        lapses: 3,
        nextReviewEpochMs: Date.now() + 86400000,
        mnemonicHint: "Person (亻) + Good (好) greeting warmly!",
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Reject mutating immutable wordKey
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("srs_cards")
      .doc("card_101")
      .update({
        wordKey: "hijacked_key",
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Reject invalid easeFactor (< 1.3)
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("srs_cards")
      .doc("card_101")
      .update({
        easeFactor: 0.5,
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Cross-user denial
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("srs_cards").get()
  );
});

// --- 4. SAVED STORIES SUBCOLLECTION TESTS ---

test("SavedStories: owner create, list, delete, immutable update denial, and cross-user denial", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("saved_stories")
      .doc("story_1")
      .set({
        titleZh: "周末去喝茶",
        titleEn: "Going for Tea on the Weekend",
        hskLevel: 2,
        topicName: "Food & Restaurant",
        sentences: [
          {
            zh: "周末我和朋友去茶馆喝茶。",
            pinyin: "Zhōumò wǒ hé péngyǒu qù cháguǎn hē chá.",
            en: "On the weekend my friend and I went to a teahouse.",
          },
        ],
        quizQuestion: "他们周末去了哪里？",
        quizOptions: ["茶馆", "医院", "机场"],
        quizCorrectIndex: 0,
        createdAt: created,
        updatedAt: created,
      })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_stories").get()
  );

  // Updates are disabled on saved_stories
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("saved_stories")
      .doc("story_1")
      .update({
        titleEn: "Modified",
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Cross-user denial
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("saved_stories").get()
  );

  // Owner delete succeeds
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("saved_stories")
      .doc("story_1")
      .delete()
  );
});
