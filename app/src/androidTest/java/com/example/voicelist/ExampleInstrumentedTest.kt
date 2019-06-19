package com.example.voicelist

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
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
        onView(withRecyclerView(R.id.originList).findViewAt(R.id.rowText, position = 1)).check(matches(isDisplayed()))
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
        fun findViewAt(targetViewid: Int, position: Int): Matcher<View> {
            return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description?) {
                //  val recyclerViewName = Resources.getSystem().getResourceName(mRecyclerViewId)
                description?.appendText("$targetViewid with $position of recycelerview")
            }

                override fun matchesSafely(item: View): Boolean {
                    val targetView = item.findViewById<View>(targetViewid)
                    val recyclerView = findRecyclerView(item)
                    val childView = recyclerView?.getChildAt(position) ?: return false
                    // childView Recycler　viewのItemView  大抵はViewGroupであろう｡
                    if (childView.equals(targetView)) return true
                    val grandchildView = if (childView is ViewGroup) findDescendingView(childView, targetView)
                    else null
                    grandchildView?.let { if (it.equals(item)) return true }
                    return false
                }
            }
    }

        private fun findDescendingView(viewGroup: ViewGroup, targetView: View): View? {
        val groupCount = viewGroup.childCount
        for (i in 0..groupCount) {
            val view = viewGroup.getChildAt(i)
            if (view::class.java == targetView::class.java) return view
            else if (view is ViewGroup) {
                val childView = findDescendingView(view, targetView)
                if (childView != null) return childView
            }
        }
        return null
    }

        fun findRecyclerView(view: View): RecyclerView? {

            val groupCount = viewGroup.childCount
            for (i in 0..groupCount) {
                val view = viewGroup.getChildAt(i)
                if (view is RecyclerView) return view
            }
            return null
        }
}
}


