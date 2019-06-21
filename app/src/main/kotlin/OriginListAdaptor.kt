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
import kotlinx.android.synthetic.main.originlist_contents.view.*

class OriginListAdaptor(
    private val vModel: MainViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
            cItem -> R.layout.originlist_contents // アイテム表示　(0～アイテムの個数)　編集可能TextView
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

    // View Binder
    private fun bindContentRow(vH: ViewHolderOfCell, position: Int) {
        val iV = vH.rowView // Holder item View
        val list = vModel.getLiveList()[position].split(",") // 表示アイテムを先頭要素、子要素に分割する
        iV.rowEditText.setText(list[0])
        iV.originGoChild.visibility = if (list.size >= 2) View.VISIBLE
        else View.GONE
        iV.originGoChild.setOnClickListener {
            mHandler.transitOriginToChild(list[0])
        }
        iV.rowEditText.setOnKeyListener { v, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    moveToUpperRow(v, position)
                    return@setOnKeyListener true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    moveToLowerRow(v, position)
                    return@setOnKeyListener true
                }
                else -> return@setOnKeyListener false
            }
        }
        iV.rowEditText.setOnEditorActionListener { editText, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onContentsEditorEnd(editText, position)
                return@setOnEditorActionListener true
            }
            if (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) { // Enterキー押したとき
                onContentsEditorEnd(editText, position)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false

        }
    }
    private fun bindFooter(holder: ViewHolderOfCell, position: Int) {
        val iV = holder.itemView
        iV.originNewText.setOnEditorActionListener { editText, actionId, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onFooterEditorEnd(editText, position)
                return@setOnEditorActionListener true
            }
            if (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                onFooterEditorEnd(editText, position)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        iV.originAddButton.setOnClickListener {
            onFooterEditorEnd(iV, position)
        }
    }

    // Key event
    private fun moveToUpperRow(view: View, position: Int) {
        if (position < 1) {
            Log.w("editor", "$position is at upper limit.")
            return
        }
        val recyclerView = view.parent.findAscendingRecyclerView()
        recyclerView?.let {
            val animatorCurrent = findViewAnimatorAt(it, position)
            animatorCurrent?.showPrevious()
            val animatorUpper = findViewAnimatorAt(it, position - 1)
            animatorUpper?.showNext()
            val editorUpper = it.findEditorAtPosition(position - 1)
            editorUpper?.requestFocus()
        }
    }
    private fun moveToLowerRow(view: View, position: Int) {
        if (position > vModel.getOriginList().lastIndex - 1) {
            Log.w("editor", "$position is at lower limit.")
            return
        }
        val recyclerView = view.parent.findAscendingRecyclerView()
        recyclerView?.let {
            val animatorCurrent = findViewAnimatorAt(it, position)
            animatorCurrent?.showPrevious()
            val animatorUpper = findViewAnimatorAt(it, position + 1)
            animatorUpper?.showNext()
            val editorUpper = it.findEditorAtPosition(position + 1)
            editorUpper?.requestFocus()
        }
    }
    private fun onContentsEditorEnd(view: View, position: Int) {
        val parent = view.parent
        val recyclerView = parent.findAscendingRecyclerView()
        val editor = recyclerView?.findEditorAtPosition(position)
        editor?.let {
            val newText = it.text.toString()
            if (newText.isBlank()) {
                confirmDelete(view, position)
                val origin = vModel.getOriginList()[position]
                Log.i("Editor", "$origin at $position will be deleted.")
            } else {
                vModel.setLiveListAt(position, 0, newText)
                Log.i("Editor", "$position will be $newText")
                this@OriginListAdaptor.notifyItemChanged(position)
            }
            view.hideSoftKeyBoard()
        }
    }
    private fun onFooterEditorEnd(view: View, position: Int) {
        val parent = view.parent
        val recyclerView = parent.findAscendingRecyclerView()
        val editor = recyclerView?.findEditorAtPosition(position)
        editor?.let {
            val newText = it.text.toString()
            if (newText.isBlank()) return
            vModel.addLiveList(newText)
            it.text.clear()
            it.hideSoftKeyBoard()
            val nextEditor = recyclerView.findEditorAtPosition(position + 1)
            nextEditor?.requestFocus()
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
                        vModel.deleteLiveListAt(position) // IndexOfOrigin = position
                        this@OriginListAdaptor.notifyItemRemoved(position)
                    }
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
}