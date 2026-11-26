package com.example.data

import com.example.model.LabHskBand
import com.example.model.MeasureDrillQuestion
import com.example.model.MeasureExample
import com.example.model.MeasureWordEntry
import com.example.model.SynonymBattleItem
import com.example.model.SynonymQuizPrompt
import com.example.model.SynonymSide

object MeasureAndSynonymData {

    val measureWords: List<MeasureWordEntry> = listOf(
        // --- HSK 1–2 (12 core everyday classifiers) ---
        MeasureWordEntry(
            id = "ge",
            hanzi = "个",
            pinyin = "gè",
            hskLevel = 1,
            band = LabHskBand.HSK_1_2,
            visualCategory = "General · People & 3-D",
            shapeGlyph = "●",
            visualLogicRule = "Default classifier for people, round/3-D items, time units, and abstract concepts without a specialized classifier.",
            examples = listOf(
                MeasureExample("一", "苹果", "一个苹果", "yí gè píngguǒ", "an apple"),
                MeasureExample("两", "朋友", "两个朋友", "liǎng gè péngyou", "two friends"),
                MeasureExample("三", "星期", "三个星期", "sān gè xīngqī", "three weeks")
            )
        ),
        MeasureWordEntry(
            id = "zhang",
            hanzi = "张",
            pinyin = "zhāng",
            hskLevel = 1,
            band = LabHskBand.HSK_1_2,
            visualCategory = "Flat & Spread Surfaces",
            shapeGlyph = "▭",
            visualLogicRule = "Flat, wide, or stretched surfaces — paper, tickets, photos, tables, beds, and even mouths/faces.",
            examples = listOf(
                MeasureExample("一", "桌子", "一张桌子", "yì zhāng zhuōzi", "a table"),
                MeasureExample("两", "电影票", "两张电影票", "liǎng zhāng diànyǐngpiào", "two movie tickets"),
                MeasureExample("一", "照片", "一张照片", "yì zhāng zhàopiàn", "a photograph")
            )
        ),
        MeasureWordEntry(
            id = "tiao",
            hanzi = "条",
            pinyin = "tiáo",
            hskLevel = 2,
            band = LabHskBand.HSK_1_2,
            visualCategory = "Long, Winding & Flexible",
            shapeGlyph = "〰",
            visualLogicRule = "Long, narrow, flexible, or winding things — fish, rivers, roads, pants, skirts, dogs, and news items.",
            examples = listOf(
                MeasureExample("一", "鱼", "一条鱼", "yì tiáo yú", "a fish"),
                MeasureExample("一", "裤子", "一条裤子", "yì tiáo kùzi", "a pair of pants"),
                MeasureExample("一", "路", "一条路", "yì tiáo lù", "a road")
            )
        ),
        MeasureWordEntry(
            id = "ben",
            hanzi = "本",
            pinyin = "běn",
            hskLevel = 1,
            band = LabHskBand.HSK_1_2,
            visualCategory = "Bound Printed Matter",
            shapeGlyph = "▤",
            visualLogicRule = "Anything bound along a spine with pages — books, notebooks, dictionaries, magazines, and passports.",
            examples = listOf(
                MeasureExample("一", "书", "一本书", "yì běn shū", "a book"),
                MeasureExample("两", "词典", "两本词典", "liǎng běn cídiǎn", "two dictionaries"),
                MeasureExample("一", "笔记本", "一本笔记本", "yì běn bǐjìběn", "a notebook")
            )
        ),
        MeasureWordEntry(
            id = "zhi_animal",
            hanzi = "只",
            pinyin = "zhī",
            hskLevel = 2,
            band = LabHskBand.HSK_1_2,
            visualCategory = "Small Animals & Singles",
            shapeGlyph = "🐾",
            visualLogicRule = "Small/medium animals (cats, birds, chickens) and ONE item from a natural pair (one hand, one shoe, one ear).",
            examples = listOf(
                MeasureExample("一", "猫", "一只猫", "yì zhī māo", "a cat"),
                MeasureExample("两", "小鸟", "两只小鸟", "liǎng zhī xiǎoniǎo", "two little birds"),
                MeasureExample("一", "鞋", "一只鞋", "yì zhī xié", "one single shoe")
            )
        ),
        MeasureWordEntry(
            id = "bei",
            hanzi = "杯",
            pinyin = "bēi",
            hskLevel = 1,
            band = LabHskBand.HSK_1_2,
            visualCategory = "Cups & Glasses",
            shapeGlyph = "☕",
            visualLogicRule = "Container classifier for drinks served in cups, mugs, or glasses.",
            examples = listOf(
                MeasureExample("一", "茶", "一杯茶", "yì bēi chá", "a cup of tea"),
                MeasureExample("两", "咖啡", "两杯咖啡", "liǎng bēi kāfēi", "two cups of coffee"),
                MeasureExample("一", "水", "一杯水", "yì bēi shuǐ", "a glass of water")
            )
        ),
        MeasureWordEntry(
            id = "jian",
            hanzi = "件",
            pinyin = "jiàn",
            hskLevel = 2,
            band = LabHskBand.HSK_1_2,
            visualCategory = "Upper-Body Clothing & Matters",
            shapeGlyph = "👕",
            visualLogicRule = "Upper-body garments (shirts, coats, sweaters), luggage pieces, gifts, and matters/events (事情).",
            examples = listOf(
                MeasureExample("一", "衣服", "一件衣服", "yí jiàn yīfu", "a piece of clothing"),
                MeasureExample("一", "事情", "一件事情", "yí jiàn shìqing", "one matter / affair"),
                MeasureExample("两", "行李", "两件行李", "liǎng jiàn xíngli", "two pieces of luggage")
            )
        ),
        MeasureWordEntry(
            id = "liang",
            hanzi = "辆",
            pinyin = "liàng",
            hskLevel = 2,
            band = LabHskBand.HSK_1_2,
            visualCategory = "Wheeled Land Vehicles",
            shapeGlyph = "🚗",
            visualLogicRule = "Land vehicles with wheels — cars, bicycles, buses, taxis (note: NOT trains or airplanes!).",
            examples = listOf(
                MeasureExample("一", "汽车", "一辆汽车", "yí liàng qìchē", "a car"),
                MeasureExample("两", "自行车", "两辆自行车", "liǎng liàng zìxíngchē", "two bicycles"),
                MeasureExample("一", "出租车", "一辆出租车", "yí liàng chūzūchē", "a taxi")
            )
        ),
        MeasureWordEntry(
            id = "wei",
            hanzi = "位",
            pinyin = "wèi",
            hskLevel = 2,
            band = LabHskBand.HSK_1_2,
            visualCategory = "Polite Classifier for People",
            shapeGlyph = "🙇",
            visualLogicRule = "Respectful, polite way to count people — teachers, guests, customers, elders, and doctors.",
            examples = listOf(
                MeasureExample("一", "老师", "一位老师", "yí wèi lǎoshī", "a teacher (polite)"),
                MeasureExample("两", "客人", "两位客人", "liǎng wèi kèrén", "two guests"),
                MeasureExample("三", "医生", "三位医生", "sān wèi yīshēng", "three doctors")
            )
        ),
        MeasureWordEntry(
            id = "kuai",
            hanzi = "块",
            pinyin = "kuài",
            hskLevel = 1,
            band = LabHskBand.HSK_1_2,
            visualCategory = "Chunks, Pieces & Currency",
            shapeGlyph = "▣",
            visualLogicRule = "Lumps, slices, chunks (cake, soap, watermelon), watches, and spoken Chinese yuan (人民币).",
            examples = listOf(
                MeasureExample("一", "蛋糕", "一块蛋糕", "yí kuài dàngāo", "a piece of cake"),
                MeasureExample("五", "钱", "五块钱", "wǔ kuài qián", "five yuan"),
                MeasureExample("一", "手表", "一块手表", "yí kuài shǒubiǎo", "a wristwatch")
            )
        ),
        MeasureWordEntry(
            id = "shuang",
            hanzi = "双",
            pinyin = "shuāng",
            hskLevel = 3,
            band = LabHskBand.HSK_3_4,
            visualCategory = "Matching Pairs",
            shapeGlyph = "👯",
            visualLogicRule = "Items that naturally come in symmetrical pairs — shoes, socks, chopsticks, gloves, hands, and eyes.",
            examples = listOf(
                MeasureExample("一", "鞋", "一双鞋", "yì shuāng xié", "a pair of shoes"),
                MeasureExample("一", "筷子", "一双筷子", "yì shuāng kuàizi", "a pair of chopsticks"),
                MeasureExample("两", "袜子", "两双袜子", "liǎng shuāng wàzi", "two pairs of socks")
            )
        ),
        MeasureWordEntry(
            id = "ping",
            hanzi = "瓶",
            pinyin = "píng",
            hskLevel = 2,
            band = LabHskBand.HSK_1_2,
            visualCategory = "Bottles & Flasks",
            shapeGlyph = "🍾",
            visualLogicRule = "Liquids or goods packaged inside bottles — water, milk, juice, beer, and perfume.",
            examples = listOf(
                MeasureExample("一", "矿泉水", "一瓶矿泉水", "yì píng kuàngquánshuǐ", "a bottle of mineral water"),
                MeasureExample("两", "牛奶", "两瓶牛奶", "liǎng píng niúnǎi", "two bottles of milk"),
                MeasureExample("一", "果汁", "一瓶果汁", "yì píng guǒzhī", "a bottle of fruit juice")
            )
        ),

        // --- HSK 3–4 (8 essential intermediate classifiers) ---
        MeasureWordEntry(
            id = "ba",
            hanzi = "把",
            pinyin = "bǎ",
            hskLevel = 3,
            band = LabHskBand.HSK_3_4,
            visualCategory = "Objects with Handles / Grips",
            shapeGlyph = "☂",
            visualLogicRule = "Things you grab by a handle, back, or handful — umbrellas, chairs, knives, keys, scissors, and guitars.",
            examples = listOf(
                MeasureExample("一", "雨伞", "一把雨伞", "yì bǎ yǔsǎn", "an umbrella"),
                MeasureExample("两", "椅子", "两把椅子", "liǎng bǎ yǐzi", "two chairs"),
                MeasureExample("一", "钥匙", "一把钥匙", "yì bǎ yàoshi", "a key")
            )
        ),
        MeasureWordEntry(
            id = "wan",
            hanzi = "碗",
            pinyin = "wǎn",
            hskLevel = 3,
            band = LabHskBand.HSK_3_4,
            visualCategory = "Bowls of Food",
            shapeGlyph = "🍜",
            visualLogicRule = "Container classifier for rice, noodles, soup, and porridge served in a bowl.",
            examples = listOf(
                MeasureExample("一", "米饭", "一碗米饭", "yì wǎn mǐfàn", "a bowl of rice"),
                MeasureExample("两", "面条", "两碗面条", "liǎng wǎn miàntiáo", "two bowls of noodles"),
                MeasureExample("一", "汤", "一碗汤", "yì wǎn tāng", "a bowl of soup")
            )
        ),
        MeasureWordEntry(
            id = "ke_plant",
            hanzi = "棵",
            pinyin = "kē",
            hskLevel = 3,
            band = LabHskBand.HSK_3_4,
            visualCategory = "Trees & Rooted Plants",
            shapeGlyph = "🌳",
            visualLogicRule = "Notice the 木 (wood) radical! Used for trees, bushes, grass, and rooted leafy vegetables like cabbage.",
            examples = listOf(
                MeasureExample("一", "树", "一棵树", "yì kē shù", "a tree"),
                MeasureExample("两", "小草", "两棵小草", "liǎng kē xiǎocǎo", "two blades/tufts of grass"),
                MeasureExample("一", "白菜", "一棵白菜", "yì kē báicài", "a head of napa cabbage")
            )
        ),
        MeasureWordEntry(
            id = "feng",
            hanzi = "封",
            pinyin = "fēng",
            hskLevel = 4,
            band = LabHskBand.HSK_3_4,
            visualCategory = "Sealed Mail & Emails",
            shapeGlyph = "✉",
            visualLogicRule = "Originally 'to seal' — used for letters, envelopes, emails (电子邮件), and formal invitations.",
            examples = listOf(
                MeasureExample("一", "信", "一封信", "yì fēng xìn", "a letter"),
                MeasureExample("两", "邮件", "两封邮件", "liǎng fēng yóujiàn", "two emails"),
                MeasureExample("一", "感谢信", "一封感谢信", "yì fēng gǎnxièxìn", "a thank-you letter")
            )
        ),
        MeasureWordEntry(
            id = "tai",
            hanzi = "台",
            pinyin = "tái",
            hskLevel = 4,
            band = LabHskBand.HSK_3_4,
            visualCategory = "Machines & Appliances",
            shapeGlyph = "💻",
            visualLogicRule = "Electronic devices, appliances, and heavy machinery — computers, TVs, air conditioners, washing machines.",
            examples = listOf(
                MeasureExample("一", "电脑", "一台电脑", "yì tái diànnǎo", "a computer"),
                MeasureExample("一", "电视", "一台电视", "yì tái diànshì", "a television"),
                MeasureExample("两", "空调", "两台空调", "liǎng tái kōngtiáo", "two air conditioners")
            )
        ),
        MeasureWordEntry(
            id = "shou",
            hanzi = "首",
            pinyin = "shǒu",
            hskLevel = 4,
            band = LabHskBand.HSK_3_4,
            visualCategory = "Songs & Poems",
            shapeGlyph = "🎵",
            visualLogicRule = "Musical compositions, songs, and poetry — literally 'head/stanza'.",
            examples = listOf(
                MeasureExample("一", "歌", "一首歌", "yì shǒu gē", "a song"),
                MeasureExample("两", "古诗", "两首古诗", "liǎng shǒu gǔshī", "two classical poems"),
                MeasureExample("一", "音乐", "一首音乐", "yì shǒu yīnyuè", "a piece of music")
            )
        ),
        MeasureWordEntry(
            id = "fen",
            hanzi = "份",
            pinyin = "fèn",
            hskLevel = 4,
            band = LabHskBand.HSK_3_4,
            visualCategory = "Portions, Jobs & Sets",
            shapeGlyph = "📋",
            visualLogicRule = "A share, serving, or complete copy —一份工作 (a job), 一份礼物 (a gift), 一份报纸 (a newspaper), 一份炒饭 (an order of fried rice).",
            examples = listOf(
                MeasureExample("一", "工作", "一份工作", "yí fèn gōngzuò", "a job"),
                MeasureExample("一", "报纸", "一份报纸", "yí fèn bàozhǐ", "a newspaper"),
                MeasureExample("一", "礼物", "一份礼物", "yí fèn lǐwù", "a gift")
            )
        ),
        MeasureWordEntry(
            id = "pian",
            hanzi = "篇",
            pinyin = "piān",
            hskLevel = 4,
            band = LabHskBand.HSK_3_4,
            visualCategory = "Articles, Essays & Papers",
            shapeGlyph = "📝",
            visualLogicRule = "Written compositions with multiple paragraphs — essays, articles, blog posts, diaries, and research papers.",
            examples = listOf(
                MeasureExample("一", "文章", "一篇文章", "yì piān wénzhāng", "an article / essay"),
                MeasureExample("一", "日记", "一篇日记", "yì piān rìjì", "a diary entry"),
                MeasureExample("两", "论文", "两篇论文", "liǎng piān lùnwén", "two academic papers")
            )
        ),

        // --- HSK 5–6 (4 high-value upper-intermediate/advanced classifiers) ---
        MeasureWordEntry(
            id = "zuo",
            hanzi = "座",
            pinyin = "zuò",
            hskLevel = 5,
            band = LabHskBand.HSK_5_6,
            visualCategory = "Large Immovable Structures",
            shapeGlyph = "⛰",
            visualLogicRule = "Massive, stationary landmarks that 'sit' firmly on the earth — mountains, bridges, buildings, cities, and statues.",
            examples = listOf(
                MeasureExample("一", "山", "一座山", "yí zuò shān", "a mountain"),
                MeasureExample("一", "桥", "一座桥", "yí zuò qiáo", "a bridge"),
                MeasureExample("一", "城市", "一座城市", "yí zuò chéngshì", "a city")
            )
        ),
        MeasureWordEntry(
            id = "chang",
            hanzi = "场",
            pinyin = "chǎng",
            hskLevel = 5,
            band = LabHskBand.HSK_5_6,
            visualCategory = "Events, Matches & Showers",
            shapeGlyph = "🏟",
            visualLogicRule = "Events that unfold over a duration from start to finish — sports games, concerts, movies, exams, and rain/snow storms.",
            examples = listOf(
                MeasureExample("一", "比赛", "一场比赛", "yì chǎng bǐsài", "a match / competition"),
                MeasureExample("一", "大雨", "一场大雨", "yì chǎng dàyǔ", "a heavy rainstorm"),
                MeasureExample("一", "音乐会", "一场音乐会", "yì chǎng yīnyuèhuì", "a concert")
            )
        ),
        MeasureWordEntry(
            id = "jia",
            hanzi = "架",
            pinyin = "jià",
            hskLevel = 5,
            band = LabHskBand.HSK_5_6,
            visualCategory = "Framed Craft & Instruments",
            shapeGlyph = "✈",
            visualLogicRule = "Machines or instruments supported by a frame or wings — airplanes, pianos, cameras, and drones.",
            examples = listOf(
                MeasureExample("一", "飞机", "一架飞机", "yí jià fēijī", "an airplane"),
                MeasureExample("一", "钢琴", "一架钢琴", "yí jià gāngqín", "a piano"),
                MeasureExample("一", "照相机", "一架照相机", "yí jià zhàoxiàngjī", "a camera")
            )
        ),
        MeasureWordEntry(
            id = "dao",
            hanzi = "道",
            pinyin = "dào",
            hskLevel = 5,
            band = LabHskBand.HSK_5_6,
            visualCategory = "Courses, Questions &一道光",
            shapeGlyph = "🍽",
            visualLogicRule = "Prepared dishes/courses, exam questions, doorways, walls, and streaks of light/rivers.",
            examples = listOf(
                MeasureExample("一", "菜", "一道菜", "yí dào cài", "a cooked dish"),
                MeasureExample("两", "题", "两道题", "liǎng dào tí", "two exam problems"),
                MeasureExample("一", "门", "一道门", "yí dào mén", "a doorway / gate")
            )
        )
    )

