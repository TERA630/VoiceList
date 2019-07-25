package com.example.voicelist

import android.animation.LayoutTransition
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
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
    private lateinit var mHandler: OriginFragment.EventToFragment
    private lateinit var mMinusDrawable: Drawable
    private lateinit var mPlusDrawable: Drawable

    private val isOpened: MutableSet<String> = mutableSetOf()
    private var isVoiceHearing: Boolean = false


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val context = recyclerView.context
        mMinusDrawable = ContextCompat.getDrawable(context, R.drawable.ic_square_minus)
            ?: throw java.lang.IllegalStateException("fail to get Drawable at OriginListAdapter")
        mPlusDrawable = ContextCompat.getDrawable(context, R.drawable.ic_square_plus)
            ?: throw java.lang.IllegalStateException("fail to get Drawable at OriginListAdapter")
    }
    override fun getItemCount(): Int = vModel.getOriginList().size + 1 // データ＋入力用フッタ
    override fun getItemViewType(position: Int): Int {
        val itemRange = IntRange(0, vModel.getOriginList().lastIndex)
        return when (position) {
            in itemRange -> cItem
            else -> cFooter
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            cItem -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.originlist_contents, parent, false)
                val containerView = itemView.findViewById<ConstraintLayout>(R.id.card)
                containerView?.layoutTransition?.enableTransitionType(LayoutTransition.CHANGING)
                ViewHolderOfCell(itemView)
            } // アイテム表示　(0～アイテムの個数)　編集可能TextView
            else -> {
                val footerView = LayoutInflater.from(parent.context).inflate(R.layout.origin_footer ,parent,false)
                ViewHolderOfCell(footerView)
            }   // Footer アイテム追加
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vH = holder as ViewHolderOfCell
        val contentRange = IntRange(0, vModel.getOriginList().lastIndex)  // position 0～アイテム個数　コンテンツ
        val footRange = vModel.getOriginList().lastIndex + 1         //　position 最終行　フッター
        when (position) {
            in contentRange -> bindContentRow(vH, position)
            footRange -> bindFooter(vH, position)
            else -> throw IllegalStateException("$position is out of range")
        }
    }
    // public method
    fun setUIHandler(_handler: OriginFragment.EventToFragment) {
        this.mHandler = _handler
    }
    class ViewHolderOfCell(val rowView: View) : RecyclerView.ViewHolder(rowView)
    // View Binder
    private fun bindContentRow(vH: ViewHolderOfCell, position: Int) {
        val iV = vH.rowView // Holder item View
        val (rowTitle, description) = vModel.getPairTitleAndDescription(position, 0)
        if (description != null) {
            bindContentDescription(rowTitle, description, iV, position)
        } else {  // descriptionがNULL
            iV.originGoDescription.setImageDrawable(mMinusDrawable)
            iV.originGoDescription.setOnLongClickListener {
                mHandler.transitOriginToDescription(position, 0)
                true
            }
                iV.originDescription.visibility = View.GONE
            }


        iV.rowEditText.setText(rowTitle)
        if (vModel.getChildListAt(position).isNotEmpty()) { // 子階層があれば移動ボタンの表示とイベント
            iV.originGoChild.visibility = View.VISIBLE
            iV.originGoChild.setOnClickListener {
                mHandler.transitOriginToChild(rowTitle)
            }
        } else {
            iV.originGoChild.visibility = View.GONE
        }
        iV.rowEditText.setOnKeyListener { v, keyCode, event ->
            Log.i("keyLog", ", $position and Event is $event ")
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
        }                // タイトルのキーイベント
        iV.rowEditText.setOnEditorActionListener { editText, actionId, event ->
            Log.i("keyLog", ", $position and Event is $event ")
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onContentsEditorEnd(editText, position)
                return@setOnEditorActionListener true
            }
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
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
    private fun bindContentDescription(rowTitle: String, rowDescription: String, iV: View, position: Int) {
        if (isOpened.contains(rowTitle)) {
            iV.originGoDescription.setImageDrawable(mMinusDrawable)
        } else {
            iV.originGoDescription.setImageDrawable(mPlusDrawable)
        }
        iV.originGoDescription.setOnClickListener {
            if (isOpened.contains(rowTitle)) {
                isOpened.remove(rowTitle)
                iV.originGoDescription.setImageDrawable(mPlusDrawable)
                iV.originDescription.visibility = View.GONE
            } else {
                isOpened.add(rowTitle)
                iV.originDescription.visibility = View.VISIBLE
                iV.originDescription.text = rowDescription
                iV.originGoDescription.setImageDrawable(mMinusDrawable)
            }
        }
        iV.originDescription.setOnLongClickListener {
            mHandler.transitOriginToDescription(position,0)
            true }
    }
    private fun bindFooter(holder: ViewHolderOfCell, position: Int) {
        val iV = holder.itemView
        iV.originNewText.setOnEditorActionListener { editText, actionId, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onFooterEditorEnd(editText, position)
                return@setOnEditorActionListener true
            }
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                onFooterEditorEnd(editText, position)
                return@setOnEditorActionListener true
            }
            if (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                onFooterEditorEnd(editText, position)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        iV.originVoiceButton.setOnClickListener { buttonView ->
            isVoiceHearing = if (isVoiceHearing) {
                    buttonView.alpha = 0.3f
                mHandler.startHearing(iV.originNewText)
                    false
            } else {
                    buttonView.alpha = 1.0f
                mHandler.stopHearing()
                    true
            }
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
            val editorUpper = it.findEditorAtPosition(position - 1)
            editorUpper?.requestFocus()
        }
    }
    private fun moveToLowerRow(view: View, position: Int) {
        if (position > vModel.getOriginList().lastIndex - 1) {
            return
        }
        val recyclerView = view.parent.findAscendingRecyclerView()
        recyclerView?.let {
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
            } else {
                vModel.setLiveListAt(position, 0, newText)
                //        this@OriginListAdaptor.notifyItemChanged(position)
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
            vModel.appendLiveList(newText)
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
                        vModel.deleteLiveListAt(position) // IndexOfOrigin = position
                        this@OriginListAdaptor.notifyItemRemoved(position)
                    }
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

}