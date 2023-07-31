import java.io.File

fun main(args: Array<String>) {
    val wordsFile: List<String> = File("some_words.txt")
        .readLines()

    wordsFile.forEach{
        println(it)
    }
}