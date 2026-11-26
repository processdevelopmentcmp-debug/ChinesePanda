package com.example.data

data class RadicalPiece(
    val glyph: String,
    val name: String,
    val meaning: String
)

data class RadicalFusionRecipe(
    val leftRadical: String,
    val rightComponent: String,
    val resultChar: String,
    val pinyin: String,
    val english: String,
    val hskLevel: Int,
    val mnemonic: String
)

data class FamilyTreeMember(
    val char: String,
    val pinyin: String,
    val english: String,
    val semanticRadical: String,
    val semanticMeaning: String,
    val mnemonic: String,
    val hskLevel: Int
)

data class CharacterFamilyTree(
    val phoneticStem: String,
    val stemPinyin: String,
    val stemMeaning: String,
    val soundCueNote: String,
    val members: List<FamilyTreeMember>
)

data class TonePairDrillItem(
    val id: String,
    val tone1: Int,
    val tone2: Int,
    val hanzi: String,
    val pinyin: String,
    val english: String,
    val rhythmTip: String
)

data class MinimalPairQuestion(
    val id: String,
    val category: String,
    val leftHanzi: String,
    val leftPinyin: String,
    val leftEnglish: String,
    val rightHanzi: String,
    val rightPinyin: String,
    val rightEnglish: String,
    val distinctionTip: String
)

data class ClozeContextCard(
    val id: String,
    val hskLevel: Int,
    val sentenceWithBlank: String,
    val fullSentence: String,
    val fullPinyin: String,
    val englishTranslation: String,
    val missingWord: String,
    val missingPinyin: String,
    val options: List<String>,
    val grammarNote: String
)

data class ShadowingDialogueScript(
    val id: String,
    val title: String,
    val hskLevel: Int,
    val scenario: String,
    val hanzi: String,
    val pinyin: String,
    val english: String
)

/**
 * Structured Mandarin drills: Interactive Radical Builder recipes, Semantic+Phonetic Family Trees,
 * all 20 Mandarin Tone-Pair Drills, Minimal-Pair Listening Quizzes, Cloze-Deletion Context Cards,
 * and HSK Shadowing Dialogue scripts.
 */
object MandarinDrillsData {

    val semanticRadicals: List<RadicalPiece> = listOf(
        RadicalPiece("氵", "Water", "liquid / river"),
        RadicalPiece("扌", "Hand", "action / touch"),
        RadicalPiece("亻", "Person", "human / posture"),
        RadicalPiece("讠", "Speech", "words / talking"),
        RadicalPiece("日", "Sun", "time / weather"),
        RadicalPiece("忄", "Heart", "emotion / mind"),
        RadicalPiece("口", "Mouth", "voice / eating"),
        RadicalPiece("女", "Woman", "family / female"),
        RadicalPiece("木", "Wood", "tree / nature"),
        RadicalPiece("足", "Foot", "run / walk"),
        RadicalPiece("火", "Fire", "heat / light"),
        RadicalPiece("饣", "Food", "eat / hunger")
    )

    val combinableComponents: List<RadicalPiece> = listOf(
        RadicalPiece("青", "qīng", "blue-green / sound clue"),
        RadicalPiece("包", "bāo", "wrap / sound clue"),
        RadicalPiece("马", "mǎ", "horse / sound clue"),
        RadicalPiece("木", "mù", "tree / wood"),
        RadicalPiece("子", "zǐ", "child / son"),
        RadicalPiece("可", "kě", "can / sound clue"),
        RadicalPiece("门", "mén", "door / gate"),
        RadicalPiece("巴", "bā", "cling / sound clue"),
        RadicalPiece("也", "yě", "also / sound clue"),
        RadicalPiece("主", "zhǔ", "master / host"),
        RadicalPiece("工", "gōng", "work / sound clue"),
        RadicalPiece("丁", "dīng", "nail / strike")
    )

