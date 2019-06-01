package com.example.voicelist

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_origin.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

class OriginFragment : Fragment() {
    private lateinit var mAdaptor: OriginListAdaptor
    private lateinit var mList: MutableList<String>

    companion object {
        @JvmStatic
        fun newInstance(arrayList: ArrayList<String>) =
            OriginFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_PARAM1, arrayList)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val list = it.getStringArrayList(ARG_PARAM1)
            if (!list.isNullOrEmpty()) {
                mList = list.toMutableList()
            }
            mAdaptor = OriginListAdaptor(mList)
            mAdaptor.setUIHandler(object : DeliverEventToActivity {
                override fun onUserInterAction(parentString: String, _list: List<String>) {
                    val activity = this@OriginFragment.activity
                    if (activity is MainActivity) activity.transitOriginToChildFragment(parentString, ArrayList(_list))
                    else Log.w("test", "fail to handle adaptor event")
                }
            })
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_origin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originList.adapter = mAdaptor
        //   mAdaptor.setUIHandler()
    }

    interface DeliverEventToActivity {
        fun onUserInterAction(parentString: String, _list: List<String>)
    }

}
