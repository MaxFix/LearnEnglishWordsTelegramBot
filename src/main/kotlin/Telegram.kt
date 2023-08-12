import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0
    var chatId = 0
    var text = ""


    while (true) {
        Thread.sleep(1000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val regexUpdateId = "\"update_id\":(.+?),".toRegex()
        val regexChatId = "\"chat\":\\{\"id\":(.+?),".toRegex()
        val regexText = "\"text\":\"(.+?)\"".toRegex()

        val matchResultUpdateId = regexUpdateId.find(updates)
        val matchResultChatId = regexChatId.find(updates)
        val matchResultText = regexText.find(updates)

        if (matchResultUpdateId != null) {
            updateId = matchResultUpdateId.groupValues[1].toInt() + 1
        }
        if (matchResultChatId != null) {
            chatId = matchResultChatId.groupValues[1].toInt()
        }

        if (matchResultText != null) {
            text = matchResultText.groupValues[1]
        }

        if (text.lowercase() == "hello") {
            sendMessage(botToken, chatId, "Hello")
        }

    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlUpdate = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"

    val client: HttpClient = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder().uri(URI.create(urlUpdate)).build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

    return response.body()
}


fun sendMessage(botToken: String, chatId: Int, message: String): String {
    val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
    val requestBodyString = "chat_id=$chatId&text=$message"

    val client: HttpClient = HttpClient.newBuilder().build()
    val request = HttpRequest
        .newBuilder()
        .uri(URI.create(urlSendMessage))
        .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
        .build()
    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

    return response.body()
}