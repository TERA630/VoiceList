package com.example.voicelist

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.card_folder.view.*
import kotlinx.android.synthetic.main.card_list.view.*
import kotlinx.android.synthetic.main.card_list.view.rowText
import java.util.*

class
OriginListAdaptor(private var mResults: MutableList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // ViewType 0 or except 1: item, 1: folder
    // String   "something, some1 , some 2 , ..."
    private lateinit var mHandler: OriginFragment.DeliverEventToActivity

    // Lifecycle of Recycler View
    override fun getItemCount(): Int = mResults.size
    override fun getItemViewType(position: Int): Int = indicateViewType(mResults[position])
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder = ViewHolderOfCell(inflater.inflate(R.layout.card_list, parent, false))
        when (viewType) {
            1 -> {
                viewHolder.hasChild = true
            }
            else -> {
                viewHolder.hasChild = false
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val list = mResults[position].split(",")
        val parentString = list[0]
        val droppedList = list.drop(1)
        val vH = holder as ViewHolderOfCell
        if (vH.hasChild) {
            vH.childList = droppedList.toMutableList()
            vH.rowView.folderIcon.visibility = View.VISIBLE
        } else {
            vH.rowView.folderIcon.visibility = View.GONE
        }
        vH.rowView.rowText.text = parentString
        vH.rowView.rowText.setOnClickListener {
            Log.i("test", "Text clicked")
        }
    }

    fun addResult(result: String) {
        mResults.add(0, "$result,0")
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
        return if (list.size > 2) 1 else 0 // ITEMが一つなら　ViewType:0　Itemをかえす。　そうでなければFolderをかえす。
    }


    class ViewHolderOfCell(val rowView: View) : RecyclerView.ViewHolder(rowView) {
        var hasChild = false
        var childList: MutableList<String> = emptyList<String>().toMutableList()
    }
}