package studio.seno.companion_animal.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FeedItemBinding
import studio.seno.companion_animal.databinding.LastSearchItemBinding
import studio.seno.companion_animal.ui.feed.FeedListAdapter
import studio.seno.companion_animal.ui.feed.FeedViewModel
import studio.seno.companion_animal.ui.feed.OnItemClickListener
import studio.seno.domain.model.LastSearch


class LastSearchAdapter : ListAdapter<LastSearch, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<LastSearch>() {
        override fun areItemsTheSame(oldItem: LastSearch, newItem: LastSearch): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: LastSearch, newItem: LastSearch): Boolean {
            return oldItem == newItem
        }

    }
){
    private var listener: OnLastSearchListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding : LastSearchItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.last_search_item, parent, false)
        return LastSearchViewHolder(binding, listener!!)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var holder = holder as LastSearchViewHolder
        var item = getItem(position)
        val model = LastSearchModel()
        model.setLastSearchLiveData(item)
        holder.setViewModel(model, item)
    }
    fun setOnItemClickListener(listener: OnLastSearchListener) {
        this.listener = listener
    }

    private class LastSearchViewHolder(binding : LastSearchItemBinding, listener: OnLastSearchListener) : RecyclerView.ViewHolder(binding.root){
        private var mBinding: LastSearchItemBinding = binding
        private var mListener = listener

        fun setViewModel(model : LastSearchModel, item : LastSearch){
            mBinding.model = model
            mBinding.executePendingBindings()

            setEvent(item)
        }

        fun setEvent(item : LastSearch){
            mBinding.keywordContainer.setOnClickListener {
                mListener.onItemClicked(mBinding.content.text.toString().trim())
            }

            mBinding.closeBtn.setOnClickListener {
                mListener.onDeleteClicked(item.timestamp, item)
            }
        }
    }
}