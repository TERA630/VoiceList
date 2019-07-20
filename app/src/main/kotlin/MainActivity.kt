package com.example.voicelist
import android.Manifest
import android.Manifest.permission
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

const val CURRENT_ITEMS ="currentItems"
const val REQUEST_CODE_RECORD = 1
const val COLOR_HEARING = "colorHearing"
const val COLOR_NOT_HEARING = "colorNotHearing"

class MainActivity : AppCompatActivity() {

    private lateinit var vModel: MainViewModel

    private var mSpeechService: SpeechService? = null
    private var mVoiceRecorder: VoiceRecorder? = null // given after on Start and permission was granted
    private var mVoiceTarget: TextView? = null

    private lateinit var mSpeechServiceListener: SpeechService.Listener // initialized by on Create
    private lateinit var mServiceConnection: ServiceConnection // initialized by startSpeechConnection
    private lateinit var mVoiceCallback: VoiceRecorder.Callback // initialized by onCreate
    private var mColorHearing = 0
    private var mColorNotHearing = 0

    // Activity life cycles
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vModel = ViewModelProviders.of(this@MainActivity).get(MainViewModel::class.java)
        setSupportActionBar(toolbar)
        makeOriginFragment(savedInstanceState, vModel)
        mColorHearing = getColor(R.color.status_hearing)
        mColorNotHearing = getColor(R.color.status_not_hearing)
        startSpeechConnection()
    }
    override fun onStart() {
        super.onStart()
        // Prepare Cloud Speech API
        val intent = Intent(this, SpeechService::class.java)
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        val hasPermission = checkAudioPermission()
        //       if (hasPermission) startVoiceRecorder()
    }
    override fun onStop() {
        //  mSpeechService?.removeListener(mSpeechServiceListener)
        unbindService(mServiceConnection)
        mSpeechService = null
        super.onStop()
    }
    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        // on Pauseや回転後 on Stop前
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.apply {
            putStringArrayList(CURRENT_ITEMS, ArrayList(vModel.getLiveList()))
            putInt(COLOR_HEARING, mColorHearing)
            putInt(COLOR_NOT_HEARING, mColorNotHearing)
        }
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        val stringArray = savedInstanceState?.getStringArrayList(CURRENT_ITEMS)
        stringArray?.let {
            vModel.initLiveList(it.toMutableList())
        }
        mColorHearing = savedInstanceState?.getInt(COLOR_HEARING) ?: 0x7B1FA2
        mColorNotHearing = savedInstanceState?.getInt(COLOR_NOT_HEARING) ?: 0x757575
    }
    override fun onPause() {
        super.onPause()
        stopVoiceRecorder()
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
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
            // AUDIO RECORD Permission Granted
            // startVoiceRecorder()
        } else if (shouldShowRequestPermissionRationale(permission.RECORD_AUDIO)) {
            Log.w("test", "permission request was disabled")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else {
            Log.w("test", "permission was refused by request")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    // Private function
    private fun checkAudioPermission(): Boolean {
        val audioPermission = ContextCompat.checkSelfPermission(this.baseContext, Manifest.permission.RECORD_AUDIO)
        when {
            audioPermission == PERMISSION_GRANTED -> {
                Log.i("test", "this app has already permission.")
                return true
            }
            shouldShowRequestPermissionRationale(permission.RECORD_AUDIO) -> {
                AlertDialog.Builder(this)
                    .setTitle("permission")
                    .setMessage(R.string.requireAudioPermission)
                Log.w("test", "permission request was disabled")
                return false
            }
            else -> {
                Log.w("test", "this app has no permission yet.")
                ActivityCompat.requestPermissions(this, arrayOf(permission.RECORD_AUDIO), REQUEST_CODE_RECORD)
                return false
            }
        }
    }
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
    private fun startSpeechConnection() {
        mSpeechServiceListener = object : SpeechService.Listener {
            override fun onSpeechRecognized(text: String, isFinal: Boolean) {
                if (isFinal) mVoiceRecorder?.dismiss()
                if (text.isNotEmpty()) {
                    runOnUiThread {
                        if (isFinal) {
                            conditionLabel.text = ""
                        } else {
                            conditionLabel.text = text
                            mVoiceTarget?.text = text
                            vModel.appendLiveList(text)
                        }
                    }
                }
            }
        }
        mServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder) {
                mSpeechService = SpeechService().from(service)
                mSpeechService?.addListener(listener = mSpeechServiceListener)
                status.visibility = View.VISIBLE
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                mSpeechService?.removeListener(mSpeechServiceListener)
                mSpeechService = null
            }
        }
        mVoiceCallback = object : VoiceRecorder.Callback { // 音声認識エンジン
            override fun onVoiceStart() {
                showStatus(true)
                val sampleRate = mVoiceRecorder?.getSampleRate() ?: 0
                if (sampleRate != 0) mSpeechService?.startRecognizing(sampleRate)
            }
            override fun onVoice(data: ByteArray, size: Int) {
                super.onVoice(data, size)
                showStatus(true)
                mSpeechService?.recognize(data, size)
            }
            override fun onVoiceEnd() {
                showStatus(false)
                mSpeechService?.finishRecognizing()
            }
        }
    }

    fun startVoiceRecorder(textView: TextView) {
        mVoiceRecorder?.stop()
        mVoiceRecorder = VoiceRecorder(mVoiceCallback)
        mVoiceTarget = textView
        mVoiceRecorder?.start()
    }

    fun stopVoiceRecorder() {
        mVoiceRecorder?.stop()
        mVoiceRecorder = null
    }
    private fun showStatus(hearingVoice: Boolean) {
        runOnUiThread {
            // UIスレッドにカラー変更処置を投げる。
            val color = if (hearingVoice) mColorHearing else mColorNotHearing
            status.setTextColor(color)
        }
    }

}