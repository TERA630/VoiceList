package com.example.voicelist

import android.content.Context
import android.content.Context.MODE_APPEND
import android.content.Context.MODE_PRIVATE
import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

const val VOICELIST_FILE = "voicelist.txt"

fun saveListToTextFile(context: Context, _list: List<String>) {
    try {
        val fileOut = context.openFileOutput(VOICELIST_FILE, MODE_PRIVATE and MODE_APPEND)
        val bw = BufferedWriter(OutputStreamWriter(fileOut, "UTF-8"))
        for (index in _list.indices) {
            bw.write(_list[index])
            bw.newLine()
        }
        bw.close()
    } catch (e: Exception) {
        Log.e("test", "${e.message} occur by ${e.cause} at saveListToTxtFileAtSdCard")
        e.printStackTrace()
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

fun inputStreamToLines(_inputStream: java.io.InputStream): List<String>? {
    return try {
        val isr = InputStreamReader(_inputStream)
        val br = BufferedReader(isr)
        val result = br.readLines()
        br.close()
        result
    } catch (e: Exception) {
        Log.w("test", "${e.message} at inputStreamToLines")
        null
    }
}