    fun measureWordsForBand(band: LabHskBand): List<MeasureWordEntry> =
        measureWords.filter { band.matchesLevel(it.hskLevel) }

    fun buildSpeedDrillRound(band: LabHskBand, count: Int = 10): List<MeasureDrillQuestion> {
        val pool = measureWordsForBand(band).ifEmpty { measureWords }
        val distractorSource = if (pool.size >= 4) pool else measureWords
        val candidates = pool.flatMap { entry ->
            entry.examples.map { ex -> entry to ex }
        }.shuffled()

        val selected = mutableListOf<Pair<MeasureWordEntry, MeasureExample>>()
        val usedIds = mutableSetOf<String>()
        // Prioritize distinct measure words first for variety
        for (pair in candidates) {
            if (selected.size >= count) break
            if (usedIds.add(pair.first.id)) {
                selected.add(pair)
            }
        }
        for (pair in candidates) {
            if (selected.size >= count) break
            if (pair !in selected) {
                selected.add(pair)
            }
        }

        return selected.mapIndexed { idx, (entry, ex) ->
            val distractors = distractorSource
                .filter { it.id != entry.id }
                .shuffled()
                .take(3)
            val options = (distractors + entry).shuffled()
            MeasureDrillQuestion(
                id = "mw_q_${idx}_${entry.id}_${ex.nounHanzi}",
                promptBlankHanzi = "${ex.numberPrefix} [ ___ ] ${ex.nounHanzi}",
                nounOnlyHanzi = ex.nounHanzi,
                fullHanzi = ex.fullHanzi,
                fullPinyin = ex.fullPinyin,
                english = ex.english,
                correctEntry = entry,
                optionEntries = options,
                whyItFits = "${entry.hanzi} (${entry.pinyin}) fits '${ex.nounHanzi}' because it classifies ${entry.visualCategory.lowercase()}: ${entry.visualLogicRule}"
            )
        }
    }

