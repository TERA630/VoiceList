package com.example.voicelist

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*

const val CURRENT_ITEMS ="currentItems"

class MainActivity : AppCompatActivity() {
    private lateinit var vModel: MainViewModel
    private lateinit var mServiceConnection: ServiceConnection // initialized by onCreate
    private lateinit var mVoiceCallback: VoiceRecorder.Callback // initialized by onCreate

    private var mSpeechService: SpeechService? = null // given after SpeechService begun
    private var mVoiceRecorder: VoiceRecorder? = null // given after on Start and permission was granted


    // Activity life cycles
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vModel = ViewModelProviders.of(this@MainActivity).get(MainViewModel::class.java)
        setSupportActionBar(toolbar)
        makeOriginFragment(savedInstanceState, vModel)
    }

    override fun onStart() {
        super.onStart()
        // Prepare Cloud Speech API
        val intent = Intent(this, SpeechService::class.java)
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)

        val audioPermission = ContextCompat.checkSelfPermission(this.baseContext, Manifest.permission.RECORD_AUDIO)
        if (audioPermission == PackageManager.PERMISSION_GRANTED) {
            Log.i("test", "this app has already permission.")
            startVoiceRecorder()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            AlertDialog.Builder(this)
                .setTitle("permission")
                .setMessage("このアプリの利用には音声の録音を許可してください.")
            Log.w("test", "permission request was disabled")
        } else {
            Log.w("test", "this app has no permission yet.")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE_RECORD)
        }

    }
    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        // on Pauseや回転後 on Stop前
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.apply {
            putStringArrayList(CURRENT_ITEMS, ArrayList(vModel.getLiveList()))
        }
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        val stringArray = savedInstanceState?.getStringArrayList(CURRENT_ITEMS)
        stringArray?.let {
            vModel.initLiveList(it.toMutableList())
        }
    }
    override fun onPause() {
        super.onPause()
        saveListAsSCSV(baseContext, vModel.getLiveList())
        //  saveListToTextFile(baseContext, vModel.getLiveList())
    }
    // Activity Event
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_undo -> {
                vModel.restoreDeleted()
                true
            }
            R.id.action_restoreDefault -> {
                vModel.setLiveListDefault()
                true
            }
            R.id.action_saveCurrentToFile -> {
                saveListAsSCSV(baseContext, vModel.getLiveList())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    // Private function
    private fun makeOriginFragment(savedInstanceState: Bundle?, model: MainViewModel) {
        val result = loadSCSVFromTextFile(baseContext)
        result?.let { model.initLiveList(it.toMutableList()) } ?: vModel.setLiveListDefault()
        if (savedInstanceState == null) { // 初回起動でのみフラグメント追加
            val originFragment = OriginFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.activityFrame, originFragment)
                .commit()
        }
    }

    private fun startVoiceRecorder() {
        mVoiceRecorder?.stop()
        mVoiceRecorder = VoiceRecorder(mVoiceCallback)
        mVoiceRecorder?.start()
    }
}