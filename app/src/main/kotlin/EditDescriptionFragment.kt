package com.example.voicelist

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_edit_description.*

class EditDescriptionFragment : Fragment() {
    private lateinit var vModel: MainViewModel
    companion object {
        @JvmStatic
        fun newInstance(originIndex:Int,childIndex:Int): OriginFragment {
            val bundle = Bundle()
            bundle.putInt("originIndex",originIndex)
            bundle.putInt("childIndex",childIndex)
            return OriginFragment()
        }
    }

    // Fragment lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vModel = ViewModelProviders.of(this.activity!!).get(MainViewModel::class.java)
        val originIndex = arguments?.getInt("originIndex") ?: 0
        val childIndex = arguments?.getInt("childIndex") ?: 0
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? {
        return inflater.inflate(R.layout.fragment_edit_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val originIndex = arguments?.getInt("originIndex") ?: 0
        val childIndex = arguments?.getInt("childIndex") ?: 0

        val (rowTitle,rowDescription) = vModel.getPairTitleAndDescription(originIndex,childIndex)


    }

}