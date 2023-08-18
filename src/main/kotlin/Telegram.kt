import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val MENU_TEXT = "/start"
const val STATISTICS_TEXT = "statistics_clicked"
const val LEARN_WORD_TEXT = "learn_words_clicked"
const val EXIT_BTN = "exit_btn"
const val RESET_CLICKED = "reset_clicked"

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0L

    val json = Json {
        ignoreUnknownKeys = true
    }

    val trainers = HashMap<Long, WordsTrainer>()

    while (true) {
        Thread.sleep(1000)
        val responseString: String = TelegramBotService(botToken).getUpdates(botToken, lastUpdateId)
        println(responseString)
        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, json, botToken, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(update: Update, json: Json, botToken: String, trainers: HashMap<Long, WordsTrainer>) {

    val botService = TelegramBotService(botToken)

    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { WordsTrainer("$chatId.txt") }

    val statistics = trainer.getStatistics()

    if (message?.lowercase() == MENU_TEXT) {
        botService.sendMenu(json, botToken, chatId)
    }
    if (data == STATISTICS_TEXT.lowercase()) {
        botService.sendMessage(
            json, botToken, chatId,
            "Статистика: Выучено ${statistics.correctAnswersCount} из ${statistics.totalAnswers} | ${statistics.percent}%"
        )
    }
    if (data == LEARN_WORD_TEXT) {
        checkNextQuestionAndSend(json, trainer, botToken, chatId)
    }
    if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
        val answerNumber = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
        if (trainer.checkAnswer(answerNumber)) {
            botService.sendMessage(json, botToken, chatId, "Правильно!")
        } else {
            botService.sendMessage(
                json, botToken, chatId,
                "Не правильно: ${trainer.question?.correctAnswer?.original} - " +
                        "${trainer.question?.correctAnswer?.translate}"
            )
        }
        checkNextQuestionAndSend(json, trainer, botToken, chatId)
    }
    if (data == EXIT_BTN.lowercase()) {
        botService.sendMenu(json, botToken, chatId)
    }

    if (data == RESET_CLICKED) {
        trainer.resetProgress()
        botService.sendMessage(json, botToken, chatId, "Прогресс сброшен")
    }
}

fun checkNextQuestionAndSend(json: Json, trainer: WordsTrainer, botToken: String, chatId: Long) {
    val botService = TelegramBotService(botToken)
    val question = trainer.createAndGetNextQuestion()?.variants
    if (question != null) {
        trainer.createAndGetNextQuestion()?.let { botService.sendQuestionToUser(json, botToken, chatId, it) }
    } else {
        botService.sendMessage(json, botToken, chatId, "Вы выучили все слова!")
    }
}