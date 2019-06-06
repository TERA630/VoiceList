package com.example.voicelist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.origin_list.view.*
import java.util.*

class OriginListAdaptor(
    private var mResults: MutableList<String>,
    private val mModel: MainViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // ViewType 0 or except 1: item, 1: folder
    // String   "title(contents):root, descending1 , descending2 , ..."
    private lateinit var mHandler: OriginFragment.DeliverEventToActivity

    // Lifecycle of Recycler View
    override fun getItemCount(): Int = mResults.size

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
        val list = mResults[position].split(",")
        val head = mModel.getOriginList()[position]

        val childList = mModel.getChildListAt(position)
        val vH = holder as ViewHolderOfCell
        vH.headString = head
        val lV = vH.rowView // list View

        if (vH.hasChild) {
            vH.childList = childList.toMutableList()
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
        lV.editEndButton.setOnClickListener { v ->
            val newText = lV.rowEditText.text
            vH.headString = newText.toString()
            this.mResults[position] = newText.toString()
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
    fun addResult(result: String) {
        mResults.add(0, result)
        notifyItemInserted(0)
    }
    fun getResults(): MutableList<String> = mResults
    fun upDateResultList(stringArray: ArrayList<String>) {
        mResults = stringArray
        notifyDataSetChanged()
    }
    fun setUIHandler(_handler: OriginFragment.DeliverEventToActivity) {
        this.mHandler = _handler
    }
    // private method

    /*  private fun editParent(_view: View, parentString: String, _position: Int) {
          if (_view is TextView) {
          } else {
          }
      }*/
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