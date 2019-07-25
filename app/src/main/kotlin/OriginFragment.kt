package com.example.voicelist

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_origin.*
import kotlinx.android.synthetic.main.origin_footer.*

class OriginFragment : Fragment() {
    private lateinit var mAdaptor: OriginListAdaptor
    private lateinit var vModel: MainViewModel
    companion object {
        @JvmStatic
        fun newInstance(): OriginFragment {
            return OriginFragment()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vModel = ViewModelProviders.of(this.activity!!).get(MainViewModel::class.java)
        mAdaptor = OriginListAdaptor(vModel)
        mAdaptor.setUIHandler(object : EventToFragment {
            override fun transitOriginToChild(parentToGo: String) {
                activity?.let {
                    it.supportFragmentManager.beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(
                            R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_right, R.anim.slide_out_left
                        )
                        .replace(R.id.activityFrame, ChildFragment.newInstance())
                        .commit()
                        vModel.pushNextNavigation(parentToGo)
                    }
                }
            override fun transitOriginToDescription(indexOfOrigin: Int, indexOfChild: Int) {
                    activity?.let{
                        it.supportFragmentManager.beginTransaction()
                        .addToBackStack(null)
                            .setCustomAnimations(
                                R.anim.slide_in_right, R.anim.slide_out_left,
                                R.anim.slide_in_left, R.anim.slide_out_right
                            )
                        .replace(R.id.activityFrame,EditDescriptionFragment.newInstance(indexOfOrigin,indexOfChild))
                        .commit()
                }
            }
            override fun startHearing(textView: TextView) {
                val activity = activity
                if (activity != null && activity is MainActivity) activity.startVoiceRecorder(originNewText)
                else throw IllegalStateException("activity was not reached by fragment")
            }
            override fun stopHearing() {
                val activity = activity
                if (activity != null && activity is MainActivity) activity.stopVoiceRecorder()
                else throw IllegalStateException("activity was not reached by fragment")
            }
        })
    } // One Create
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_origin, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originList.adapter = mAdaptor
    }
    override fun onStart() {
        super.onStart()
        vModel.liveList.observe(this, Observer {
            val old = vModel.getPreviousLiveList()
            val new = vModel.getLiveList()
            val diff = DiffUtil.calculateDiff(DiffCallback(old, new), false)
            diff.dispatchUpdatesTo(mAdaptor)
        })
    }
    interface EventToFragment {
        fun transitOriginToChild(parentToGo: String)
        fun transitOriginToDescription(indexOfOrigin:Int,indexOfChild:Int)
        fun startHearing(textView: TextView)
        fun stopHearing()
    }
}
