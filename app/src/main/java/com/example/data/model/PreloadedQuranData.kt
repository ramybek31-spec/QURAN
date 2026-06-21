package com.example.data.model

data class PreloadedVerse(
    val surahNumber: Int,
    val verseNumber: Int,
    val textArabic: String,
    val textEnglish: String,
    val textUrdu: String = ""
)

object PreloadedQuranData {
    val verses = listOf(
        // Al-Fatihah (1)
        PreloadedVerse(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "شروع اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے"),
        PreloadedVerse(1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -", "سب تعریفیں اللہ ہی کے لیے ہیں جو تمام جہانوں کا پالنے والا ہے"),
        PreloadedVerse(1, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", "بڑا مہربان نہایت رحم والا ہے"),
        PreloadedVerse(1, 4, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense.", "روزِ جزا (یعنی قیامت کے دن) کا مالک ہے"),
        PreloadedVerse(1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد چاہتے ہیں"),
        PreloadedVerse(1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path -", "ہمیں سیدھے راستے پر چلا"),
        PreloadedVerse(1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", "ان لوگوں کے راستے پر جن پر تو نے اپنا فضل کیا، نہ ان کے راستے پر جن پر تیرا غضب ہوا اور نہ گمراہوں کے"),

        // Al-Asr (103)
        PreloadedVerse(103, 1, "وَالْعَصْرِ", "By time,", "زمانے کی قسم"),
        PreloadedVerse(103, 2, "إِنَّ الْإِنْسَانَ لَفِي خُسْرٍ", "Indeed, mankind is in loss,", "بیشک انسان خسارے میں ہے"),
        PreloadedVerse(103, 3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.", "سوائے ان لوگوں کے جو ایمان لائے اور نیک عمل کرتے رہے اور ایک دوسرے کو حق کی وصیت اور صبر کی تاکید کرتے رہے"),

        // Al-Kawthar (108)
        PreloadedVerse(108, 1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Indeed, We have granted you, [O Muhammad], al-Kawthar.", "بیشک ہم نے آپ کو کوثر (بے انتہا بھلائی) عطا فرمائی"),
        PreloadedVerse(108, 2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "So pray to your Lord and sacrifice [to Him alone].", "پس آپ اپنے رب کے لیے نماز پڑھیں اور قربانی دیں"),
        PreloadedVerse(108, 3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "Indeed, your enemy is the one cut off.", "بیشک آپ کا دشمن ہی بے نام و نشان ہوگا"),

        // Al-Ikhlas (112)
        PreloadedVerse(112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, \"He is Allah, [who is] One,", "آپ فرمائیے: وہ اللہ ایک ہے"),
        PreloadedVerse(112, 2, "اللَّهُ الصَّمَدُ", "Allah, the Eternal Refuge.", "اللہ بے نیاز ہے (سب اس کے محتاج ہیں)"),
        PreloadedVerse(112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", "نہ اس نے کسی کو جنا اور نہ وہ کسی سے جنا گیا"),
        PreloadedVerse(112, 4, "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ", "Nor is there to Him any equivalent.\"", "اور اس کا کوئی ہمسر نہیں ہے"),

        // Al-Falaq (113)
        PreloadedVerse(113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, \"I seek refuge in the Lord of daybreak", "آپ فرمائیے: میں صبح کے رب کی پناہ مانگتا ہوں"),
        PreloadedVerse(113, 2, "مِنْ شَرِّ مَا خَلَقَ", "From the evil of that which He created", "ہر اس چیز کے شر سے جو اس نے پیدا کی"),
        PreloadedVerse(113, 3, "وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", "اور اندھیری رات کے شر سے جب وہ چھا جائے"),
        PreloadedVerse(113, 4, "وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots", "اور گرہوں میں پھونکنے والیوں کے شر سے"),
        PreloadedVerse(113, 5, "وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.\"", "اور حسد کرنے والے کے شر سے جب وہ حسد کرے"),

        // An-Nas (114)
        PreloadedVerse(114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, \"I seek refuge in the Lord of mankind,", "آپ فرمائیے: میں انسانوں کے رب کی پناہ مانگتا ہوں"),
        PreloadedVerse(114, 2, "مَلِكِ النَّاسِ", "The Sovereign of mankind,", "انسانوں کے بادشاہ کی"),
        PreloadedVerse(114, 3, "إِلَهِ النَّاسِ", "The God of mankind,", "انسانوں کے حقیقی معبود کی"),
        PreloadedVerse(114, 4, "مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer -", "وسوسہ ڈالنے والے (اور پناہ میں) چھپ جانے والے کے شر سے"),
        PreloadedVerse(114, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers [evil] into the breasts of mankind -", "جو لوگوں کے سینوں میں وسوسے ڈالتا ہے"),
        PreloadedVerse(114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.\"", "خواہ وہ جنات میں سے ہو یا انسانوں میں سے")
    )

    val tafsirs = listOf(
        // Al-Fatihah (1)
        PreloadedTafsir(1, 1, "Bismillah (In the Name of Allah...): The Holy Quran starts with the name of its Author, Allah. All actions should begin by calling upon His Mercy and Grace. It reminds us of His absolute role as the compassionate source of all existence."),
        PreloadedTafsir(1, 2, "Alhamdulillah: Absolute praise and gratitude belong solely to Allah, the sovereign Lord, Creator, Nourisher, and Sustainer of the entire cosmos (Aalameen)."),
        PreloadedTafsir(1, 3, "Ar-Rahman Ar-Rahim: These two names derive from Rahmah (mercy). Ar-Rahman refers to His vast, all-encompassing mercy towards all creation in this worldly life. Ar-Rahim refers to His specific, lasting mercy reserved for seekers of truth and believers."),
        PreloadedTafsir(1, 4, "Maliki Yawmid-Deen: This establishes accountability. Allah is the sole Master and Judge of the Day of Judgment (Day of Recompense), where justice will be perfectly served and no human soul will be wronged."),
        PreloadedTafsir(1, 5, "Iyyaka Na'budu...: This expresses the ultimate monotheistic covenant: we devote our ultimate worship, submission, and absolute devotion to You alone, and from You alone we seek spiritual and practical aid."),
        PreloadedTafsir(1, 6, "Ihdinas-Siratal-Mustaqim: The most crucial prayer of a believer: guide us, make us firm, and keep us steady upon the Straight Path (the path of clarity, balance, and absolute truth)."),
        PreloadedTafsir(1, 7, "Siratalladhina an'amta...: Clarifies the path: it is the path of those who received God's active grace (prophets, truthful ones, martyrs, and righteous), not of those who knew the truth but neglected it (evoking anger), nor those who lost the path entirely (astray)."),

        // Al-Asr (103)
        PreloadedTafsir(103, 1, "Wal-'Asr: Allah swears by Time (or the late afternoon era). Swearing by time emphasizes its precious nature, as time is the capital of human life which is constantly ticking away."),
        PreloadedTafsir(103, 2, "Innal-Insana lafi khusr: By default, human beings are in a state of absolute loss, spiritual bankruptcy, and wasted opportunity as time slips past them."),
        PreloadedTafsir(103, 3, "Illalladhina aamanu...: The cure for human loss consists of four interconnected pillars: 1) True conviction/faith (Imaan); 2) Constructive, righteous deeds; 3) Collectively inviting and advising one another to Truth; 4) Supporting each other to beautiful Steadfastness (Sabr)."),

        // Al-Kawthar (108)
        PreloadedTafsir(108, 1, "Inna a'taynakal-Kawthar: Al-Kawthar literally means 'Abundant Goodness'. It also refers specifically to a majestic, beautiful river in Paradise promised to Prophet Muhammad, representing eternal satisfaction and spiritual abundance."),
        PreloadedTafsir(108, 2, "Fa salli liRabbika wanhar: In gratitude for this boundless custom grace, dedicate your prayer and your sacrifice exclusively to your Lord, rejecting all forms of idolatry or worship of worldly objects."),
        PreloadedTafsir(108, 3, "Inna shani'aka...: Those who mock or oppose the divine light and the message will be cut off from all legacy, goodness, and ultimate success, while the truth remains elevated forever."),

        // Al-Ikhlas (112)
        PreloadedTafsir(112, 1, "Qul Huwallahu Ahad: The declaration of absolute Unity (Tawhid). Say: He is Allah, singular, unique, with no partners, divisions, or equivalents in His essence, names, or actions."),
        PreloadedTafsir(112, 2, "Allahus-Samad: Al-Samad means the Self-Sufficient Master whom all creation absolutely depends upon for their needs, while He needs nothing from anyone."),
        PreloadedTafsir(112, 3, "Lam yalid wa lam yoolad: He transcends all creaturely attributes; He does not beget (has no children/descendants), nor is He begotten (has no parents, originators, or ancestors). He is eternal, startingless, and endless."),
        PreloadedTafsir(112, 4, "Wa lam yakun lahu...: There is absolutely nothing in existence that is equal, comparable, or similar to Him in any manner."),

        // Al-Falaq (113)
        PreloadedTafsir(113, 1, "Qul a'udhu biRabbil-Falaq: Seek active refuge in the Lord of the Daybreak (the force that breaks the dark night with morning light), symbolizing hope and active protection from spiritual darkness."),
        PreloadedTafsir(113, 2, "Min sharri ma khalaq: Protection from the potential harm and evil inherent within created things, whether visible or hidden."),
        PreloadedTafsir(113, 3, "Wa min sharri ghasiqin...: Protection from the evils of the dark night when it settles, as darkness often conceals physical, psychological, and spiritual harms."),
        PreloadedTafsir(113, 4, "Wa min sharrin-naffathati fil-'uqad: Protection from those who practice secretive malice, envy, division, and occult manipulation (metaphor of blowing on knots), aiming to disrupt love and family bounds."),
        PreloadedTafsir(113, 5, "Wa min sharri hasidin...: Protection from the destructive envy of an envier when he acts upon his envy, wanting others to lose their blessings."),

        // An-Nas (114)
        PreloadedTafsir(114, 1, "Qul a'udhu biRabbin-Nas: Seek refuge in the Lord of mankind, He who nurtures and sustains all mankind."),
        PreloadedTafsir(114, 2, "Malikin-Nas: The sovereign Ruler and absolute King of all mankind, to whom all authorities are subservient."),
        PreloadedTafsir(114, 3, "Ilahin-Nas: The ultimate God and Object of worship of mankind, who alone deserves obedience."),
        PreloadedTafsir(114, 4, "Min sharril-waswasil-khannas: Protection from the whisperer who retreats. The whisperer implants doubts and evil thoughts in minds, but quickly retreats when one remembers and calls upon Allah."),
        PreloadedTafsir(114, 5, "Alladhi yuwaswisu...: Those active negative forces that inject negative suggestions, anxieties, doubts, and evil thoughts directly into the hearts and minds of people."),
        PreloadedTafsir(114, 6, "Minal-jinnati wan-nas: Clarifies that whisperers can belong to either the hidden world of spirits (jinn) or the visible world of fellow human beings who misguide others.")
    )
}

data class PreloadedTafsir(
    val surahNumber: Int,
    val verseNumber: Int,
    val tafsirText: String
)

