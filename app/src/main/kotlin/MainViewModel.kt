package com.example.voicelist

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


class MainViewModel : ViewModel() {
    val errorList = listOf("OriginList", "was", "null", "or", "empty", "Check", "the", "code.")
    var originList: MutableLiveData<MutableList<String>> = MutableLiveData()

    fun initLiveList(_list: MutableList<String>) {
        originList.postValue(_list)
    }

    fun getOriginList(): List<String> {
        if (originList.value == null) return errorList
        else {
            val titleRegex = "^(.+):origin.*".toRegex()
            val safeOriginList = originList.value as MutableList<String>
            val result = mutableListOf<String>()
            for (i in safeOriginList.indices) {
                val matchResult = titleRegex.matchEntire(safeOriginList[i])
                matchResult?.destructured?.let { (header) ->
                    result.add(header)
                }
            }
            return result
        }
    }

    fun getChildListAt(index: Int): List<String> {
        if (originList.value == null) return errorList
        else {
            val safeOriginList = originList.value as MutableList<String>
            val headAndChildCSV = safeOriginList[index]
            val list = headAndChildCSV.split(",")
            return list.drop(1)
        }
    }

    fun setOriginListAt(index: Int, _value: String) {
        if (originList.value == null) return
        else {
            val safeOriginList = originList.value as MutableList<String>
            safeOriginList[index] = _value
            originList.postValue(safeOriginList)
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