    val radicalFusionRecipes: List<RadicalFusionRecipe> = listOf(
        RadicalFusionRecipe("氵", "青", "清", "qīng", "clear / pure", 2, "Water (氵) that looks crystal blue-green (青) is pure and clear."),
        RadicalFusionRecipe("讠", "青", "请", "qǐng", "please / invite", 1, "Polite words (讠) with the sound of 青 — saying 'please' gracefully."),
        RadicalFusionRecipe("日", "青", "晴", "qíng", "sunny / clear sky", 2, "When the sun (日) shines into a blue-green (青) sky, it's sunny!"),
        RadicalFusionRecipe("忄", "青", "情", "qíng", "feeling / emotion", 2, "The heart (忄) moved like 青 carries deep emotions and feelings."),
        RadicalFusionRecipe("扌", "包", "抱", "bào", "hug / hold", 3, "Use your hand (扌) to wrap around (包) someone in a warm hug."),
        RadicalFusionRecipe("足", "包", "跑", "pǎo", "run", 2, "Your feet (足) moving fast like 包 carry you forward as you run!"),
        RadicalFusionRecipe("氵", "包", "泡", "pào", "bubble / steep tea", 4, "Water (氵) wrapped (包) around air creates a bubble — or steeps tea."),
        RadicalFusionRecipe("饣", "包", "饱", "bǎo", "full (after eating)", 3, "When food (饣) completely wraps/fills (包) your belly, you feel full."),
        RadicalFusionRecipe("女", "马", "妈", "mā", "mom / mother", 1, "Woman (女) + sound clue 马 (mǎ) gives the loving word for Mom (mā)."),
        RadicalFusionRecipe("口", "马", "吗", "ma", "question particle", 1, "A mouth (口) adding the 马 sound turns any statement into a yes/no question."),
        RadicalFusionRecipe("亻", "木", "休", "xiū", "rest", 2, "A person (亻) leaning quietly against a shady tree (木) to rest."),
        RadicalFusionRecipe("木", "木", "林", "lín", "woods / grove", 3, "Two trees (木 + 木) standing side by side grow into a peaceful forest grove."),
        RadicalFusionRecipe("女", "子", "好", "hǎo", "good / well", 1, "A mother (女) cradling her child (子) is the timeless symbol of 'good'."),
        RadicalFusionRecipe("氵", "可", "河", "hé", "river", 2, "Flowing water (氵) that can (可) carry boats along forms a wide river."),
        RadicalFusionRecipe("口", "门", "问", "wèn", "ask", 1, "A mouth (口) peeking inside a doorway (门) to ask a question."),
        RadicalFusionRecipe("忄", "门", "闷", "mèn", "stuffy / bored", 4, "A heart (忄) trapped behind a closed door (门) feels stuffy and restless."),
        RadicalFusionRecipe("扌", "巴", "把", "bǎ", "grasp / object marker", 2, "A hand (扌) firmly gripping (巴) something in front of you."),
        RadicalFusionRecipe("口", "巴", "吧", "ba", "suggestion particle", 1, "A soft mouth (口) sound 'ba' (巴) makes a gentle suggestion: 'Let's go!'"),
        RadicalFusionRecipe("亻", "也", "他", "tā", "he / him", 1, "A person (亻) who is 'also' (也) another human being — he/him."),
        RadicalFusionRecipe("女", "也", "她", "tā", "she / her", 1, "A woman (女) paired with 也 forms the feminine pronoun she/her."),
        RadicalFusionRecipe("氵", "也", "池", "chí", "pond / pool", 4, "Water (氵) collected in a basin (也) becomes a calm lotus pond."),
        RadicalFusionRecipe("亻", "主", "住", "zhù", "live / stay", 1, "A person (亻) staying as the host/master (主) of a home lives there."),
        RadicalFusionRecipe("氵", "主", "注", "zhù", "pour / pay attention", 3, "Pouring water (氵) steadily toward one spot (主) — focusing your attention!"),
        RadicalFusionRecipe("氵", "工", "江", "jiāng", "great river (Yangtze)", 4, "Water (氵) + sound clue 工 (gōng → jiāng) creates a mighty river."),
        RadicalFusionRecipe("扌", "丁", "打", "dǎ", "hit / play / call", 1, "A hand (扌) striking a nail (丁) is the classic action verb 'to strike/play'."),
        RadicalFusionRecipe("火", "丁", "灯", "dēng", "lamp / light", 2, "Fire (火) perched on a nail/stand (丁) lights up the room like a lamp."),
        RadicalFusionRecipe("讠", "丁", "订", "dìng", "book / reserve", 4, "Words (讠) nailed down (丁) firmly mean booking a ticket or reservation.")
    )

