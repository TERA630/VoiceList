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
        fun newInstance(originIndex:Int,childIndex:Int): EditDescriptionFragment {
            val bundle = Bundle().apply {
                putInt("originIndex", originIndex)
                putInt("childIndex", childIndex)
            }
            val fragment = EditDescriptionFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
    // Fragment lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vModel = ViewModelProviders.of(this.activity!!).get(MainViewModel::class.java)
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
        descriptionTitle.text = rowTitle
        rowDescription?.let{ editDescription.setText(it)}
        descriptionEditOK.setOnClickListener{
            val newDescription = editDescription.text.toString()
            if (newDescription.isNotEmpty()) vModel.setDescriptionAt(rowTitle,newDescription,originIndex,childIndex)
            backToListFragment()
        }
        descriptionEditCancel.setOnClickListener { backToListFragment() }
    }

    private fun backToListFragment() {
        val fm = activity?.supportFragmentManager
            ?: throw IllegalStateException("fail to approach fragment manager at EditDescriptionFragment.")
        fm.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(R.id.activityFrame, OriginFragment.newInstance())
            .commit()
    }
}