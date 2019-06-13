package com.example.voicelist

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.origin_footer.view.*
import kotlinx.android.synthetic.main.origin_list.view.*

class OriginListAdaptor(
    private val mModel: MainViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // View Type Const
    private val cItemWithoutChild = 0
    private val cItemWithChild = 1
    private val cFooter = 2

    // String   "title(contents):root, descending1 , descending2 , ..."
    private lateinit var mHandler: OriginFragment.DeliverEventToActivity

    // Lifecycle of Recycler View
    override fun getItemCount(): Int = mModel.getOriginList().size + 1 // データ＋入力用フッタ

    override fun getItemViewType(position: Int): Int = indicateViewType(position) // position 0..getItemCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            cItemWithChild, cItemWithoutChild -> {
                val view = inflater.inflate(R.layout.origin_list, parent, false)
                val viewHolder = ViewHolderOfCell(view)
                viewHolder.hasChild = viewType == 1
                return viewHolder
            }
            else -> {
                val view = inflater.inflate(R.layout.origin_footer, parent, false)
                return ViewHolderOfCell(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position > mModel.getLiveList().lastIndex) {
            bindAdditionWindow(holder as ViewHolderOfCell, position)
            return
        }
        val list = mModel.getOriginList()
        val head = if (list.isNotEmpty()) list[position] else "empty!!!"

        val vH = holder as ViewHolderOfCell
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
        lV.folderIcon.setOnClickListener {
            mHandler.onUserInterAction(head, vH.childList)
        }
        lV.editEndButton.setOnClickListener { v ->
            val newText = lV.rowEditText.text.toString()
            mModel.setLiveListAt(position, 0, newText)
            lV.rowText.text = newText
            this@OriginListAdaptor.notifyItemChanged(position)
            lV.textWrapper.showPrevious()
            if (vH.hasChild) lV.imageWrapper.showPrevious()
            else v.visibility = View.GONE
            v.hideSoftKeyBoard()
        }
        lV.rowEditText.setOnFocusChangeListener { v, hasFocus ->
            when (hasFocus) {
                true -> v.showSoftKeyBoard()
                false -> v.hideSoftKeyBoard()
            }
        }
    }

    // public method
    fun setUIHandler(_handler: OriginFragment.DeliverEventToActivity) {
        this.mHandler = _handler
    }
    private fun indicateViewType(position: Int): Int {
        if (position > mModel.getOriginList().lastIndex) return cFooter
        val childList = mModel.getChildListAt(position)
        return if (childList.isEmpty()) cItemWithoutChild else cItemWithChild // ChildItemがなければ　ViewType:0　Itemをかえす。　そうでなければFolderをかえす。
    }
    class ViewHolderOfCell(val rowView: View) : RecyclerView.ViewHolder(rowView) {
        var hasChild = false
        var childList = mutableListOf<String>()
    }

    // private method
    private fun bindAdditionWindow(holder: OriginListAdaptor.ViewHolderOfCell, position: Int) {
        val iV = holder.itemView
        Log.i("recyclerView", "$position is footer..")
        iV.originNewText.setOnFocusChangeListener { v, hasFocus ->
              when (hasFocus) {
                  true -> v.showSoftKeyBoard()
                  false -> v.hideSoftKeyBoard()
              }
        }

        iV.originAddButton.setOnClickListener { v ->
            val newText = iV.originNewText.text.toString()
            Log.i("text", " $newText will add")
            mModel.addLiveList(newText)
            val originList = v.parent
            val view = originList?.findAscendingRecyclerView()
            val newpos = this.itemCount
            view?.scrollToPosition(newpos)

        }
    }
}