    val characterFamilyTrees: List<CharacterFamilyTree> = listOf(
        CharacterFamilyTree(
            phoneticStem = "青",
            stemPinyin = "qīng",
            stemMeaning = "blue-green / fresh",
            soundCueNote = "Every character in this tree echoes the '-ing' sound of 青 (qīng / qǐng / qíng / jīng)!",
            members = listOf(
                FamilyTreeMember("请", "qǐng", "please / invite", "讠", "speech", "Polite speech (讠) + 青 = please.", 1),
                FamilyTreeMember("清", "qīng", "clear / clean", "氵", "water", "Clear water (氵) + 青 = clean/clear.", 2),
                FamilyTreeMember("晴", "qíng", "sunny", "日", "sun", "Bright sun (日) + 青 = sunny sky.", 2),
                FamilyTreeMember("情", "qíng", "emotion / affair", "忄", "heart", "Heart (忄) + 青 = deep feeling.", 2),
                FamilyTreeMember("睛", "jīng", "eyeball", "目", "eye", "Eye (目) + 青 = clear pupil/eye.", 2)
            )
        ),
        CharacterFamilyTree(
            phoneticStem = "包",
            stemPinyin = "bāo",
            stemMeaning = "wrap / bundle",
            soundCueNote = "Shared '-ao' rhyme (bāo / bào / pǎo / pào / bǎo) paired with 5 everyday meaning radicals!",
            members = listOf(
                FamilyTreeMember("抱", "bào", "hug / hold", "扌", "hand", "Arms/hand (扌) wrapping (包) = hug.", 3),
                FamilyTreeMember("跑", "pǎo", "run", "足", "foot", "Feet (足) propelling you + 包 = run.", 2),
                FamilyTreeMember("泡", "pào", "bubble / soak", "氵", "water", "Water (氵) wrapped around air = bubble.", 4),
                FamilyTreeMember("饱", "bǎo", "full (eaten)", "饣", "food", "Food (饣) filling your belly = full.", 3),
                FamilyTreeMember("炮", "pào", "firecracker / cannon", "火", "fire", "Fire (火) packed in paper (包) = firecracker.", 5)
            )
        ),
        CharacterFamilyTree(
            phoneticStem = "马",
            stemPinyin = "mǎ",
            stemMeaning = "horse",
            soundCueNote = "One of the most famous sound families in Mandarin — all share 'ma' across different tones!",
            members = listOf(
                FamilyTreeMember("妈", "mā", "mother / mom", "女", "woman", "Woman (女) + 马 sound = mom.", 1),
                FamilyTreeMember("吗", "ma", "question marker", "口", "mouth", "Mouth (口) + 马 sound = yes/no ?", 1),
                FamilyTreeMember("码", "mǎ", "code / number", "石", "stone", "Counting stones (石) + 马 = code/QR.", 3),
                FamilyTreeMember("骂", "mà", "scold", "口口", "two mouths", "Two shouting mouths over 马 = scold.", 4)
            )
        ),
        CharacterFamilyTree(
            phoneticStem = "巴",
            stemPinyin = "bā",
            stemMeaning = "cling / hope",
            soundCueNote = "Listen for the '-a' sound (bà / ba / bǎ / pá) — change the left radical, unlock 4 HSK words!",
            members = listOf(
                FamilyTreeMember("爸", "bà", "father / dad", "父", "father", "Father (父) + 巴 sound = dad.", 1),
                FamilyTreeMember("吧", "ba", "suggestion tone", "口", "mouth", "Mouth (口) + 巴 = gentle 'let's'.", 1),
                FamilyTreeMember("把", "bǎ", "hold / handle", "扌", "hand", "Hand (扌) gripping 巴 = grasp.", 2),
                FamilyTreeMember("爬", "pá", "climb / crawl", "爪", "claw", "Claws/hands (爪) clinging to 巴 = climb.", 3)
            )
        ),
        CharacterFamilyTree(
            phoneticStem = "方",
            stemPinyin = "fāng",
            stemMeaning = "direction / square",
            soundCueNote = "All share the 'f-ang' sound (fáng / fàng / fǎng) — ideal for HSK 2–4 expansion!",
            members = listOf(
                FamilyTreeMember("房", "fáng", "house / room", "户", "door/home", "House roof/door (户) + 方 = room.", 2),
                FamilyTreeMember("放", "fàng", "put / release", "攵", "tap/action", "Action hand (攵) + 方 = place/release.", 2),
                FamilyTreeMember("访", "fǎng", "visit / interview", "讠", "speech", "Talking (讠) in another place (方) = visit.", 4),
                FamilyTreeMember("防", "fáng", "defend / prevent", "阝", "mound/wall", "Wall (阝) guarding a place (方) = defend.", 4)
            )
        ),
        CharacterFamilyTree(
            phoneticStem = "可",
            stemPinyin = "kě",
            stemMeaning = "can / approve",
            soundCueNote = "Notice the '-e' vowel family (hé / gē / ā) spanning water, music, and family!",
            members = listOf(
                FamilyTreeMember("河", "hé", "river", "氵", "water", "Water (氵) + 可 = river.", 2),
                FamilyTreeMember("哥", "gē", "older brother", "可可", "double 可", "Two 可 stacked = big brother.", 1),
                FamilyTreeMember("歌", "gē", "song / sing", "欠", "exhale/open mouth", "Brother's voice + breath (欠) = song.", 2),
                FamilyTreeMember("何", "hé", "what / how", "亻", "person", "Person (亻) asking 可 = what/which.", 4)
            )
        )
    )

