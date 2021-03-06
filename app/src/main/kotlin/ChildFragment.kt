package com.example.voicelist

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.child_list.*

class ChildFragment : Fragment() {
    private lateinit var mAdaptor: ChildListAdaptor
    private lateinit var vModel: MainViewModel

    companion object {
        @JvmStatic
        fun newInstance() = ChildFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vModel = ViewModelProviders.of(this.activity!!).get(MainViewModel::class.java)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.child_list, container, false)
        // Set the adapter
        mAdaptor = ChildListAdaptor(vModel)
        mAdaptor.setUIHandler(object : DeliverEvent {
            override fun advanceChildToChild(itemToGo: String, _list: List<String>) {
                val trace = vModel.pushNextNavigation(itemToGo)
                Log.i("transit", "$trace is pushed from $itemToGo")
                mAdaptor.changeListItem(itemToGo, _list)
                mAdaptor.notifyDataSetChanged()
            }
            override fun backChildToChild(itemToBack: String, _list: List<String>) {
                val trace = vModel.popNavigation()
                Log.i("transit", "$trace was pop, going to $itemToBack")
                mAdaptor.notifyDataSetChanged()
            }
            override fun onGotoOrigin() {
                transitChildToOrigin()
            }
        })
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childList.adapter = mAdaptor

    }
    /*   override fun onStart() {
           super.onStart()
           vModel.liveList.observe(this, Observer{
               mAdaptor.changeListItem(vModel.navigationHistory.last(),vModel.getChildOf(vModel.navigationHistory.last()) )
           })
       }*/
    interface DeliverEvent {
        fun advanceChildToChild(itemToGo: String, _list: List<String>)
        fun backChildToChild(itemToBack: String, _list: List<String>)
        fun onGotoOrigin()
    }
    fun transitChildToOrigin() {
        activity!!.supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            .replace(R.id.activityFrame, OriginFragment.newInstance())
            .commit()
    }
}
