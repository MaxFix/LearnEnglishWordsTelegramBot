import java.io.File
import java.lang.IllegalStateException
import java.lang.IndexOutOfBoundsException

data class Statistics(
    val correctAnswersCount: Int,
    val totalAnswers: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class WordsTrainer(
    fileName: String = "words.txt",
    private val wordsFile: File = File(fileName),
    var question: Question? = null,
    private val maxLearnedCounter: Int = 3,
    private val numberOfSelectedWords: Int = 4,
) {
    private val dictionary = loadDictionary()


    fun getStatistics(): Statistics {
        val correctAnswersCount = dictionary.filter { it.learned == maxLearnedCounter }.size
        val totalAnswers = dictionary.size
        val correctAnswersPercent = (correctAnswersCount * 100) / totalAnswers
        return Statistics(
            correctAnswersCount,
            totalAnswers,
            correctAnswersPercent
        )
    }

    fun createAndGetNextQuestion(): Question? {
        var learnedWords = dictionary.filter { it.learned >= maxLearnedCounter }
        val unlearnedWords = dictionary.filter { it.learned < maxLearnedCounter }
        if (unlearnedWords.isEmpty()) return null
        val selectedValues = unlearnedWords.shuffled().take(numberOfSelectedWords)

        if (selectedValues.size < numberOfSelectedWords) {
            learnedWords = learnedWords.take(numberOfSelectedWords - selectedValues.size).shuffled()
        }

        val unlearnedWord = selectedValues.random()
        val allVariants = (unlearnedWords + learnedWords).take(4).shuffled()

        question = Question(
            variants = allVariants,
            correctAnswer = unlearnedWord,
        )

        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val answerIndex = it.variants.indexOf(it.correctAnswer)

            if (userAnswerIndex == answerIndex) {
                it.correctAnswer.learned++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        try {
            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }
            val dictionary = mutableListOf<Word>()

            wordsFile.readLines().forEach {
                val line = it.split("|")
                val word = Word(original = line[0], translate = line[1], learned = line[2].toInt())
                dictionary.add(word)
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Файл с некорректными данными или пустой")
        }
    }

    private fun saveDictionary() {
        wordsFile.writeText("")
        dictionary.forEach {
            val word = Word(original = it.original, translate = it.translate, learned = it.learned)
            wordsFile.appendText("${word.original}|${word.translate}|${word.learned}\n")
        }
    }

    fun resetProgress() {
        dictionary.forEach{ it.learned = 0 }
        saveDictionary()
    }
}