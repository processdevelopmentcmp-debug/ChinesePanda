package com.example.data

import com.example.model.GradedStory
import com.example.model.HskLevelMeta
import com.example.model.LearningUnit
import com.example.model.LessonQuestion
import com.example.model.ListeningQuestion
import com.example.model.ListeningQuestionType
import com.example.model.RadicalItem
import com.example.model.SandhiPhrase
import com.example.model.SandhiToneTile
import com.example.model.StorySentenceDoc
import com.example.model.TileSentenceItem
import com.example.model.TopicCategory
import com.example.model.VocabWord

object HskCurriculumData {

    val hskLevels: List<HskLevelMeta> = listOf(
        HskLevelMeta(1, "HSK 1", "Beginner", "300 words", "300"),
        HskLevelMeta(2, "HSK 2", "Elementary", "+200 words", "500"),
        HskLevelMeta(3, "HSK 3", "Pre-Int.", "+500 words", "1,000"),
        HskLevelMeta(4, "HSK 4", "Intermediate", "+1,000 words", "2,000"),
        HskLevelMeta(5, "HSK 5", "Upper-Int.", "+1,600 words", "3,600"),
        HskLevelMeta(6, "HSK 6", "Advanced", "+1,800 words", "5,400"),
        HskLevelMeta(7, "HSK 7–9", "Pro Mastery", "+5,600 words", "11,000")
    )

    val topics: List<TopicCategory> = listOf(
        TopicCategory(1, "Greetings & Pronouns", "问候与代词", "Social", 1),
        TopicCategory(2, "Numbers & Measure Words", "数字与量词", "Core", 1),
        TopicCategory(3, "Time & Calendar", "时间与日期", "Core", 1),
        TopicCategory(4, "Family & People", "家庭与人物", "Life", 1),
        TopicCategory(5, "Food & Restaurant", "饮食与点菜", "Life", 1),
        TopicCategory(6, "Daily Actions & Verbs", "日常动作", "Core", 1),
        TopicCategory(7, "Places & Directions", "地点与方位", "Travel", 1),
        TopicCategory(8, "Travel & Transport", "交通与出行", "Travel", 2),
        TopicCategory(9, "Study & Work", "学习与工作", "Career", 2),
        TopicCategory(10, "Shopping & Modern Tech", "购物与现代生活", "HSK 3.0", 2),
        TopicCategory(11, "Home & Everyday Objects", "家居与物品", "Life", 1),
        TopicCategory(12, "Weather & Nature", "天气与自然", "Nature", 2),
        TopicCategory(13, "Adjectives & Feelings", "形容词与情感", "Express", 2),
        TopicCategory(14, "Grammar & Connectors", "语法与连接词", "Grammar", 2)
    )

