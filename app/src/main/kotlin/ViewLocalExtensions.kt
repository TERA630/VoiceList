package com.example.voicelist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.ViewAnimator


fun View.hideSoftKeyBoard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun View.showSoftKeyBoard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    imm.hideSoftInputFromWindow(windowToken, InputMethodManager.SHOW_IMPLICIT)
}

fun ViewParent.findAscendingRecyclerView(): RecyclerView? {
    if (this is RecyclerView) return this
    var thisGroup: ViewGroup? = this as ViewGroup
    while (thisGroup != null) {
        val view = findRecyclerView(thisGroup)
        if (view != null) return view
        // 同じ階層にRecyclerViewがなければ上の階層を探す
        val parentOfThisGroup = thisGroup.parent
        if (parentOfThisGroup != null) {
            thisGroup = parentOfThisGroup as ViewGroup
        } else return null // もう上の階層が無い場合はNULLをかえす
    }
    return null
}

fun findDescendingEditorText(viewGroup: ViewGroup): EditText? {
    val groupCount = viewGroup.childCount
    for (i in 0..groupCount) {
        val view = viewGroup.getChildAt(i)
        if (view is EditText) return view
        else if (view is ViewGroup) {
            val childView = findDescendingEditorText(view)
            if (childView != null) return childView
        }
    }
    return null
}

fun findDescendingTextView(viewGroup: ViewGroup): TextView? {
    val groupCount = viewGroup.childCount
    for (i in 0..groupCount) {
        val view = viewGroup.getChildAt(i)
        if (view is TextView) return view
        else if (view is ViewGroup) {
            val childView = findDescendingTextView(view)
            if (childView != null) return childView
        }
    }
    return null
}

fun findRecyclerView(viewGroup: ViewGroup): RecyclerView? {
    if (viewGroup is RecyclerView) return viewGroup
    val groupCount = viewGroup.childCount
    for (i in 0..groupCount) {
        val view = viewGroup.getChildAt(i)
        if (view is RecyclerView) return view
    }
    return null
}

fun findDescendingTextViewrAtPosition(recyclerView: RecyclerView, position: Int): TextView? {
    val childView = recyclerView.getChildAt(position)
    if (childView is TextView) return childView
    return if (childView is ViewGroup) {
        findDescendingTextView(childView)
    } else null
}

fun findDescendingEditorAtPosition(recyclerView: RecyclerView, position: Int): EditText? {
    val childView = recyclerView.getChildAt(position)
    if (childView is EditText) return childView
    return if (childView is ViewGroup) {
        findDescendingEditorText(childView)
    } else null
}

fun findViewAnimatorAt(recyclerView: RecyclerView, position: Int): ViewAnimator? {
    val childView = recyclerView.getChildAt(position)
    if (childView is ViewAnimator) return childView
    return if (childView is ViewGroup) {
        findDescendingViewAnimator(childView)
    } else null
}

fun findDescendingViewAnimator(viewGroup: ViewGroup): ViewAnimator? {
    val groupCount = viewGroup.childCount
    for (i in 0..groupCount) {
        val view = viewGroup.getChildAt(i)
        if (view is ViewAnimator) return view
        else if (view is ViewGroup) {
            val childView = findDescendingViewAnimator(view)
            if (childView != null) return childView
        }
    }
    return null
}