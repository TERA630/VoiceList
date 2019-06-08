package com.example.voicelist

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

class MainViewModel : ViewModel() {
    val errorList = listOf("OriginList", "was", "null", "or", "empty", "Check", "the", "code.")
    var liveList: MutableLiveData<MutableList<String>> = MutableLiveData()
    // Init
    fun initLiveList(_list: MutableList<String>) {
        liveList.postValue(_list)
    }

    // Public methods
    fun getLiveListAsArrayList(): ArrayList<String> {
        if (liveList.value == null) return ArrayList(errorList)
        val safeOriginList = liveList.value as MutableList<String>
        return ArrayList(safeOriginList)
    }

    fun getChildListAt(index: Int): List<String> {
        if (liveList.value == null) return errorList
        else {
            val safeOriginList = liveList.value as MutableList<String>
            val headAndChildCSV = safeOriginList[index]
            val list = headAndChildCSV.split(",")
            return list.drop(1)
        }
    }

    fun getLiveList(): List<String> {
        if (liveList.value == null) return errorList
        else {
            return liveList.value as MutableList<String>
        }
    }

    fun setOriginListAt(index: Int, _value: String) {
        if (liveList.value == null) return
        else {
            val safeLiveList = liveList.value as MutableList<String>
            safeLiveList[index] = _value
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