    val builtInVocab: List<VocabWord> = listOf(
        // Topic 1: Greetings & Pronouns (HSK 1-7)
        VocabWord("w101", "你好", "nǐ hǎo", "Hello / Hi", 1, 1, true, "你好！很高兴认识你。", "Nǐ hǎo! Hěn gāoxìng rènshi nǐ.", "Hello! Nice to meet you."),
        VocabWord("w102", "谢谢", "xiè xie", "Thank you", 1, 1, true, "非常谢谢你的帮助。", "Fēicháng xièxie nǐ de bāngzhù.", "Thank you very much for your help."),
        VocabWord("w103", "我", "wǒ", "I / Me", 1, 1, true, "我是学习中文的学生。", "Wǒ shì xuéxí Zhōngwén de xuésheng.", "I am a student learning Chinese."),
        VocabWord("w104", "你", "nǐ", "You", 1, 1, true, "你今天有时间吗？", "Nǐ jīntiān yǒu shíjiān ma?", "Do you have time today?"),
        VocabWord("w105", "您", "nín", "You (polite)", 1, 1, true, "请问您贵姓？", "Qǐngwèn nín guìxìng?", "May I ask your surname?"),
        VocabWord("w106", "对不起", "duì bu qǐ", "I am sorry", 1, 1, true, "对不起，我来晚了。", "Duìbuqǐ, wǒ lái wǎn le.", "Sorry, I arrived late."),
        VocabWord("w107", "认识", "rèn shi", "To know / meet", 1, 1, true, "很高兴认识大家。", "Hěn gāoxìng rènshi dàjiā.", "Nice to meet everyone."),
        VocabWord("w108", "久仰", "jiǔ yǎng", "Honored to finally meet you", 7, 1, false, "久仰大名，今天终于见面了。", "Jiǔyǎng dàmíng, jīntiān zhōngyú jiànmiàn le.", "I have long admired your reputation; glad to finally meet."),

        // Topic 2: Numbers & Measure Words
        VocabWord("w201", "一", "yī", "One", 1, 2, true, "我买了一本书。", "Wǒ mǎi le yì běn shū.", "I bought one book."),
        VocabWord("w202", "个", "gè", "General measure word", 1, 2, true, "桌上有三个苹果。", "Zhuō shàng yǒu sān gè píngguǒ.", "There are three apples on the table."),
        VocabWord("w203", "本", "běn", "Measure word for books", 1, 2, true, "这本词典非常实用。", "Zhè běn cídiǎn fēicháng shíyòng.", "This dictionary is very practical."),
        VocabWord("w204", "杯", "bēi", "Cup / glass of", 1, 2, true, "请给我一杯热茶。", "Qǐng gěi wǒ yì bēi rè chá.", "Please give me a cup of hot tea."),
        VocabWord("w205", "两", "liǎng", "Two (before measure words)", 1, 2, true, "我们等了两个小时。", "Wǒmen děng le liǎng gè xiǎoshí.", "We waited for two hours."),
        VocabWord("w206", "次", "cì", "Time / occurrence", 2, 2, true, "这是我第一次来北京。", "Zhè shì wǒ dì yī cì lái Běijīng.", "This is my first time coming to Beijing."),
        VocabWord("w207", "比例", "bǐ lì", "Proportion / ratio", 5, 2, true, "新能源的比例逐年上升。", "Xīn néngyuán de bǐlì zhúnián shàngshēng.", "The proportion of renewable energy rises year by year."),

        // Topic 3: Time & Calendar
        VocabWord("w301", "今天", "jīn tiān", "Today", 1, 3, true, "今天天气真舒服。", "Jīntiān tiānqì zhēn shūfu.", "Today's weather is really comfortable."),
        VocabWord("w302", "明天", "míng tiān", "Tomorrow", 1, 3, true, "明天早上八点集合。", "Míngtiān zǎoshang bā diǎn jíhé.", "Gather tomorrow morning at eight."),
        VocabWord("w303", "现在", "xiàn zài", "Now", 1, 3, true, "现在几点了？", "Xiànzài jǐ diǎn le?", "What time is it now?"),
        VocabWord("w304", "周末", "zhōu mò", "Weekend", 2, 3, true, "周末你打算去哪儿？", "Zhōumò nǐ dǎsuàn qù nǎr?", "Where do you plan to go this weekend?"),
        VocabWord("w305", "按时", "àn shí", "On time / punctual", 3, 3, true, "请大家按时完成作业。", "Qǐng dàjiā ànshí wánchéng zuòyè.", "Please finish the homework on time."),
        VocabWord("w306", "效率", "xiào lǜ", "Efficiency", 4, 3, true, "提前规划能大大提高效率。", "Tíqián guīhuà néng dàdà tígāo xiàolǜ.", "Planning ahead greatly improves efficiency."),
        VocabWord("w307", "循序渐进", "xún xù jiàn jìn", "Step-by-step progress", 7, 3, false, "学语言必须循序渐进。", "Xué yǔyán bìxū xúnxù jiànjìn.", "Learning a language must proceed step by step."),

        // Topic 4: Family & People
        VocabWord("w401", "妈妈", "mā ma", "Mom / Mother", 1, 4, true, "妈妈做的菜最好吃。", "Māma zuò de cài zuì hǎochī.", "Mom's cooking is the most delicious."),
        VocabWord("w402", "爸爸", "bà ba", "Dad / Father", 1, 4, true, "爸爸每天都去公园散步。", "Bàba měitiān dōu qù gōngyuán sànbù.", "Dad walks in the park every day."),
        VocabWord("w403", "朋友", "péng you", "Friend", 1, 4, true, "他是我的好朋友。", "Tā shì wǒ de hǎo péngyou.", "He is my good friend."),
        VocabWord("w404", "老师", "lǎo shī", "Teacher", 1, 4, true, "熊猫老师教得很有趣。", "Xióngmāo lǎoshī jiāo de hěn yǒuqù.", "Panda Laoshi teaches in a very fun way."),
        VocabWord("w405", "邻居", "lín jū", "Neighbor", 3, 4, true, "我们的新邻居非常热情。", "Wǒmen de xīn línjū fēicháng rèqíng.", "Our new neighbors are very warm."),
        VocabWord("w406", "沟通", "gōu tōng", "Communicate", 4, 4, true, "家人之间要多沟通。", "Jiārén zhījiān yào duō gōutōng.", "Family members should communicate often."),
        VocabWord("w407", "共识", "gòng shí", "Consensus", 6, 4, true, "双方终于达成了共识。", "Shuāngfāng zhōngyú dáchéng le gòngshí.", "Both sides finally reached consensus."),

        // Topic 5: Food & Restaurant
        VocabWord("w501", "米饭", "mǐ fàn", "Cooked rice", 1, 5, true, "我中午吃了一碗米饭。", "Wǒ zhōngwǔ chī le yì wǎn mǐfàn.", "I ate a bowl of rice at noon."),
        VocabWord("w502", "水", "shuǐ", "Water", 1, 5, true, "运动后要多喝水。", "Yùndòng hòu yào duō hē shuǐ.", "Drink plenty of water after exercise."),
        VocabWord("w503", "茶", "chá", "Tea", 1, 5, true, "中国绿茶清香好喝。", "Zhōngguó lǜchá qīngxiāng hǎohē.", "Chinese green tea is fragrant and delicious."),
        VocabWord("w504", "点菜", "diǎn cài", "Order food", 2, 5, true, "服务员，我们要点菜。", "Fúwùyuán, wǒmen yào diǎncài.", "Waiter, we would like to order."),
        VocabWord("w505", "豆浆", "dòu jiāng", "Soy milk (HSK 3.0)", 3, 5, true, "早上我喝了一杯热豆浆。", "Zǎoshang wǒ hē le yì bēi rè dòujiāng.", "I drank a cup of hot soy milk this morning."),
        VocabWord("w506", "外卖", "wài mài", "Food delivery (HSK 3.0)", 3, 5, true, "下雨天大家喜欢点外卖。", "Xiàyǔ tiān dàjiā xǐhuan diǎn wàimài.", "On rainy days people love ordering takeout."),
        VocabWord("w507", "营养", "yíng yǎng", "Nutrition", 4, 5, true, "早餐要吃得有营养。", "Zǎocān yào chī de yǒu yíngyǎng.", "Breakfast should be nutritious."),
        VocabWord("w508", "品尝", "pǐn cháng", "Taste / savor", 5, 5, true, "欢迎品尝本地特色小吃。", "Huānyíng pǐncháng běndì tèsè xiǎochī.", "Welcome to taste local specialty snacks."),

        // Topic 6: Daily Actions & Verbs
        VocabWord("w601", "看", "kàn", "Look / watch / read", 1, 6, true, "我喜欢看中文电影。", "Wǒ xǐhuan kàn Zhōngwén diànyǐng.", "I like watching Chinese movies."),
        VocabWord("w602", "学习", "xué xí", "Study / learn", 1, 6, true, "我每天学习二十分钟中文。", "Wǒ měitiān xuéxí èrshí fēnzhōng Zhōngwén.", "I study Chinese for 20 minutes daily."),
        VocabWord("w603", "休息", "xiū xi", "Rest", 2, 6, true, "累了就休息一会儿吧。", "Lèi le jiù xiūxi yíhuìr ba.", "Rest a bit if you are tired."),
        VocabWord("w604", "准备", "zhǔn bèi", "Prepare / be ready", 2, 6, true, "我已经准备好考试了。", "Wǒ yǐjīng zhǔnbèi hǎo kǎoshì le.", "I am already ready for the exam."),
        VocabWord("w605", "坚持", "jiān chí", "Persist / keep up", 4, 6, true, "只要坚持，一定能学好。", "Zhǐyào jiānchí, yídìng néng xuéhǎo.", "As long as you persist, you will master it."),
        VocabWord("w606", "克服", "kè fú", "Overcome", 5, 6, true, "他勇敢地克服了困难。", "Tā yǒnggǎn de kèfú le kùnnan.", "He bravely overcame the difficulty."),
        VocabWord("w607", "潜移默化", "qián yí mò huà", "Subtle gradual influence", 8, 6, false, "阅读对思维有潜移默化的影响。", "Yuèdú duì sīwéi yǒu qiányímòhuà de yǐngxiǎng.", "Reading exerts a subtle, transformative influence on thinking."),

        // Topic 7: Places & Directions
        VocabWord("w701", "学校", "xué xiào", "School", 1, 7, true, "学校离我家很近。", "Xuéxiào lí wǒ jiā hěn jìn.", "The school is very close to my home."),
        VocabWord("w702", "饭馆", "fàn guǎn", "Restaurant", 1, 7, true, "前面有一家四川饭馆。", "Qiánmiàn yǒu yì jiā Sìchuān fànguǎn.", "There is a Sichuan restaurant ahead."),
        VocabWord("w703", "前面", "qián miàn", "In front / ahead", 1, 7, true, "一直往前走就到了。", "Yìzhí wǎng qián zǒu jiù dào le.", "Walk straight ahead and you'll arrive."),
        VocabWord("w704", "图书馆", "tú shū guǎn", "Library", 2, 7, true, "图书馆里非常安静。", "Túshūguǎn lǐ fēicháng ānjìng.", "It is very quiet inside the library."),
        VocabWord("w705", "附近", "fù jìn", "Nearby / vicinity", 3, 7, true, "这附近有地铁站吗？", "Zhè fùjìn yǒu dìtiězhàn ma?", "Is there a subway station nearby?"),
        VocabWord("w706", "坐标", "zuò biāo", "Coordinates / location", 6, 7, true, "请把具体坐标发给我。", "Qǐng bǎ jùtǐ zuòbiāo fā gěi wǒ.", "Please send me the exact coordinates."),

        // Topic 8: Travel & Transport
        VocabWord("w801", "地铁", "dì tiě", "Subway / metro", 2, 8, true, "坐地铁上班又快又方便。", "Zuò dìtiě shàngbān yòu kuài yòu fāngbiàn.", "Taking the subway to work is fast and convenient."),
        VocabWord("w802", "机场", "jī chǎng", "Airport", 2, 8, true, "我们提前两小时到机场。", "Wǒmen tíqián liǎng xiǎoshí dào jīchǎng.", "We arrive at the airport two hours early."),
        VocabWord("w803", "高铁", "gāo tiě", "High-speed rail (HSK 3.0)", 3, 8, true, "坐高铁去上海只要四小时。", "Zuò gāotiě qù Shànghǎi zhǐ yào sì xiǎoshí.", "High-speed rail to Shanghai takes only four hours."),
        VocabWord("w804", "护照", "hù zhào", "Passport", 3, 8, true, "出国旅行别忘了带护照。", "Chūguó lǚxíng bié wàng le dài hùzhào.", "Don't forget your passport when traveling abroad."),
        VocabWord("w805", "航班", "háng bān", "Flight", 4, 8, true, "今天的航班准点起飞。", "Jīntiān de hángbān zhǔndiǎn qǐfēi.", "Today's flight departs on time."),
        VocabWord("w806", "枢纽", "shū niǔ", "Transport hub", 7, 8, false, "这座城市是重要的交通枢纽。", "Zhè zuò chéngshì shì zhòngyào de jiāotōng shūniǔ.", "This city is a vital transportation hub."),

        // Topic 9: Study & Work
        VocabWord("w901", "电脑", "diàn nǎo", "Computer", 1, 9, true, "我用电脑写报告。", "Wǒ yòng diànnǎo xiě bàogào.", "I write reports on my computer."),
        VocabWord("w902", "考试", "kǎo shì", "Exam / test", 2, 9, true, "下个月我要参加HSK考试。", "Xià gè yuè wǒ yào cānjiā HSK kǎoshì.", "Next month I will take the HSK exam."),
        VocabWord("w903", "经验", "jīng yàn", "Experience", 4, 9, true, "她有丰富的教学经验。", "Tā yǒu fēngfù de jiàoxué jīngyàn.", "She has rich teaching experience."),
        VocabWord("w904", "合作", "hé zuò", "Cooperate / collaborate", 4, 9, true, "期待与您的团队合作。", "Qīdài yǔ nín de tuánduì hézuò.", "Looking forward to collaborating with your team."),
        VocabWord("w905", "创新", "chuàng xīn", "Innovation", 5, 9, true, "科技创新推动社会发展。", "Kējì chuàngxīn tuīdòng shèhuì fāzhǎn.", "Tech innovation drives social progress."),
        VocabWord("w906", "人工智能", "rén gōng zhì néng", "Artificial Intelligence (HSK 3.0)", 6, 9, false, "人工智能正在改变学习方式。", "Réngōng zhìnéng zhèngzài gǎibiàn xuéxí fāngshì.", "AI is transforming how we learn."),

        // Topic 10: Shopping & Modern Tech (HSK 3.0 Showcase)
        VocabWord("w1001", "手机", "shǒu jī", "Mobile phone", 1, 10, true, "我的手机快没电了。", "Wǒ de shǒujī kuài méi diàn le.", "My phone is almost out of battery."),
        VocabWord("w1002", "扫码", "sǎo mǎ", "Scan QR code (HSK 3.0)", 2, 10, true, "请扫码付款。", "Qǐng sǎomǎ fùkuǎn.", "Please scan the QR code to pay."),
        VocabWord("w1003", "便宜", "pián yi", "Inexpensive / cheap", 2, 10, true, "这件衣服既好看又便宜。", "Zhè jiàn yīfu jì hǎokàn yòu piányi.", "This piece of clothing is both nice and affordable."),
        VocabWord("w1004", "充值", "chōng zhí", "Top up / recharge (HSK 3.0)", 3, 10, true, "交通卡需要充值了。", "Jiāotōngkǎ xūyào chōngzhí le.", "The transit card needs to be topped up."),
        VocabWord("w1005", "电商", "diàn shāng", "E-commerce (HSK 3.0)", 3, 10, true, "电商让购物更加便利。", "Diànshāng ràng gòuwù gèngjiā biànlì.", "E-commerce makes shopping much more convenient."),
        VocabWord("w1006", "直播", "zhí bō", "Livestream (HSK 3.0)", 4, 10, true, "很多品牌通过直播介绍产品。", "Hěn duō pǐnpái tōngguò zhíbō jièshào chǎnpǐn.", "Many brands introduce products via livestream."),
        VocabWord("w1007", "隐私", "yǐn sī", "Privacy", 5, 10, true, "上网要注意保护个人隐私。", "Shàngwǎng yào zhùyì bǎohù gèrén yǐnsī.", "Pay attention to protecting personal privacy online."),
        VocabWord("w1008", "算法", "suàn fǎ", "Algorithm", 7, 10, false, "间隔重复算法能优化记忆曲线。", "Jiàngé chóngfù suànfǎ néng yōuhuà jìyì qūxiàn.", "Spaced repetition algorithms optimize memory curves."),

        // Topic 11: Home & Everyday Objects
        VocabWord("w1101", "桌子", "zhuō zi", "Table / desk", 1, 11, true, "书在桌子上。", "Shū zài zhuōzi shàng.", "The book is on the desk."),
        VocabWord("w1102", "椅子", "yǐ zi", "Chair", 1, 11, true, "请坐在这把椅子上。", "Qǐng zuò zài zhè bǎ yǐzi shàng.", "Please sit on this chair."),
        VocabWord("w1103", "房间", "fáng jiān", "Room", 2, 11, true, "这个房间光线很充足。", "Zhège fángjiān guāngxiàn hěn chōngzú.", "This room has plenty of natural light."),
        VocabWord("w1104", "空调", "kōng tiáo", "Air conditioner", 3, 11, true, "夏天开空调要节约用电。", "Xiàtiān kāi kōngtiáo yào jiéyuē yòngdiàn.", "Save electricity when running AC in summer."),
        VocabWord("w1105", "装修", "zhuāng xiū", "Renovate / decorate", 5, 11, true, "他们刚把新房装修好。", "Tāmen gāng bǎ xīnfáng zhuāngxiū hǎo.", "They just finished renovating the new house."),

        // Topic 12: Weather & Nature
        VocabWord("w1201", "天气", "tiān qì", "Weather", 1, 12, true, "北京秋天的天气最美。", "Běijīng qiūtiān de tiānqì zuì měi.", "Beijing's autumn weather is the prettiest."),
        VocabWord("w1202", "下雨", "xià yǔ", "Rain", 1, 12, true, "外面正在下大雨，带把伞吧。", "Wàimiàn zhèngzài xià dàyǔ, dài bǎ sǎn ba.", "It is raining heavily outside; take an umbrella."),
        VocabWord("w1203", "森林", "sēn lín", "Forest", 3, 12, true, "保护森林就是保护地球。", "Bǎohù sēnlín jiùshì bǎohù dìqiú.", "Protecting forests means protecting the Earth."),
        VocabWord("w1204", "环境", "huán jìng", "Environment", 3, 12, true, "大家都应该爱护环境。", "Dàjiā dōu yīnggāi àihù huánjìng.", "Everyone should care for the environment."),
        VocabWord("w1205", "碳中和", "tàn zhōng hé", "Carbon neutrality (HSK 3.0)", 7, 12, false, "实现碳中和需要全球协作。", "Shíxiàn tànzhōnghé xūyào quánqiú xiézuò.", "Achieving carbon neutrality requires global collaboration."),

        // Topic 13: Adjectives & Feelings
        VocabWord("w1301", "高兴", "gāo xìng", "Happy / glad", 1, 13, true, "听到这个消息我真高兴。", "Tīngdào zhège xiāoxi wǒ zhēn gāoxìng.", "I am truly glad to hear this news."),
        VocabWord("w1302", "漂亮", "piào liang", "Beautiful / pretty", 1, 13, true, "你的汉字写得真漂亮！", "Nǐ de Hànzì xiě de zhēn piàoliang!", "Your Chinese characters are written so beautifully!"),
        VocabWord("w1303", "方便", "fāng biàn", "Convenient", 2, 13, true, "移动支付非常方便。", "Yídòng zhīfù fēicháng fāngbiàn.", "Mobile payment is very convenient."),
        VocabWord("w1304", "耐心", "nài xīn", "Patient / patience", 4, 13, true, "老师总是非常耐心地解答。", "Lǎoshī zǒngshì fēicháng nàixīn de jiědá.", "The teacher always answers patiently."),
        VocabWord("w1305", "从容", "cóng róng", "Calm / composed", 6, 13, true, "面对挑战她总是从容不迫。", "Miànduì tiǎozhàn tā zǒngshì cóngróng bú pò.", "Facing challenges she stays calm and unhurried."),

        // Topic 14: Grammar & Connectors
        VocabWord("w1401", "因为", "yīn wèi", "Because", 2, 14, true, "因为下雨，所以我没去跑步。", "Yīnwèi xiàyǔ, suǒyǐ wǒ méi qù pǎobù.", "Because it rained, I didn't go running."),
        VocabWord("w1402", "所以", "suǒ yǐ", "Therefore / so", 2, 14, true, "我喜欢中文，所以天天练习。", "Wǒ xǐhuan Zhōngwén, suǒyǐ tiāntiān liànxí.", "I love Chinese, so I practice daily."),
        VocabWord("w1403", "虽然", "suī rán", "Although / though", 2, 14, true, "虽然汉字难，但是很有趣。", "Suīrán Hànzì nán, dànshì hěn yǒuqù.", "Although Hanzi are hard, they are fascinating."),
        VocabWord("w1404", "但是", "dàn shì", "But / however", 2, 14, true, "我想去，但是今天太忙了。", "Wǒ xiǎng qù, dànshì jīntiān tài máng le.", "I want to go, but today is too busy."),
        VocabWord("w1405", "如果", "rú guǒ", "If", 3, 14, true, "如果明天晴天，我们就去爬山。", "Rúguǒ míngtiān qíngtiān, wǒmen jiù qù páshān.", "If tomorrow is sunny, we'll go hiking."),
        VocabWord("w1406", "不但", "bú dàn", "Not only", 3, 14, true, "她不但会说中文，而且会写书法。", "Tā búdàn huì shuō Zhōngwén, érqiě huì xiě shūfǎ.", "She not only speaks Chinese, but also does calligraphy."),
        VocabWord("w1407", "尽管", "jǐn guǎn", "Even though / despite", 4, 14, true, "尽管任务艰巨，团队还是按时完成了。", "Jǐnguǎn rènwu jiānjù, tuánduì háishì ànshí wánchéng le.", "Even though the task was tough, the team finished on time."),
        VocabWord("w1408", "与其", "yǔ qí", "Rather than", 6, 14, true, "与其死记硬背，不如理解字根。", "Yǔqí sǐjì yìngbèi, bùrú lǐjiě zìgēn.", "Rather than rote memorization, understand character radicals.")
    )

