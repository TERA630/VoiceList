package com.example.voicelist

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.origin_list.view.*

class OriginListAdaptor(
    private val mModel: MainViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // ViewType 0 or except 1: item, 1: folder
    // String   "title(contents):root, descending1 , descending2 , ..."
    private lateinit var mHandler: OriginFragment.DeliverEventToActivity

    // Lifecycle of Recycler View
    override fun getItemCount(): Int = mModel.getLiveList().size

    override fun getItemViewType(position: Int): Int = indicateViewType(position)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder = ViewHolderOfCell(inflater.inflate(R.layout.origin_list, parent, false))
        viewHolder.hasChild = when (viewType) {
            1 -> true
            else -> false
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val list = mModel.getLiveListHeader()
        val head = if (list.isNotEmpty()) list[position] else "empty!!!"


        val vH = holder as ViewHolderOfCell
        vH.headString = head
        val lV = vH.rowView // list View

        if (vH.hasChild) {
            vH.childList = mModel.getChildListAt(position).toMutableList()
            lV.folderIcon.visibility = View.VISIBLE
        } else {
            lV.folderIcon.visibility = View.GONE
        }
        lV.rowText.text = head
        lV.rowText.setOnClickListener {
            lV.textWrapper.showNext()
            lV.imageWrapper.showNext()
        }
        lV.rowEditText.setText(head, TextView.BufferType.NORMAL)
        lV.folderIcon.setOnClickListener { v ->
            Snackbar.make(v, "${vH.childList}", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        lV.editEndButton.setOnClickListener { v ->
            val newText = lV.rowEditText.text.toString()
            vH.headString = newText
            mModel.setOriginListAt(position, newText)
            lV.rowText.text = newText
            this@OriginListAdaptor.notifyItemChanged(position)
            lV.textWrapper.showPrevious()
            if (vH.hasChild) lV.imageWrapper.showPrevious()
            else v.visibility = View.GONE
            v.hideSoftKeyBoard()
        }
        lV.rowEditText.setOnFocusChangeListener { v, hasFocus ->
            when (hasFocus) {
                true -> {
                    v.showSoftKeyBoard()
                }
                false -> {
                    v.hideSoftKeyBoard()
                }
            }
        }
    }
    fun setUIHandler(_handler: OriginFragment.DeliverEventToActivity) {
        this.mHandler = _handler
    }
    private fun indicateViewType(position: Int): Int {
        val childList = mModel.getChildListAt(position)
        return if (childList.isNotEmpty()) 1 else 0 // ChildItemがなければ　ViewType:0　Itemをかえす。　そうでなければFolderをかえす。
    }
    class ViewHolderOfCell(val rowView: View) : RecyclerView.ViewHolder(rowView) {
        var headString = ""
        var hasChild = false
        var childList: MutableList<String> = emptyList<String>().toMutableList()
    }
}