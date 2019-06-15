package com.example.voicelist

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.childlist_contents.view.*
import kotlinx.android.synthetic.main.childlist_footer.view.*
import kotlinx.android.synthetic.main.childlist_header.view.*

class ChildListAdaptor(private val vModel: MainViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // vModel.navigationHistory[lastIndex] = 現在表示されているアイテムの親アイテム

    private val childHeader = 0
    private val childContents = 1
    private val childFooter = 2
    private var mCurrentParent = ""
    private lateinit var mList: List<String>
    private lateinit var mUIHandler: ChildFragment.DeliverEvent


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mCurrentParent = vModel.navigationHistory.last()
        mList = vModel.getChildOf(mCurrentParent)
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
        if (position == 0) {
            holder.mView.childHeaderText.text = mCurrentParent
            vH.itemView.backToParent.setOnClickListener {
                // 戻るボタンを押したときの挙動
                if (vModel.navigationHistory.size < 3) { // Origin -> child 1 の時
                    mUIHandler.onGotoOrigin()
                    val trace = vModel.popNavigation()
                    Log.i("transit", "from $trace to back to Origin")
                } else {// Origin -> child 1 -> child 2 以上の時
                    val lastParent = vModel.navigationHistory[vModel.navigationHistory.lastIndex - 1] // 一つ前のアイテムへ
                    mUIHandler.backChildToChild(lastParent, vModel.getChildOf(lastParent))
                    Log.i("transit", "back to $lastParent")
                }
            }
        } else if (position <= mList.lastIndex + 1) { // Contents ヘッダが先頭、1個後ろにずれる
            val childHeader = mList[position - 1]
            vH.mView.childContents.text = childHeader
            val originIndex = vModel.findIndexOfOrigin(childHeader)
            if (originIndex > 0 && vModel.getChildListAt(originIndex).isNotEmpty()) {
                //　表示する行にさらに子アイテムがあれば､移動ボタンを表示
                vH.itemView.goChild.visibility = View.VISIBLE
                vH.itemView.goChild.setOnClickListener {
                    mUIHandler.advanceChildToChild(childHeader, vModel.getChildListAt(originIndex))
                    Log.i("transit", "from $mCurrentParent to $childHeader")
                }
            } else {
                holder.itemView.goChild.visibility = View.GONE
            }
        } else {
            vH.mView.childAddButton.setOnClickListener { view ->

                val originIndex = vModel.findIndexOfOrigin(mCurrentParent)           //   現在表示されているアイテム達の親
                if (originIndex < 0) { //originにアイテムが無い場合は追加・・
                    Log.i("Item", "$mCurrentParent origin　was Not Found")
                } else {
                    Log.i("Item", "$mCurrentParent origin　was at $originIndex")
                    editorTextDone(view, originIndex)
                }
            }
        }
    }

    // public method

    fun setUIHandler(_handler: ChildFragment.DeliverEvent) {
        this.mUIHandler = _handler // Fragmentのインスタンスやメンバを操作するため､インターフェイスを経由
    }
    class ChildRowHolder
        (val mView: View) : RecyclerView.ViewHolder(mView)

    private fun editorTextDone(view: View, originIndex: Int) {
        val parent = view.parent
        val recyclerView = parent?.findAscendingRecyclerView()
        val editor = recyclerView?.let { findDescendingEditorTextAtPosition(it, originIndex) }
        editor?.let {
            val newText = it.text.toString()
            if (newText.isBlank()) return
            Log.i("EditorEvent", " $newText will add")
            vModel.addChildAt(originIndex, newText)
            it.text.clear()
            it.hideSoftKeyBoard()
        }
    }
}