    val radicals: List<RadicalItem> = listOf(
        RadicalItem("氵", "shuǐ", "Three-Dot Water", "Liquid, rivers, washing", "Pictograph of three flowing droplets from 水.", listOf("洗", "海", "河", "汤")),
        RadicalItem("扌", "shǒu", "Hand Radical", "Actions done by hand", "Side form of 手 (hand); powers action verbs.", listOf("打", "扫", "推", "拉")),
        RadicalItem("亻", "rén", "Standing Person", "People, roles, human states", "Side form of 人 (person).", listOf("你", "他", "休", "们")),
        RadicalItem("木", "mù", "Tree / Wood", "Trees, furniture, plants", "Trunk, branches, and spreading roots.", listOf("林", "森", "桌", "椅")),
        RadicalItem("日", "rì", "Sun / Day", "Time, daylight, brightness", "Ancient circle of the sun with a center dot.", listOf("明", "时", "早", "晴")),
        RadicalItem("月", "yuè", "Moon / Flesh", "Lunar cycles or body parts", "Crescent moon shape; also represents 肉 (flesh).", listOf("明", "朋", "期", "脚")),
        RadicalItem("目", "mù", "Eye Radical", "Seeing, eyes, watching", "Human eye rotated vertically.", listOf("看", "眼", "睛", "睡")),
        RadicalItem("口", "kǒu", "Mouth Radical", "Speaking, eating, sounds", "Open mouth square outline.", listOf("吃", "喝", "问", "吗")),
        RadicalItem("心", "xīn", "Heart / Mind", "Emotions, thoughts, attitude", "Depicts the human heart chambers (also 忄).", listOf("想", "念", "情", "忙")),
        RadicalItem("辶", "chuò", "Walking Path", "Motion, distance, travel", "Foot stepping forward along a road.", listOf("过", "运", "这", "近")),
        RadicalItem("讠", "yán", "Speech Radical", "Language, speaking, asking", "Simplified form of 言 (words/speech).", listOf("请", "说", "谢", "认")),
        RadicalItem("饣", "shí", "Food Radical", "Meals, eating, hunger", "Simplified form of 食 (food).", listOf("饭", "馆", "饿", "饮")),
        RadicalItem("女", "nǚ", "Woman Radical", "Family, kindness, female", "Kneeling figure in classical oracle bone script.", listOf("好", "妈", "姐", "她")),
        RadicalItem("火", "huǒ", "Fire Radical", "Heat, cooking, light", "Rising flames (also four dots 灬 at bottom).", listOf("热", "炒", "灯", "烧")),
        RadicalItem("土", "tǔ", "Earth / Soil", "Land, places, ground", "Mound of earth sitting on the ground line.", listOf("地", "城", "坐", "场")),
        RadicalItem("艹", "cǎo", "Grass Radical", "Plants, tea, flowers, herbs", "Two sprouts growing up side by side.", listOf("茶", "菜", "花", "草")),
        RadicalItem("钅", "jīn", "Metal / Gold", "Metals, tools, currency", "Simplified form of 金 (gold/metal).", listOf("钱", "银", "铁", "钟")),
        RadicalItem("门", "mén", "Gate / Door", "Doorways, intervals, asking", "Double swinging doors of a courtyard.", listOf("问", "间", "闪", "闭"))
    )

