package dev.eastar.ktx

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {

        val a: String? = "null"
        val b: String? = "B"
        val c: String? = null

        println(a?.length ?: b ?: c)
    }
}