package com.example.voicelist

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
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
        "five",
        "six:origin,Tina,Rock,Mash,Edo"
    )
    @DataPoints
    val dataOne = Fixture(1, listOf("had", "had"))
    val dataTwo = Fixture(2, listOf("give", "gave", "given"))

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
        assertThat(result).isEqualTo(mutableListOf("have", "two", "three", "four", "six"))
    }

    data class Fixture(val dataIndex: Int, val dataList: List<String>)


    @Theory
    fun testChildList(index: Int, expected: List<String>) {
        val headAndChildCSV = sampleData[index]
        val list = headAndChildCSV.split(",")
        val result = list.drop(1)
        assertThat(result).isEqualTo(expected)
    }


}
