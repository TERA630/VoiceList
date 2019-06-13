package com.example.voicelist

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.childlist_contents.view.*
import kotlinx.android.synthetic.main.childlist_header.view.*

class ChildListAdaptor(
    private val model: MainViewModel,
    private var mList: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val childHeader = 0
    private val childContents = 1
    private lateinit var mUIHandler: ChildFragment.DeliverEvent

    override fun getItemCount(): Int = mList.size + 1 // list と　Header
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
        val vH = holder as ChildRowHolder
        if (position == 0) {
            holder.mView.childHeaderText.text = model.navigationHistory.last()
            vH.itemView.backToParent.setOnClickListener {
                if (model.navigationHistory.size < 3) { // Origin -> child 1 の時
                    mUIHandler.onGotoOrigin()
                    val trace = model.popNavigation()
                    Log.i("transit", "from $trace to back to Origin")
                } else {// Origin -> child 1 -> child 2 以上の時
                    val lastParent = model.navigationHistory[model.navigationHistory.lastIndex - 1] // 一つ前のアイテムへ
                    mUIHandler.backChildToChild(lastParent, model.getChildOf(lastParent))
                    Log.i("transit", "back to $lastParent")
                }
            }
        } else { // Contents
            val childHeader = mList[position - 1]
            vH.mView.childContents.text = childHeader
            val originIndex = model.findIndexOfOrigin(childHeader)
            if (originIndex > 0 && model.getChildListAt(originIndex).isNotEmpty()) {
                vH.itemView.goChild.visibility = View.VISIBLE
                vH.itemView.goChild.setOnClickListener {
                    mUIHandler.advanceChildToChild(childHeader, model.getChildListAt(originIndex))
                    Log.i("transit", "from ${model.navigationHistory.last()} to $childHeader")
                }
            } else {
                holder.itemView.goChild.visibility = View.GONE
            }
        }
    }

    // public method
    fun updateList(_list: List<String>) {
        mList = _list
    }
    fun setUIHandler(_handler: ChildFragment.DeliverEvent) {
        this.mUIHandler = _handler // Fragmentのインスタンスやメンバを操作するため､インターフェイスを経由
    }


    class ChildRowHolder
        (val mView: View) : RecyclerView.ViewHolder(mView)
}
