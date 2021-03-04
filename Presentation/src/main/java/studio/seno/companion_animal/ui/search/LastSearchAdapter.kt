package studio.seno.companion_animal.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.LastSearchItemBinding
import studio.seno.domain.model.LastSearch


class LastSearchAdapter : ListAdapter<LastSearch, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<LastSearch>() {
        override fun areItemsTheSame(oldItem: LastSearch, newItem: LastSearch): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: LastSearch, newItem: LastSearch): Boolean {
            return oldItem == newItem
        }

    }
){
    private var lastSearchListener: OnLastSearchListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding : LastSearchItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.last_search_item, parent, false)
        return LastSearchViewHolder(
            binding,
            lastSearchListener!!
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as LastSearchViewHolder
        val item = getItem(position)
        val model = LastSearchModel()
        model.setLastSearchLiveData(item)
        holder.setViewModel(model, item)

    }
    fun setOnItemClickListener(lastSearchListener: OnLastSearchListener) {
        this.lastSearchListener = lastSearchListener
    }

    private class LastSearchViewHolder(binding : LastSearchItemBinding, lastSearchListener: OnLastSearchListener) : RecyclerView.ViewHolder(binding.root){
        private var mBinding: LastSearchItemBinding = binding
        private var mLastSearchListener = lastSearchListener

        fun setViewModel(model : LastSearchModel, item : LastSearch){
            mBinding.model = model
            mBinding.executePendingBindings()

            setEvent(item)
        }

        fun setEvent(item : LastSearch){
            mBinding.keywordContainer.setOnClickListener {
                mLastSearchListener.onItemClicked(mBinding.content.text.toString().trim())
            }

            mBinding.closeBtn.setOnClickListener {
                mLastSearchListener.onDeleteClicked(item.timestamp, item)
            }
        }
    }
}