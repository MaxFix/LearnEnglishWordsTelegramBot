import java.io.File

const val MAX_LEARNED_COUNTER = 3
const val NUM_OF_SELECTED_WORDS = 4

fun main(args: Array<String>) {

    val dictionary = mutableListOf<Word>()
    val wordsFile: List<String> = File("some_words.txt")
        .readLines()

    wordsFile.forEach {
        val line = it.split("|")
        val word = Word(original = line[0], translate = line[1], learned = line[2].toInt())
        dictionary.add(word)
    }

    fun saveDictionary(dictionary: MutableList<Word>) {
        val fileWithWordsDictionary = File("some_words.txt")
        fileWithWordsDictionary.writeText("")
        dictionary.forEach {
            val word = Word(original = it.original, translate = it.translate, learned = it.learned)
            fileWithWordsDictionary.appendText("${word.original}|${word.translate}|${word.learned}\n")
        }
    }

    while (true) {
        val correctAnswersCount = dictionary.filter { it.learned == MAX_LEARNED_COUNTER }.size
        val correctAnswersPercent = (correctAnswersCount * 100) / wordsFile.size
        println("Введите 1 - Учить слова, 2 - Статистика, 0 - Выход")
        when (readln().toInt()) {
            1 -> {
                val unlearnedWords = dictionary.filter { it.learned < MAX_LEARNED_COUNTER }

                if (unlearnedWords.isNotEmpty()) {
                    var learnedWords = dictionary.filter { it.learned >= MAX_LEARNED_COUNTER }
                    val selectedValues = unlearnedWords.shuffled().take(NUM_OF_SELECTED_WORDS)
                    if (selectedValues.size < NUM_OF_SELECTED_WORDS) {
                        learnedWords = learnedWords.shuffled().take(NUM_OF_SELECTED_WORDS - selectedValues.size)
                    }

                    val unlearnedWord = selectedValues.random().original
                    println("Исходное слово: $unlearnedWord")

                    val allWords = unlearnedWords + learnedWords
                    allWords.forEachIndexed { index, value ->
                        val number = index + 1
                        println("${number}: ${value.translate}")
                    }
                    println("0: Меню")

                    val userInput = readln().toInt()
                    if (userInput == 0) continue

                    val answerIndex = allWords.indexOfFirst { it.original == unlearnedWord } + 1
                    if (userInput == answerIndex) {
                        println("Верно!")
                        val selectedWord = allWords.find { it.original == unlearnedWord }
                        selectedWord?.learned = selectedWord?.learned?.plus(1)!!
                        saveDictionary(dictionary)
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
    var learned: Int = 0,
)