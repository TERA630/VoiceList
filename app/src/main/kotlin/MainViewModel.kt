package com.example.voicelist

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ViewAnimator

class MainViewModel : ViewModel() {
    private val errorList = listOf("OriginList", "was", "null", "or", "empty", "Check", "code.")
    var liveList: MutableLiveData<MutableList<String>> = MutableLiveData()
    val navigationHistory = mutableListOf("origin")
    var deleteHistory = mutableListOf<String>()

    // Initialization of liveList   Must to be called at First.. before calling other methods
    fun initLiveList(_list: MutableList<String>) {
        liveList.postValue(_list)
    }
    // Public methods which deals liveList

    fun addLiveList(_value: String) {
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        val safeLiveList = liveList.value as MutableList<String>
        safeLiveList.add(_value)
        liveList.postValue(safeLiveList)
    }
    private fun addLiveListAt(indexOfOrigin: Int, _value: String) {
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        val safeLiveList = liveList.value as MutableList<String>
        safeLiveList.add(indexOfOrigin, _value)
        liveList.postValue(safeLiveList)
    }
    fun appendChildAt(_indexOfOrigin: Int, _value: String) {
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        else if (_value.isBlank()) return
        else {
            val safeLiveList = liveList.value as MutableList<String>
            val safeLiveListDestructed = safeLiveList[_indexOfOrigin].split(",").toMutableList()
            safeLiveListDestructed.add(_value)
            val newListElement = safeLiveListDestructed.joinToString()
            safeLiveList[_indexOfOrigin] = newListElement
            liveList.postValue(safeLiveList)
        }
    }
    fun deleteLiveListAt(indexOfLiveList: Int) {
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        else {
            val safeLiveList = liveList.value as MutableList<String>
            val itemToDelete = StringBuilder("$indexOfLiveList:")
                .append(safeLiveList[indexOfLiveList])
                .toString()
            deleteHistory.add(itemToDelete)
            safeLiveList.removeAt(indexOfLiveList)
        }
    }

    fun deleteChildOfOriginAt(indexOfOrigin: Int, indexOfChild: Int) {
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        else {
            val safeLiveList = liveList.value as MutableList<String>
            val safeLiveListDestructed = safeLiveList[indexOfOrigin].split(",").toMutableList()
            Log.i("Origin", "$safeLiveListDestructed[$indexOfChild] will be deleted..")
            safeLiveListDestructed.removeAt(indexOfChild)
            val result = safeLiveListDestructed.joinToString()
            safeLiveList[indexOfOrigin] = result
            liveList.postValue(safeLiveList)
        }
    }
    fun indexOfOriginOf(_string: String): Int {
        return getOriginList().indexOfFirst { it.matches("^$_string".toRegex()) }
    }

    fun getChildListAt(indexOfLiveList: Int): List<String> {
        val headAndChildCSV = getLiveList()[indexOfLiveList]
        val list = headAndChildCSV.split(",")
        return list.drop(1)
    }

    fun getChildOf(_parent: String): List<String> {
        val indexOfOrigin = indexOfOriginOf(_parent)
        return if (indexOfOrigin >= 0) {
            val result = getChildListAt(indexOfOrigin)
            result
        } else {
            // ParentはChildを持っているはず､理論的には来ない
            errorList
        }
    }
    fun getLiveList(): List<String> {
        return if (liveList.value == null) errorList
        else liveList.value as MutableList<String>
    }
    fun getOriginList(): MutableList<String> {
        // Liveリストの先頭要素のみを並べたもの
        val safeLiveList = getLiveList()
        val safeLiveListHeaders = mutableListOf<String>()
        for (i in safeLiveList.indices) {
            val list = safeLiveList[i].split(",")
            safeLiveListHeaders.add(list[0])
        }
        return safeLiveListHeaders
    }

    fun pushNextNavigation(_traceOfParent: String) {
        navigationHistory.add(_traceOfParent)
    }
    fun popNavigation(): String {
        val result = navigationHistory.last()
        if (navigationHistory.size > 1) {
            navigationHistory.removeAt(navigationHistory.lastIndex)
        }
        return result
    }

    fun restoreDeleted() {
        if (deleteHistory.size == 0) return
        val itemToRestore = deleteHistory.last()
        Regex("(.+):(.+)").find(itemToRestore)?.destructured?.let { (index, originValue) ->
            Log.i("Origin", "$originValue will be restored at $index")
            addLiveListAt(index.toInt(), originValue)
        }
        deleteHistory.removeAt(deleteHistory.lastIndex)
    }

    fun setLiveListAt(indexOfOrigin: Int, columnIndex: Int, _value: String) { // CSV 形式のリストに　値を設定します。
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        else {
            val safeLiveList = liveList.value as MutableList<String>
            val safeLiveListDestructed = safeLiveList[indexOfOrigin].split(",").toMutableList()
            safeLiveListDestructed[columnIndex] = _value

            val newListElement = safeLiveListDestructed.joinToString()
            safeLiveList[indexOfOrigin] = newListElement
            liveList.postValue(safeLiveList)
        }
    }
}

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
fun findRecyclerView(viewGroup: ViewGroup): RecyclerView? {
    if (viewGroup is RecyclerView) return viewGroup
    val groupCount = viewGroup.childCount
    for (i in 0..groupCount) {
        val view = viewGroup.getChildAt(i)
        if (view is RecyclerView) return view
    }
    return null
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