    val learningUnits: List<LearningUnit> = listOf(
        LearningUnit(
            id = "unit_1",
            order = 1,
            hskBadge = "HSK 1",
            title = "Pinyin, Tones & Pictographs",
            subtitle = "Build intuition with compound visual characters",
            keyHanzi = listOf("休", "森", "明", "看"),
            xpReward = 40,
            questions = listOf(
                LessonQuestion(
                    id = "u1_q1",
                    kind = "RADICAL",
                    prompt = "Person (亻) leaning against a Tree (木) forms which character meaning 'Rest'?",
                    hanziHighlight = "亻 + 木 = ?",
                    pinyinHint = "xiū",
                    audioText = "休息",
                    options = listOf("休 (xiū)", "林 (lín)", "明 (míng)", "看 (kàn)"),
                    correctIndex = 0,
                    explanation = "休 (xiū) shows a person resting beside a tree—the origin of 休息 (to rest)!"
                ),
                LessonQuestion(
                    id = "u1_q2",
                    kind = "RADICAL",
                    prompt = "Sun (日) + Moon (月) combine to create which bright HSK 1 character?",
                    hanziHighlight = "日 + 月 = ?",
                    pinyinHint = "míng",
                    audioText = "明天",
                    options = listOf("晴 (qíng)", "明 (míng)", "早 (zǎo)", "时 (shí)"),
                    correctIndex = 1,
                    explanation = "明 (míng) combines sun and moon to mean 'bright', used in 明天 (tomorrow)."
                ),
                LessonQuestion(
                    id = "u1_q3",
                    kind = "RADICAL",
                    prompt = "A Hand (手) shading an Eye (目) creates which verb?",
                    hanziHighlight = "手 + 目 = ?",
                    pinyinHint = "kàn",
                    audioText = "看书",
                    options = listOf("吃 (chī)", "听 (tīng)", "看 (kàn)", "说 (shuō)"),
                    correctIndex = 2,
                    explanation = "看 (kàn, to look/watch/read) visualizes holding a hand above your eyes to look far."
                ),
                LessonQuestion(
                    id = "u1_q4",
                    kind = "TONE",
                    prompt = "In 你好 (nǐ hǎo), what happens due to the 3rd-tone Sandhi rule?",
                    hanziHighlight = "你好",
                    pinyinHint = "nǐ + hǎo",
                    audioText = "你好",
                    options = listOf(
                        "Pronounced ní hǎo (Tone 2 + Tone 3)",
                        "Pronounced nī hāo (Tone 1 + Tone 1)",
                        "Pronounced nì hào (Tone 4 + Tone 4)",
                        "No tone change occurs"
                    ),
                    correctIndex = 0,
                    explanation = "When two 3rd tones meet, the first shifts smoothly to a rising 2nd tone: ní hǎo."
                )
            )
        ),
        LearningUnit(
            id = "unit_2",
            order = 2,
            hskBadge = "HSK 1",
            title = "Water (氵) & Hand (扌) Power Words",
            subtitle = "Decode meaning instantly from left-side semantic radicals",
            keyHanzi = listOf("洗", "海", "打", "扫"),
            xpReward = 45,
            questions = listOf(
                LessonQuestion(
                    id = "u2_q1",
                    kind = "RADICAL",
                    prompt = "Which radical signals water or liquid in 洗 (wash), 河 (river), and 海 (ocean)?",
                    hanziHighlight = "洗 · 河 · 海",
                    pinyinHint = "xǐ · hé · hǎi",
                    audioText = "洗手",
                    options = listOf("扌 (Hand)", "氵 (Water)", "亻 (Person)", "讠 (Speech)"),
                    correctIndex = 1,
                    explanation = "氵 (Three-Dot Water) is a compact form of 水 and appears on hundreds of liquid-related Hanzi."
                ),
                LessonQuestion(
                    id = "u2_q2",
                    kind = "TRANSLATE",
                    prompt = "Select the meaning of 请喝水 (Qǐng hē shuǐ).",
                    hanziHighlight = "请喝水",
                    pinyinHint = "Qǐng hē shuǐ",
                    audioText = "请喝水",
                    options = listOf("Please eat rice", "Please drink water", "Please sit down", "Please look here"),
                    correctIndex = 1,
                    explanation = "请 (please) + 喝 (drink, mouth radical 口) + 水 (water)."
                ),
                LessonQuestion(
                    id = "u2_q3",
                    kind = "RADICAL",
                    prompt = "扫码 (sǎo mǎ, scan QR code) starts with 扌 in 扫. Why?",
                    hanziHighlight = "扫 = 扌 + 彐",
                    pinyinHint = "sǎo",
                    audioText = "扫码",
                    options = listOf(
                        "It involves a sweeping hand motion",
                        "It relates to water",
                        "It relates to food",
                        "It relates to trees"
                    ),
                    correctIndex = 0,
                    explanation = "扫 originally meant 'to sweep with a hand', now extended to scanning a QR code with your phone!"
                )
            )
        ),
        LearningUnit(
            id = "unit_3",
            order = 3,
            hskBadge = "HSK 2",
            title = "HSK 2 Grammatical Connectors",
            subtitle = "Link cause-and-effect and contrast naturally",
            keyHanzi = listOf("因为", "所以", "虽然", "但是"),
            xpReward = 55,
            questions = listOf(
                LessonQuestion(
                    id = "u3_q1",
                    kind = "GRAMMAR",
                    prompt = "Complete: ＿＿＿ 今天下雨，＿＿＿ 我们不出去跑步。(Because it rains today, so we don't run.)",
                    hanziHighlight = "___ 今天下雨，___ 我们不出去跑步。",
                    pinyinHint = "Cause & Effect",
                    audioText = "因为今天下雨，所以我们不出去跑步。",
                    options = listOf("因为 … 所以 …", "虽然 … 但是 …", "如果 … 就 …", "不但 … 而且 …"),
                    correctIndex = 0,
                    explanation = "因为...所以... expresses cause and effect. Unlike English, Chinese pairs both words together!"
                ),
                LessonQuestion(
                    id = "u3_q2",
                    kind = "GRAMMAR",
                    prompt = "Complete: ＿＿＿ 汉字很难，＿＿＿ 很有趣。(Although Hanzi is hard, it is interesting.)",
                    hanziHighlight = "___ 汉字很难，___ 很有趣。",
                    pinyinHint = "Contrast pair",
                    audioText = "虽然汉字很难，但是很有趣。",
                    options = listOf("因为 … 所以 …", "虽然 … 但是 …", "只要 … 就 …", "既 … 又 …"),
                    correctIndex = 1,
                    explanation = "虽然 (although) always pairs cleanly with 但是 (but) in Mandarin sentences."
                ),
                LessonQuestion(
                    id = "u3_q3",
                    kind = "TRANSLATE",
                    prompt = "What does 周末你打算去哪儿？ mean?",
                    hanziHighlight = "周末你打算去哪儿？",
                    pinyinHint = "Zhōumò nǐ dǎsuàn qù nǎr?",
                    audioText = "周末你打算去哪儿？",
                    options = listOf(
                        "Where did you eat yesterday?",
                        "Where do you plan to go this weekend?",
                        "What time do you wake up?",
                        "Who are you going with?"
                    ),
                    correctIndex = 1,
                    explanation = "周末 (weekend) + 打算 (plan to) + 去哪儿 (go where)."
                )
            )
        ),
        LearningUnit(
            id = "unit_4",
            order = 4,
            hskBadge = "HSK 3",
            title = "HSK 3.0 Modern Life & Tech",
            subtitle = "2026 HSK 3.0 additions: QR pay, high-speed rail, delivery",
            keyHanzi = listOf("扫码", "豆浆", "充值", "高铁"),
            xpReward = 65,
            questions = listOf(
                LessonQuestion(
                    id = "u4_q1",
                    kind = "TRANSLATE",
                    prompt = "Choose the natural English meaning of 请扫码付款。",
                    hanziHighlight = "请扫码付款",
                    pinyinHint = "Qǐng sǎomǎ fùkuǎn",
                    audioText = "请扫码付款",
                    options = listOf(
                        "Please scan the QR code to pay",
                        "Please recharge your battery",
                        "Please order soy milk",
                        "Please show your passport"
                    ),
                    correctIndex = 0,
                    explanation = "扫码 (scan QR code) + 付款 (make a payment) is one of the most frequent everyday phrases in China."
                ),
                LessonQuestion(
                    id = "u4_q2",
                    kind = "GRAMMAR",
                    prompt = "Complete: ＿＿＿ 明天晴天，我们 ＿＿＿ 去坐高铁旅行。",
                    hanziHighlight = "___ 明天晴天，我们 ___ 去坐高铁旅行。",
                    pinyinHint = "Conditional: If... then...",
                    audioText = "如果明天晴天，我们就去坐高铁旅行。",
                    options = listOf("因为 … 所以 …", "如果 … 就 …", "虽然 … 但是 …", "与其 … 不如 …"),
                    correctIndex = 1,
                    explanation = "如果...就... expresses conditional 'If..., then...'."
                ),
                LessonQuestion(
                    id = "u4_q3",
                    kind = "LISTEN",
                    prompt = "Listen and pick what the speaker ordered for breakfast.",
                    hanziHighlight = "🎧 Tap Audio Above",
                    pinyinHint = "HSK 3.0 Modern Breakfast",
                    audioText = "早上我点了一杯热豆浆。",
                    options = listOf("Hot soy milk (热豆浆)", "Iced coffee (冰咖啡)", "Fried noodles (炒面)", "Green tea (绿茶)"),
                    correctIndex = 0,
                    explanation = "豆浆 (dòujiāng, soy milk) was newly added in HSK 3.0 Level 3!"
                )
            )
        ),
        LearningUnit(
            id = "unit_5",
            order = 5,
            hskBadge = "HSK 4",
            title = "Workplace, Efficiency & Nuance",
            subtitle = "Express collaboration, patience, and compound logic",
            keyHanzi = listOf("效率", "沟通", "坚持", "尽管"),
            xpReward = 75,
            questions = listOf(
                LessonQuestion(
                    id = "u5_q1",
                    kind = "GRAMMAR",
                    prompt = "Which sentence properly uses 只要…就… (As long as... then...)?",
                    hanziHighlight = "只要 … 就 …",
                    pinyinHint = "Sufficient condition",
                    audioText = "只要坚持，一定能学好中文。",
                    options = listOf(
                        "只要坚持，一定能学好中文。",
                        "只要下雨，所以我回家。",
                        "只要他很高，但是跑得慢。",
                        "只要吃饭，与其喝茶。"
                    ),
                    correctIndex = 0,
                    explanation = "只要 (as long as) pairs with 就 / 一定能 to express a sufficient condition."
                ),
                LessonQuestion(
                    id = "u5_q2",
                    kind = "TRANSLATE",
                    prompt = "What does 提前规划能大大提高效率 mean?",
                    hanziHighlight = "提前规划能大大提高效率",
                    pinyinHint = "Tíqián guīhuà néng dàdà tígāo xiàolǜ",
                    audioText = "提前规划能大大提高效率",
                    options = listOf(
                        "Planning ahead greatly improves efficiency",
                        "Working late always causes stress",
                        "Arriving early avoids rain",
                        "Teamwork reduces daily expenses"
                    ),
                    correctIndex = 0,
                    explanation = "提前规划 (plan ahead) + 大大提高 (greatly improve) + 效率 (efficiency)."
                )
            )
        ),
        LearningUnit(
            id = "unit_6",
            order = 6,
            hskBadge = "HSK 5–6",
            title = "Innovation, AI & Formal Discourse",
            subtitle = "Master advanced comparisons and modern technology terms",
            keyHanzi = listOf("创新", "隐私", "人工智能", "与其"),
            xpReward = 90,
            questions = listOf(
                LessonQuestion(
                    id = "u6_q1",
                    kind = "GRAMMAR",
                    prompt = "Complete the HSK 6 preference pattern: ＿＿＿ 死记硬背，＿＿＿ 理解字根。",
                    hanziHighlight = "___ 死记硬背，___ 理解字根。",
                    pinyinHint = "Rather than A, better B",
                    audioText = "与其死记硬背，不如理解字根。",
                    options = listOf("与其 … 不如 …", "因为 … 所以 …", "虽然 … 但是 …", "如果 … 就 …"),
                    correctIndex = 0,
                    explanation = "与其 A，不如 B means 'Rather than doing A, it is better to do B'—classic HSK 6 elegance!"
                ),
                LessonQuestion(
                    id = "u6_q2",
                    kind = "TRANSLATE",
                    prompt = "Translate: 双方终于达成了共识。",
                    hanziHighlight = "双方终于达成了共识",
                    pinyinHint = "Shuāngfāng zhōngyú dáchéng le gòngshí",
                    audioText = "双方终于达成了共识",
                    options = listOf(
                        "Both sides finally reached consensus",
                        "Both sides canceled the meeting",
                        "Both teams launched an app",
                        "Both companies lowered prices"
                    ),
                    correctIndex = 0,
                    explanation = "达成共识 (dáchéng gòngshí) is a high-frequency formal collocation meaning 'reach consensus'."
                )
            )
        ),
        LearningUnit(
            id = "unit_7",
            order = 7,
            hskBadge = "HSK 7–9",
            title = "Pro Mastery: Idioms & Academic Chinese",
            subtitle = "Chengyu (成语), algorithms, sustainability & literary poise",
            keyHanzi = listOf("循序渐进", "潜移默化", "碳中和", "算法"),
            xpReward = 120,
            questions = listOf(
                LessonQuestion(
                    id = "u7_q1",
                    kind = "TRANSLATE",
                    prompt = "Which Chengyu means 'making steady step-by-step progress'?",
                    hanziHighlight = "HSK 7–9 成语",
                    pinyinHint = "Idiomatic Mastery",
                    audioText = "学习语言必须循序渐进。",
                    options = listOf("循序渐进 (xúnxù jiànjìn)", "久仰大名 (jiǔyǎng dàmíng)", "从容不迫 (cóngróng búpò)", "潜移默化 (qiányí mòhuà)"),
                    correctIndex = 0,
                    explanation = "循序渐进 literally means 'following the sequence and advancing gradually'."
                ),
                LessonQuestion(
                    id = "u7_q2",
                    kind = "TRANSLATE",
                    prompt = "What does 间隔重复算法能优化记忆曲线 mean?",
                    hanziHighlight = "间隔重复算法能优化记忆曲线",
                    pinyinHint = "Jiàngé chóngfù suànfǎ néng yōuhuà jìyì qūxiàn",
                    audioText = "间隔重复算法能优化记忆曲线",
                    options = listOf(
                        "Spaced repetition algorithms optimize memory curves",
                        "AI translation replaces daily reading",
                        "Carbon neutrality requires clean energy",
                        "High-speed rail connects major hubs"
                    ),
                    correctIndex = 0,
                    explanation = "间隔重复 (spaced repetition) + 算法 (algorithm) + 优化 (optimize) + 记忆曲线 (memory curve)."
                )
            )
        )
    )

