package com.example.voicelist

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.experimental.theories.suppliers.TestedOn
import org.junit.runner.RunWith

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

}

@RunWith(Theories::class)
class RegexTest {
    val sampleData = listOf(
        "have:origin,had,had",
        "two:origin,give,gave,given",
        "three:origin,fire,blizzard,thunder",
        "four:origin,fire,fira,figa",
        "five:origin,thunder,thundara,thundaga",
        "six:origin,Tina,Rock,Mash,Edo"
    )
    val sampleIndexToExpected = mapOf(
        0 to listOf("had", "had"),
        1 to listOf("give", "gave", "given"),
        2 to listOf("fire", "blizzard", "thunder"),
        3 to listOf("fire", "fira", "figa"),
        4 to listOf("thunder", "thundara", "thundaga"),
        5 to listOf("Tina", "Rock", "Mash", "Edo")
    )


    @Test
    fun testOriginRegEx() {
        val titleRegex = "^(.+):origin,.*".toRegex()
        val result = mutableListOf<String>()
        for (i in sampleData.indices) {
            val matchResult = titleRegex.matchEntire(sampleData[i])
            matchResult?.destructured?.let { (header) ->
                result.add(header)
            }
        }
        assertThat(result).isEqualTo(mutableListOf("have", "two", "three", "four", "five", "six"))
    }
    @Theory
    fun testChildList(@TestedOn(ints = intArrayOf(0, 1, 2, 3, 4)) int: Int) {
        val headAndChildCSV = sampleData[int]
        val list = headAndChildCSV.split(",")
        val result = list.drop(1)
        assertThat(result).isEqualTo(sampleIndexToExpected[int])
    }


}
