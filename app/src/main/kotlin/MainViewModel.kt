package com.example.voicelist

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log

class MainViewModel : ViewModel() {
    private val errorList = mutableListOf("OriginList", "was", "null", "or", "empty")
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
        val (new, current) = getWorkAndCurrentList()
        new.add(_value)
        saveCurrentAndPost(new, current)
    }
    fun appendChildAt(_indexOfOrigin: Int, _value: String) {
        val (new, current) = getWorkAndCurrentList()
        when {
            _value.isBlank() -> return
            else -> {
                val currentElements = current[_indexOfOrigin].split(",").toMutableList()
                currentElements.add(_value)
                new[_indexOfOrigin] = currentElements.joinToString()
                saveCurrentAndPost(new, current)
            }
        }
    }
    fun deleteLiveListAt(indexOfLiveList: Int) {
        val (new, current) = getWorkAndCurrentList()
        val itemToDelete = StringBuilder("$indexOfLiveList:").append(current[indexOfLiveList])
        deleteHistory.add(itemToDelete.toString())
        new.removeAt(indexOfLiveList)
        saveCurrentAndPost(new, current)
    }
    fun deleteChildOfOriginAt(indexOfOrigin: Int, indexOfChild: Int) {
        val (new, current) = getWorkAndCurrentList()
        val element = current[indexOfOrigin].split(",").toMutableList()
        element.removeAt(indexOfChild)
        new[indexOfOrigin] = element.joinToString()
        saveCurrentAndPost(new, current)
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

    fun getLiveList(): MutableList<String> {
        return if (liveList.value == null) errorList
        else liveList.value as MutableList<String>
    }
    fun getOriginList(): MutableList<String> {
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

    fun getPreviousLiveList(): List<String> {
        return previousLiveListStr
    }

    private fun getWorkAndCurrentList(): Pair<MutableList<String>, List<String>> {
        if (liveList.value == null) throw IllegalStateException("Live list was not initialized.")
        else {
            val current = List(liveList.value!!.size) { index -> liveList.value!![index] }
            val new = MutableList(liveList.value!!.size) { index -> liveList.value!![index] }
            return Pair(new, current)
        }
    }
    fun indexOfOriginOf(_string: String): Int {
        return getOriginList().indexOfFirst { it.startsWith(_string) }
    }
    private fun insertLiveListAt(indexOfOrigin: Int, _value: String) {
        val (new, current) = getWorkAndCurrentList()
        new.add(indexOfOrigin, _value)
        saveCurrentAndPost(new, current)
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

    private fun saveCurrentAndPost(newList: MutableList<String>, current: List<String>) {
        val oldList = List(current.size) { index -> current[index] }
        previousLiveListStr = oldList
        liveList.postValue(newList)
    }

    fun setDescriptionAt(rowTitle: String, description: String, indexOfOrigin: Int, indexOfChild: Int) {
        if (rowTitle.isEmpty() || description.isEmpty()) return
        setLiveListAt(indexOfOrigin, indexOfChild, "$rowTitle($description)")
    }
    fun setLiveListAt(indexOfOrigin: Int, columnIndex: Int, _value: String) { // CSV 形式のリストに　値を設定します。
        val (new, current) = getWorkAndCurrentList()
        val element = current[indexOfOrigin].split(",").toMutableList()
        element[columnIndex] = _value
        new[indexOfOrigin] = element.joinToString()
        saveCurrentAndPost(new, current)
    }
    fun setLiveListDefault() {
        val list = mutableListOf(
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
        if (liveList.value.isNullOrEmpty()) initLiveList(list)
        else saveCurrentAndPost(list.toMutableList(), getLiveList())
    }
}