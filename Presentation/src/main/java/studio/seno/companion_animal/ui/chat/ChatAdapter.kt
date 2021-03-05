package studio.seno.companion_animal.ui.chat

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.anko.sdk27.coroutines.onClick
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ChatItemBinding
import studio.seno.companion_animal.databinding.ChatListItemBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.util.Constants
import studio.seno.domain.model.Chat

class ChatAdapter(type: String) : ListAdapter<Chat, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
) {
    private var mChatItemListener: OnChatItemClickListener? = null
    private val mType = type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (mType == "chat") {
            val binding: ChatItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.chat_item,
                parent,
                false
            )
            return ChatViewHolder(binding)
        } else {
            val binding: ChatListItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.chat_list_item,
                parent,
                false
            )
            return ChatListViewHolder(binding, mChatItemListener!!)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (mType == "chat") {
            val holder = holder as ChatViewHolder
            val chat = getItem(position)
            val model = ChatViewModel()
            model.setChatLiveData(chat)
            holder.setModel(model)
            holder.setVisibility(chat)
        } else if (mType == "chat_list") {
            val holder = holder as ChatListViewHolder
            val chat = getItem(position)
            val model = ChatViewModel()
            model.setChatLiveData(chat)
            holder.setModel(model)
            holder.setItemEvent(chat)
            holder.setVisibility(chat)
        }
    }


    fun setOnChatItemClickListener(chatItemClickListener: OnChatItemClickListener) {
        mChatItemListener = chatItemClickListener
    }


    private class ChatViewHolder(binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val mBinding = binding

        fun setVisibility(chat: Chat) {
            mBinding.exitTextView.visibility = View.GONE
            if (!chat.isExit) {
                if (chat.email == CommonFunction.getInstance()!!
                        .makeChatPath(FirebaseAuth.getInstance().currentUser?.email.toString())
                ) {
                    mBinding.chatObject.visibility = View.GONE
                    mBinding.chatMe.visibility = View.VISIBLE
                } else {
                    mBinding.chatObject.visibility = View.VISIBLE
                    mBinding.chatMe.visibility = View.GONE
                }
            } else {
                mBinding.exitTextView.visibility = View.VISIBLE
                mBinding.chatObject.visibility = View.GONE
                mBinding.chatMe.visibility = View.GONE
            }
        }

        fun setModel(model: ChatViewModel) {
            mBinding.chatViewModel = model
            mBinding.executePendingBindings()
        }
    }

    private class ChatListViewHolder(
        binding: ChatListItemBinding,
        chatItemClickListener: OnChatItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        private val mBinding = binding
        private var mChatItemListener = chatItemClickListener


        fun setModel(model: ChatViewModel) {
            mBinding.chatViewModel = model
            mBinding.executePendingBindings()
        }

        fun setVisibility(chat: Chat) {
            if (chat.isRead)
                mBinding.checkDot.visibility = View.GONE
            else {
                if (chat.realEmail == FirebaseAuth.getInstance().currentUser?.email.toString())
                    mBinding.checkDot.visibility = View.GONE
                else
                    mBinding.checkDot.visibility = View.VISIBLE
            }
        }

        fun setItemEvent(chat: Chat) {
            mBinding.chatLayout.setOnClickListener {
                mChatItemListener.onChatItemClicked(
                    chat,
                    mBinding.checkDot
                )
            }
            mBinding.exitBtn.setOnClickListener { mChatItemListener.onExitButtonClicked(chat) }
            mBinding.profileImage.setOnClickListener { mChatItemListener.onImageClicked(chat, absoluteAdapterPosition)
            }

            mBinding.profileImage.performClick()
        }
    }
}
