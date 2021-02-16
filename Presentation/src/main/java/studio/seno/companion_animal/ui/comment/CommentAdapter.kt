package studio.seno.companion_animal.ui.comment

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

    }){
    private var listenerComment : OnCommentEventListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == Constants.PARENT.toInt()) {
            val binding = DataBindingUtil.inflate<CommentParentBinding>(LayoutInflater.from(parent.context), R.layout.comment_parent, parent, false)
            return ParentCommentViewHolder(binding, listenerComment!!)
        } else {
            val binding = DataBindingUtil.inflate<CommentChildBinding>(LayoutInflater.from(parent.context), R.layout.comment_child, parent, false)
            return ChildCommentViewHolder(binding, listenerComment!!)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)

        if(viewType == Constants.PARENT.toInt()) {
            val holder = holder as ParentCommentViewHolder
            val item = getItem(position)
            var item2 : Comment? = null
            if(position + 1 < itemCount)
                item2  = getItem(position + 1)
            val model = CommentParentViewModel()
            model.setParentCommentLiveData(item)
            holder.setViewModel(model, item, item2)
        } else {
            val holder = holder as ChildCommentViewHolder
            val item = getItem(position)
            val model = CommentChildViewModel()
            model.setChildCommentLiveData(item)
            holder.setViewModel(model, item)
        }
    }

    override fun getCurrentList(): MutableList<Comment> {
        return super.getCurrentList()
    }


    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.toInt()
    }

    fun setOnEventListener(listenerComment : OnCommentEventListener) {
        this.listenerComment = listenerComment
    }

    private class ParentCommentViewHolder(binding : CommentParentBinding, listenerComment : OnCommentEventListener) : RecyclerView.ViewHolder(binding.root){
        private val binding : CommentParentBinding = binding
        private var mListener = listenerComment



        fun setViewModel(model : CommentParentViewModel, comment : Comment, nextComment : Comment?) {
            if(comment.getChildren()!!.size == 0) {
                binding.readAnswer.visibility = View.GONE

                if(nextComment != null && nextComment.type == Constants.CHILD) {
                    binding.readAnswer.visibility = View.VISIBLE
                    binding.readAnswer.text = binding.readAnswer.context.getString(R.string.comment_fold_answer)
                }
            } else {
                binding.readAnswer.visibility = View.VISIBLE
                binding.readAnswer.text = binding.readAnswer.context.getString(R.string.comment_read_answer)
            }

            binding.model = model
            binding.executePendingBindings()

            setEvent(comment)
        }

        fun setEvent(comment: Comment){
            binding.readAnswer.setOnClickListener{ mListener.onReadAnswerClicked(binding.readAnswer, comment) }
            binding.writeAnswer.setOnClickListener { mListener.onWriteAnswerClicked(comment,adapterPosition) }
            binding.commentMenu.setOnClickListener { mListener.onMenuClicked(comment, adapterPosition) }
        }
    }

    private class ChildCommentViewHolder(binding : CommentChildBinding, listenerComment : OnCommentEventListener) : RecyclerView.ViewHolder(binding.root){
        private var binding : CommentChildBinding = binding
        private var mListener = listenerComment

        fun setViewModel(model : CommentChildViewModel, commentAnswer: Comment) {
            binding.model = model
            binding.executePendingBindings()

            setEvent(commentAnswer)
        }

        fun setEvent(commentAnswer : Comment){
            binding.commentMenu.setOnClickListener { mListener.onMenuClicked(commentAnswer, adapterPosition) }
        }
    }
}