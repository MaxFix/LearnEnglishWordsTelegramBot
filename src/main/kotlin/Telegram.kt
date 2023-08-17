const val WELCOME_TEXT = "Hello"
const val MENU_TEXT = "/start"
const val STATISTICS_TEXT = "statistics_clicked"
const val LEARN_WORD_TEXT = "learn_words_clicked"

fun main(args: Array<String>) {

    val botService = TelegramBotService()
    val botToken = args[0]
    var updateId = 0
    var chatId = 0

    val regexUpdateId = "\"update_id\":(.+?),".toRegex()
    val regexChatId = "\"chat\":\\{\"id\":(.+?),".toRegex()
    val regexText = "\"text\":\"(.+?)\"".toRegex()
    val regexData = "\"data\":\"(.+?)\"".toRegex()

    val trainer = WordsTrainer()
    val statistics = trainer.getStatistics()

    while (true) {
        Thread.sleep(1000)
        val updates: String = botService.getUpdates(botToken, updateId)
        println(updates)

        val matchResultUpdateId = regexUpdateId.find(updates)
        val matchResultChatId = regexChatId.find(updates)
        val matchResultText = regexText.find(updates)
        val matchResultData = regexData.find(updates)

        if (matchResultUpdateId != null) {
            updateId = matchResultUpdateId.groupValues[1].toInt() + 1
        }
        if (matchResultChatId != null) {
            chatId = matchResultChatId.groupValues[1].toInt()
        }

        val text = matchResultText?.groupValues?.getOrNull(1)
        val data = matchResultData?.groupValues?.getOrNull(0)

        if (matchResultText != null && matchResultText.groupValues[1] == WELCOME_TEXT.lowercase()) {
            botService.sendMessage(botToken, chatId, WELCOME_TEXT)
        }
        if (text == MENU_TEXT.lowercase()) {
            botService.sendMenu(botToken, chatId)
        }
        if (matchResultData != null && matchResultData.groupValues[1] == STATISTICS_TEXT.lowercase()) {
            botService.sendMessage(botToken, chatId, "Статистика: Выучено ${statistics.correctAnswersCount} из " +
                    "${statistics.totalAnswers} | ${statistics.percent}%")
        }
        if (matchResultData != null && matchResultData.groupValues[1] == LEARN_WORD_TEXT.lowercase()) {
            checkNextQuestionAndSend(trainer, botToken, chatId)
        }
        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val answerNumber = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if(trainer.checkAnswer(answerNumber)) {
                println("Правильно!")
            } else {
                println("Не правильно: ${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}")
            }
            checkNextQuestionAndSend(trainer, botToken, chatId)
        }
    }
}

fun checkNextQuestionAndSend(trainer: WordsTrainer, botToken: String, chatId: Int) {
    val botService = TelegramBotService()
    val createAndGetQuestion = trainer.createAndGetNextQuestion()
    if (createAndGetQuestion != null) {
        createAndGetQuestion.let { botService.sendQuestionToUser(botToken, chatId, it) }
    } else {
        println("Вы выучили все слова в базе")
    }
}