    val tileSentences: List<TileSentenceItem> = listOf(
        TileSentenceItem(
            id = "ts_1", hskLevel = 1, topicId = 5,
            promptEn = "I will go to the restaurant to eat at 12 o'clock.",
            tiles = listOf("我" to "wǒ", "十二点" to "shí'èr diǎn", "去" to "qù", "饭馆" to "fànguǎn", "吃饭" to "chīfàn"),
            correctHanzi = "我十二点去饭馆吃饭",
            grammarNote = "Subject (我) + Time (十二点) + Verb (去) + Place (饭馆) + Purpose (吃饭)."
        ),
        TileSentenceItem(
            id = "ts_2", hskLevel = 2, topicId = 10,
            promptEn = "Please scan the QR code to pay.",
            tiles = listOf("请" to "qǐng", "扫码" to "sǎomǎ", "付款" to "fùkuǎn"),
            correctHanzi = "请扫码付款",
            grammarNote = "Polite imperative: 请 + Serial Verb (扫码 + 付款)."
        ),
        TileSentenceItem(
            id = "ts_3", hskLevel = 1, topicId = 6,
            promptEn = "Tomorrow morning I will study Chinese at the library.",
            tiles = listOf("我" to "wǒ", "明天早上" to "míngtiān zǎoshang", "在图书馆" to "zài túshūguǎn", "学习" to "xuéxí", "中文" to "Zhōngwén"),
            correctHanzi = "我明天早上在图书馆学习中文",
            grammarNote = "Classic SVO order: Subject + Time + 在Place + Verb + Object!"
        ),
        TileSentenceItem(
            id = "ts_4", hskLevel = 2, topicId = 14,
            promptEn = "Because it rained, so I didn't go running.",
            tiles = listOf("因为" to "yīnwèi", "下雨" to "xiàyǔ", "所以" to "suǒyǐ", "我" to "wǒ", "没去跑步" to "méi qù pǎobù"),
            correctHanzi = "因为下雨所以我没去跑步",
            grammarNote = "Cause-effect pair: 因为 [reason] + 所以 [result]."
        ),
        TileSentenceItem(
            id = "ts_5", hskLevel = 3, topicId = 8,
            promptEn = "Taking high-speed rail to Shanghai is very convenient.",
            tiles = listOf("坐高铁" to "zuò gāotiě", "去上海" to "qù Shànghǎi", "非常" to "fēicháng", "方便" to "fāngbiàn"),
            correctHanzi = "坐高铁去上海非常方便",
            grammarNote = "Verb phrase (坐高铁去上海) acts as the Subject before 非常方便."
        ),
        TileSentenceItem(
            id = "ts_6", hskLevel = 3, topicId = 5,
            promptEn = "In the morning I drink a cup of hot soy milk.",
            tiles = listOf("早上" to "zǎoshang", "我" to "wǒ", "喝" to "hē", "一杯" to "yì bēi", "热豆浆" to "rè dòujiāng"),
            correctHanzi = "早上我喝一杯热豆浆",
            grammarNote = "Time (早上) can stand before Subject for gentle emphasis."
        ),
        TileSentenceItem(
            id = "ts_7", hskLevel = 4, topicId = 9,
            promptEn = "As long as you persist, you will definitely learn Chinese well.",
            tiles = listOf("只要" to "zhǐyào", "坚持" to "jiānchí", "就一定能" to "jiù yídìng néng", "学好" to "xuéhǎo", "中文" to "Zhōngwén"),
            correctHanzi = "只要坚持就一定能学好中文",
            grammarNote = "HSK 4 conditional connector: 只要...就..."
        ),
        TileSentenceItem(
            id = "ts_8", hskLevel = 6, topicId = 14,
            promptEn = "Rather than rote memorization, it is better to understand radicals.",
            tiles = listOf("与其" to "yǔqí", "死记硬背" to "sǐjì yìngbèi", "不如" to "bùrú", "理解" to "lǐjiě", "字根" to "zìgēn"),
            correctHanzi = "与其死记硬背不如理解字根",
            grammarNote = "HSK 6 rhetorical comparison: 与其 A，不如 B."
        )
    )

