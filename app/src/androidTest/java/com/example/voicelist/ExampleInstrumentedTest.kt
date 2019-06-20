package com.example.voicelist

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.util.Log
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
        fun findViewAt(targetViewId: Int, position: Int): Matcher<View> {
            return object : TypeSafeMatcher<View>() {
                override fun describeTo(description: Description?) {
                    //  val recyclerViewName = Resources.getSystem().getResourceName(mRecyclerViewId)
                    description?.appendText("$targetViewId with $position of recycler view")
                }
                override fun matchesSafely(item: View): Boolean {
                    val targetView = item.findViewById<View>(targetViewId) ?: return false
                    val targetClass = targetView::class.java
                    val itemView = findViewAtAdapterPosition(position, targetView) ?: return false
                    if (itemView::class.java == targetClass) return true
                    else if (itemView is ViewGroup) {
                        val childView = findContainingView(itemView, targetClass)
                        if (childView != null) return true
                    } else return false
                    return false
                }
            }
        }

        fun findContainingView(viewGroup: ViewGroup, targetclass: Class<out View>): View? {
            val groupCount = viewGroup.childCount
            for (i in 0..groupCount) {
                val view = viewGroup.getChildAt(i) ?: continue
                if (view::class.java == targetclass) {
                    Log.i("match", "$view in $viewGroup matched $targetclass")
                    return view
                } else if (view is ViewGroup) {
                    val childView = findContainingView(view, targetclass)
                    if (childView != null) return childView
                }
            }
            return null
        }

        fun findViewAtAdapterPosition(position: Int, targetView: View): View? {
            return findRecyclerView(targetView)?.findViewHolderForAdapterPosition(position)?.itemView ?: return null
            // val targetClass = targetView::class.java
            // if(itemView::class.java == targetClass) return itemView
            // else if(itemView is ViewGroup) return findContainingView(itemView,targetClass)
            // else return null
    }

        private fun findRecyclerView(view: View): RecyclerView? {
            if (view is RecyclerView) return view // いきなりマッチした場合
            if (view is ViewGroup) {
                val groupCount = view.childCount
            for (i in 0..groupCount) {
                val childView = view.getChildAt(i)
                if (childView is RecyclerView) return childView
                else if (childView is ViewGroup) findRecyclerView(childView) // さらに子ビューがあればネストして探していく
            }
            }
            return null
        }
    }
}
// Activity Test
