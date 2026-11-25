package es.labjc.aplicaciontest.data

data class Question(
    val number: Int,
    val text: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: Char
) {
    /** Opciones en orden, como pares (letra, texto). */
    val options: List<Pair<Char, String>>
        get() = listOf('A' to optionA, 'B' to optionB, 'C' to optionC, 'D' to optionD)

    fun optionText(letter: Char): String =
        options.firstOrNull { it.first == letter.uppercaseChar() }?.second.orEmpty()

    fun isCorrect(answer: Char?): Boolean =
        answer != null && answer.uppercaseChar() == correctAnswer.uppercaseChar()
}