    /**
     * All 20 native Mandarin two-syllable tone-pair combinations (Tones 1..4 × Tones 1..5).
     */
    val twentyTonePairs: List<TonePairDrillItem> = listOf(
        // Tone 1 + (1, 2, 3, 4, 5)
        TonePairDrillItem("tp_11", 1, 1, "咖啡", "kā fēi", "coffee", "Stay steady and high across both syllables (55 → 55)."),
        TonePairDrillItem("tp_12", 1, 2, "中国", "zhōng guó", "China", "Hold high, drop briefly to mid, then sweep up cleanly (55 → 35)."),
        TonePairDrillItem("tp_13", 1, 3, "开始", "kāi shǐ", "begin / start", "High level then relax deep down into Tone 3 (55 → 214)."),
        TonePairDrillItem("tp_14", 1, 4, "高兴", "gāo xìng", "happy", "High flat launch right into a decisive downward drop (55 → 51)."),
        TonePairDrillItem("tp_15", 1, 5, "妈妈", "mā ma", "mom", "High sustained 1st syllable, light mid-low 2nd tap (55 → neutral)."),
        // Tone 2 + (1, 2, 3, 4, 5)
        TonePairDrillItem("tp_21", 2, 1, "时间", "shí jiān", "time", "Rise cleanly up to the ceiling and hold flat (35 → 55)."),
        TonePairDrillItem("tp_22", 2, 2, "学习", "xué xí", "study / learn", "Two crisp roller-coaster climbs: rise, reset mid, rise again (35 → 35)."),
        TonePairDrillItem("tp_23", 2, 3, "牛奶", "niú nǎi", "milk", "Climb high on niú, then drop deeply into nǎi (35 → 214)."),
        TonePairDrillItem("tp_24", 2, 4, "学校", "xué xiào", "school", "Peak at the end of xué, then plunge down sharply on xiào (35 → 51)."),
        TonePairDrillItem("tp_25", 2, 5, "明白", "míng bai", "understand", "Rising 1st syllable followed by a quick relaxed neutral drop."),
        // Tone 3 + (1, 2, 3, 4, 5)
        TonePairDrillItem("tp_31", 3, 1, "老师", "lǎo shī", "teacher", "Keep lǎo a low half-third (21), then jump high for shī (55)."),
        TonePairDrillItem("tp_32", 3, 2, "语言", "yǔ yán", "language", "Low half-third (21) smoothly gliding upward on yán (35)."),
        TonePairDrillItem("tp_33", 2, 3, "你好", "ní hǎo", "hello (3+3→2+3)", "Tone Sandhi! First 3rd tone rises like Tone 2 before Tone 3."),
        TonePairDrillItem("tp_34", 3, 4, "米饭", "mǐ fàn", "cooked rice", "Low half-third dip (21), leap to the top and crash down on fàn (51)."),
        TonePairDrillItem("tp_35", 3, 5, "喜欢", "xǐ huan", "like / enjoy", "Low Tone 3 base with a gentle, slightly higher neutral tap."),
        // Tone 4 + (1, 2, 3, 4, 5)
        TonePairDrillItem("tp_41", 4, 1, "面包", "miàn bāo", "bread", "Sharp fall on miàn (51), bounce straight back up to hold bāo (55)."),
        TonePairDrillItem("tp_42", 4, 2, "问题", "wèn tí", "question / problem", "V-shape flow: fall sharply on wèn, then scoop right up on tí (51 → 35)."),
        TonePairDrillItem("tp_43", 4, 3, "电脑", "diàn nǎo", "computer", "Drop down on diàn and settle comfortably low for nǎo (51 → 214)."),
        TonePairDrillItem("tp_44", 4, 4, "再见", "zài jiàn", "goodbye", "Double waterfall: sharp fall, quick high reset, sharp fall again (51 → 51)."),
        TonePairDrillItem("tp_45", 4, 5, "谢谢", "xiè xie", "thank you", "Crisp fall on xiè followed by an ultra-light low neutral tail.")
    )