    val sandhiPhrases: List<SandhiPhrase> = listOf(
        SandhiPhrase(
            id = "sp_1", hskLevel = 1, hanzi = "你好",
            ruleTitle = "3 + 3 Sandhi Rule → Tone 2 + Tone 3",
            ruleDetail = "Two consecutive 3rd tones shift the first syllable to Tone 2: nǐ hǎo → ní hǎo.",
            translationEn = "Hello",
            tiles = listOf(
                SandhiToneTile("你", "nǐ", "ní", 2),
                SandhiToneTile("好", "hǎo", "hǎo", 3)
            )
        ),
        SandhiPhrase(
            id = "sp_2", hskLevel = 1, hanzi = "我很好",
            ruleTitle = "3 + 3 + 3 Sandhi → Tone 2 + Tone 2 + Tone 3",
            ruleDetail = "In a three 3rd-tone chain, the first two lift to Tone 2 for effortless flow: wó hén hǎo.",
            translationEn = "I am very good",
            tiles = listOf(
                SandhiToneTile("我", "wǒ", "wó", 2),
                SandhiToneTile("很", "hěn", "hén", 2),
                SandhiToneTile("好", "hǎo", "hǎo", 3)
            )
        ),
        SandhiPhrase(
            id = "sp_3", hskLevel = 1, hanzi = "不对",
            ruleTitle = "不 (bù) + Tone 4 → Tone 2 + Tone 4",
            ruleDetail = "Before another 4th tone, 不 rises to 2nd tone (bú): bú duì.",
            translationEn = "Incorrect / Not right",
            tiles = listOf(
                SandhiToneTile("不", "bù", "bú", 2),
                SandhiToneTile("对", "duì", "duì", 4)
            )
        ),
        SandhiPhrase(
            id = "sp_4", hskLevel = 1, hanzi = "一杯茶",
            ruleTitle = "一 (yī) + Tone 1 → Tone 4 + Tone 1",
            ruleDetail = "Before Tones 1, 2, or 3, 一 drops to 4th tone (yì): yì bēi chá.",
            translationEn = "A cup of tea",
            tiles = listOf(
                SandhiToneTile("一", "yī", "yì", 4),
                SandhiToneTile("杯", "bēi", "bēi", 1),
                SandhiToneTile("茶", "chá", "chá", 2)
            )
        ),
        SandhiPhrase(
            id = "sp_5", hskLevel = 1, hanzi = "一个苹果",
            ruleTitle = "一 (yī) + Tone 4 → Tone 2 + Tone 4",
            ruleDetail = "Before a 4th tone (gè), 一 shifts up to Tone 2 (yí): yí gè píngguǒ.",
            translationEn = "One apple",
            tiles = listOf(
                SandhiToneTile("一", "yī", "yí", 2),
                SandhiToneTile("个", "gè", "ge", 5),
                SandhiToneTile("苹", "píng", "píng", 2),
                SandhiToneTile("果", "guǒ", "guǒ", 3)
            )
        ),
        SandhiPhrase(
            id = "sp_6", hskLevel = 2, hanzi = "妈妈骑马",
            ruleTitle = "Four-Tone Contrast Drill",
            ruleDetail = "Master flat Tone 1 (mā), neutral (ma), rising Tone 2 (qí), and dipping Tone 3 (mǎ).",
            translationEn = "Mom rides a horse",
            tiles = listOf(
                SandhiToneTile("妈", "mā", "mā", 1),
                SandhiToneTile("妈", "ma", "ma", 5),
                SandhiToneTile("骑", "qí", "qí", 2),
                SandhiToneTile("马", "mǎ", "mǎ", 3)
            )
        ),
        SandhiPhrase(
            id = "sp_7", hskLevel = 3, hanzi = "请扫码付款",
            ruleTitle = "3 + 3 Sandhi inside Modern HSK 3.0 Phrase",
            ruleDetail = "扫码 (sǎo mǎ) triggers 3+3 Sandhi → sáo mǎ!",
            translationEn = "Please scan QR code to pay",
            tiles = listOf(
                SandhiToneTile("请", "qǐng", "qǐng", 3),
                SandhiToneTile("扫", "sǎo", "sáo", 2),
                SandhiToneTile("码", "mǎ", "mǎ", 3),
                SandhiToneTile("付", "fù", "fù", 4),
                SandhiToneTile("款", "kuǎn", "kuǎn", 3)
            )
        ),
        SandhiPhrase(
            id = "sp_8", hskLevel = 7, hanzi = "循序渐进",
            ruleTitle = "HSK 7–9 Chengyu Cadence (2-4-4-4)",
            ruleDetail = "Crisp rising Tone 2 opening followed by three decisive falling 4th tones.",
            translationEn = "Advance step by step in order",
            tiles = listOf(
                SandhiToneTile("循", "xún", "xún", 2),
                SandhiToneTile("序", "xù", "xù", 4),
                SandhiToneTile("渐", "jiàn", "jiàn", 4),
                SandhiToneTile("进", "jìn", "jìn", 4)
            )
        )
    )

