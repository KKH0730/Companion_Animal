package studio.seno.companion_animal.ui.feed

import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FeedItemBinding
import studio.seno.domain.model.Feed

class FeedListAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
) : ListAdapter<Feed, RecyclerView.ViewHolder>(

    object : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }

    }
) {

    private val mLifecycle = lifecycle
    private val mFm = fm
    private var listener: OnItemClickListener? = null
    private lateinit var binding: FeedItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.feed_item,
            parent,
            false
        )
        return FeedViewHolder(binding, listener!!)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var holder = holder as FeedViewHolder
        var item = getItem(position)
        val model = FeedViewModel(
            mLifecycle,
            mFm,
            holder.itemView.findViewById(R.id.indicator)
        )
        model.setFeedLiveData(item)
        holder.setViewModel(model, item)
    }


    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    private class FeedViewHolder(feedBinding: FeedItemBinding, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(feedBinding.root){
        private var binding: FeedItemBinding = feedBinding
        private var mListener = listener


        fun setViewModel(model: FeedViewModel, feed: Feed) {
            binding.model = model
            binding.executePendingBindings()

            setEvent(feed)
        }

        fun setEvent(feed: Feed) {
            binding.commentBtn.setOnClickListener {
                if (binding.content.text.isNotEmpty()) {
                    mListener.onCommentBtnClicked(
                        feed,
                        binding.comment,
                        binding.commentCount,
                        binding.commentContainer
                    )
                }
            }

            binding.comment.addTextChangedListener(textWatcher)

            binding.commentShow.setOnClickListener { mListener.onCommentShowClicked(binding.commentCount, feed) }
            binding.feedMenu.setOnClickListener { mListener.onMenuClicked(feed, adapterPosition) }
            binding.heartBtn.setOnClickListener { mListener.onHeartClicked(feed, binding.heartCount, binding.heartBtn) }
            binding.bookmarkBtn.setOnClickListener { mListener.onBookmarkClicked(feed, binding.bookmarkBtn) }
            binding.detailBtn.setOnClickListener { mListener.onDetailClicked(feed) }

        }

        private val textWatcher : TextWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(binding.comment.text.isEmpty())
                    binding.commentBtn.visibility = View.INVISIBLE
                else
                    binding.commentBtn.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }
}