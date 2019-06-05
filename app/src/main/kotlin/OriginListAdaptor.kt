package com.example.voicelist

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.origin_list.view.*
import java.util.*

class
OriginListAdaptor(private var mResults: MutableList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // ViewType 0 or except 1: item, 1: folder
    // String   "title(contents):root, descending1 , descending2 , ..."
    private lateinit var mHandler: OriginFragment.DeliverEventToActivity

    // Lifecycle of Recycler View
    override fun getItemCount(): Int = mResults.size
    override fun getItemViewType(position: Int): Int = indicateViewType(mResults[position])
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
        val head = list[0]
        val droppedList = list.drop(1)
        val vH = holder as ViewHolderOfCell
        vH.headString = head
        val lV = vH.rowView // list View

        if (vH.hasChild) {
            vH.childList = droppedList.toMutableList()
            lV.folderIcon.visibility = View.VISIBLE
        } else {
            lV.folderIcon.visibility = View.GONE
        }
        lV.rowText.text = head
        lV.rowText.setOnClickListener {
            Log.i("test", "TextView was clicked")
            lV.textWrapper.showNext()
            lV.imageWrapper.showNext()
        }
        lV.rowEditText.setText(head, TextView.BufferType.NORMAL)
        lV.rowEditText.setOnClickListener {
            Log.i("test", "EditText was clicked")
            lV.textWrapper.showPrevious()
            lV.imageWrapper.showPrevious()
        }
        lV.editEndButton.setOnClickListener {
            Log.i("test", "text edit end. ")
            lV.textWrapper.showPrevious()
            lV.imageWrapper.showPrevious()

        }
        lV.rowEditText.setOnFocusChangeListener { v, hasFocus ->
            when (hasFocus) {
                true -> {
                    Log.i("test", "focus is coming. ime will on")
                }
                false -> {
                    Log.i("test", "focus was gone. ")
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
    private fun indicateViewType(_string: String): Int {
        val list = _string.split(",")
        return if (list.size > 1) 1 else 0 // ITEMが一つなら　ViewType:0　Itemをかえす。　そうでなければFolderをかえす。
    }
    class ViewHolderOfCell(val rowView: View) : RecyclerView.ViewHolder(rowView) {
        var headString = ""
        var hasChild = false
        var childList: MutableList<String> = emptyList<String>().toMutableList()
    }
}