    val listeningQuestions: List<ListeningQuestion> = listOf(
        ListeningQuestion(
            id = "lq_1", hskLevel = 1, topicId = 5,
            type = ListeningQuestionType.WORD_MATCH,
            audioScript = "请给我一杯热茶。",
            audioPinyin = "Qǐng gěi wǒ yì bēi rè chá.",
            promptText = "What drink did the speaker request?",
            options = listOf("Hot tea (热茶)", "Cold water (冷水)", "Soy milk (豆浆)", "Apple juice (苹果汁)"),
            correctIndex = 0,
            targetWordKey = "w503"
        ),
        ListeningQuestion(
            id = "lq_2", hskLevel = 2, topicId = 10,
            type = ListeningQuestionType.SENTENCE_MEANING,
            audioScript = "您好，这里可以直接扫码付款，非常方便。",
            audioPinyin = "Nín hǎo, zhèlǐ kěyǐ zhíjiē sǎomǎ fùkuǎn, fēicháng fāngbiàn.",
            promptText = "How can the customer pay here?",
            options = listOf("Cash only", "Scan a QR code directly", "Bank wire transfer", "Cheque"),
            correctIndex = 1,
            targetWordKey = "w1002"
        ),
        ListeningQuestion(
            id = "lq_3", hskLevel = 2, topicId = 14,
            type = ListeningQuestionType.MINI_DIALOGUE,
            audioScript = "男：我们今天去公园跑步吧？ 女：因为外面在下大雨，所以我们明天再去吧。",
            audioPinyin = "Nán: Wǒmen jīntiān qù gōngyuán pǎobù ba? Nǚ: Yīnwèi wàimiàn zài xià dàyǔ, suǒyǐ wǒmen míngtiān zài qù ba.",
            promptText = "Why aren't they going running today?",
            options = listOf("The park is closed", "It is raining heavily outside", "She forgot her shoes", "They are too hungry"),
            correctIndex = 1,
            targetWordKey = "w1401"
        ),
        ListeningQuestion(
            id = "lq_4", hskLevel = 3, topicId = 8,
            type = ListeningQuestionType.TRUE_FALSE,
            audioScript = "从北京坐高铁去上海只需要四个多小时，又快又舒适。",
            audioPinyin = "Cóng Běijīng zuò gāotiě qù Shànghǎi zhǐ xūyào sì gè duō xiǎoshí, yòu kuài yòu shūshì.",
            promptText = "Statement: Taking the high-speed train from Beijing to Shanghai takes over 12 hours.",
            options = listOf("True (对)", "False (错)"),
            correctIndex = 1,
            targetWordKey = "w803"
        ),
        ListeningQuestion(
            id = "lq_5", hskLevel = 3, topicId = 5,
            type = ListeningQuestionType.WORD_MATCH,
            audioScript = "每天早上我都会在楼下买一杯热豆浆。",
            audioPinyin = "Měitiān zǎoshang wǒ dōu huì zài lóuxià mǎi yì bēi rè dòujiāng.",
            promptText = "What does the speaker buy every morning?",
            options = listOf("Rice (米饭)", "Hot soy milk (热豆浆)", "Noodles (面条)", "Green tea (绿茶)"),
            correctIndex = 1,
            targetWordKey = "w505"
        ),
        ListeningQuestion(
            id = "lq_6", hskLevel = 4, topicId = 9,
            type = ListeningQuestionType.SENTENCE_MEANING,
            audioScript = "提前做好计划能够大大提高我们的工作效率。",
            audioPinyin = "Tíqián zuòhǎo jìhuà nénggòu dàdà tígāo wǒmen de gōngzuò xiàolǜ.",
            promptText = "What is the main benefit of planning ahead?",
            options = listOf("Higher salary", "Greatly improved work efficiency", "Longer holidays", "Shorter commute"),
            correctIndex = 1,
            targetWordKey = "w306"
        ),
        ListeningQuestion(
            id = "lq_7", hskLevel = 5, topicId = 10,
            type = ListeningQuestionType.MINI_DIALOGUE,
            audioScript = "女：你觉得网上购物需要注意什么？ 男：除了看评价，还要特别注意保护个人隐私。",
            audioPinyin = "Nǚ: Nǐ juéde wǎngshàng gòuwù xūyào zhùyì shénme? Nán: Chúle kàn píngjià, hái yào tèbié zhùyì bǎohù gèrén yǐnsī.",
            promptText = "Besides reviews, what does the man emphasize?",
            options = listOf("Finding free shipping", "Protecting personal privacy", "Buying during livestreams", "Using coupons"),
            correctIndex = 1,
            targetWordKey = "w1007"
        ),
        ListeningQuestion(
            id = "lq_8", hskLevel = 7, topicId = 3,
            type = ListeningQuestionType.TRUE_FALSE,
            audioScript = "掌握一门外语没有捷径，唯有循序渐进，才能厚积薄发。",
            audioPinyin = "Zhǎngwò yì mén wàiyǔ méiyǒu jiéjìng, wéiyǒu xúnxù jiànjìn, cái néng hòujī bófā.",
            promptText = "Statement: The speaker believes mastering a foreign language requires gradual, step-by-step effort.",
            options = listOf("True (对)", "False (错)"),
            correctIndex = 0,
            targetWordKey = "w307"
        )
    )

