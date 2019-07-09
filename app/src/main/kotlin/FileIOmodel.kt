package com.example.voicelist

import android.content.Context
import android.content.Context.MODE_APPEND
import android.content.Context.MODE_PRIVATE
import android.util.Log
import java.io.*

const val VOICELIST_FILE = "voicelist.txt"

fun saveListToTextFile(context: Context, _list: List<String>) {
    try {
        val fileOut = context.openFileOutput(VOICELIST_FILE, MODE_PRIVATE and MODE_APPEND)
        val osw = OutputStreamWriter(fileOut, "UTF-8")
        val bw = BufferedWriter(osw)
        for (index in _list.indices) {
            val rowElement = _list[index]
            bw.write(rowElement)
            bw.newLine()
        }
        bw.close()
    } catch (e: Exception) {
        Log.e("test", "${e.message} occur by ${e.cause} at saveListToTxtFileAtSdCard")
        e.printStackTrace()
    }
}

fun saveListAsSCSV(context: Context, _list: List<String>) {
    var bw: BufferedWriter? = null
    val _data = _list.joinToString(";")
    try {
        val fileOut = context.openFileOutput(VOICELIST_FILE, MODE_PRIVATE and MODE_APPEND)
        val osw = OutputStreamWriter(fileOut, "UTF-8")
        bw = BufferedWriter(osw)
        bw.write(_data)
        bw.flush()
    } catch (e: Exception) {
        Log.e("test", "${e.message} occur by ${e.cause} at saveListToTxtFileAtSdCard")
        e.printStackTrace()
    } finally {
        bw?.close()
    }
}

fun loadSCSVFromTextFile(_context: Context): List<String>? {
    return try {
        val listWithLine = inputStreamToLines(_context.openFileInput(VOICELIST_FILE))
        if (listWithLine.isNullOrEmpty()) return null
        val result = listWithLine.joinToString("\n")
        val list = result.split(";")
        return list
    } catch (e: IOException) {
        Log.w("test", "${e.cause} bring {${e.message} at LoadListFromTextFile")
        null
    }
}
fun loadListFromTextFile(_context: Context, _fileName: String): List<String>? {
    return try {
        inputStreamToLines(_context.openFileInput(_fileName))
    } catch (e: Exception) {
        Log.w("test", "${e.cause} bring {${e.message} at LoadListFromTextFile")
        null
    }
}

fun inputStreamToLines(_inputStream: InputStream): List<String>? {
    return try {
        val br = BufferedReader(InputStreamReader(_inputStream))
        val result = br.readLines()
        br.close()
        result
    } catch (e: Exception) {
        Log.w("test", "${e.message} at inputStreamToLines")
        null
    }
}