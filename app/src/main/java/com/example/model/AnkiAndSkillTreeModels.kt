package com.example.model

enum class WordStrength(val label: String) {
    WEAK("Weak"),
    MEDIUM("Medium"),
    STRONG("Strong")
}

enum class WordStrengthFilter(val label: String) {
    ALL("All"),
    WEAK("Weak"),
    MEDIUM("Medium"),
    STRONG("Strong")
}

data class LearnedWordEntry(
    val word: VocabWord,
    val srsCard: SrsCardDoc? = null,
    val strength: WordStrength = WordStrength.WEAK,
    val isDue: Boolean = true
)

data class ActiveAnkiSession(
    val id: String,
    val title: String,
    val subtitle: String,
    val entries: List<LearnedWordEntry>
)

data class ShortcutQuizQuestion(
    val word: VocabWord,
    val options: List<String>,
    val correctIndex: Int
)

data class ActiveShortcutQuiz(
    val targetHskLevel: Int,
    val title: String,
    val questions: List<ShortcutQuizQuestion>
)

data class GrammarVaultItem(
    val id: String,
    val hskLevel: Int,
    val pattern: String,
    val pinyin: String,
    val title: String,
    val explanation: String,
    val exampleHanzi: String,
    val examplePinyin: String,
    val exampleEnglish: String
)

