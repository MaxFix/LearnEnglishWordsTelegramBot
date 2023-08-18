import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val MENU_TEXT = "/start"
const val STATISTICS_TEXT = "statistics_clicked"
const val LEARN_WORD_TEXT = "learn_words_clicked"
const val EXIT_BTN = "exit_btn"
private const val botToken = "5902907319:AAFb-XNI2kqZeQ1HN4zjJNAIgvq_mtjOXLA"

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

    val botService = TelegramBotService(botToken)
    val botToken = args[0]
    var lastUpdateId = 0L

    val json = Json {
        ignoreUnknownKeys = true
    }

    val trainer = WordsTrainer()
    val statistics = trainer.getStatistics()

    while (true) {
        Thread.sleep(1000)
        val responseString: String = botService.getUpdates(botToken, lastUpdateId)
        println(responseString)
        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val message = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val data = firstUpdate.callbackQuery?.data

        if (message?.lowercase() == MENU_TEXT && chatId != null) {
            botService.sendMenu(json, botToken, chatId)
        }
        if (data == STATISTICS_TEXT.lowercase() && chatId != null) {
            botService.sendMessage(
                json, botToken, chatId,
                "Статистика: Выучено ${statistics.correctAnswersCount} из ${statistics.totalAnswers} | ${statistics.percent}%"
            )
        }
        if (data == LEARN_WORD_TEXT && chatId != null) {
            checkNextQuestionAndSend(json, trainer, botToken, chatId)
        }
        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true && chatId != null) {
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
        if (data == EXIT_BTN.lowercase() && chatId != null) {
            botService.sendMenu(json, botToken, chatId)
        }
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