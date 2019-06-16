package com.example.voicelist

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.origin_footer.view.*
import kotlinx.android.synthetic.main.originlist_item.view.*

class OriginListAdaptor(
    private val vModel: MainViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //TODO 行の編集のリスナ､改行やIME DONEの実装


    // View Type Const
    private val cItem = 1
    private val cFooter = 2

    // String   "title(contents):root, descending1 , descending2 , ..."
    private lateinit var mHandler: OriginFragment.DeliverEventToActivity

    // Lifecycle of Recycler View
    override fun getItemCount(): Int = vModel.getOriginList().size + 1 // データ＋入力用フッタ
    override fun getItemViewType(position: Int): Int {
        val itemRange = IntRange(0, vModel.getOriginList().lastIndex)
        return when (position) {
            in itemRange -> cItem
            else -> cFooter
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val resID = when (viewType) {
            cItem -> R.layout.originlist_item // アイテム表示　(0～アイテムの個数)　編集可能TextView
            else -> R.layout.origin_footer    // Footer アイテム追加
        }
        return ViewHolderOfCell(LayoutInflater.from(parent.context).inflate(resID, parent, false))
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vH = holder as ViewHolderOfCell
        val contentRange = IntRange(0, vModel.getOriginList().lastIndex)
        val footRange = vModel.getOriginList().lastIndex + 1
        when (position) {
            in contentRange -> bindContentRow(vH, position)
            footRange -> bindFooter(vH, position)
            else -> throw IllegalStateException("$position is out of range")
        }
    }

    // public method
    fun setUIHandler(_handler: OriginFragment.DeliverEventToActivity) {
        this.mHandler = _handler
    }
    class ViewHolderOfCell(val rowView: View) : RecyclerView.ViewHolder(rowView)

    // private method
    private fun bindContentRow(vH: ViewHolderOfCell, position: Int) {
        val iV = vH.rowView // Holder item View
        val list = vModel.getLiveList()[position].split(",") // 表示アイテムを先頭要素、子要素に分割する
        iV.rowText.text = list[0]   //リストの先頭要素が親
        iV.rowEditText.setText(list[0])
        if (list.size >= 2) iV.originGoChild.visibility = View.VISIBLE

        else {
            iV.originGoChild.visibility = View.GONE
        }
        iV.rowText.setOnClickListener {
            iV.textWrapper.showNext()
            iV.imageWrapper.showNext()
        }
        iV.originGoChild.setOnClickListener {
            mHandler.transitOriginToChild(list[0])
        }
        iV.editEndButton.setOnClickListener { view ->
            val newText = iV.rowEditText.text.toString()
            if (newText.isBlank()) {
                confirmDelete(view, position)
                val origin = vModel.getOriginList()[position]
                Log.i("Editor", "$origin at $position will be deleted.")
            } else {
                vModel.setLiveListAt(position, 0, newText)
                iV.rowText.text = newText
                this@OriginListAdaptor.notifyItemChanged(position)
            }
            iV.textWrapper.showPrevious()
            if (vModel.getChildListAt(position).isNotEmpty()) iV.imageWrapper.showPrevious()
            else view.visibility = View.GONE
            view.hideSoftKeyBoard()
        }
        iV.rowEditText.setOnFocusChangeListener { v, hasFocus ->
            when (hasFocus) {
                true -> v.showSoftKeyBoard()
                false -> v.hideSoftKeyBoard()
            }
        }
    }
    private fun bindFooter(holder: ViewHolderOfCell, position: Int) {
        val iV = holder.itemView
        iV.originNewText.setOnFocusChangeListener { v, hasFocus ->
              when (hasFocus) {
                  true -> v.showSoftKeyBoard()
                  false -> v.hideSoftKeyBoard()
              }
        }
        iV.originNewText.setOnEditorActionListener { textView, actionId, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onNewTextRowEditorEnd(textView, position)
                return@setOnEditorActionListener true
            }
            if (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                onNewTextRowEditorEnd(textView, position)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        iV.originAddButton.setOnClickListener {
            onNewTextRowEditorEnd(iV, position)
        }
    }

    private fun onNewTextRowEditorEnd(view: View, position: Int) {
        val parent = view.parent
        val recyclerView = parent.findAscendingRecyclerView()
        val editor = recyclerView?.let { findDescendingEditorAtPosition(it, position) }
        editor?.let {
            val newText = it.text.toString()
            if (newText.isBlank()) return
            Log.i("Editor", " $newText will add")
            vModel.addLiveList(newText)
            it.text.clear()
            it.hideSoftKeyBoard()
        }
    }

    private fun confirmDelete(view: View, position: Int) {
        AlertDialog.Builder(view.context)
            .setTitle(R.string.itemDeleteTitle)
            .setMessage(R.string.itemDeleteMessage)
            .setPositiveButton(R.string.yes) { _, which ->
                when (which) {
                    DialogInterface.BUTTON_NEGATIVE -> return@setPositiveButton
                    DialogInterface.BUTTON_POSITIVE -> {
                        this@OriginListAdaptor.notifyItemRemoved(position)
                        vModel.deleteLiveListAt(position) // IndexOfOrigin = position
                    }
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}
