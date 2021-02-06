package studio.seno.companion_animal.ui.feed

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FeedItemBinding
import studio.seno.domain.model.Feed

class FeedAdapter(context : Context) : ListAdapter<Feed, RecyclerView.ViewHolder>(

    object : DiffUtil.ItemCallback<Feed>(){
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }

    }
){
    private var mContext = context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding : FeedItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.feed_item, parent, false)
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var holder = holder as FeedViewHolder
        var item = getItem(position)
        val model = FeedViewModel()
        model.setFeedLiveData(item)
        holder.setViewModel(model, item, mContext)
    }

    private class FeedViewHolder(feedBinding: FeedItemBinding) : RecyclerView.ViewHolder(feedBinding.root) {
        private var binding : FeedItemBinding = feedBinding

        fun setViewModel(model : FeedViewModel, feed : Feed, context : Context){
            binding.model = model
            binding.executePendingBindings()

            Glide.with(context)
                .load(Uri.parse(feed.remoteUri?.get(0)))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .into(binding.testImageview)
        }
    }
}