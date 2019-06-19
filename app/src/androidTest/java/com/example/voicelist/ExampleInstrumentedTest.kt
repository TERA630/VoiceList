package com.example.voicelist

import android.content.res.Resources
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
import android.widget.TextView
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

    fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
): Matcher<View> {

    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("Child at position $position in parent ")
            parentMatcher.describeTo(description)
        }

        public override fun matchesSafely(view: View): Boolean {
            val parent = view.parent
            return (parent is ViewGroup && parentMatcher.matches(parent)
                    && view == parent.getChildAt(position))
        }
    }
}


fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher {
    return RecyclerViewMatcher(recyclerViewId)
}

class RecyclerViewMatcher(val mRecyclerViewId: Int) {

    fun findDescendingTextAtPosition(recyclerView: RecyclerView, position: Int): TextView? {
        val childView = recyclerView.getChildAt(position)
        if (childView is TextView) return childView
        return if (childView is ViewGroup) {
            findDescendingText(childView)
        } else null
    }

    fun findDescendingText(viewGroup: ViewGroup): TextView? {
        val groupCount = viewGroup.childCount
        for (i in 0..groupCount) {
            val view = viewGroup.getChildAt(i)
            if (view is TextView) return view
            else if (view is ViewGroup) {
                val childView = findDescendingEditorText(view)
                if (childView != null) return childView
            }
        }
        return null
    }
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

            override fun matchesSafely(tarGetView: View): Boolean {
                val recyclerView = tarGetView.findViewById<RecyclerView>(mRecyclerViewId)
                if (recyclerView == null) return false
                else {
                    childView = findViewAt(recyclerView, tarGetView, position)
                    return childView != null
                }
            }
        }
    }

    fun findViewAt(recyclerView: RecyclerView, targetView: View, position: Int): View? {
        val childView = recyclerView.getChildAt(position)
        if (childView::class.java == targetView::class.java) return childView
        return if (childView is ViewGroup) {
            findDescendingView(childView, targetView)
        } else null
    }

    fun findDescendingView(viewGroup: ViewGroup, targetView: View): View? {
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
}
}


