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
import kotlinx.android.synthetic.main.childlist_contents.view.*
import kotlinx.android.synthetic.main.childlist_footer.view.*
import kotlinx.android.synthetic.main.childlist_header.view.*

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

    private fun updateListItem() {
        changeListItem(mCurrentParent, _currentChild = vModel.getChildOf(mCurrentParent))
    }
    class ChildRowHolder
        (mView: View) : RecyclerView.ViewHolder(mView)

    // ViewHolder Binder
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
        rowView.childRowTitle.text = childHeader
        rowView.childEditor.setText(childHeader)
        rowView.childRowTitle.setOnClickListener {
            rowView.childTextWrapper.showNext()
        }
        rowView.childEditor.setOnKeyListener { v, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (event.repeatCount > 1) return@setOnKeyListener false
                    moveToUpperRow(v, position)
                    return@setOnKeyListener true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (event.repeatCount > 1) return@setOnKeyListener false
                    moveToLowerRow(v, position)
                    return@setOnKeyListener true
                }
                else -> return@setOnKeyListener false
            }
        }
        rowView.childEditor.setOnEditorActionListener { editText, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                childContentsEditorEnd(editText, position)
                return@setOnEditorActionListener true
            }
            if (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                childContentsEditorEnd(editText, position)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
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
            childFooterEditorDone(view, position)
        }
        rowView.childNewText.setOnEditorActionListener { textView, actionId, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                childFooterEditorDone(textView, position)
                return@setOnEditorActionListener true
            }
            if (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                childFooterEditorDone(textView, position)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun childContentsEditorEnd(view: View, position: Int) {
        val recyclerView = view.parent?.findAscendingRecyclerView()
        val editor = recyclerView?.findEditorAtPosition(position)
        editor?.let {
            val newText = it.text.toString()
            val originIndex = vModel.indexOfOriginOf(mCurrentParent)
            val origin = vModel.getOriginList()[originIndex]
            if (newText.isBlank()) {
                confirmDelete(view, position)
                Log.i("Editor", " child ${mList[position - 1]} of $origin will be deleted")
            } else {
                vModel.setLiveListAt(position, 0, newText)
                val textView = findDescendingTextViewAtPosition(recyclerView, position)
                textView?.text = newText
                Log.i("Editor", "$origin at $originIndex as child $position will be $newText.")
                this.updateListItem()
                this@ChildListAdaptor.notifyItemChanged(position)
            }
            recyclerView.childTextWrapper.showPrevious()
            view.hideSoftKeyBoard()
        }
    }

    private fun childFooterEditorDone(view: View, position: Int) {
        val originIndex = vModel.indexOfOriginOf(mCurrentParent)  //   現在表示されているアイテム達の親
        val parent = view.parent
        val recyclerView = parent?.findAscendingRecyclerView()
        val editor = recyclerView?.findEditorAtPosition(position)
        editor?.let {
            val newText = it.text.toString()
            if (newText.isBlank()) return
            vModel.appendChildAt(originIndex, newText)
            it.text.clear()
            it.hideSoftKeyBoard()
            updateListItem()
            notifyItemChanged(position)
            notifyItemInserted(position)
        }
    }

    private fun confirmDelete(view: View, position: Int) {
        // ↑　mParentString:　現在の親アイテム　IndexOriginOf(mCurrentParent) 親アイテムのLiveList上の位置
        // Position : 子アイテムのポジション　1　ならば　　[$mParentString],item 0,item 1,....

        AlertDialog.Builder(view.context)
            .setTitle(R.string.itemDeleteTitle)
            .setMessage("${mList[position - 1]} を削除しますか")
            .setPositiveButton(R.string.yes) { _, which ->
                when (which) {
                    DialogInterface.BUTTON_NEGATIVE -> return@setPositiveButton
                    DialogInterface.BUTTON_POSITIVE -> {
                        val item = mList[position - 1]
                        Log.i("Editor", "$item at $position will be deleted..")
                        vModel.deleteChildOfOriginAt(
                            vModel.indexOfOriginOf(mCurrentParent),
                            position
                        ) // indexOf child ヘッダ分一個前､トップ分一個後
                        changeListItem(mCurrentParent, vModel.getChildOf(mCurrentParent))
                        updateListItem()
                        notifyDataSetChanged()
                    }
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    // Key event
    private fun moveToUpperRow(view: View, position: Int) {
        if (position < 2) {
            Log.w("editor", "$position is at upper limit.")
            return
        }
        val recyclerView = view.parent.findAscendingRecyclerView()
        recyclerView?.let {
            val animatorCurrent = findViewAnimatorAt(it, position)
            animatorCurrent?.showPrevious()
            val animatorUpper = findViewAnimatorAt(it, position - 1)
            animatorUpper?.showNext()
            it.findEditorAtPosition(position - 1)?.requestFocus()
        }
    }

    private fun moveToLowerRow(view: View, position: Int) {
        if (position >= mList.lastIndex + 1) {
            Log.w("editor", "$position is at lower limit.")
            return
        }
        val recyclerView = view.parent.findAscendingRecyclerView()
        recyclerView?.let {
            val animatorCurrent = findViewAnimatorAt(it, position)
            animatorCurrent?.showPrevious()
            val animatorUpper = findViewAnimatorAt(it, position + 1)
            animatorUpper?.showNext()
            it.findEditorAtPosition(position + 1)?.requestFocus()
        }
    }

}
