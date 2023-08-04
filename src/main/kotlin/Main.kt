import java.io.File

fun main(args: Array<String>) {
    val dictionary = mutableListOf<Word>()
    val wordsFile: List<String> = File("some_words.txt")
        .readLines()

    wordsFile.forEach {
        val line = it.split("|")
        val word = Word(original = line[0], translate = line[1], learned = line[2].toInt())
        dictionary.add(word)
    }

    while (true) {
        val correctAnswersCount = dictionary.filter { it.learned == 3 }.size
        val correctAnswersPercent = (correctAnswersCount * 100) / wordsFile.size
        println("Введите 1 - Учить слова, 2 - Статистика, 0 - Выход")
        when (readln().toInt()) {
            1 -> {
                val unlearnedWords = dictionary.filter { it.learned < 3 }
                var learnedWords = dictionary.filter { it.learned >= 3 }

                if (unlearnedWords.isNotEmpty()) {
                    val selectedValues = unlearnedWords.shuffled().take(4)
                    if (selectedValues.size < 4) {
                        learnedWords = learnedWords.shuffled().take(4 - selectedValues.size)
                    }

                    val unlearnedWord = selectedValues.random().original
                    println("Исходное слово: $unlearnedWord")

                    val allWords = unlearnedWords + learnedWords
                    allWords.forEachIndexed { index, value ->
                        val number = index + 1
                        println("${number}: ${value.translate}")
                    }
                } else {
                    println("Вы выучили все слова")
                    break
                }
            }

            2 -> println("Статистика: Выучено $correctAnswersCount из ${wordsFile.size} | ${correctAnswersPercent}%")
            0 -> break
            else -> println("Предупреждение! Вводить можно только 0, 1 или 2")
        }
    }
}

data class Word(
    val original: String,
    val translate: String,
    val learned: Int = 0,
)