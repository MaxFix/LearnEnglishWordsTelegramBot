import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val API_TELEGRAM = "https://api.telegram.org/bot"

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyBoard>>,
)

@Serializable
data class InlineKeyBoard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

class TelegramBotService(
    private val botToken: String,
    private val json: Json,
) {

    fun getUpdates(lastUpdateId: Long): String {
        val urlUpdate = "$API_TELEGRAM${botToken}/getUpdates?offset=$lastUpdateId"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(urlUpdate))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: Long, message: String): String {

        val urlSendMessage = "$API_TELEGRAM${botToken}/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Long): String {
        val urlSendMessage = "$API_TELEGRAM${this.botToken}/sendMessage"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyBoard(text = "Изучать слова", callbackData = LEARN_WORD_TEXT),
                        InlineKeyBoard(text = "Статистика", callbackData = STATISTICS_TEXT),
                    ),
                    listOf(
                        InlineKeyBoard(text = "Сбросить статистику", callbackData = RESET_CLICKED),
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    private fun sendQuestionToUser(chatId: Long, question: Question): String {
        val urlSendMessage = "$API_TELEGRAM${this.botToken}/sendMessage"
        val questionVariants = question.variants.mapIndexed { index, word ->
            listOf( InlineKeyBoard(
                text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"))
        }
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                inlineKeyboard = questionVariants + listOf(
                    listOf(InlineKeyBoard(text = "Выйти в основное меню", callbackData = "exit_btn"))
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: WordsTrainer, chatId: Long) {
        val question = trainer.createAndGetNextQuestion()?.variants
        if (question != null) {
            trainer.createAndGetNextQuestion()?.let { sendQuestionToUser(chatId, it) }
        } else {
            sendMessage(chatId, "Вы выучили все слова!")
        }
    }
}