    val minimalPairQuizzes: List<MinimalPairQuestion> = listOf(
        MinimalPairQuestion("mp1", "Tone 3 vs Tone 4", "买", "mǎi", "buy", "卖", "mài", "sell", "mǎi dips low (Tone 3); mài falls sharply from high (Tone 4)."),
        MinimalPairQuestion("mp2", "x- vs sh- Initial", "西", "xī", "west", "师", "shī", "teacher", "xī spreads lips flat with tongue behind lower teeth; shī curls tongue back."),
        MinimalPairQuestion("mp3", "Tone 1 vs Tone 3", "妈", "mā", "mom", "马", "mǎ", "horse", "mā stays high and flat; mǎ drops deep into the low vocal register."),
        MinimalPairQuestion("mp4", "s- vs sh- & Tone", "四", "sì", "four", "十", "shí", "ten", "sì is flat-tongue + falling Tone 4; shí is curled-tongue + rising Tone 2."),
        MinimalPairQuestion("mp5", "z- vs zh- Initial", "早", "zǎo", "early", "找", "zhǎo", "look for", "zǎo keeps the tongue flat behind teeth; zhǎo curls the tongue tip back."),
        MinimalPairQuestion("mp6", "-n vs -ng Final", "分", "fēn", "minute / divide", "风", "fēng", "wind", "fēn ends with tongue tip on ridge (front -n); fēng resonates in the back (-ng)."),
        MinimalPairQuestion("mp7", "sh- vs x- & u/ü", "书", "shū", "book", "需", "xū", "need", "shū has rounded 'oo' vowel; xū uses tight pursed-lip 'ü' sound."),
        MinimalPairQuestion("mp8", "q- vs x- Initial", "七", "qī", "seven", "西", "xī", "west", "qī starts with a crisp burst of air (aspirated); xī is a smooth hiss."),
        MinimalPairQuestion("mp9", "zh- vs j- Initial", "知", "zhī", "know", "鸡", "jī", "chicken", "zhī is retroflex (curled tongue); jī is palatal (tongue flat, smiling lips)."),
        MinimalPairQuestion("mp10", "Tone 1 vs Tone 2", "窗", "chuāng", "window", "床", "chuáng", "bed", "chuāng holds steady high (Tone 1); chuáng climbs upward (Tone 2)."),
        MinimalPairQuestion("mp11", "Tone 1 vs Tone 4", "八", "bā", "eight", "爸", "bà", "dad", "bā is sustained high level; bà drops fast and short."),
        MinimalPairQuestion("mp12", "n- vs l- Initial", "南", "nán", "south", "蓝", "lán", "blue", "nán lets air buzz through the nose; lán taps the tongue cleanly without nasal air.")
    )

