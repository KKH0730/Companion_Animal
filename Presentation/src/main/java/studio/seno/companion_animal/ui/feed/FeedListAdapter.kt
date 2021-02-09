package studio.seno.companion_animal.ui.feed

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import studio.seno.companion_animal.OnItemClickListener
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FeedItemBinding
import studio.seno.companion_animal.ui.main_ui.PagerAdapter
import studio.seno.domain.model.Feed

class FeedListAdapter(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    lifecycleOwner: LifecycleOwner
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
    private val mLifecycleOwner = lifecycleOwner
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
            holder.itemView.findViewById(R.id.indicator),
            mLifecycleOwner
        )
        model.setFeedLiveData(item)
        holder.setViewModel(model, item)
    }


    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    private class FeedViewHolder(feedBinding: FeedItemBinding, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(feedBinding.root) {
        private var binding: FeedItemBinding = feedBinding
        private var mListener = listener


        fun setViewModel(model: FeedViewModel, feed: Feed) {
            binding.model = model
            binding.executePendingBindings()

            setEvent(model, feed)
        }

        fun setEvent(model: FeedViewModel, feed: Feed) {
            binding.commentBtn.setOnClickListener {
                if (binding.content.text.isNotEmpty()) {
                    mListener.onCommentBtnClicked(
                        feed, binding.comment,
                        binding.commentCount, model
                    )
                }
            }

            binding.commentShow.setOnClickListener {
                mListener.onCommentShowClicked(binding.commentCount, feed)
            }

        }

    }
}