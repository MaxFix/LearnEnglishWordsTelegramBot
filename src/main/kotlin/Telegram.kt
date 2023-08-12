const val WELCOME_TEXT = "Hello"

fun main(args: Array<String>) {

    val botService = TelegramBotService()
    val botToken = args[0]
    var updateId = 0
    var chatId = 0

    while (true) {
        Thread.sleep(1000)
        val updates: String = botService.getUpdates(botToken, updateId)
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

        if (matchResultText != null && matchResultText.groupValues[1] == WELCOME_TEXT.lowercase()) {
            botService.sendMessage(botToken, chatId, WELCOME_TEXT)
        }
    }
}




