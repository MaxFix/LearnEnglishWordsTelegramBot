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

    dictionary.forEach {
        println(it)
    }
}

data class Word(
    val original: String,
    val translate: String,
    val learned: Int = 0,
)