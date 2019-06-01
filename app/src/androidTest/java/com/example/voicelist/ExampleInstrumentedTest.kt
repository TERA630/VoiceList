package com.example.voicelist


import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.view.View
import com.example.voicelist.CustomMatchers.Companion.hasText
import kotlinx.android.synthetic.main.card_list.view.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.example.voicelist", appContext.packageName)
    }
}

@RunWith(AndroidJUnit4::class)
class ActivityTest {
    @Rule
    @JvmField
    val mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun activitySnack() {
        onView(withId(R.id.activityFrame)).check(matches(isDisplayed()))
        onView(withId(R.id.originList)).check(matches(isDisplayed()))
        onView(withId(R.id.originList)).check(matches(isDisplayed()))
        onView(withId(R.id.originList)).check(matches(hasText(1, "two")))
    }
}

class CustomMatchers {
    var innerText: String? = null

    companion object {
        fun hasText(position: Int, testString: String): Matcher<View> {
            return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
                override fun describeTo(description: Description?) {
                    description?.appendText("Recycler view has $testString")
                }

                override fun matchesSafely(view: RecyclerView?): Boolean {
                    if (view !is RecyclerView) return false // throw IllegalStateException("The asserted view is not RecyclerView")
                    if (view.adapter == null) return false // throw IllegalStateException("No adapter is assigned to RecyclerView")
                    else {
                        val holder = view.findViewHolderForAdapterPosition(position)
                        val text = if (holder is OriginListAdaptor.ViewHolderOfCell) {
                            holder.itemView.rowText.text
                        } else if (holder is OriginListAdaptor.ViewHolderOfFolder) {
                            holder.itemView.rowText.text
                        } else {
                            "void"
                        }
                        val judge = (text == testString)
                        return judge
                    }
                }
            }


        }
    }
}