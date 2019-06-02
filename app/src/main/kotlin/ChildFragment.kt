package com.example.voicelist

import android.os.Bundle
import android.support.v4.app.Fragment
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChildFragment.
         */
        // TODO: Rename and change types and number of parameters
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
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
        // Set the adapter
        mAdaptor = ChildListAdaptor(mParentString, mList)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentList.adapter = mAdaptor
    }
}
