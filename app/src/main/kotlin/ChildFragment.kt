package com.example.voicelist

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ChildFragment.OnListFragmentInteractionListener] interface.
 */
class ChildFragment : Fragment() {
    private val mitemList = "itemList"

    private var columnCount = 1
    private var listener: OnListFragmentInteractionListener? = null
    private var mList: List<String> = emptyList()
    private lateinit var mAdaptor: MyItemRecyclerViewAdapter

    companion object {
        @JvmStatic
        fun newInstance(_list: List<String>) =
            ChildFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(mitemList, ArrayList(_list))
                }
            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mList = it.getStringArrayList(mitemList)!!.toList()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
        // Set the adapter
        mAdaptor = MyItemRecyclerViewAdapter(mList, listener)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + "must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: String)
    }
}
