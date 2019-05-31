package com.example.voicelist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.card_folder.view.*
import kotlinx.android.synthetic.main.card_list.view.*
import kotlinx.android.synthetic.main.card_list.view.rowText
import java.util.*

class CardListAdaptor(private var mResults: ArrayList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // ViewType 0 or except 1: item, 1: folder
    lateinit var mHander: onButtonClickHander


    fun addResult(result: String) {
        mResults.add(0, "$result,0")
        notifyItemInserted(0)
    }
    fun upDateResultList(stringArray: ArrayList<String>) {
        mResults = stringArray
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = mResults.size
    fun getResults(): ArrayList<String> = mResults

    override fun getItemViewType(position: Int): Int = extractViewTypeFromString(mResults[position])

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val list = mResults[position].split(",")
        holder.itemView.rowText.text = list[0]
        if (holder is ViewHolderOfFolder) {
            holder.childList = list.drop(2).toMutableList()
            holder.rowView.folderIcon.setOnClickListener { }

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            1 -> ViewHolderOfFolder(inflater.inflate(R.layout.card_folder, parent, false))
            else -> ViewHolderOfCell(inflater.inflate(R.layout.card_list, parent, false))
        }
    }

    fun setButtonClickHandlr(_handler: CardListAdaptor.onButtonClickHander) {
        this.mHander = _handler
    }
    // private method
    private fun extractViewTypeFromString(_string: String): Int {
        val list = _string.split(",")
        return list[1].toInt()
    }
    class ViewHolderOfCell(rowView: View) : RecyclerView.ViewHolder(rowView)
    class ViewHolderOfFolder(val rowView: View) : RecyclerView.ViewHolder(rowView) {
        var childList: MutableList<String> = emptyList<String>().toMutableList()
    }

    interface onButtonClickHander {
        fun onBottonClicked(view: View)
    }
}