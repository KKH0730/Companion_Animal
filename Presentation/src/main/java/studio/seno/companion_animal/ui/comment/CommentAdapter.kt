package studio.seno.companion_animal.ui.comment

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.CommentChildBinding
import studio.seno.companion_animal.databinding.CommentParentBinding
import studio.seno.companion_animal.util.Constants
import studio.seno.domain.model.Comment

class CommentAdapter : ListAdapter<Comment, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }

    }
){
    private var listener : OnEventListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == Constants.PARENT.toInt()) {
            val binding = DataBindingUtil.inflate<CommentParentBinding>(LayoutInflater.from(parent.context), R.layout.comment_parent, parent, false)
            return ParentCommentViewHolder(binding, listener!!)
        } else {
            val binding = DataBindingUtil.inflate<CommentChildBinding>(LayoutInflater.from(parent.context), R.layout.comment_child, parent, false)
            return ChildCommentViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)

        if(viewType == Constants.PARENT.toInt()) {
            val holder = holder as ParentCommentViewHolder
            val item = getItem(position)
            val model = CommentParentViewModel()
            model.setParentCommentLiveData(item)
            holder.setViewModel(model, item)
        } else {
            val holder = holder as ChildCommentViewHolder
            val item = getItem(position)
            val model = CommentChildViewModel()
            model.setChildCommentLiveData(item)
            holder.setViewModel(model)
        }
    }

    override fun getCurrentList(): MutableList<Comment> {
        return super.getCurrentList()
    }



    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.toInt()
    }

    fun setOnEventListener(listener : OnEventListener) {
        this.listener = listener
    }

    private class ParentCommentViewHolder(binding : CommentParentBinding, listener : OnEventListener) : RecyclerView.ViewHolder(binding.root){
        private val binding : CommentParentBinding = binding
        private var mListener = listener



        fun setViewModel(model : CommentParentViewModel, comment : Comment) {
            if(comment.getChildren()!!.isEmpty()) {
                binding.readAnswer.visibility = View.GONE
            } else {
                binding.readAnswer.visibility = View.VISIBLE
            }

            binding.model = model
            binding.executePendingBindings()

            setEvent(comment)
        }

        fun setEvent(comment: Comment){
            binding.readAnswer.setOnClickListener{
                mListener.OnReadAnswerClicked(binding.readAnswer, comment)
            }

            binding.writeAnswer.setOnClickListener {
                mListener.OnWriteAnswerCilcked(comment)
            }
        }

    }

    private class ChildCommentViewHolder(binding : CommentChildBinding) : RecyclerView.ViewHolder(binding.root){
        private var binding : CommentChildBinding = binding

        fun setViewModel(model : CommentChildViewModel) {
            binding.model = model
            binding.executePendingBindings()
        }
    }
}