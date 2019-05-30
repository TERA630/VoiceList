package com.example.voicelist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.card_list.view.*
import java.util.ArrayList

class CardListAdaptor(private var mResults: ArrayList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun addResult(result: String) {
        mResults.add(0, result)
        notifyItemInserted(0)
    }
    fun upDateResultList(stringArray: ArrayList<String>) {
        mResults = stringArray
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = mResults.size
    fun getResults(): ArrayList<String> = mResults

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.rowText.text = mResults[position]
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val rowView = LayoutInflater.from(parent.context).inflate(R.layout.card_list, parent, false)
        return ViewHolderOfCell(rowView)
    }
    class ViewHolderOfCell(rowView: View) : RecyclerView.ViewHolder(rowView)
}