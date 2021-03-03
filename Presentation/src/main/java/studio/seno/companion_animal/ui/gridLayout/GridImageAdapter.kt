package studio.seno.companion_animal.ui.gridLayout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.*
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.SearchItemBinding
import studio.seno.companion_animal.ui.search.OnSearchItemClickListener
import studio.seno.domain.model.Feed

class GridImageAdapter : ListAdapter<Feed, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem.getTimestamp() == newItem.getTimestamp()
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }
    }
){
    private var listener: OnSearchItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding : SearchItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.search_item, parent, false)
        var params = binding.root.layoutParams as GridLayoutManager.LayoutParams
        params.leftMargin = 5
        params.rightMargin = 5
        binding.root.layoutParams = params
        return SearchViewHolder(
            binding,
            listener!!
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var holder = holder as SearchViewHolder
        var item = getItem(position)
        val model = GridImageViewModel()
        model.setFeedLiveData(item)
        holder.setViewModel(model, item)

    }

    fun setOnItemClickListener(listener: OnSearchItemClickListener) {
        this.listener = listener
    }

    private class SearchViewHolder(binding : SearchItemBinding, listener: OnSearchItemClickListener) : RecyclerView.ViewHolder(binding.root){
        private var mBinding: SearchItemBinding = binding
        private var mListener = listener


        fun setViewModel(model : GridImageViewModel, item : Feed){
            mBinding.model = model
            mBinding.executePendingBindings()

            setEvent(item)
        }

        fun setEvent(item : Feed){
            mBinding.container.setOnClickListener {
                mListener.onSearchItemClicked(item, bindingAdapterPosition)
            }
        }
    }
}