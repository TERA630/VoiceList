package com.example.voicelist

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_item_list.*

class ChildFragment : Fragment() {
    private val mParentStringKey = "parentString"
    private val mItemListKey = "itemList"
    private var mParentString = ""
    private var mList: List<String> = emptyList()
    private lateinit var mAdaptor: ChildListAdaptor
    private lateinit var model: MainViewModel

    companion object {
        @JvmStatic
        fun newInstance(parent: String, _list: List<String>) =
            ChildFragment().apply {
                arguments = Bundle().apply {
                    putString(mParentStringKey, parent)
                    putStringArrayList(mItemListKey, ArrayList(_list))
                }
            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mList = it.getStringArrayList(mItemListKey)!!.toList()
            mParentString = it.getString(mParentStringKey)!!
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
        // Set the adapter
        model = ViewModelProviders.of(this.activity!!).get(MainViewModel::class.java)
        mAdaptor = ChildListAdaptor(model, mParentString, mList)
        mAdaptor.setUIHandler(object : DeliverEvent {
            override fun advanceChildToChild(itemToGo: String, _list: List<String>) {
                model.pushNextNavigation(itemToGo)
                transitChildToChild(itemToGo, _list)
            }

            override fun backChildToChild(itemToBack: String, _list: List<String>) {
                val trace = model.popNavigation()
                Log.i("navigaiton", "$trace was pop, going to $itemToBack")
                transitChildToChild(itemToBack, _list)
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

    interface DeliverEvent {
        fun advanceChildToChild(itemToGo: String, _list: List<String>)
        fun backChildToChild(itemToBack: String, _list: List<String>)
        fun onGotoOrigin()
    }

    fun transitChildToChild(parentString: String, listToShowNext: List<String>) {
        mAdaptor.updateList(listToShowNext)
        mAdaptor.setCurrentParent(parentString)
        mAdaptor.notifyDataSetChanged()
    }

    fun transitChildToOrigin() {
        activity!!.supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.activityFrame, OriginFragment.newInstance())
            .commit()
    }
}
