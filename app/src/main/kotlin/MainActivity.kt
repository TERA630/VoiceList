package com.example.voicelist

import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


const val CURRENT_ITEMS ="currentItems"

class MainActivity : AppCompatActivity() {
    private lateinit var mAdaptor:CardListAdaptor
    // Activity life cycles
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        makeListAdaptor(savedInstanceState)

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
    override fun onSaveInstanceState( outState: Bundle?,
        outPersistentState: PersistableBundle?) { // on Pauseや回転後 on Stop前
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.apply {
            putStringArrayList(CURRENT_ITEMS, mAdaptor.getResults())
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
    // Private function
    private fun makeListAdaptor(savedInstanceState: Bundle?){
        val resultOptional = savedInstanceState?.getStringArrayList(CURRENT_ITEMS)
        val result =
            if (resultOptional.isNullOrEmpty()) arrayListOf("one,0", "two,1", "three,0", "four,1", "five,0", "six,1")
        else resultOptional
        mAdaptor = CardListAdaptor(result)
        recyclerView.adapter = mAdaptor
        mAdaptor.setButtonClickHandlr(object : CardListAdaptor.onButtonClickHander {
            override fun onBottonClicked(view: View) {
                supportFragmentManager.beginTransaction()
                    .replace(activityFrame,)
            }
        })

    }
}
