package com.example.voicelist

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log

class MainViewModel : ViewModel() {
    private val errorList = listOf("OriginList", "was", "null", "or", "empty")
    var liveList: MutableLiveData<MutableList<String>> = MutableLiveData()
    var previousLiveListStr = listOf("origin")

    val navigationHistory = mutableListOf("origin")
    var deleteHistory = mutableListOf<String>()

    // Initialization of liveList   Must to be called at First.. before calling other methods
    fun initLiveList(_list: MutableList<String>) {
        liveList.postValue(_list)
    }
    // Public methods which deals liveList
    private fun addLiveListAt(indexOfOrigin: Int, _value: String) {
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        val safeLiveList = liveList.value as MutableList<String>
        safeLiveList.add(indexOfOrigin, _value)
        saveCurrentLiveListAndPostNew(safeLiveList)
    }

    fun appendLiveList(_value: String) {
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        val safeLiveList = liveList.value as MutableList<String>
        safeLiveList.add(_value)
        saveCurrentLiveListAndPostNew(safeLiveList)
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
            saveCurrentLiveListAndPostNew(safeLiveList)
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
            saveCurrentLiveListAndPostNew(safeLiveList)
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
            saveCurrentLiveListAndPostNew(safeLiveList)
        }
    }
    fun indexOfOriginOf(_string: String): Int {
        return getOriginList().indexOfFirst { it.startsWith(_string) }
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

    fun getPairTitleAndDescription(indexOfOrigin: Int, indexOfChild: Int): Pair<String, String?> {

        val element = getLiveList()[indexOfOrigin].split(",")[indexOfChild]
        val rowDescriptionMatch = Regex("""([^(]+)(\(.+?\))?""")
        rowDescriptionMatch.matchEntire(element)?.destructured?.let { (rowTitle, rowDescriptionBlanket) ->
            if (rowDescriptionBlanket.isNotBlank()) {
                val descriptionRange = IntRange(1, rowDescriptionBlanket.length - 2)
                val rowDescription = rowDescriptionBlanket.substring(descriptionRange) //　前後の()を削除
                return Pair(rowTitle, rowDescription)
            } else return Pair(rowTitle, null)
        }
        throw java.lang.IllegalStateException("rowTitle was null at getPairTitleAndDescription")
    }
    fun getPreviousLiveList(): List<String> {
        return previousLiveListStr
    }
    private fun saveCurrentLiveListAndPostNew(newList: MutableList<String>) {
        previousLiveListStr =
            liveList.value ?: throw java.lang.IllegalStateException("live list was not initialized at saveCurrent")
        Log.i("Origin", "old list is $previousLiveListStr, new list is $newList")
        liveList.postValue(newList)
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
            saveCurrentLiveListAndPostNew(safeLiveList)
        }
    }

    fun setLiveListDefault() {
        Log.i("origin", "make list default.")
        val list = listOf(
            "one(Square 1987),light,chaos",
            "two(Square 1988),Firion,Maria,Ricard,Minwu",
            "three,Monk,White Mage,Thief,Dragoon,Summoner",
            "four(Square 1990),Cecil,Kain,Rydia,Rosa,Edge",
            "five,Bartz,Faris,Galuf,Lenna,Krile",
            "six,Terra,Locke,Celes,Shadow,seven",
            "seven,Cloud,Tifa,Aeris,eight",
            "eight,Squall,Rinoa,Quistis",
            "nine,Zidane,Vivi,Garnet,Freya",
            "ten,Yuna"
        )
        liveList.postValue(
            list.toMutableList()
        )
    }
}