    val clozeContextCards: List<ClozeContextCard> = listOf(
        ClozeContextCard(
            id = "clz1", hskLevel = 1,
            sentenceWithBlank = "我明天 ___ 北京。",
            fullSentence = "我明天去北京。",
            fullPinyin = "Wǒ míngtiān qù Běijīng.",
            englishTranslation = "I am going to Beijing tomorrow.",
            missingWord = "去", missingPinyin = "qù",
            options = listOf("去", "吃", "喝", "看"),
            grammarNote = "Chinese places Time (明天) right after the Subject (我), before the motion verb 去 (go)."
        ),
        ClozeContextCard(
            id = "clz2", hskLevel = 1,
            sentenceWithBlank = "我每天早上 ___ 一杯咖啡。",
            fullSentence = "我每天早上喝一杯咖啡。",
            fullPinyin = "Wǒ měitiān zǎoshang hē yì bēi kāfēi.",
            englishTranslation = "I drink a cup of coffee every morning.",
            missingWord = "喝", missingPinyin = "hē",
            options = listOf("喝", "吃", "买", "听"),
            grammarNote = "Use 喝 (hē) for all liquids (coffee, tea, water) paired with measure word 杯 (cup)."
        ),
        ClozeContextCard(
            id = "clz3", hskLevel = 1,
            sentenceWithBlank = "现在几点 ___？",
            fullSentence = "现在几点了？",
            fullPinyin = "Xiànzài jǐ diǎn le?",
            englishTranslation = "What time is it now?",
            missingWord = "了", missingPinyin = "le",
            options = listOf("了", "吗", "的", "在"),
            grammarNote = "Sentence-final 了 marks the current state or change of time: 'What time has it become now?'."
        ),
        ClozeContextCard(
            id = "clz4", hskLevel = 2,
            sentenceWithBlank = "我昨天 ___ 图书馆看书。",
            fullSentence = "我昨天在图书馆看书。",
            fullPinyin = "Wǒ zuótiān zài túshūguǎn kànshū.",
            englishTranslation = "Yesterday I read books at the library.",
            missingWord = "在", missingPinyin = "zài",
            options = listOf("在", "给", "和", "比"),
            grammarNote = "Location phrases (在 + Place) come BEFORE the main verb (看书) in Mandarin!"
        ),
        ClozeContextCard(
            id = "clz5", hskLevel = 2,
            sentenceWithBlank = "今天的天气 ___ 昨天更冷。",
            fullSentence = "今天的天气比昨天更冷。",
            fullPinyin = "Jīntiān de tiānqì bǐ zuótiān gèng lěng.",
            englishTranslation = "Today's weather is even colder than yesterday's.",
            missingWord = "比", missingPinyin = "bǐ",
            options = listOf("比", "跟", "把", "被"),
            grammarNote = "The comparison pattern is: A + 比 (bǐ) + B + Adjective."
        ),
        ClozeContextCard(
            id = "clz6", hskLevel = 2,
            sentenceWithBlank = "请用手机 ___ 付款。",
            fullSentence = "请用手机扫码付款。",
            fullPinyin = "Qǐng yòng shǒujī sǎomǎ fùkuǎn.",
            englishTranslation = "Please use your phone to scan the QR code and pay.",
            missingWord = "扫码", missingPinyin = "sǎomǎ",
            options = listOf("扫码", "吃饭", "休息", "跑步"),
            grammarNote = "扫码 (scan code) is an essential HSK 3.0 daily-life verb used everywhere in modern China."
        ),
        ClozeContextCard(
            id = "clz7", hskLevel = 3,
            sentenceWithBlank = "___ 下雨了，所以我们决定在家看电影。",
            fullSentence = "因为下雨了，所以我们决定在家看电影。",
            fullPinyin = "Yīnwèi xiàyǔ le, suǒyǐ wǒmen juédìng zài jiā kàn diànyǐng.",
            englishTranslation = "Because it rained, we decided to watch a movie at home.",
            missingWord = "因为", missingPinyin = "yīnwèi",
            options = listOf("因为", "虽然", "如果", "但是"),
            grammarNote = "因为...所以... (Because... therefore...) is a core HSK 3 paired conjunction."
        ),
        ClozeContextCard(
            id = "clz8", hskLevel = 3,
            sentenceWithBlank = "我 ___ 手机忘在出租车上了！",
            fullSentence = "我把手机忘在出租车上了！",
            fullPinyin = "Wǒ bǎ shǒujī wàng zài chūzūchē shàng le!",
            englishTranslation = "I accidentally left my phone in the taxi!",
            missingWord = "把", missingPinyin = "bǎ",
            options = listOf("把", "比", "向", "离"),
            grammarNote = "Use 把 (bǎ) to put a specific object (手机) before the verb when describing what happened to it."
        ),
        ClozeContextCard(
            id = "clz9", hskLevel = 3,
            sentenceWithBlank = "虽然汉语很难，___ 我非常喜欢学习。",
            fullSentence = "虽然汉语很难，但是我非常喜欢学习。",
            fullPinyin = "Suīrán Hànyǔ hěn nán, dànshì wǒ fēicháng xǐhuan xuéxí.",
            englishTranslation = "Although Chinese is hard, I really enjoy studying it.",
            missingWord = "但是", missingPinyin = "dànshì",
            options = listOf("但是", "所以", "或者", "而且"),
            grammarNote = "虽然 (although) always pairs with 但是 (but) in the second clause in Mandarin!"
        ),
        ClozeContextCard(
            id = "clz10", hskLevel = 4,
            sentenceWithBlank = "这个周末我 ___ 去爬山。",
            fullSentence = "这个周末我打算去爬山。",
            fullPinyin = "Zhège zhōumò wǒ dǎsuàn qù páshān.",
            englishTranslation = "This weekend I plan to go hiking.",
            missingWord = "打算", missingPinyin = "dǎsuàn",
            options = listOf("打算", "以为", "经过", "改变"),
            grammarNote = "打算 (dǎsuàn) expresses a concrete personal plan or intention before a verb."
        )
    )