    val builtInStories: List<GradedStory> = listOf(
        GradedStory(
            id = "story_hsk1",
            titleZh = "在饭馆点菜",
            titleEn = "Ordering Food at a Restaurant",
            hskLevel = 1,
            topicName = "Food & Restaurant",
            sentences = listOf(
                StorySentenceDoc("今天中午，大卫和朋友一起去一家中国饭馆吃饭。", "Jīntiān zhōngwǔ, Dàwèi hé péngyou yìqǐ qù yì jiā Zhōngguó fànguǎn chīfàn.", "Today at noon, David and his friend went to a Chinese restaurant to eat."),
                StorySentenceDoc("服务员笑着说：你好！请问你们想吃什么？", "Fúwùyuán xiàozhe shuō: Nǐ hǎo! Qǐngwèn nǐmen xiǎng chī shénme?", "The waiter smiled and said: Hello! What would you like to eat?"),
                StorySentenceDoc("大卫看了看菜单，点了一碗米饭、一条鱼和两杯热茶。", "Dàwèi kàn le kan càidān, diǎn le yì wǎn mǐfàn, yì tiáo yú hé liǎng bēi rè chá.", "David looked at the menu and ordered a bowl of rice, a fish, and two cups of hot tea."),
                StorySentenceDoc("菜很快上来了，味道非常好吃，大卫高兴极了。", "Cài hěn kuài shànglái le, wèidào fēicháng hǎochī, Dàwèi gāoxìng jí le.", "The food arrived quickly, tasted delicious, and David was thrilled.")
            ),
            quizQuestion = "大卫在饭馆点了什么？ (What did David order?)",
            quizOptions = listOf("米饭、鱼和热茶 (Rice, fish & hot tea)", "面条和咖啡 (Noodles & coffee)", "饺子和牛奶 (Dumplings & milk)"),
            quizCorrectIndex = 0
        ),
        GradedStory(
            id = "story_hsk2",
            titleZh = "第一次扫码买豆浆",
            titleEn = "First Time Scanning QR for Soy Milk",
            hskLevel = 2,
            topicName = "Shopping & Modern Tech",
            sentences = listOf(
                StorySentenceDoc("星期六早上，安娜在楼下买早餐。", "Xīngqīliù zǎoshang, Ānnà zài lóuxià mǎi zǎocān.", "On Saturday morning, Anna bought breakfast downstairs."),
                StorySentenceDoc("她很喜欢喝热豆浆，因为又香又有营养。", "Tā hěn xǐhuan hē rè dòujiāng, yīnwèi yòu xiāng yòu yǒu yíngyǎng.", "She loves drinking hot soy milk because it is fragrant and nutritious."),
                StorySentenceDoc("付钱的时候，她发现自己忘带现金了。", "Fùqián de shíhou, tā fāxiàn zìjǐ wàng dài xiànjīn le.", "When paying, she realized she forgot to bring cash."),
                StorySentenceDoc("老板说：没关系，用手机扫码付款就行，非常方便！", "Lǎobǎn shuō: Méiguānxi, yòng shǒujī sǎomǎ fùkuǎn jiù xíng, fēicháng fāngbiàn!", "The owner said: No problem, just scan the QR code with your phone—super convenient!")
            ),
            quizQuestion = "安娜最后怎么付钱的？ (How did Anna pay in the end?)",
            quizOptions = listOf("用现金 (With cash)", "用手机扫码付款 (By scanning a QR code)", "向朋友借钱 (Borrowed from a friend)"),
            quizCorrectIndex = 1
        ),
        GradedStory(
            id = "story_hsk3",
            titleZh = "周末坐高铁去杭州",
            titleEn = "Weekend High-Speed Rail to Hangzhou",
            hskLevel = 3,
            topicName = "Travel & Transport",
            sentences = listOf(
                StorySentenceDoc("虽然周五工作很忙，但是小李早就规划好了周末旅行。", "Suīrán zhōuwǔ gōngzuò hěn máng, dànshì Xiǎo Lǐ zǎojiù guīhuà hǎo le zhōumò lǚxíng.", "Although Friday was busy at work, Xiao Li had already planned a weekend trip."),
                StorySentenceDoc("周六上午八点，他准时到达车站，坐上了开往杭州的高铁。", "Zhōuliù shàngwǔ bā diǎn, tā zhǔnshí dàodá chēzhàn, zuòshàng le kāiwǎng Hángzhōu de gāotiě.", "At 8 AM Saturday, he arrived punctually and boarded the high-speed train to Hangzhou."),
                StorySentenceDoc("不到一小时，他就看到了美丽的西湖和翠绿的茶山。", "Búdào yì xiǎoshí, tā jiù kàndào le měilì de Xīhú hé cuìlǜ de cháshān.", "In under an hour, he saw the beautiful West Lake and emerald tea hills."),
                StorySentenceDoc("如果下次还有时间，他打算带父母一起来品尝龙井茶。", "Rúguǒ xiàcì hái yǒu shíjiān, tā dǎsuàn dài fùmǔ yìqǐ lái pǐncháng Lóngjǐngchá.", "If he has time next trip, he plans to bring his parents to taste Longjing tea.")
            ),
            quizQuestion = "坐高铁去杭州花了多长时间？ (How long did the high-speed train take?)",
            quizOptions = listOf("不到一小时 (Less than an hour)", "四个小时 (Four hours)", "一整天 (A whole day)"),
            quizCorrectIndex = 0
        ),
        GradedStory(
            id = "story_hsk7",
            titleZh = "算法时代的慢阅读",
            titleEn = "Slow Reading in the Age of Algorithms",
            hskLevel = 7,
            topicName = "Study & Work",
            sentences = listOf(
                StorySentenceDoc("在人工智能与推荐算法高度发达的今天，人们每天接触海量碎片化信息。", "Zài réngōng zhìnéng yǔ tuījiàn suànfǎ gāodù fādá de jīntiān, rénmen měitiān jiēchù hǎiliàng suìpiànhuà xìnxī.", "Today, with advanced AI and recommendation algorithms, people encounter vast fragmented information daily."),
                StorySentenceDoc("与其被即时推送牵着走，不如每天留出半小时静心阅读经典。", "Yǔqí bèi jíshí tuīsòng qiānzhe zǒu, bùrú měitiān liúchū bàn xiǎoshí jìngxīn yuèdú jīngdiǎn.", "Rather than being led by instant feeds, it is better to set aside half an hour daily for calm classic reading."),
                StorySentenceDoc("深度阅读如春雨般潜移默化，循序渐进地塑造独立思考的能力。", "Shēndù yuèdú rú chūnyǔ bān qiányímòhuà, xúnxù jiànjìn de sùzào dúlì sīkǎo de nénglì.", "Deep reading transforms us subtly like spring rain, gradually forging independent thinking.")
            ),
            quizQuestion = "作者对深度阅读持什么态度？ (What is the author's attitude toward deep reading?)",
            quizOptions = listOf("推崇每天静心深度阅读 (Advocates calm daily deep reading)", "认为只需看碎片推送 (Thinks feeds are enough)", "反对使用任何科技 (Opposes all technology)"),
            quizCorrectIndex = 0
        )
    )
}
