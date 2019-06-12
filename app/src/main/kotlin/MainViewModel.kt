package com.example.voicelist

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager

class MainViewModel : ViewModel() {
    private val errorList = listOf("OriginList", "was", "null", "or", "empty", "Check", "the", "code.")
    var liveList: MutableLiveData<MutableList<String>> = MutableLiveData()
    val navigationHistory = mutableListOf("origin")

    // Initialization of liveList   Must to be called at First.. before calling other methods
    fun initLiveList(_list: MutableList<String>) {
        liveList.postValue(_list)
    }

    // Public methods which deals liveList

    fun addLiveList(_value: String) {
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        val safeLiveList = liveList.value as MutableList<String>
        safeLiveList.add(0, _value)
        liveList.postValue(safeLiveList)
    }

    fun findIndexOfOrigin(_string: String): Int {
        val result = getOriginList().indexOfFirst { it.matches("^$_string.*".toRegex()) }
        if (result != -1) Log.i("test", "$_string was found at $result")
        return result
    }
    fun getChildListAt(index: Int): List<String> {
        val headAndChildCSV = getLiveList()[index]
            val list = headAndChildCSV.split(",")
            return list.drop(1)
    }

    fun getChildOf(_parent: String): List<String> {
        val indexOfOrigin = findIndexOfOrigin(_parent)
        return if (indexOfOrigin > 0) {
            val result = getChildListAt(indexOfOrigin)
            result
        } else {
            // ParentはChildを持っているはず､理論的には来ない
            errorList
        }
    }

    fun getLiveList(): List<String> {
        return if (liveList.value == null) {
            Log.w("test", "Failed to access liveList.")
            errorList
        } else {
            liveList.value as MutableList<String>
        }
    }

    fun getLiveListAsArrayList(): ArrayList<String> {
        if (liveList.value == null) return ArrayList(errorList)
        val safeOriginList = liveList.value as MutableList<String>
        return ArrayList(safeOriginList)
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
    fun setLiveListAt(rowIndex: Int, columnIndex: Int, _value: String) { // CSV 形式のリストに　値を設定します。
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        else {
            val safeLiveList = liveList.value as MutableList<String>
            val safeLiveListDestructed = safeLiveList[rowIndex].split(",").toMutableList()
            safeLiveListDestructed[columnIndex] = _value
            val newListElement = safeLiveListDestructed.joinToString()
            safeLiveList[rowIndex] = newListElement
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
}