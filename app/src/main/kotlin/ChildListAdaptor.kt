package com.example.voicelist

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.childlist_contents.view.*
import kotlinx.android.synthetic.main.childlist_footer.view.*
import kotlinx.android.synthetic.main.childlist_header.view.*
import kotlinx.android.synthetic.main.originlist_item.view.*

class ChildListAdaptor(private val vModel: MainViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // vModel.navigationHistory[lastIndex] = 現在表示されているアイテムの親アイテム

    private val childHeader = 0
    private val childContents = 1
    private val childFooter = 2
    private var mCurrentParent = ""
    private lateinit var mList: MutableList<String>
    private lateinit var mUIHandler: ChildFragment.DeliverEvent

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mCurrentParent = vModel.navigationHistory.last()
        mList = vModel.getChildOf(mCurrentParent).toMutableList()
        if (mList.isEmpty()) throw java.lang.IllegalStateException("$mCurrentParent has no child, illegal call childFragment")
    }
    override fun getItemCount(): Int = mList.size + 2 // list と　Header　と　Footer
    override fun getItemViewType(position: Int): Int {
        val contentRange = IntRange(1, mList.lastIndex + 1)
        return when (position) {
            0 -> childHeader
            in contentRange -> childContents
            else -> childFooter
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            0 -> inflater.inflate(R.layout.childlist_header, parent, false)
            1 -> inflater.inflate(R.layout.childlist_contents, parent, false)
            2 -> inflater.inflate(R.layout.childlist_footer, parent, false)
            else -> throw IllegalStateException("ChildListAdaptor#onCreateViewHolder got wrong ViewType $viewType")
        }
        return ChildRowHolder(view)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vH = holder as ChildRowHolder
        val headerRange = 0
        val contentRange = IntRange(1, mList.lastIndex + 1)
        val footerRange = mList.lastIndex + 2
        when (position) {
            headerRange -> bindHeader(holder.itemView)
            in contentRange -> bindContents(holder.itemView, position)
            footerRange -> bindFooter(holder.itemView, position)
            else -> throw  IllegalStateException("$position is out of range")
        }
    }
    // public method
    fun setUIHandler(_handler: ChildFragment.DeliverEvent) {
        this.mUIHandler = _handler // Fragmentのインスタンスやメンバを操作するため､インターフェイスを経由
    }
    fun changeListItem(_currentParent: String, _currentChild: List<String>) {
        mCurrentParent = _currentParent
        mList = _currentChild.toMutableList()
    }
    class ChildRowHolder
        (val mView: View) : RecyclerView.ViewHolder(mView)

    // private method
    private fun bindHeader(rowView: View) {
        rowView.childHeaderText.text = mCurrentParent
        rowView.backToParent.setOnClickListener {
            // 戻るボタンを押したときの挙動
            if (vModel.navigationHistory.size <= 2) { // Origin -> child 1 の時
                mUIHandler.onGotoOrigin()
                val trace = vModel.popNavigation()
                Log.i("transit", "from $trace to back to Origin")
            } else {// Origin -> child 1 -> child 2 以上の時
                val lastParent = vModel.navigationHistory[vModel.navigationHistory.lastIndex - 1] // 一つ前のアイテムへ
                mUIHandler.backChildToChild(lastParent, vModel.getChildOf(lastParent))
                Log.i("transit", "back to $lastParent")
            }
        }
    }
    private fun bindContents(rowView: View, position: Int) {
        val childHeader = mList[position - 1]
        val parentIndexOfOrigin = vModel.indexOfOriginOf(mCurrentParent)
        rowView.childContents.text = childHeader
        rowView.childEditor.setText(childHeader)
        rowView.childContents.setOnClickListener {
            rowView.childTextWrapper.showNext()
            rowView.childImageWrapper.showNext()
        }

        rowView.childEditEnd.setOnClickListener { v ->
            val newText = rowView.childEditor.text.toString()
            vModel.setLiveListAt(parentIndexOfOrigin, position, newText)
            rowView.childContents.text = newText
            this@ChildListAdaptor.notifyItemChanged(position)
            rowView.childTextWrapper.showPrevious()
            if (vModel.getChildOf(newText).isNotEmpty()) rowView.childImageWrapper.showPrevious()
            else v.visibility = View.GONE
            v.hideSoftKeyBoard()
        }
        val originIndex = vModel.indexOfOriginOf(childHeader)
        if (originIndex > 0 && vModel.getChildListAt(originIndex).isNotEmpty()) {
            //　表示する行にさらに子アイテムがあれば､移動ボタンを表示
            rowView.goChild.visibility = View.VISIBLE
            rowView.goChild.setOnClickListener {
                mUIHandler.advanceChildToChild(childHeader, vModel.getChildListAt(originIndex))
                Log.i("transit", "from $mCurrentParent to $childHeader")
            }
        } else {
            rowView.goChild.visibility = View.GONE
        }
    }

    private fun bindFooter(rowView: View, position: Int) {
        rowView.childAddButton.setOnClickListener { view ->
            editorTextDone(view, position)
        }
    }

    private fun onRowEditorEnd(view: View, position: Int) {
        val recyclerView = view.parent?.findAscendingRecyclerView()
        val editor = recyclerView?.let {
            findDescendingEditorAtPosition(it, position)
        }
        editor?.let {
            val newText = it.text.toString()
            if (newText.isBlank()) {
                confirmDelete(view, position)

                val origin = vModel.getOriginList()[position]
                Log.i("Editor", "$origin at $position will be deleted.")
            } else {
                vModel.setLiveListAt(position, 0, newText)
                recyclerView.rowText.text = newText
                Log.i("Editor", "$position will be $newText")
                this@ChildListAdaptor.notifyItemChanged(position)
            }
            recyclerView.textWrapper.showPrevious()
            view.hideSoftKeyBoard()
        }
    }

    private fun editorTextDone(view: View, position: Int) {
        val originIndex = vModel.indexOfOriginOf(mCurrentParent)           //   現在表示されているアイテム達の親
        val parent = view.parent
        val recyclerView = parent?.findAscendingRecyclerView()
        val editor = recyclerView?.let { findDescendingEditorAtPosition(it, position) }
        editor?.let {
            val newText = it.text.toString()
            if (newText.isBlank()) return
            Log.i("EditorEvent", " $newText will add at origin $originIndex")
            vModel.appendChildAt(originIndex, newText)
            it.text.clear()
            it.hideSoftKeyBoard()
            mList.add(newText)
            notifyItemChanged(position)
            notifyItemInserted(position)
        }
    }

    private fun confirmDelete(view: View, position: Int) {
        // ↑　mParentString:　現在の親アイテム　IndexOriginOf(mParentString) 親アイテムのLiveList上の位置
        // Position : 子アイテムのポジション　1　ならば　　[$mParentString],item 0,item 1,....


        AlertDialog.Builder(view.context)
            .setTitle(R.string.itemDeleteTitle)
            .setMessage(R.string.itemDeleteMessage)
            .setPositiveButton(R.string.yes) { _, which ->
                when (which) {
                    DialogInterface.BUTTON_NEGATIVE -> return@setPositiveButton
                    DialogInterface.BUTTON_POSITIVE -> {


                        this@ChildListAdaptor.notifyItemRemoved(position)
                        vModel.deleteLiveListAt(position) // IndexOfOrigin = position
                    }
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }


}
