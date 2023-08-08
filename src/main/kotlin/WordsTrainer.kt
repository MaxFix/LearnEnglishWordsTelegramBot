import java.io.File

data class Statistics(
    val correctAnswersCount: Int,
    val totalAnswers: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class WordsTrainer {

    private var question: Question? = null
    private val dictionary = loadDictionary()
    private val wordsFile = File("some_words.txt")

    private fun loadDictionary(): List<Word> {
        val wordsFile = File("some_words.txt").readLines()
        val dictionary = mutableListOf<Word>()

        wordsFile.forEach {
            val line = it.split("|")
            val word = Word(original = line[0], translate = line[1], learned = line[2].toInt())
            dictionary.add(word)
        }
        return dictionary
    }

    private fun saveDictionary(dictionary: List<Word>) {
        wordsFile.writeText("")
        dictionary.forEach {
            val word = Word(original = it.original, translate = it.translate, learned = it.learned)
            wordsFile.appendText("${word.original}|${word.translate}|${word.learned}\n")
        }
    }

    fun getStatistics(): Statistics {
        val correctAnswersCount = dictionary.filter { it.learned == MAX_LEARNED_COUNTER }.size
        val totalAnswers = dictionary.size
        val correctAnswersPercent = (correctAnswersCount * 100) / totalAnswers
        return Statistics(
            correctAnswersCount,
            totalAnswers,
            correctAnswersPercent
        )
    }

    fun getNextQuestion(): Question? {
        var learnedWords = dictionary.filter { it.learned >= MAX_LEARNED_COUNTER }
        val unlearnedWords = dictionary.filter { it.learned < MAX_LEARNED_COUNTER }
        if (unlearnedWords.isEmpty()) return null
        val selectedValues = unlearnedWords.shuffled().take(NUM_OF_SELECTED_WORDS)

        if (selectedValues.size < NUM_OF_SELECTED_WORDS) {
            learnedWords = learnedWords.shuffled().take(NUM_OF_SELECTED_WORDS - selectedValues.size)
        }

        val unlearnedWord = selectedValues.random()
        question = Question(
            variants = unlearnedWords + learnedWords,
            correctAnswer = unlearnedWord,
        )

        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val answerIndex = it.variants.indexOf(it.correctAnswer)

            if (userAnswerIndex == answerIndex) {
                it.correctAnswer.learned++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }
}