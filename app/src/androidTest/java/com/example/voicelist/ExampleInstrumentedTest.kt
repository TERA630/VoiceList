package com.example.voicelist

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.BoundedMatcher
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
        onView(withRecyclerView(R.id.originList).withTextAtPos("one", position = 0)).check(matches(isDisplayed()))
    }

    //    @Test
//    fun viewClick() {
//        //　テキストクリック→編集
//        val rowEdit = onView(withRecyclerView(R.id.originList).atPositionOnView(1, R.id.rowText))
//        rowEdit.perform(ViewActions.click())
//        //　テキスト編集、終了
//        val rowEditText = onView(withRecyclerView(R.id.originList).atPositionOnView(1, R.id.rowEditText))
//        rowEdit.check(matches(isDisplayed()))
//        rowEditText.perform(ViewActions.replaceText("Test One"))
//        //    val endButton = onView(withRecyclerView(R.id.originList).atPositionOnView(1, R.id.editEndButton))
//        //     endButton.check(matches(isDisplayed()))
////        val goLeftButton = onView(withRecyclerView(R.id.folderIcon).atPosition(1)).check(matches(isDisplayed()))
//
//        // テキスト編集終了
//        //    onView(withRecyclerView(R.id.originList).atPositionOnView(1,R.id.rowText)).check(matches(hasText(1,"Test One")))
////        onView(withRecyclerView(R.id.liveList).atPositionOnView(1, R.id.editEndButton)).check(matches(withEffectiveVisibility(Visibility.GONE))
////        )
//
//    }
//}
    private fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher {
        return RecyclerViewMatcher(recyclerViewId)
    }

    class RecyclerViewMatcher(val mRecyclerViewId: Int) {
        fun withTextAtPos(targetText: String, position: Int): Matcher<View> {
            return object : BoundedMatcher<View, TextView>(TextView::class.java) {
                override fun describeTo(description: Description?) {
                    //  val recyclerViewName = Resources.getSystem().getResourceName(mRecyclerViewId)
                    description?.appendText("$targetText with $position of recycler view is..")
                }
                override fun matchesSafely(item: TextView): Boolean {
                    if (item.text != targetText) return false
                    val recyclerView = item.parent.findAscendingRecyclerView()
                        ?: throw IllegalStateException("Recycler view was not found.")
                    if (recyclerView.id != mRecyclerViewId) return false
                    val itemView = recyclerView.findViewHolderForAdapterPosition(position)?.itemView ?: return false
                    val list = if (itemView is ViewGroup) enumerateViewWithin(itemView) else null
                    if (list.isNullOrEmpty()) return false
                    return list.contains(item)
                }
            }
        }

        fun enumerateViewWithin(viewGroup: ViewGroup): List<View>? {
            val result = mutableListOf<View>()
            val groupCount = viewGroup.childCount
            for (i in 0 until groupCount) {
                val view = viewGroup.getChildAt(i)
                result.add(view)
                if (view is ViewGroup) {
                    val childList = enumerateViewWithin(view) ?: continue
                    if (!childList.isNullOrEmpty()) result.addAll(childList)
                }
            }
            return result
        }
    }
}