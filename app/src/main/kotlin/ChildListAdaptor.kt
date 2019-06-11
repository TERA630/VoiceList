package com.example.voicelist

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.childlist_contents.view.*
import kotlinx.android.synthetic.main.childlist_header.view.*
import kotlinx.android.synthetic.main.fragment_item.view.childRowText

class ChildListAdaptor(
    private val model: MainViewModel,
    private val parentString: String,
    private val mList: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val childHeader = 0
    private val childContents = 1
    private lateinit var mUIHandler: ChildFragment.DeliverEvent

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) childHeader else childContents
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = if (viewType == 0) inflater.inflate(R.layout.childlist_header, parent, false)
        else inflater.inflate(R.layout.childlist_contents, parent, false)
        return ChildRowHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            (holder as ChildRowHolder).itemView.childRowText.text = parentString
            holder.itemView.backToParent.setOnClickListener { v ->

                //                Snackbar.make(v, "$parentString", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
            }
        } else {
            val childHeader = mList[position - 1]
            (holder as ChildRowHolder).itemView.childRowText.text = childHeader
            val originIndex = model.findIndexOfOrigin(childHeader)
            if (originIndex > 0 && model.getChildListAt(originIndex).isNotEmpty()) {
                holder.itemView.goChild.visibility = View.VISIBLE
                holder.itemView.goChild.setOnClickListener { v ->
                    Log.i("test", "child with child was clicked")
                    mUIHandler.onUserInterAction(childHeader, model.getChildListAt(originIndex))
                }

            } else {
                holder.itemView.goChild.visibility = View.GONE
            }
        }
    }

    // public method
    fun setUIHandler(_handler: ChildFragment.DeliverEvent) {
        this.mUIHandler = _handler
    }

    override fun getItemCount(): Int = mList.size + 1

    class ChildRowHolder
        (private val mView: View) : RecyclerView.ViewHolder(mView)
}
