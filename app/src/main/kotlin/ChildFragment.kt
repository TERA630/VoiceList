package com.example.voicelist

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_item_list.*

class ChildFragment : Fragment() {
    private val mItemList = "itemList"
    private var mList: List<String> = emptyList()
    private lateinit var mAdaptor: MyItemRecyclerViewAdapter

    companion object {
        @JvmStatic
        fun newInstance(_list: List<String>) =
            ChildFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(mItemList, ArrayList(_list))
                }
            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mList = it.getStringArrayList(mItemList)!!.toList()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
        // Set the adapter
        mAdaptor = MyItemRecyclerViewAdapter(mList)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentList.adapter = mAdaptor
    }
}
