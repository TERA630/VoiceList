package com.example.voicelist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_item.view.*

class ChildListAdaptor(
    private val parentStirng: String,
    private val mList: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)
        return ChildRowHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ChildRowHolder).itemView.childRowText.setText(mList[position])
    }

    override fun getItemCount(): Int = mList.size

    class ChildRowHolder
        (private val mView: View) : RecyclerView.ViewHolder(mView)
}