object HskGrammarVault {
    val items: List<GrammarVaultItem> = listOf(
        GrammarVaultItem(
            id = "g1",
            hskLevel = 1,
            pattern = "A + 是 + B",
            pinyin = "A + shì + B",
            title = "Identity with 是 (to be)",
            explanation = "Links two nouns (never directly before adjectives). Negate with 不是 (bú shì).",
            exampleHanzi = "我是学习中文的学生。",
            examplePinyin = "Wǒ shì xuéxí Zhōngwén de xuésheng.",
            exampleEnglish = "I am a student learning Chinese."
        ),
        GrammarVaultItem(
            id = "g2",
            hskLevel = 1,
            pattern = "Subject + 很 + Adjective",
            pinyin = "Subject + hěn + Adjective",
            title = "Linking Adjectives with 很",
            explanation = "Connects subjects and adjectives naturally without using 是.",
            exampleHanzi = "今天天气很舒服。",
            examplePinyin = "Jīntiān tiānqì hěn shūfu.",
            exampleEnglish = "Today's weather is very comfortable."
        ),
        GrammarVaultItem(
            id = "g3",
            hskLevel = 1,
            pattern = "Sentence + 吗？",
            pinyin = "Sentence + ma?",
            title = "Yes/No Questions with 吗",
            explanation = "Add 吗 at the end of any statement to turn it into a polite yes/no question.",
            exampleHanzi = "你今天有时间吗？",
            examplePinyin = "Nǐ jīntiān yǒu shíjiān ma?",
            exampleEnglish = "Do you have time today?"
        ),
        GrammarVaultItem(
            id = "g4",
            hskLevel = 1,
            pattern = "Number + Measure Word + Noun",
            pinyin = "Shùzì + Liàngcí + Míngcí",
            title = "Counting with Measure Words (个, 本, 杯)",
            explanation = "Mandarin always puts a classifier between a number and a noun; use 两 (liǎng) before measure words.",
            exampleHanzi = "请给我两杯热茶。",
            examplePinyin = "Qǐng gěi wǒ liǎng bēi rè chá.",
            exampleEnglish = "Please give me two cups of hot tea."
        ),
        GrammarVaultItem(
            id = "g5",
            hskLevel = 1,
            pattern = "Subject + Time + Place + Verb",
            pinyin = "STPV Word Order",
            title = "Golden Mandarin Word Order (STPV)",
            explanation = "Place time and location phrases BEFORE the main verb, never at the end of the sentence.",
            exampleHanzi = "我明天早上在图书馆学习。",
            examplePinyin = "Wǒ míngtiān zǎoshang zài túshūguǎn xuéxí.",
            exampleEnglish = "I will study at the library tomorrow morning."
        ),
        GrammarVaultItem(
            id = "g6",
            hskLevel = 2,
            pattern = "Verb + 了 / Sentence + 了",
            pinyin = "Verb + le",
            title = "Completed Action & Change of State (了)",
            explanation = "After a verb, 了 marks a completed action; at sentence end, 了 shows a new situation.",
            exampleHanzi = "我买了一本中文书。",
            examplePinyin = "Wǒ mǎi le yì běn Zhōngwén shū.",
            exampleEnglish = "I bought a Chinese book."
        ),
        GrammarVaultItem(
            id = "g7",
            hskLevel = 2,
            pattern = "A + 比 + B + Adjective",
            pinyin = "A + bǐ + B + Adj",
            title = "Comparisons with 比",
            explanation = "Compares two things directly. Add 一点儿 or 多了 after the adjective for degree.",
            exampleHanzi = "坐高铁比坐公共汽车快多了。",
            examplePinyin = "Zuò gāotiě bǐ zuò gōnggòng qìchē kuài duō le.",
            exampleEnglish = "Taking high-speed rail is much faster than taking the bus."
        ),
        GrammarVaultItem(
            id = "g8",
            hskLevel = 2,
            pattern = "正在 + Verb + 呢",
            pinyin = "zhèngzài + Verb + ne",
            title = "Ongoing Action (Progressive)",
            explanation = "Shows an action actively happening right now.",
            exampleHanzi = "我正在复习今天的生词呢。",
            examplePinyin = "Wǒ zhèngzài fùxí jīntiān de shēngcí ne.",
            exampleEnglish = "I am currently reviewing today's new words."
        ),
        GrammarVaultItem(
            id = "g9",
            hskLevel = 2,
            pattern = "Verb + 过",
            pinyin = "Verb + guo",
            title = "Past Life Experience (过)",
            explanation = "Expresses having had the experience of doing something at least once before.",
            exampleHanzi = "你去过北京吗？",
            examplePinyin = "Nǐ qù guo Běijīng ma?",
            exampleEnglish = "Have you ever been to Beijing?"
        ),
        GrammarVaultItem(
            id = "g10",
            hskLevel = 3,
            pattern = "把 + Object + Verb + Result",
            pinyin = "bǎ + Object + Verb + Result",
            title = "The 把 (Disposal) Structure",
            explanation = "Pulls a specific object before the verb to emphasize how it is handled or changed.",
            exampleHanzi = "请把护照放在包里。",
            examplePinyin = "Qǐng bǎ hùzhào fàng zài bāo lǐ.",
            exampleEnglish = "Please put the passport inside the bag."
        ),
        GrammarVaultItem(
            id = "g11",
            hskLevel = 3,
            pattern = "越 A 越 B",
            pinyin = "yuè A yuè B",
            title = "The More A, The More B",
            explanation = "Shows two qualities increasing together proportionally.",
            exampleHanzi = "中文越学越有趣。",
            examplePinyin = "Zhōngwén yuè xué yuè yǒuqù.",
            exampleEnglish = "The more you study Chinese, the more interesting it becomes."
        ),
        GrammarVaultItem(
            id = "g12",
            hskLevel = 3,
            pattern = "是 ... 的",
            pinyin = "shì ... de",
            title = "Emphasizing Time, Place, or Manner",
            explanation = "Highlights WHEN, WHERE, or HOW a past action took place.",
            exampleHanzi = "我们是坐地铁来的。",
            examplePinyin = "Wǒmen shì zuò dìtiě lái de.",
            exampleEnglish = "We came by subway."
        ),
        GrammarVaultItem(
            id = "g13",
            hskLevel = 4,
            pattern = "不但 ... 而且 ...",
            pinyin = "búdàn ... érqiě ...",
            title = "Not Only ... But Also ...",
            explanation = "Connects two progressive facts or qualities about the same subject.",
            exampleHanzi = "这家饭馆的菜不但好吃，而且不贵。",
            examplePinyin = "Zhè jiā fànguǎn de cài búdàn hǎochī, érqiě bú guì.",
            exampleEnglish = "This restaurant's food is not only delicious, but also inexpensive."
        ),
        GrammarVaultItem(
            id = "g14",
            hskLevel = 4,
            pattern = "只要 ... 就 ...",
            pinyin = "zhǐyào ... jiù ...",
            title = "As Long As ... Then ...",
            explanation = "States a sufficient condition leading directly to a result.",
            exampleHanzi = "只要每天坚持，一定能学好中文。",
            examplePinyin = "Zhǐyào měitiān jiānchí, yídìng néng xuéhǎo Zhōngwén.",
            exampleEnglish = "As long as you persist daily, you will definitely master Chinese."
        ),
        GrammarVaultItem(
            id = "g15",
            hskLevel = 5,
            pattern = "被 / 让 / 叫 (Passive)",
            pinyin = "bèi / ràng / jiào",
            title = "Passive Voice Structure",
            explanation = "Places the receiver of an action first, often for unexpected or formal events.",
            exampleHanzi = "我的问题被老师耐心地解决了。",
            examplePinyin = "Wǒ de wèntí bèi lǎoshī nàixīn de jiějué le.",
            exampleEnglish = "My question was patiently solved by the teacher."
        ),
        GrammarVaultItem(
            id = "g16",
            hskLevel = 6,
            pattern = "既 ... 又 ...",
            pinyin = "jì ... yòu ...",
            title = "Both ... And ... (Parallel Qualities)",
            explanation = "Pairs two balanced adjectives or verbs elegantly in written and spoken Mandarin.",
            exampleHanzi = "这种方法既科学又高效。",
            examplePinyin = "Zhè zhǒng fāngfǎ jì kēxué yòu gāoxiào.",
            exampleEnglish = "This method is both scientific and efficient."
        )
    )
}
