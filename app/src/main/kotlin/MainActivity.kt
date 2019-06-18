package com.example.voicelist

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*

const val CURRENT_ITEMS ="currentItems"

class MainActivity : AppCompatActivity() {
    private lateinit var vModel: MainViewModel
    // Activity life cycles
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vModel = ViewModelProviders.of(this@MainActivity).get(MainViewModel::class.java)
        setSupportActionBar(toolbar)
        makeOriginFragment(savedInstanceState, vModel)
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
            else -> super.onOptionsItemSelected(item)

        }
    }
    // Private function
    private fun makeOriginFragment(savedInstanceState: Bundle?, model: MainViewModel) {
        val resultOptional = savedInstanceState?.getStringArrayList(CURRENT_ITEMS)
        val result =
            if (resultOptional.isNullOrEmpty()) arrayListOf(
                "one,knight,mage",
                "two,Firion,Maria,Ricard,Minwu",
                "three,Monk,White Mage,Thief,Dragoon,Summoner",
                "four,Cecil,Kain,Rydia,Rosa,Edge",
                "five,Bartz,Faris,Galuf,Lenna,Krile",
                "six,Terra,Locke,Celes,Shadow,seven",
                "seven,Cloud,Tifa,Aeris,eight",
                "eight,Squall,Rinoa,Quistis",
                "nine,Zidane,Vivi,Garnet,Freya",
                "ten,Yuna"
            )
            else resultOptional

        model.initLiveList(result.toMutableList())

        if (savedInstanceState == null) { // 初回起動でのみフラグメント追加
            val originFragment = OriginFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.activityFrame, originFragment)
                .commit()
        }
    }
}