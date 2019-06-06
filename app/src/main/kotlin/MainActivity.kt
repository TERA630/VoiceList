package com.example.voicelist

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*

const val CURRENT_ITEMS ="currentItems"

class MainActivity : AppCompatActivity() {
    private lateinit var mAdaptor: OriginListAdaptor
    // Activity life cycles
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val model = ViewModelProviders.of(this@MainActivity).get(MainViewModel::class.java)
        setSupportActionBar(toolbar)
        makeListAdaptor(savedInstanceState, model)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        val stringArray = savedInstanceState?.getStringArrayList(CURRENT_ITEMS)
        if (!stringArray.isNullOrEmpty()) mAdaptor.upDateResultList(stringArray)
    }
    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        // on Pauseや回転後 on Stop前
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.apply {
            putStringArrayList(CURRENT_ITEMS, ArrayList(mAdaptor.getResults()))
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
            else -> super.onOptionsItemSelected(item)

        }
    }

    fun transitOriginToChildFragment(parentString: String, arrayList: ArrayList<String>) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.activityFrame, ChildFragment.newInstance(parentString, arrayList.toList()))
            .commit()
    }
    // Private function
    private fun makeListAdaptor(savedInstanceState: Bundle?, model: MainViewModel) {
        val resultOptional = savedInstanceState?.getStringArrayList(CURRENT_ITEMS)
        val result =
            if (resultOptional.isNullOrEmpty()) arrayListOf(
                "one",
                "two,win,won,wan",
                "three",
                "four,hat,hut,hot",
                "five",
                "six,sox,sex,sax"
            )
            else resultOptional

        model.initLiveList(result.toList())

        if (savedInstanceState == null) { // 初回起動でのみフラグメント追加
            val originFragment = OriginFragment.newInstance(result)
            supportFragmentManager.beginTransaction()
                .add(R.id.activityFrame, originFragment)
                .commit()
        }
    }
}