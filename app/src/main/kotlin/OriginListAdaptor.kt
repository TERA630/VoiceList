package com.example.voicelist
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
        if (position > vModel.getLiveList().lastIndex) {
            bindAdditionWindow(holder as ViewHolderOfCell, position)
            return
        }
        val vH = holder as ViewHolderOfCell
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
        iV.editEndButton.setOnClickListener { v ->
            val newText = iV.rowEditText.text.toString()
            vModel.setLiveListAt(position, 0, newText)
            iV.rowText.text = newText
            this@OriginListAdaptor.notifyItemChanged(position)
            iV.textWrapper.showPrevious()
            if (vModel.getChildListAt(position).isNotEmpty()) iV.imageWrapper.showPrevious()
            else v.visibility = View.GONE
            v.hideSoftKeyBoard()
        }
        iV.rowEditText.setOnFocusChangeListener { v, hasFocus ->
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

    class ViewHolderOfCell(val rowView: View) : RecyclerView.ViewHolder(rowView)

    // private method
    private fun bindAdditionWindow(holder: ViewHolderOfCell, position: Int) {
        val iV = holder.itemView
        Log.i("recyclerView", "$position is footer..")
        iV.originNewText.setOnFocusChangeListener { v, hasFocus ->
              when (hasFocus) {
                  true -> v.showSoftKeyBoard()
                  false -> v.hideSoftKeyBoard()
              }
        }
        iV.originNewText.setOnEditorActionListener { textView, actionId, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                editorTextDone(textView, position)
                return@setOnEditorActionListener true
            }
            if (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                editorTextDone(textView, position)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }


        iV.originAddButton.setOnClickListener {
            editorTextDone(iV, position)
        }
    }

    private fun editorTextDone(view: View, position: Int) {
        val parent = view.parent
        val recyclerView = parent.findAscendingRecyclerView()
        val editor = recyclerView?.let { findDescendingEditorAtPosition(it, position) }
        editor?.let {
            val newText = it.text.toString()
            if (newText.isBlank()) return
            Log.i("EditorEvent", " $newText will add")
            vModel.addLiveList(newText)
            it.text.clear()
            it.hideSoftKeyBoard()
        }
    }
}