    // --- 16 CLASSIC HSK NEAR-SYNONYM BATTLES ---

    val synonymBattles: List<SynonymBattleItem> = listOf(
        // ==================== HSK 1–2 (6 Battles) ====================
        SynonymBattleItem(
            id = "er_vs_liang",
            title = "二 vs 两",
            hskLevel = 1,
            band = LabHskBand.HSK_1_2,
            sides = listOf(
                SynonymSide(
                    hanzi = "二",
                    pinyin = "èr",
                    coreMeaning = "Number 2 (counting & ordinals)",
                    usageRule = "Use in pure numbers (12, 20), dates (二月), ordinals (第二), and phone/room numbers.",
                    exampleHanzi = "今天是二月十二号。",
                    examplePinyin = "Jīntiān shì èr yuè shí èr hào.",
                    exampleEnglish = "Today is February 12th."
                ),
                SynonymSide(
                    hanzi = "两",
                    pinyin = "liǎng",
                    coreMeaning = "Two of something (quantity)",
                    usageRule = "Use directly before a measure word (两个人, 两本书) and for 2 o'clock (两点).",
                    exampleHanzi = "我买了两杯咖啡。",
                    examplePinyin = "Wǒ mǎi le liǎng bēi kāfēi.",
                    exampleEnglish = "I bought two cups of coffee."
                )
            ),
            coreDifference = "Use 二 for digits, math, dates, and order (第二). Use 两 right before a measure word to count two items (两个).",
            commonMistakeWrong = "我有二个哥哥。",
            commonMistakeRight = "我有两个哥哥。",
            commonMistakeNote = "Before the measure word 个, you must use 两 — never 二.",
            offlineExtraExamples = "1) 现在是两点十分。(Xiànzài shì liǎng diǎn shí fēn.) — It's 2:10 now. (Use 两 for 2 o'clock)\n2) 这是我第二次来北京。(Zhè shì wǒ dì èr cì lái Běijīng.) — This is my second time in Beijing. (Use 二 after 第)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_er_1",
                    battleId = "er_vs_liang",
                    battleTitle = "二 vs 两",
                    hskLevel = 1,
                    band = LabHskBand.HSK_1_2,
                    sentenceWithBlank = "桌子上放着 ___ 本中文书。",
                    fullHanzi = "桌子上放着两本中文书。",
                    fullPinyin = "Zhuōzi shàng fàngzhe liǎng běn Zhōngwén shū.",
                    english = "There are two Chinese books on the table.",
                    options = listOf("二", "两"),
                    correctOption = "两",
                    grammarReason = "Directly before the measure word 本, Mandarin requires 两 instead of 二.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_er_2",
                    battleId = "er_vs_liang",
                    battleTitle = "二 vs 两",
                    hskLevel = 1,
                    band = LabHskBand.HSK_1_2,
                    sentenceWithBlank = "他是我们班第 ___ 名。",
                    fullHanzi = "他是我们班第二名。",
                    fullPinyin = "Tā shì wǒmen bān dì èr míng.",
                    english = "He is second place in our class.",
                    options = listOf("二", "两"),
                    correctOption = "二",
                    grammarReason = "Ordinal numbers after 第 always use 二 (第二), never 两.",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "hui_neng_keyi",
            title = "会 vs 能 vs 可以",
            hskLevel = 2,
            band = LabHskBand.HSK_1_2,
            sides = listOf(
                SynonymSide(
                    hanzi = "会",
                    pinyin = "huì",
                    coreMeaning = "Know how to (learned skill)",
                    usageRule = "Skills acquired through learning/practice (languages, swimming, cooking) or future likelihood.",
                    exampleHanzi = "我会说一点儿汉语。",
                    examplePinyin = "Wǒ huì shuō yìdiǎnr Hànyǔ.",
                    exampleEnglish = "I know how to speak a little Mandarin."
                ),
                SynonymSide(
                    hanzi = "能",
                    pinyin = "néng",
                    coreMeaning = "Be physically/circumstantially able",
                    usageRule = "Physical capability, capacity (run fast, drink 3 cups), or whether circumstances allow it.",
                    exampleHanzi = "我今天生病了，不能去上班。",
                    examplePinyin = "Wǒ jīntiān shēngbìng le, bù néng qù shàngbān.",
                    exampleEnglish = "I'm sick today, so I can't go to work."
                ),
                SynonymSide(
                    hanzi = "可以",
                    pinyin = "kěyǐ",
                    coreMeaning = "May / be permitted",
                    usageRule = "Asking or granting permission ('May I...?') or suggesting a reasonable option.",
                    exampleHanzi = "请问，我可以坐这里吗？",
                    examplePinyin = "Qǐngwèn, wǒ kěyǐ zuò zhèlǐ ma?",
                    exampleEnglish = "Excuse me, may I sit here?"
                )
            ),
            coreDifference = "会 = learned skill ('know how'); 能 = physical ability or situation allows; 可以 = permission ('may I').",
            commonMistakeWrong = "请问，我会进来吗？",
            commonMistakeRight = "请问，我可以进来吗？",
            commonMistakeNote = "Walking through a door isn't a learned skill — use 可以 to politely ask permission.",
            offlineExtraExamples = "1) 他一分钟能写五十个汉字。(Tā yì fēnzhōng néng xiě wǔshí gè Hànzì.) — He can write 50 characters per minute. (Physical capacity → 能)\n2) 妈妈会做北京烤鸭。(Māma huì zuò Běijīng kǎoyā.) — Mom knows how to make Peking duck. (Learned skill → 会)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_hui_1",
                    battleId = "hui_neng_keyi",
                    battleTitle = "会 vs 能 vs 可以",
                    hskLevel = 2,
                    band = LabHskBand.HSK_1_2,
                    sentenceWithBlank = "妈妈教了我以后，我现在 ___ 包饺子了。",
                    fullHanzi = "妈妈教了我以后，我现在会包饺子了。",
                    fullPinyin = "Māma jiāo le wǒ yǐhòu, wǒ xiànzài huì bāo jiǎozi le.",
                    english = "After Mom taught me, I now know how to wrap dumplings.",
                    options = listOf("会", "能", "可以"),
                    correctOption = "会",
                    grammarReason = "Wrapping dumplings is a learned skill acquired through teaching, which takes 会.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_hui_2",
                    battleId = "hui_neng_keyi",
                    battleTitle = "会 vs 能 vs 可以",
                    hskLevel = 2,
                    band = LabHskBand.HSK_1_2,
                    sentenceWithBlank = "老师，我 ___ 用手机查一下词典吗？",
                    fullHanzi = "老师，我可以用手机查一下词典吗？",
                    fullPinyin = "Lǎoshī, wǒ kěyǐ yòng shǒujī chá yíxià cídiǎn ma?",
                    english = "Teacher, may I use my phone to check the dictionary?",
                    options = listOf("会", "能", "可以"),
                    correctOption = "可以",
                    grammarReason = "Politely asking a teacher for permission uses 可以 ('may I').",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "yidianr_vs_youdianr",
            title = "一点儿 vs 有点儿",
            hskLevel = 2,
            band = LabHskBand.HSK_1_2,
            sides = listOf(
                SynonymSide(
                    hanzi = "一点儿",
                    pinyin = "yìdiǎnr",
                    coreMeaning = "A little bit (after verb/adj, before noun)",
                    usageRule = "Goes AFTER an adjective (贵一点儿 = a bit more expensive) or BEFORE a noun (一点儿水 = some water).",
                    exampleHanzi = "请给我一点儿热水。",
                    examplePinyin = "Qǐng gěi wǒ yìdiǎnr rèshuǐ.",
                    exampleEnglish = "Please give me a little hot water."
                ),
                SynonymSide(
                    hanzi = "有点儿",
                    pinyin = "yǒudiǎnr",
                    coreMeaning = "Somewhat / a little too (before adj)",
                    usageRule = "Goes BEFORE an adjective, almost always expressing a mild complaint or unwanted feeling.",
                    exampleHanzi = "这件衣服有点儿贵。",
                    examplePinyin = "Zhè jiàn yīfu yǒudiǎnr guì.",
                    exampleEnglish = "This piece of clothing is a bit too expensive."
                )
            ),
            coreDifference = "Word order & tone! 有点儿 + Adj (complaining: 'a bit too...'). Adj + 一点儿 (comparing/requesting: 'a bit more...') or 一点儿 + Noun.",
            commonMistakeWrong = "今天天气一点儿冷。",
            commonMistakeRight = "今天天气有点儿冷。",
            commonMistakeNote = "Before an adjective like 冷 (cold), you must use 有点儿 — 一点儿 can only follow an adjective!",
            offlineExtraExamples = "1) 你能说慢一点儿吗？(Nǐ néng shuō màn yìdiǎnr ma?) — Could you speak a little slower? (Adj + 一点儿)\n2) 我今天有点儿累。(Wǒ jīntiān yǒudiǎnr lèi.) — I'm a little tired today. (有点儿 + Adj)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_yidian_1",
                    battleId = "yidianr_vs_youdianr",
                    battleTitle = "一点儿 vs 有点儿",
                    hskLevel = 2,
                    band = LabHskBand.HSK_1_2,
                    sentenceWithBlank = "这双鞋 ___ 小，有大一号的吗？",
                    fullHanzi = "这双鞋有点儿小，有大一号的吗？",
                    fullPinyin = "Zhè shuāng xié yǒudiǎnr xiǎo, yǒu dà yī hào de ma?",
                    english = "These shoes are a bit too small; do you have one size larger?",
                    options = listOf("一点儿", "有点儿"),
                    correctOption = "有点儿",
                    grammarReason = "有点儿 goes before an adjective (小) to express a mild dissatisfaction ('a bit too small').",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_yidian_2",
                    battleId = "yidianr_vs_youdianr",
                    battleTitle = "一点儿 vs 有点儿",
                    hskLevel = 2,
                    band = LabHskBand.HSK_1_2,
                    sentenceWithBlank = "请把声音开大 ___ ，我听不清楚。",
                    fullHanzi = "请把声音开大一点儿，我听不清楚。",
                    fullPinyin = "Qǐng bǎ shēngyīn kāi dà yìdiǎnr, wǒ tīng bu qīngchu.",
                    english = "Please turn the volume up a little; I can't hear clearly.",
                    options = listOf("一点儿", "有点儿"),
                    correctOption = "一点儿",
                    grammarReason = "After an adjective (大) when requesting a small adjustment, use 一点儿.",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "haishi_vs_huozhe",
            title = "还是 vs 或者",
            hskLevel = 2,
            band = LabHskBand.HSK_1_2,
            sides = listOf(
                SynonymSide(
                    hanzi = "还是",
                    pinyin = "háishi",
                    coreMeaning = "Or (in questions)",
                    usageRule = "Used in choice questions ('A or B?') and indirect questions ('I don't know whether A or B').",
                    exampleHanzi = "你喝茶还是喝咖啡？",
                    examplePinyin = "Nǐ hē chá háishi hē kāfēi?",
                    exampleEnglish = "Do you drink tea or coffee?"
                ),
                SynonymSide(
                    hanzi = "或者",
                    pinyin = "huòzhě",
                    coreMeaning = "Or (in statements)",
                    usageRule = "Used in declarative statements offering options or possibilities ('either A or B is fine').",
                    exampleHanzi = "周末我常看书或者爬山。",
                    examplePinyin = "Zhōumò wǒ cháng kànshū huòzhě páshān.",
                    exampleEnglish = "On weekends I often read or go hiking."
                )
            ),
            coreDifference = "Asking a question → 还是 (?). Making a statement → 或者 (。).",
            commonMistakeWrong = "你想吃米饭或者面条？",
            commonMistakeRight = "你想吃米饭还是面条？",
            commonMistakeNote = "Never use 或者 in a direct 'A or B?' question — swap it to 还是!",
            offlineExtraExamples = "1) 明天坐地铁或者打车去都可以。(Míngtiān zuò dìtiě huòzhě dǎchē qù dōu kěyǐ.) — Taking the subway or a taxi tomorrow are both fine. (Statement → 或者)\n2) 我不知道他去还是不去。(Wǒ bù zhīdào tā qù háishi bú qù.) — I don't know whether he's going or not. (Embedded question → 还是)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_haishi_1",
                    battleId = "haishi_vs_huozhe",
                    battleTitle = "还是 vs 或者",
                    hskLevel = 2,
                    band = LabHskBand.HSK_1_2,
                    sentenceWithBlank = "我们星期六 ___ 星期天去公园？",
                    fullHanzi = "我们星期六还是星期天去公园？",
                    fullPinyin = "Wǒmen xīngqīliù háishi xīngqītiān qù gōngyuán?",
                    english = "Are we going to the park on Saturday or Sunday?",
                    options = listOf("还是", "或者"),
                    correctOption = "还是",
                    grammarReason = "Direct choice questions asking the listener to pick A or B always use 还是.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_haishi_2",
                    battleId = "haishi_vs_huozhe",
                    battleTitle = "还是 vs 或者",
                    hskLevel = 2,
                    band = LabHskBand.HSK_1_2,
                    sentenceWithBlank = "午饭吃饺子 ___ 包子我都喜欢。",
                    fullHanzi = "午饭吃饺子或者包子我都喜欢。",
                    fullPinyin = "Wǔfàn chī jiǎozi huòzhě bāozi wǒ dōu xǐhuan.",
                    english = "I like eating either dumplings or steamed buns for lunch.",
                    options = listOf("还是", "或者"),
                    correctOption = "或者",
                    grammarReason = "Declarative statements expressing 'either A or B' use 或者.",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "bu_vs_mei",
            title = "不 vs 没",
            hskLevel = 1,
            band = LabHskBand.HSK_1_2,
            sides = listOf(
                SynonymSide(
                    hanzi = "不",
                    pinyin = "bù",
                    coreMeaning = "Not (present, future, habits, will)",
                    usageRule = "Negates present/future actions, habits, adjectives (不冷), 是 (不是), and willingness (不去).",
                    exampleHanzi = "我平时不喝冰水。",
                    examplePinyin = "Wǒ píngshí bù hē bīngshuǐ.",
                    exampleEnglish = "I normally don't drink ice water."
                ),
                SynonymSide(
                    hanzi = "没",
                    pinyin = "méi",
                    coreMeaning = "Did not / have not (past & 有)",
                    usageRule = "Negates past completed actions ('didn't') and ALWAYS negates 有 (没有 — never 不有!).",
                    exampleHanzi = "昨天晚上我没看电视。",
                    examplePinyin = "Zuótiān wǎnshang wǒ méi kàn diànshì.",
                    exampleEnglish = "I didn't watch TV last night."
                )
            ),
            coreDifference = "不 = habits, future, adjectives, and 是. 没 = past events ('didn't') and ALWAYS pairs with 有 (没有).",
            commonMistakeWrong = "昨天我不去学校。",
            commonMistakeRight = "昨天我没去学校。",
            commonMistakeNote = "For an objective fact about the past ('didn't go yesterday'), use 没 instead of 不.",
            offlineExtraExamples = "1) 我今天没有时间。(Wǒ jīntiān méiyǒu shíjiān.) — I don't have time today. (有 always takes 没)\n2) 明天我不去健身房。(Míngtiān wǒ bú qù jiànshēnfáng.) — I won't go to the gym tomorrow. (Future intention → 不)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_bumei_1",
                    battleId = "bu_vs_mei",
                    battleTitle = "不 vs 没",
                    hskLevel = 1,
                    band = LabHskBand.HSK_1_2,
                    sentenceWithBlank = "早上太忙了，我 ___ 吃早饭。",
                    fullHanzi = "早上太忙了，我没吃早饭。",
                    fullPinyin = "Zǎoshang tài máng le, wǒ méi chī zǎofàn.",
                    english = "This morning was too busy, so I didn't eat breakfast.",
                    options = listOf("不", "没"),
                    correctOption = "没",
                    grammarReason = "Negating a past event that did not happen ('didn't eat') requires 没.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_bumei_2",
                    battleId = "bu_vs_mei",
                    battleTitle = "不 vs 没",
                    hskLevel = 1,
                    band = LabHskBand.HSK_1_2,
                    sentenceWithBlank = "这个菜太辣了，我 ___ 喜欢吃。",
                    fullHanzi = "这个菜太辣了，我不喜欢吃。",
                    fullPinyin = "Zhège cài tài là le, wǒ bù xǐhuan chī.",
                    english = "This dish is too spicy; I don't like eating it.",
                    options = listOf("不", "没"),
                    correctOption = "不",
                    grammarReason = "Negating present feelings, preferences, and adjectives like 喜欢 uses 不.",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "xiang_vs_yao",
            title = "想 vs 要",
            hskLevel = 1,
            band = LabHskBand.HSK_1_2,
            sides = listOf(
                SynonymSide(
                    hanzi = "想",
                    pinyin = "xiǎng",
                    coreMeaning = "Would like to / feel like (softer)",
                    usageRule = "Polite desire, wish, or plan in your mind — softer than 要. Also means 'to miss' or 'to think'.",
                    exampleHanzi = "我想学一点儿书法。",
                    examplePinyin = "Wǒ xiǎng xué yìdiǎnr shūfǎ.",
                    exampleEnglish = "I would like to learn a little calligraphy."
                ),
                SynonymSide(
                    hanzi = "要",
                    pinyin = "yào",
                    coreMeaning = "Want / be going to / need (stronger)",
                    usageRule = "Stronger determination, ordering food ('我要一杯茶'), necessity, or imminent future ('要下雨了').",
                    exampleHanzi = "服务员，我要一碗牛肉面。",
                    examplePinyin = "Fúwùyuán, wǒ yào yì wǎn niúròumiàn.",
                    exampleEnglish = "Waiter, I'll have a bowl of beef noodles."
                )
            ),
            coreDifference = "想 = polite wish ('would like to' + Verb). 要 = decisive order/plan ('I want / am going to') and can take a Noun directly (我要茶).",
            commonMistakeWrong = "我想一杯咖啡。",
            commonMistakeRight = "我要一杯咖啡。/ 我想喝一杯咖啡。",
            commonMistakeNote = "想 cannot go directly before a noun like 咖啡 without a verb (喝)! Use 要 + Noun when ordering.",
            offlineExtraExamples = "1) 天黑了，要下雨了。(Tiān hēi le, yào xiàyǔ le.) — The sky is dark; it's about to rain. (Imminent future → 要)\n2) 假期我想去成都看熊猫。(Jiàqī wǒ xiǎng qù Chéngdū kàn xióngmāo.) — I'd like to visit Chengdu to see pandas over break. (Wish/plan → 想)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_xiangyao_1",
                    battleId = "xiang_vs_yao",
                    battleTitle = "想 vs 要",
                    hskLevel = 1,
                    band = LabHskBand.HSK_1_2,
                    sentenceWithBlank = "我 ___ 喝咖啡，你呢？",
                    fullHanzi = "我想喝咖啡，你呢？",
                    fullPinyin = "Wǒ xiǎng hē kāfēi, nǐ ne?",
                    english = "I feel like drinking coffee — how about you?",
                    options = listOf("想", "要"),
                    correctOption = "想",
                    grammarReason = "Expressing a casual, polite desire ('feel like / would like to drink') before a verb uses 想.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_xiangyao_2",
                    battleId = "xiang_vs_yao",
                    battleTitle = "想 vs 要",
                    hskLevel = 1,
                    band = LabHskBand.HSK_1_2,
                    sentenceWithBlank = "老板，我 ___ 两斤苹果。",
                    fullHanzi = "老板，我要两斤苹果。",
                    fullPinyin = "Lǎobǎn, wǒ yào liǎng jīn píngguǒ.",
                    english = "Boss, I'll take two jin of apples.",
                    options = listOf("想", "要"),
                    correctOption = "要",
                    grammarReason = "Directly before a noun phrase (两斤苹果) with no second verb, you must use 要.",
                    targetSides = emptyList()
                )
            )
        ),

        // ==================== HSK 3–4 (6 Battles) ====================
        SynonymBattleItem(
            id = "gang_vs_gangcai",
            title = "刚 vs 刚才",
            hskLevel = 3,
            band = LabHskBand.HSK_3_4,
            sides = listOf(
                SynonymSide(
                    hanzi = "刚",
                    pinyin = "gāng",
                    coreMeaning = "Just (adverb — subjective recency)",
                    usageRule = "Adverb placed ONLY after the subject and before the verb. Can refer to days/months ago if it feels recent.",
                    exampleHanzi = "我刚搬来上海一个月。",
                    examplePinyin = "Wǒ gāng bān lái Shànghǎi yí gè yuè.",
                    exampleEnglish = "I just moved to Shanghai a month ago."
                ),
                SynonymSide(
                    hanzi = "刚才",
                    pinyin = "gāngcái",
                    coreMeaning = "Just now (time noun — minutes ago)",
                    usageRule = "Time noun meaning 'a few minutes ago'. Can go BEFORE or AFTER the subject and can be negated (刚才没去).",
                    exampleHanzi = "刚才谁给你打电话了？",
                    examplePinyin = "Gāngcái shéi gěi nǐ dǎ diànhuà le?",
                    exampleEnglish = "Who called you just a moment ago?"
                )
            ),
            coreDifference = "刚 is an adverb (Subject + 刚 + Verb) for subjective recency. 刚才 is a time noun ('a few minutes ago') that can start a sentence!",
            commonMistakeWrong = "刚我看见张老师了。",
            commonMistakeRight = "刚才我看见张老师了。/ 我刚看见张老师。",
            commonMistakeNote = "刚 is an adverb so it can NEVER come before the subject 我 — use 刚才 at the start of a sentence.",
            offlineExtraExamples = "1) 刚才我在楼下碰到他了。(Gāngcái wǒ zài lóuxià pèngdào tā le.) — I bumped into him downstairs just now. (Before subject → 刚才)\n2) 电影刚演了十分钟。(Diànyǐng gāng yǎn le shí fēnzhōng.) — The movie has only been playing for 10 minutes. (Duration after verb → 刚)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_gang_1",
                    battleId = "gang_vs_gangcai",
                    battleTitle = "刚 vs 刚才",
                    hskLevel = 3,
                    band = LabHskBand.HSK_3_4,
                    sentenceWithBlank = "___ 下了一阵雨，现在地上还是湿的。",
                    fullHanzi = "刚才下了一阵雨，现在地上还是湿的。",
                    fullPinyin = "Gāngcái xià le yí zhèn yǔ, xiànzài dìshang háishi shī de.",
                    english = "It rained just a moment ago, and the ground is still wet.",
                    options = listOf("刚", "刚才"),
                    correctOption = "刚才",
                    grammarReason = "At the beginning of a sentence referring to a few minutes ago, only the time noun 刚才 works.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_gang_2",
                    battleId = "gang_vs_gangcai",
                    battleTitle = "刚 vs 刚才",
                    hskLevel = 3,
                    band = LabHskBand.HSK_3_4,
                    sentenceWithBlank = "他 ___ 大学毕业半年。",
                    fullHanzi = "他刚大学毕业半年。",
                    fullPinyin = "Tā gāng dàxué bìyè bàn nián.",
                    english = "He just graduated from university half a year ago.",
                    options = listOf("刚", "刚才"),
                    correctOption = "刚",
                    grammarReason = "Half a year ago is subjectively recent (not literally minutes ago), so only adverb 刚 fits.",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "liaojie_vs_renshi",
            title = "了解 vs 认识",
            hskLevel = 3,
            band = LabHskBand.HSK_3_4,
            sides = listOf(
                SynonymSide(
                    hanzi = "了解",
                    pinyin = "liǎojiě",
                    coreMeaning = "Understand deeply / know well",
                    usageRule = "Deep, thorough knowledge of someone's character, a culture, or a situation. Can take degree adverbs like 很了解.",
                    exampleHanzi = "他很了解中国的历史。",
                    examplePinyin = "Tā hěn liǎojiě Zhōngguó de lìshǐ.",
                    exampleEnglish = "He understands Chinese history very well."
                ),
                SynonymSide(
                    hanzi = "认识",
                    pinyin = "rènshi",
                    coreMeaning = "Know / recognize / meet",
                    usageRule = "Being acquainted with a person, recognizing a Chinese character (认识汉字), or knowing a road.",
                    exampleHanzi = "很高兴认识你！",
                    examplePinyin = "Hěn gāoxìng rènshi nǐ!",
                    exampleEnglish = "Nice to meet you!"
                )
            ),
            coreDifference = "认识 = surface acquaintance or recognition (meet a person, read a Hanzi). 了解 = deep understanding of how someone/something works.",
            commonMistakeWrong = "我认识很多中国文化，但不了解这个汉字。",
            commonMistakeRight = "我了解很多中国文化，但不认识这个汉字。",
            commonMistakeNote = "You 认识 (recognize) characters and people; you 了解 (deeply understand) culture, situations, and people's hearts.",
            offlineExtraExamples = "1) 我认识他，但是不太了解他。(Wǒ rènshi tā, dànshì bú tài liǎojiě tā.) — I know who he is, but I don't know him well.\n2) 这些生词你都认识吗？(Zhèxiē shēngcí nǐ dōu rènshi ma?) — Do you recognize all these new words?",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_liaojie_1",
                    battleId = "liaojie_vs_renshi",
                    battleTitle = "了解 vs 认识",
                    hskLevel = 3,
                    band = LabHskBand.HSK_3_4,
                    sentenceWithBlank = "虽然我们刚刚认识，但我很想 ___ 你的家乡。",
                    fullHanzi = "虽然我们刚刚认识，但我很想了解你的家乡。",
                    fullPinyin = "Suīrán wǒmen gānggāng rènshi, dàn wǒ hěn xiǎng liǎojiě nǐ de jiāxiāng.",
                    english = "Although we just met, I really want to learn deeply about your hometown.",
                    options = listOf("了解", "认识"),
                    correctOption = "了解",
                    grammarReason = "Learning in depth about a place or culture uses 了解, whereas meeting someone uses 认识.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_liaojie_2",
                    battleId = "liaojie_vs_renshi",
                    battleTitle = "了解 vs 认识",
                    hskLevel = 3,
                    band = LabHskBand.HSK_3_4,
                    sentenceWithBlank = "你 ___ 那个戴眼镜的男生吗？",
                    fullHanzi = "你认识那个戴眼镜的男生吗？",
                    fullPinyin = "Nǐ rènshi nàge dài yǎnjìng de nánshēng ma?",
                    english = "Do you know that guy wearing glasses?",
                    options = listOf("了解", "认识"),
                    correctOption = "认识",
                    grammarReason = "Asking whether you are acquainted with or recognize a person uses 认识.",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "juede_vs_yiwei",
            title = "觉得 vs 以为",
            hskLevel = 4,
            band = LabHskBand.HSK_3_4,
            sides = listOf(
                SynonymSide(
                    hanzi = "觉得",
                    pinyin = "juéde",
                    coreMeaning = "Feel / think (neutral opinion)",
                    usageRule = "Expresses a personal opinion, impression, or physical feeling (我觉得冷) with no implication of being wrong.",
                    exampleHanzi = "我觉得这部电影很有意思。",
                    examplePinyin = "Wǒ juéde zhè bù diànyǐng hěn yǒuyìsi.",
                    exampleEnglish = "I think this movie is very interesting."
                ),
                SynonymSide(
                    hanzi = "以为",
                    pinyin = "yǐwéi",
                    coreMeaning = "Mistakenly thought / assumed",
                    usageRule = "Almost ALWAYS signals a false assumption that turned out to be wrong ('I thought X, but actually Y!').",
                    exampleHanzi = "我以为今天是星期五呢！",
                    examplePinyin = "Wǒ yǐwéi jīntiān shì xīngqīwǔ ne!",
                    exampleEnglish = "I mistakenly thought today was Friday!"
                )
            ),
            coreDifference = "觉得 = normal opinion/feeling ('I think / I feel'). 以为 = wrong assumption ('I mistakenly thought...').",
            commonMistakeWrong = "我以为你的中文说得非常好！",
            commonMistakeRight = "我觉得你的中文说得非常好！",
            commonMistakeNote = "Saying '我以为...' implies you were WRONG (ouch!). Always use 觉得 for genuine opinions and compliments.",
            offlineExtraExamples = "1) 我还以为你已经回去了。(Wǒ hái yǐwéi nǐ yǐjīng huíqù le.) — I thought you had already gone back! (Wrong assumption → 以为)\n2) 你觉得这件蓝色的怎么样？(Nǐ juéde zhè jiàn lánsè de zěnmeyàng?) — What do you think of this blue one? (Opinion → 觉得)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_juede_1",
                    battleId = "juede_vs_yiwei",
                    battleTitle = "觉得 vs 以为",
                    hskLevel = 4,
                    band = LabHskBand.HSK_3_4,
                    sentenceWithBlank = "我 ___ 他是日本人，原来他是中国人。",
                    fullHanzi = "我以为他是日本人，原来他是中国人。",
                    fullPinyin = "Wǒ yǐwéi tā shì Rìběnrén, yuánlái tā shì Zhōngguórén.",
                    english = "I mistakenly thought he was Japanese, turns out he's Chinese.",
                    options = listOf("觉得", "以为"),
                    correctOption = "以为",
                    grammarReason = "The clause 原来... ('turns out...') reveals the assumption was mistaken, which requires 以为.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_juede_2",
                    battleId = "juede_vs_yiwei",
                    battleTitle = "觉得 vs 以为",
                    hskLevel = 4,
                    band = LabHskBand.HSK_3_4,
                    sentenceWithBlank = "穿毛衣吧，我 ___ 外面挺冷的。",
                    fullHanzi = "穿毛衣吧，我觉得外面挺冷的。",
                    fullPinyin = "Chuān máoyī ba, wǒ juéde wàimiàn tǐng lěng de.",
                    english = "Wear a sweater — I feel it's quite cold outside.",
                    options = listOf("觉得", "以为"),
                    correctOption = "觉得",
                    grammarReason = "Expressing a direct sensory feeling or genuine judgment uses 觉得.",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "zhongyu_vs_zuihou",
            title = "终于 vs 最后",
            hskLevel = 3,
            band = LabHskBand.HSK_3_4,
            sides = listOf(
                SynonymSide(
                    hanzi = "终于",
                    pinyin = "zhōngyú",
                    coreMeaning = "Finally / at last (after long wait)",
                    usageRule = "Adverb carrying strong emotion or relief after a long wait or hard effort (Subject + 终于 + Verb).",
                    exampleHanzi = "等了一个小时，公交车终于来了！",
                    examplePinyin = "Děng le yí gè xiǎoshí, gōngjiāochē zhōngyú lái le!",
                    exampleEnglish = "After waiting an hour, the bus finally arrived!"
                ),
                SynonymSide(
                    hanzi = "最后",
                    pinyin = "zuìhòu",
                    coreMeaning = "Last / in the end (sequence)",
                    usageRule = "Neutral time/order noun or modifier ('last one', 'lastly') — can precede the subject or modify a noun (最后一天).",
                    exampleHanzi = "这是我们的最后一节课。",
                    examplePinyin = "Zhè shì wǒmen de zuìhòu yì jié kè.",
                    exampleEnglish = "This is our very last class."
                )
            ),
            coreDifference = "终于 = emotional 'at last!' after effort/waiting (adverb only). 最后 = factual sequence 'lastly / final' (can modify nouns like 最后一天).",
            commonMistakeWrong = "今天是假期的终于一天。",
            commonMistakeRight = "今天是假期的最后一天。",
            commonMistakeNote = "终于 is strictly an adverb and can NEVER modify a noun — use 最后 for 'the last day / final item'.",
            offlineExtraExamples = "1) 经过三年努力，她终于考上了理想的大学。(Jīngguò sān nián nǔlì, tā zhōngyú kǎoshàng le lǐxiǎng de dàxué.) — After 3 years of effort, she finally got into her dream university.\n2) 最后，我想感谢大家的支持。(Zuìhòu, wǒ xiǎng gǎnxiè dàjiā de zhīchí.) — Lastly, I want to thank everyone for your support.",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_zhongyu_1",
                    battleId = "zhongyu_vs_zuihou",
                    battleTitle = "终于 vs 最后",
                    hskLevel = 3,
                    band = LabHskBand.HSK_3_4,
                    sentenceWithBlank = "练了半年，我 ___ 学会游泳了！",
                    fullHanzi = "练了半年，我终于学会游泳了！",
                    fullPinyin = "Liàn le bàn nián, wǒ zhōngyú xuéhuì yóuyǒng le!",
                    english = "After practicing for half a year, I finally learned to swim!",
                    options = listOf("终于", "最后"),
                    correctOption = "终于",
                    grammarReason = "终于 expresses joyful relief after sustained effort ('at last!').",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_zhongyu_2",
                    battleId = "zhongyu_vs_zuihou",
                    battleTitle = "终于 vs 最后",
                    hskLevel = 3,
                    band = LabHskBand.HSK_3_4,
                    sentenceWithBlank = "请把 ___ 一道题再检查一遍。",
                    fullHanzi = "请把最后一道题再检查一遍。",
                    fullPinyin = "Qǐng bǎ zuìhòu yí dào tí zài jiǎnchá yíbiàn.",
                    english = "Please double-check the very last question.",
                    options = listOf("终于", "最后"),
                    correctOption = "最后",
                    grammarReason = "Modifying a noun ('the last question') strictly requires 最后, never 终于.",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "bixu_vs_yinggai",
            title = "必须 vs 应该",
            hskLevel = 4,
            band = LabHskBand.HSK_3_4,
            sides = listOf(
                SynonymSide(
                    hanzi = "必须",
                    pinyin = "bìxū",
                    coreMeaning = "Must / have to (mandatory)",
                    usageRule = "Strict requirement, rule, or non-negotiable necessity. Negation is 不必 / 不用 (NOT 不必须!).",
                    exampleHanzi = "过马路必须看红绿灯。",
                    examplePinyin = "Guò mǎlù bìxū kàn hónglǜdēng.",
                    exampleEnglish = "You must watch the traffic lights when crossing the street."
                ),
                SynonymSide(
                    hanzi = "应该",
                    pinyin = "yīnggāi",
                    coreMeaning = "Should / ought to (advice & likelihood)",
                    usageRule = "Friendly advice, moral duty, or logical deduction ('He should be home by now'). Negation: 不应该.",
                    exampleHanzi = "你感冒了，应该多喝热水。",
                    examplePinyin = "Nǐ gǎnmào le, yīnggāi duō hē rèshuǐ.",
                    exampleEnglish = "You have a cold; you should drink more hot water."
                )
            ),
            coreDifference = "必须 = mandatory rule ('must', no choice). 应该 = advice or probability ('should / ought to').",
            commonMistakeWrong = "现在八点了，他必须已经到家了。",
            commonMistakeRight = "现在八点了，他应该已经到家了。",
            commonMistakeNote = "必须 cannot express probability/guessing — use 应该 for 'should have arrived by now'.",
            offlineExtraExamples = "1) 坐飞机必须带护照。(Zuò fēijī bìxū dài hùzhào.) — You must bring your passport to fly. (Strict rule → 必须)\n2) 我们不应该浪费食物。(Wǒmen bù yīnggāi làngfèi shíwù.) — We shouldn't waste food. (Advice/negation → 不应该)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_bixu_1",
                    battleId = "bixu_vs_yinggai",
                    battleTitle = "必须 vs 应该",
                    hskLevel = 4,
                    band = LabHskBand.HSK_3_4,
                    sentenceWithBlank = "进入图书馆 ___ 保持安静，这是规定。",
                    fullHanzi = "进入图书馆必须保持安静，这是规定。",
                    fullPinyin = "Jìnrù túshūguǎn bìxū bǎochí ānjìng, zhè shì guīdìng.",
                    english = "You must stay quiet upon entering the library — it's the rule.",
                    options = listOf("必须", "应该"),
                    correctOption = "必须",
                    grammarReason = "Mandatory institutional rules ('这是规定') require the strict modal 必须.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_bixu_2",
                    battleId = "bixu_vs_yinggai",
                    battleTitle = "必须 vs 应该",
                    hskLevel = 4,
                    band = LabHskBand.HSK_3_4,
                    sentenceWithBlank = "按时间算，火车 ___ 快到站了。",
                    fullHanzi = "按时间算，火车应该快到站了。",
                    fullPinyin = "Àn shíjiān suàn, huǒchē yīnggāi kuài dàozhàn le.",
                    english = "Going by the time, the train should be arriving at the station soon.",
                    options = listOf("必须", "应该"),
                    correctOption = "应该",
                    grammarReason = "Logical estimation or probability ('should be arriving') uses 应该, never 必须.",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "faxian_vs_zhaodao",
            title = "发现 vs 找到",
            hskLevel = 4,
            band = LabHskBand.HSK_3_4,
            sides = listOf(
                SynonymSide(
                    hanzi = "发现",
                    pinyin = "fāxiàn",
                    coreMeaning = "Discover / notice (unexpected)",
                    usageRule = "Noticing a fact, secret, mistake, or scientific discovery — often without actively searching for it.",
                    exampleHanzi = "我突然发现今天下雨了。",
                    examplePinyin = "Wǒ tūrán fāxiàn jīntiān xiàyǔ le.",
                    exampleEnglish = "I suddenly noticed it's raining today."
                ),
                SynonymSide(
                    hanzi = "找到",
                    pinyin = "zhǎodào",
                    coreMeaning = "Find (after actively looking)",
                    usageRule = "Result complement of 找 (search) + 到 (succeed) — you searched for a lost item, person, or job and found it!",
                    exampleHanzi = "我终于找到我的钥匙了！",
                    examplePinyin = "Wǒ zhōngyú zhǎodào wǒ de yàoshi le!",
                    exampleEnglish = "I finally found my keys!"
                )
            ),
            coreDifference = "发现 = notice/discover a fact or pattern (can take a whole clause). 找到 = locate something you actively looked for (keys, a job, a place).",
            commonMistakeWrong = "我找了半天，终于发现了我的手机。",
            commonMistakeRight = "我找了半天，终于找到了我的手机。",
            commonMistakeNote = "Because you actively searched ('找了半天'), use 找到 ('found'), not 发现 ('discovered').",
            offlineExtraExamples = "1) 科学家发现了一颗新行星。(Kēxuéjiā fāxiàn le yì kē xīn xíngxīng.) — Scientists discovered a new planet. (Discovery → 发现)\n2) 她毕业后很快找到了一份好工作。(Tā bìyè hòu hěn kuài zhǎodào le yí fèn hǎo gōngzuò.) — She quickly found a great job after graduating. (Job search → 找到)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_faxian_1",
                    battleId = "faxian_vs_zhaodao",
                    battleTitle = "发现 vs 找到",
                    hskLevel = 4,
                    band = LabHskBand.HSK_3_4,
                    sentenceWithBlank = "出门以后，我才 ___ 忘带钱包了。",
                    fullHanzi = "出门以后，我才发现忘带钱包了。",
                    fullPinyin = "Chūmén yǐhòu, wǒ cái fāxiàn wàng dài qiánbāo le.",
                    english = "Only after leaving the house did I realize I forgot my wallet.",
                    options = listOf("发现", "找到"),
                    correctOption = "发现",
                    grammarReason = "Realizing or noticing a fact/clause ('forgot my wallet') uses 发现.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_faxian_2",
                    battleId = "faxian_vs_zhaodao",
                    battleTitle = "发现 vs 找到",
                    hskLevel = 4,
                    band = LabHskBand.HSK_3_4,
                    sentenceWithBlank = "按照地图走了十分钟，我们 ___ 了那家咖啡馆。",
                    fullHanzi = "按照地图走了十分钟，我们找到了那家咖啡馆。",
                    fullPinyin = "Ànzhào dìtú zǒu le shí fēnzhōng, wǒmen zhǎodào le nà jiā kāfēiguǎn.",
                    english = "Following the map for 10 minutes, we found that café.",
                    options = listOf("发现", "找到"),
                    correctOption = "找到",
                    grammarReason = "Successfully locating a specific target you were looking for uses 找到.",
                    targetSides = emptyList()
                )
            )
        ),

        // ==================== HSK 5–6 (4 Battles) ====================
        SynonymBattleItem(
            id = "manzu_vs_manyi",
            title = "满足 vs 满意",
            hskLevel = 5,
            band = LabHskBand.HSK_5_6,
            sides = listOf(
                SynonymSide(
                    hanzi = "满足",
                    pinyin = "mǎnzú",
                    coreMeaning = "Satisfy (needs) / feel content",
                    usageRule = "Transitive verb ('satisfy demands/conditions': 满足要求) OR feeling content with what you have (很满足).",
                    exampleHanzi = "这款手机能满足我的日常需求。",
                    examplePinyin = "Zhè kuǎn shǒujī néng mǎnzú wǒ de rìcháng xūqiú.",
                    exampleEnglish = "This phone can meet my daily needs."
                ),
                SynonymSide(
                    hanzi = "满意",
                    pinyin = "mǎnyì",
                    coreMeaning = "Satisfied / pleased with (adj)",
                    usageRule = "Adjective ONLY — CANNOT take a direct object! Must use pattern: 对 + [something] + 满意.",
                    exampleHanzi = "老板对你的报告非常满意。",
                    examplePinyin = "Lǎobǎn duì nǐ de bàogào fēicháng mǎnyì.",
                    exampleEnglish = "The boss is very pleased with your report."
                )
            ),
            coreDifference = "满足 takes an object directly (满足要求 / 满足条件). 满意 is an adjective that uses 对…满意 and NEVER takes a direct object!",
            commonMistakeWrong = "这个方案能满意客户的要求。",
            commonMistakeRight = "这个方案能满足客户的要求。",
            commonMistakeNote = "满意 cannot take an object like 客户的要求 — use verb 满足 ('fulfill/meet')!",
            offlineExtraExamples = "1) 顾客对这家餐厅的服务很满意。(Gùkè duì zhè jiā cāntīng de fúwù hěn mǎnyì.) — Customers are very satisfied with this restaurant's service. (对…满意)\n2) 只有满足所有条件才能报名。(Zhǐyǒu mǎnzú suǒyǒu tiáojiàn cái néng bàomíng.) — You can only register if you meet all requirements. (满足 + Object)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_manzu_1",
                    battleId = "manzu_vs_manyi",
                    battleTitle = "满足 vs 满意",
                    hskLevel = 5,
                    band = LabHskBand.HSK_5_6,
                    sentenceWithBlank = "他对这次考试的成绩不太 ___ 。",
                    fullHanzi = "他对这次考试的成绩不太满意。",
                    fullPinyin = "Tā duì zhè cì kǎoshì de chéngjì bú tài mǎnyì.",
                    english = "He isn't very satisfied with his score on this exam.",
                    options = listOf("满足", "满意"),
                    correctOption = "满意",
                    grammarReason = "The structure 对…很/不太___ ('pleased with X') pairs specifically with adjective 满意.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_manzu_2",
                    battleId = "manzu_vs_manyi",
                    battleTitle = "满足 vs 满意",
                    hskLevel = 5,
                    band = LabHskBand.HSK_5_6,
                    sentenceWithBlank = "这个设计完全 ___ 了用户的需要。",
                    fullHanzi = "这个设计完全满足了用户的需要。",
                    fullPinyin = "Zhège shèjì wánquán mǎnzú le yònghù de xūyào.",
                    english = "This design completely satisfied users' needs.",
                    options = listOf("满足", "满意"),
                    correctOption = "满足",
                    grammarReason = "Only 满足 can act as a transitive verb taking the direct object 用户的需要.",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "jianchi_vs_baochi",
            title = "坚持 vs 保持",
            hskLevel = 5,
            band = LabHskBand.HSK_5_6,
            sides = listOf(
                SynonymSide(
                    hanzi = "坚持",
                    pinyin = "jiānchí",
                    coreMeaning = "Persist / stick to (despite difficulty)",
                    usageRule = "Persevering with an action, habit, or principle despite hardship — 坚持跑步, 坚持学习, 坚持原则.",
                    exampleHanzi = "他每天坚持学三十分钟中文。",
                    examplePinyin = "Tā měitiān jiānchí xué sānshí fēnzhōng Zhōngwén.",
                    exampleEnglish = "He persists in studying Chinese for 30 minutes every day."
                ),
                SynonymSide(
                    hanzi = "保持",
                    pinyin = "bǎochí",
                    coreMeaning = "Maintain / keep (a state)",
                    usageRule = "Keeping a state, standard, or condition steady without letting it drop — 保持安静, 保持联系, 保持健康.",
                    exampleHanzi = "我们毕业以后也要保持联系。",
                    examplePinyin = "Wǒmen bìyè yǐhòu yě yào bǎochí liánxì.",
                    exampleEnglish = "Let's stay in touch even after graduation."
                )
            ),
            coreDifference = "坚持 = persevere with an action/effort (often + Verb). 保持 = maintain a state or standard (保持安静 / 保持冷静 / 保持联系).",
            commonMistakeWrong = "请坚持安静，会议马上开始。",
            commonMistakeRight = "请保持安静，会议马上开始。",
            commonMistakeNote = "Keeping a room in a quiet state uses 保持 ('maintain'), not 坚持 ('persevere').",
            offlineExtraExamples = "1) 遇到紧急情况要保持冷静。(Yùdào jǐnjí qíngkuàng yào bǎochí lěngjìng.) — Stay calm in an emergency. (Maintain a state → 保持)\n2) 只要坚持下去，就一定会成功。(Zhǐyào jiānchí xiàqù, jiù yídìng huì chénggōng.) — As long as you persevere, you'll succeed. (Persist → 坚持)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_jianchi_1",
                    battleId = "jianchi_vs_baochi",
                    battleTitle = "坚持 vs 保持",
                    hskLevel = 5,
                    band = LabHskBand.HSK_5_6,
                    sentenceWithBlank = "为了参加马拉松，她每天早上都 ___ 跑步。",
                    fullHanzi = "为了参加马拉松，她每天早上都坚持跑步。",
                    fullPinyin = "Wèile cānjiā mǎlāsōng, tā měitiān zǎoshang dōu jiānchí pǎobù.",
                    english = "To run the marathon, she sticks to jogging every morning.",
                    options = listOf("坚持", "保持"),
                    correctOption = "坚持",
                    grammarReason = "Sticking to a disciplined verb/action (跑步) through effort uses 坚持.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_jianchi_2",
                    battleId = "jianchi_vs_baochi",
                    battleTitle = "坚持 vs 保持",
                    hskLevel = 5,
                    band = LabHskBand.HSK_5_6,
                    sentenceWithBlank = "开车时请与前车 ___ 安全距离。",
                    fullHanzi = "开车时请与前车保持安全距离。",
                    fullPinyin = "Kāichē shí qǐng yǔ qiánchē bǎochí ānquán jùlí.",
                    english = "Please maintain a safe distance from the car ahead while driving.",
                    options = listOf("坚持", "保持"),
                    correctOption = "保持",
                    grammarReason = "Maintaining a steady condition or gap (安全距离) uses 保持.",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "chansheng_vs_fasheng",
            title = "产生 vs 发生",
            hskLevel = 5,
            band = LabHskBand.HSK_5_6,
            sides = listOf(
                SynonymSide(
                    hanzi = "产生",
                    pinyin = "chǎnshēng",
                    coreMeaning = "Produce / give rise to (new feelings/ideas)",
                    usageRule = "New things emerging from existing ones — feelings, interest, ideas, electricity, or effects (产生兴趣, 产生影响).",
                    exampleHanzi = "我对中国茶文化产生了浓厚的兴趣。",
                    examplePinyin = "Wǒ duì Zhōngguó chá wénhuà chǎnshēng le nónghòu de xìngqù.",
                    exampleEnglish = "I developed a strong interest in Chinese tea culture."
                ),
                SynonymSide(
                    hanzi = "发生",
                    pinyin = "fāshēng",
                    coreMeaning = "Happen / occur (events & changes)",
                    usageRule = "Events, accidents, stories, or sudden changes taking place — 发生事故, 发生变化, 发生什么事了.",
                    exampleHanzi = "最近这座城市发生了很大的变化。",
                    examplePinyin = "Zuìjìn zhè zuò chéngshì fāshēng le hěn dà de biànhuà.",
                    exampleEnglish = "Great changes have occurred in this city recently."
                )
            ),
            coreDifference = "产生 = generate/develop feelings, thoughts, or results (兴趣, 想法, 影响). 发生 = events or changes happen/occur (事情, 事故, 变化).",
            commonMistakeWrong = "昨天路上产生了一场交通事故。",
            commonMistakeRight = "昨天路上发生了一场交通事故。",
            commonMistakeNote = "Accidents and events HAPPEN (发生), they aren't 'generated' (产生).",
            offlineExtraExamples = "1) 这种做法可能会产生误会。(Zhè zhǒng zuòfǎ kěnéng huì chǎnshēng wùhuì.) — This approach might create misunderstandings. (产生误会)\n2) 外面怎么这么吵？发生什么事了？(Wàimiàn zěnme zhème chǎo? Fāshēng shénme shì le?) — Why is it so noisy outside? What happened? (发生事情)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_chansheng_1",
                    battleId = "chansheng_vs_fasheng",
                    battleTitle = "产生 vs 发生",
                    hskLevel = 5,
                    band = LabHskBand.HSK_5_6,
                    sentenceWithBlank = "这本书对我的人生态度 ___ 了深刻的影响。",
                    fullHanzi = "这本书对我的人生态度产生了深刻的影响。",
                    fullPinyin = "Zhè běn shū duì wǒ de rénshēng tàidù chǎnshēng le shēnkè de yǐngxiǎng.",
                    english = "This book had a profound impact on my outlook on life.",
                    options = listOf("产生", "发生"),
                    correctOption = "产生",
                    grammarReason = "Producing an effect or impact (影响) collocates naturally with 产生.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_chansheng_2",
                    battleId = "chansheng_vs_fasheng",
                    battleTitle = "产生 vs 发生",
                    hskLevel = 5,
                    band = LabHskBand.HSK_5_6,
                    sentenceWithBlank = "这个感人的故事 ___ 在十年前。",
                    fullHanzi = "这个感人的故事发生在十年前。",
                    fullPinyin = "Zhège gǎnrén de gùshi fāshēng zài shí nián qián.",
                    english = "This touching story took place ten years ago.",
                    options = listOf("产生", "发生"),
                    correctOption = "发生",
                    grammarReason = "A story or event taking place at a point in time always uses 发生.",
                    targetSides = emptyList()
                )
            )
        ),
        SynonymBattleItem(
            id = "qubie_vs_chabie",
            title = "区别 vs 差别",
            hskLevel = 5,
            band = LabHskBand.HSK_5_6,
            sides = listOf(
                SynonymSide(
                    hanzi = "区别",
                    pinyin = "qūbié",
                    coreMeaning = "Difference / to distinguish (Noun & Verb)",
                    usageRule = "Can be a NOUN ('what's the difference between A and B?') OR a VERB ('to distinguish A from B': 区别真假).",
                    exampleHanzi = "你能区别这两个近义词吗？",
                    examplePinyin = "Nǐ néng qūbié zhè liǎng gè jìnyìcí ma?",
                    exampleEnglish = "Can you distinguish these two near-synonyms?"
                ),
                SynonymSide(
                    hanzi = "差别",
                    pinyin = "chābié",
                    coreMeaning = "Gap / disparity (Noun only)",
                    usageRule = "NOUN ONLY — emphasizes a measurable gap in level, price, quality, or degree (价格差别很大).",
                    exampleHanzi = "南北方的气候差别很大。",
                    examplePinyin = "Nán-běifāng de qìhòu chābié hěn dà.",
                    exampleEnglish = "The climate gap between the south and north is huge."
                )
            ),
            coreDifference = "区别 works as BOTH noun & verb ('distinguish'). 差别 is a NOUN only, highlighting a gap in degree, level, or price.",
            commonMistakeWrong = "大家要学会差别对与错。",
            commonMistakeRight = "大家要学会区别对与错。",
            commonMistakeNote = "差别 is strictly a noun — when you need the verb 'to distinguish', you must use 区别!",
            offlineExtraExamples = "1) 这两款产品的价格差别不小。(Zhè liǎng kuǎn chǎnpǐn de jiàgé chābié bù xiǎo.) — The price gap between these two products isn't small. (Gap in degree → 差别)\n2) ‘二’和‘两’在用法上有什么区别？('Èr' hé 'liǎng' zài yòngfǎ shàng yǒu shénme qūbié?) — What is the usage difference between 二 and 两? (Conceptual distinction → 区别)",
            quizQuestions = listOf(
                SynonymQuizPrompt(
                    id = "syn_q_qubie_1",
                    battleId = "qubie_vs_chabie",
                    battleTitle = "区别 vs 差别",
                    hskLevel = 5,
                    band = LabHskBand.HSK_5_6,
                    sentenceWithBlank = "我们要学会 ___ 事实和观点。",
                    fullHanzi = "我们要学会区别事实和观点。",
                    fullPinyin = "Wǒmen yào xuéhuì qūbié shìshí hé guāndiǎn.",
                    english = "We need to learn to distinguish facts from opinions.",
                    options = listOf("区别", "差别"),
                    correctOption = "区别",
                    grammarReason = "Only 区别 can be used as a verb ('to distinguish'); 差别 is strictly a noun.",
                    targetSides = emptyList()
                ),
                SynonymQuizPrompt(
                    id = "syn_q_qubie_2",
                    battleId = "qubie_vs_chabie",
                    battleTitle = "区别 vs 差别",
                    hskLevel = 5,
                    band = LabHskBand.HSK_5_6,
                    sentenceWithBlank = "这两家酒店的收费 ___ 非常大。",
                    fullHanzi = "这两家酒店的收费差别非常大。",
                    fullPinyin = "Zhè liǎng jiā jiǔdiàn de shōufèi chābié fēicháng dà.",
                    english = "The price gap between these two hotels is huge.",
                    options = listOf("区别", "差别"),
                    correctOption = "差别",
                    grammarReason = "Emphasizing a large measurable disparity in price or degree naturally takes 差别.",
                    targetSides = emptyList()
                )
            )
        )
    )

    fun synonymBattlesForBand(band: LabHskBand): List<SynonymBattleItem> =
        synonymBattles.filter { band.matchesLevel(it.hskLevel) }

    fun buildSynonymBattleQuiz(band: LabHskBand, count: Int = 10): List<SynonymQuizPrompt> {
        val battles = synonymBattlesForBand(band).ifEmpty { synonymBattles }
        val allPrompts = battles.flatMap { battle ->
            battle.quizQuestions.map { q ->
                q.copy(targetSides = battle.sides)
            }
        }.shuffled()

        val selected = mutableListOf<SynonymQuizPrompt>()
        val usedBattles = mutableSetOf<String>()
        for (q in allPrompts) {
            if (selected.size >= count) break
            if (usedBattles.add(q.battleId)) {
                selected.add(q)
            }
        }
        for (q in allPrompts) {
            if (selected.size >= count) break
            if (q !in selected) {
                selected.add(q)
            }
        }
        return selected
    }
}
