package com.example.voicelist

import android.content.res.Resources
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.view.View
import com.example.voicelist.CustomMatchers.Companion.hasText
import kotlinx.android.synthetic.main.origin_list.view.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
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
    fun viewCheck() {
        onView(withId(R.id.activityFrame)).check(matches(isDisplayed()))
        onView(withId(R.id.originList)).check(matches(isDisplayed()))
        onView(withId(R.id.originList)).check(matches(isDisplayed()))
        onView(withId(R.id.originList)).check(matches(hasText(0, "one")))
        onView(withRecyclerView(R.id.originList).atPositionOnView(1, R.id.folderIcon)).check(matches(isDisplayed()))
        onView(withRecyclerView(R.id.originList).atPositionOnView(0, R.id.textWrapper)).check(matches(isDisplayed()))
    }

    @Test
    fun viewClick() {
        //　テキストクリック→編集
        onView(withRecyclerView(R.id.originList).atPositionOnView(0, R.id.rowText)).perform(ViewActions.click())
        onView(withRecyclerView(R.id.originList).atPositionOnView(0, R.id.rowEditText)).check(matches(isDisplayed()))
        onView(withRecyclerView(R.id.originList).atPositionOnView(1, R.id.rowText)).perform(ViewActions.click())
        //　テキスト編集、終了
        onView(withRecyclerView(R.id.originList).atPositionOnView(1, R.id.rowEditText)).check(matches(isDisplayed()))
        onView(withRecyclerView(R.id.originList).atPositionOnView(1, R.id.rowEditText)).perform(ViewActions.clearText())
        onView(
            withRecyclerView(R.id.originList).atPositionOnView(
                1,
                R.id.rowEditText
            )
        ).perform(ViewActions.typeText("Test One"))
        // テキスト編集終了
        onView(withRecyclerView(R.id.originList).atPositionOnView(1, R.id.editEndButton)).check(matches(isDisplayed()))
        onView(withRecyclerView(R.id.originList).atPositionOnView(1, R.id.editEndButton)).perform(ViewActions.click())
        onView(withRecyclerView(R.id.originList).atPositionOnView(1, R.id.rowText)).check(matches(isDisplayed()))
        //      onView(withRecyclerView(R.id.originList).atPositionOnView(1,R.id.rowText)).check(matches(hasText(1,"Test One")))
        onView(withRecyclerView(R.id.originList).atPositionOnView(1, R.id.editEndButton)).check(
            matches(
                withEffectiveVisibility(Visibility.GONE)
            )
        )

    }
}

class CustomMatchers {
    var innerText: String? = null

    companion object {
        fun hasText(position: Int, testString: String): Matcher<View> {
            return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
                override fun describeTo(description: Description?) {
                    description?.appendText("position $position of the recycler view has $testString")
                }

                override fun matchesSafely(view: RecyclerView?): Boolean {
                    if (view !is RecyclerView) throw IllegalStateException("The asserted view is not RecyclerView")
                    if (view.adapter == null) throw IllegalStateException("No adapter is assigned to RecyclerView")
                    else {
                        val holder = view.findViewHolderForAdapterPosition(position)
                        val text = holder?.itemView?.rowText?.text ?: "null"
                        val judge = (text == testString)
                        return judge
                    }
                }
            }
        }
    }
}

fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher {
    return RecyclerViewMatcher(recyclerViewId)
}

class RecyclerViewMatcher(val mRecyclerViewId: Int) {
    fun atPosition(position: Int): Matcher<View> {
        return atPositionOnView(position, -1)
    }

    fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            var resources: Resources? = null
            var childView: View? = null
            override fun describeTo(description: Description?) {
                val id = if (targetViewId == -1) {
                    mRecyclerViewId
                } else {
                    targetViewId
                }
                var idDescription = id.toString()
                this.resources?.let {
                    try {
                        idDescription = it.getResourceName(id)
                    } catch (var4: Resources.NotFoundException) {
                        idDescription += "$id not found"
                    }
                }
                description?.appendText("with id:$idDescription")
            }
            override fun matchesSafely(item: View): Boolean {
                this.resources = item.resources
                if (childView == null) {
                    val recyclerView = item.rootView.findViewById<RecyclerView>(mRecyclerViewId)
                    if (recyclerView != null) {
                        childView = recyclerView.findViewHolderForAdapterPosition(position)?.itemView
                    } else {
                        return false
                    }
                }
                if (targetViewId == -1) {
                    return (item == childView)
                } else {
                    val targetView = childView?.findViewById<View>(targetViewId)
                    return (item == targetView)
                }
            }
        }
    }
}