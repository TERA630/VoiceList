package com.example.voicelist

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_origin.*

private const val ARG_PARAM1 = "param1"

class OriginFragment : Fragment() {
    private lateinit var mAdaptor: OriginListAdaptor
    companion object {
        @JvmStatic
        fun newInstance() =
            OriginFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = ViewModelProviders.of(this.activity!!).get(MainViewModel::class.java)

        mAdaptor = OriginListAdaptor(model)
            mAdaptor.setUIHandler(object : DeliverEventToActivity {
                override fun onUserInterAction(parentString: String, _list: List<String>) {
                    val activity = this@OriginFragment.activity
                    if (activity is MainActivity) activity.transitOriginToChildFragment(parentString, ArrayList(_list))
                    else Log.w("test", "fail to handle adaptor event")
                }
            })
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
        originList.setHasFixedSize(true)
    }

    override fun onStart() {
        super.onStart()
        val model = ViewModelProviders.of(this.activity!!).get(MainViewModel::class.java)
        model.liveList.observe(this, Observer {
            mAdaptor.notifyItemInserted(0)
            originList.smoothScrollToPosition(model.getLiveList().lastIndex)
        })
    }
    interface DeliverEventToActivity {
        fun onUserInterAction(parentString: String, _list: List<String>)
    }
}