    val shadowingScripts: List<ShadowingDialogueScript> = listOf(
        ShadowingDialogueScript(
            id = "sh1", title = "Ordering Coffee", hskLevel = 1, scenario = "Cafe",
            hanzi = "你好，我要一杯热咖啡，不要糖，谢谢。",
            pinyin = "Nǐ hǎo, wǒ yào yì bēi rè kāfēi, bú yào táng, xièxie.",
            english = "Hello, I'd like a cup of hot coffee, no sugar, thank you."
        ),
        ShadowingDialogueScript(
            id = "sh2", title = "Asking Directions", hskLevel = 2, scenario = "Subway",
            hanzi = "请问，地铁站在哪儿？离这里远吗？",
            pinyin = "Qǐngwèn, dìtiězhàn zài nǎr? Lí zhèlǐ yuǎn ma?",
            english = "Excuse me, where is the subway station? Is it far from here?"
        ),
        ShadowingDialogueScript(
            id = "sh3", title = "Weekend Plans", hskLevel = 2, scenario = "Friends",
            hanzi = "这个周末天气很好，我们一起去公园散步吧。",
            pinyin = "Zhège zhōumò tiānqì hěn hǎo, wǒmen yìqǐ qù gōngyuán sànbù ba.",
            english = "The weather is great this weekend, let's go for a walk in the park together."
        ),
        ShadowingDialogueScript(
            id = "sh4", title = "Paying by QR Code", hskLevel = 3, scenario = "Restaurant",
            hanzi = "服务员，买单！请问可以用手机扫码付款吗？",
            pinyin = "Fúwùyuán, mǎidān! Qǐngwèn kěyǐ yòng shǒujī sǎomǎ fùkuǎn ma?",
            english = "Waiter, check please! May I pay by scanning the QR code on my phone?"
        ),
        ShadowingDialogueScript(
            id = "sh5", title = "Learning Mandarin", hskLevel = 3, scenario = "Study",
            hanzi = "虽然汉字有点难，但是每天练习就会越来越容易。",
            pinyin = "Suīrán Hànzì yǒudiǎn nán, dànshì měitiān liànxí jiù huì yuèláiyuè róngyì.",
            english = "Although Chinese characters are a bit difficult, practicing every day makes them easier and easier."
        )
    )

    val grammarTransformStarterSentences: List<Pair<String, String>> = listOf(
        "我吃苹果" to "I eat apples",
        "我喝咖啡" to "I drink coffee",
        "他去北京" to "He goes to Beijing",
        "我看电影" to "I watch a movie",
        "我买手机" to "I buy a phone",
        "她学习汉语" to "She studies Chinese"
    )
}
