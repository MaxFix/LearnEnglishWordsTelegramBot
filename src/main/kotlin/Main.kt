import java.lang.Exception

const val MAX_LEARNED_COUNTER = 3
const val NUM_OF_SELECTED_WORDS = 4

data class Word(
    val original: String,
    val translate: String,
    var learned: Int = 0,
)

fun Question.asConsoleString() {
    val allWords = this.variants
    allWords.forEachIndexed { index, value ->
        println("${index + 1}: ${value.translate}")
    }
    println("0: Меню")
}

fun main(args: Array<String>) {

    val trainer =  try {
        WordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {

        println("Введите 1 - Учить слова, 2 - Статистика, 0 - Выход")
        when (readln().toIntOrNull()) {
            1 -> {
                val question = trainer.getNextQuestion()

                if (question != null) {
                    println("Исходное слово: ${question.correctAnswer.original}")

                    question.asConsoleString()

                    val userInput = readln().toIntOrNull()
                    if (userInput == 0) continue

                    if (trainer.checkAnswer(userInput?.minus(1))) {
                        println("Верно!")
                    } else {
                        println("Неправильно! ${question.correctAnswer.original} - это ${question.correctAnswer.translate}")
                    }
                } else {
                    println("Вы выучили все слова")
                    break
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println(
                    "Статистика: Выучено ${statistics.correctAnswersCount} из " +
                            "${statistics.totalAnswers} | ${statistics.percent}%"
                )
            }

            0 -> break
            else -> println("Предупреждение! Вводить можно только 0, 1 или 2")
        }
    }
}