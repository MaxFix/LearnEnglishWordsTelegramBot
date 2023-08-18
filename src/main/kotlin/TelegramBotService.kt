import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val API_TELEGRAM = "https://api.telegram.org/bot"

class TelegramBotService(
    private val botToken: String,
) {

    fun getUpdates(botToken: String, updateId: Int): String {
        val urlUpdate = "$API_TELEGRAM${this.botToken}/getUpdates?offset=$updateId"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(urlUpdate))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(botToken: String, chatId: Int, message: String): String {
        val encoded = URLEncoder.encode(
            message,
            StandardCharsets.UTF_8
        )
        println(encoded)

        val urlSendMessage = "$API_TELEGRAM${this.botToken}/sendMessage?chat_id=$chatId&text=$encoded"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(urlSendMessage))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMenu(botToken: String, chatId: Int): String {
        val urlSendMessage = "$API_TELEGRAM${this.botToken}/sendMessage"
        val sendMenuBody = """
            {
            	"chat_id": $chatId,
            	"text": "Основное меню",
            	"reply_markup": {
            		"inline_keyboard": [
                        [
            	 		{
            	  			"text": "Изучить слова",
            	  			"callback_data": "learn_words_clicked"
            			},
            			{
            				"text": "Статистика",
            				"callback_data": "statistics_clicked"
            			}
                        ]
            		]
            	}
            }
            """.trimIndent()

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendQuestionToUser(botToken: String, chatId: Int, question: Question): String {
        val urlSendMessage = "$API_TELEGRAM${this.botToken}/sendMessage"
        val allWords = question.variants
        val sendQuestionBody = """
                {
                "chat_id": $chatId,
                "text": "${question.correctAnswer.original}",
                "callback_data": "question",
                "reply_markup": {
                "inline_keyboard": [
                            ${
            allWords.mapIndexed { index, answer ->
                "[{\"text\": \"${answer.translate}\", \"callback_data\": \"${CALLBACK_DATA_ANSWER_PREFIX + index}\"}]"
            }.joinToString(",")
        }
        
        ,[
                {
                "text": "Выйти",
            	"callback_data": "exit_btn"
                }
                            ]
                        ]
                    }
                }
            """.trimIndent()

        println(sendQuestionBody)

        val client: HttpClient = HttpClient.newBuilder().build()
        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }
}