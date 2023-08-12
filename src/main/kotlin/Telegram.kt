import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0


    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val regex = "\"update_id\":(.+?),".toRegex()
        val matchResult = regex.find(updates)
        if (matchResult != null) {
            updateId = matchResult.groupValues[1].toInt() + 1
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