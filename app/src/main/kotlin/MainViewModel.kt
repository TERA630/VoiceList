package com.example.voicelist

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log

class MainViewModel : ViewModel() {
    private val errorList = listOf("OriginList", "was", "null", "or", "empty")
    var liveList: MutableLiveData<MutableList<String>> = MutableLiveData()
    private var previousLiveListStr = listOf("origin")
    val navigationHistory = mutableListOf("origin")
    var deleteHistory = mutableListOf<String>()

    // Initialization of liveList   Must to be called at First.. before calling other methods
    fun initLiveList(_list: MutableList<String>) {
        liveList.postValue(_list)
    }
    // Public methods which deals liveList

    fun appendLiveList(_value: String) {
        val current = if (liveList.value == null) listOf("")
        else List(liveList.value!!.size) { index -> liveList.value!![index] }
        val new = MutableList(current.size) { index -> current[index] }
        new.add(_value)
        saveCurrentLiveListAndPostNew(new, current)
    }
    fun appendChildAt(_indexOfOrigin: Int, _value: String) {
        val current = if (liveList.value == null) listOf("")
        else List(liveList.value!!.size) { index -> liveList.value!![index] }
        val new = MutableList(current.size) { index -> current[index] }
        when {
            _value.isBlank() -> return
            else -> {
                val currentElements = current[_indexOfOrigin].split(",").toMutableList()
                currentElements.add(_value)
                val newListElement = currentElements.joinToString()
                new[_indexOfOrigin] = newListElement
                saveCurrentLiveListAndPostNew(new, current)
            }
        }
    }
    fun deleteLiveListAt(indexOfLiveList: Int) {
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        else {
            val current = List(liveList.value!!.size) { index -> liveList.value!![index] }
            val itemToDelete = StringBuilder("$indexOfLiveList:")
                .append(current[indexOfLiveList])
                .toString()
            deleteHistory.add(itemToDelete)
            val new = MutableList(liveList.value!!.size) { index -> liveList.value!![index] }
            new.removeAt(indexOfLiveList)
            saveCurrentLiveListAndPostNew(new, current)
        }
    }
    fun deleteChildOfOriginAt(indexOfOrigin: Int, indexOfChild: Int) {
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        else {
            val current = List(liveList.value!!.size) { index -> liveList.value!![index] }
            val element = current[indexOfOrigin].split(",").toMutableList()
            element.removeAt(indexOfChild)
            val result = element.joinToString()
            val new = MutableList(liveList.value!!.size) { index -> liveList.value!![index] }
            new[indexOfOrigin] = result
            saveCurrentLiveListAndPostNew(new, current)
        }
    }

    fun getChildListAt(indexOfLiveList: Int): List<String> {
        val headAndChildCSV = getLiveList()[indexOfLiveList]
        val list = headAndChildCSV.split(",")
        return list.drop(1)
    }

    fun getChildOf(_parent: String): List<String> {
        val indexOfOrigin = indexOfOriginOf(_parent)
        return if (indexOfOrigin >= 0) getChildListAt(indexOfOrigin)
        else errorList
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
        val rowDescriptionMatch = Regex("""([^(]+)(\([\S\s]+?\))?""")
        rowDescriptionMatch.matchEntire(element)?.destructured?.let { (rowTitle, rowDescriptionBlanket) ->
            if (rowDescriptionBlanket.isNotBlank()) {
                val descriptionRange = IntRange(1, rowDescriptionBlanket.length - 2)
                val rowDescription = rowDescriptionBlanket.substring(descriptionRange) //　前後の()を削除
                return Pair(rowTitle, rowDescription)
            } else {
                if (rowTitle.isEmpty()) {
                    Log.e("regEx", "$element was not decoded ")
                    return Pair("error", null)
                }
                return Pair(rowTitle, null)
            }
        }
        Log.e("regEx", "$element was not decoded ")
        return Pair("error", null)
    }

    fun indexOfOriginOf(_string: String): Int {
        return getOriginList().indexOfFirst { it.startsWith(_string) }
    }

    private fun insertLiveListAt(indexOfOrigin: Int, _value: String) {
        val current = if (liveList.value == null) listOf("")
        else List(liveList.value!!.size) { index -> liveList.value!![index] }
        val new = MutableList(current.size) { index -> current[index] }
        new.add(indexOfOrigin, _value)
        saveCurrentLiveListAndPostNew(new, current)
    }
    fun setDescriptionAt(rowTitle:String,description:String,indexOfOrigin: Int,indexOfChild: Int){
        if(rowTitle.isEmpty() || description.isEmpty()) return
        setLiveListAt(indexOfOrigin,indexOfChild,"$rowTitle($description)")
    }
    fun getPreviousLiveList(): List<String> {
        return previousLiveListStr
    }

    private fun saveCurrentLiveListAndPostNew(newList: MutableList<String>, current: List<String>) {
        val oldList = List(current.size) { index -> current[index] }
        previousLiveListStr = oldList
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
            insertLiveListAt(index.toInt(), originValue)
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
        if (liveList.value.isNullOrEmpty()) initLiveList(list.toMutableList())
        else saveCurrentLiveListAndPostNew(list.toMutableList())
    }
}
