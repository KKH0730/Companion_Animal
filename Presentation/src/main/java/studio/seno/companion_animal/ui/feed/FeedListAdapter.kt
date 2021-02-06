package studio.seno.companion_animal.ui.feed

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FeedItemBinding
import studio.seno.companion_animal.ui.main_ui.PagerAdapter
import studio.seno.domain.model.Feed

class FeedListAdapter(
    context: Context,
    fm: FragmentManager,
    lifecycle: Lifecycle
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
    private var mContext = context
    private var mLifecycle = lifecycle
    private var mFm = fm

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: FeedItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.feed_item,
            parent,
            false
        )
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var holder = holder as FeedViewHolder
        var item = getItem(position)
        val model = FeedViewModel()
        model.setFeedLiveData(item)
        holder.setViewModel(model)
        holder.bindData(item, mContext, mFm, mLifecycle)
    }

    private class FeedViewHolder(feedBinding: FeedItemBinding) :
        RecyclerView.ViewHolder(feedBinding.root) {
        private var binding: FeedItemBinding = feedBinding

        fun setViewModel(model: FeedViewModel) {
            binding.model = model
            binding.executePendingBindings()
        }

        fun bindData(feed: Feed, context: Context, fm: FragmentManager, lifecycle: Lifecycle) {
            var pagerAdapter = PagerAdapter(fm, lifecycle)
            for(i in 0 until feed.remoteUri!!.size) {
                pagerAdapter.addItem(FeedPagerFragment.newInstance(feed.remoteUri!![i]))
            }

            binding.feedViewPager.adapter = pagerAdapter
            binding.indicator.setViewPager(binding.feedViewPager)
        }
    }
}