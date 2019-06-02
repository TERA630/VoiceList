package com.example.voicelist

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ViewAnimator
import kotlinx.android.synthetic.main.card_folder.view.*
import kotlinx.android.synthetic.main.card_list.view.*
import kotlinx.android.synthetic.main.card_list.view.rowText
import java.util.*

class OriginListAdaptor(private var mResults: MutableList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // ViewType 0 or except 1: item, 1: folder
    // String   "something, some1 , some 2 , ..."
    private lateinit var mHandler: OriginFragment.DeliverEventToActivity

    fun addResult(result: String) {
        mResults.add(0, "$result,0")
        notifyItemInserted(0)
    }
    fun upDateResultList(stringArray: ArrayList<String>) {
        mResults = stringArray
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = mResults.size
    fun getResults(): MutableList<String> = mResults

    override fun getItemViewType(position: Int): Int = indicateViewType(mResults[position])

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val list = mResults[position].split(",")
        val parentString = list[0]
        holder.itemView.rowText.setOnClickListener {
            Log.i("test", "EditText clicked")
        }
        if (holder is ViewHolderOfFolder) {
            holder.rowView.rowText.text = parentString
            holder.parent = parentString
            val droppedList = list.drop(1)
            holder.childList = droppedList.toMutableList()
            holder.rowView.folderIcon.setOnClickListener {
                Log.i("test", "folder clicked ${holder.childList}")
                mHandler.onUserInterAction(parentString, holder.childList)
            }
        } else if (holder is ViewHolderOfCell) {
            holder.rowView.rowText.text = parentString
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            1 -> {
                return ViewHolderOfFolder(inflater.inflate(R.layout.card_folder, parent, false))
            }
            else -> {
                val VH = ViewHolderOfCell(inflater.inflate(R.layout.card_list, parent, false))
                VH.textWrapper = parent.textWrapper
                return VH
            }

        }
    }

    fun setUIHandler(_handler: OriginFragment.DeliverEventToActivity) {
        this.mHandler = _handler
    }
    // private method

    private fun editParent(_view: View, parentString: String, _position: Int) {
        if (_view is TextView) {


        } else {

        }


    }
    private fun indicateViewType(_string: String): Int {
        val list = _string.split(",")
        return if (list.size > 2) 1 else 0 // ITEMが一つなら　ViewType:0　Itemをかえす。　そうでなければFolderをかえす。
    }


    class ViewHolderOfCell(val rowView: View) : RecyclerView.ViewHolder(rowView) {
        var textWrapper: ViewAnimator? = null
    }
    class ViewHolderOfFolder(val rowView: View) : RecyclerView.ViewHolder(rowView) {
        var parent: String = ""
        var childList: MutableList<String> = emptyList<String>().toMutableList()
    }

}