package studio.seno.companion_animal.ui.follow

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ProfileItemBinding
import studio.seno.companion_animal.ui.notification.OnNotificationClickedListener
import studio.seno.domain.model.Follow

class FollowAdapter(category: String) : ListAdapter<Follow, RecyclerView.ViewHolder>(
    object: DiffUtil.ItemCallback<Follow>(){
        override fun areItemsTheSame(oldItem: Follow, newItem: Follow): Boolean {
            return oldItem.email == newItem.email
        }

        override fun areContentsTheSame(oldItem: Follow, newItem: Follow): Boolean {
            return oldItem == newItem
        }
    }
){
    private var onFollowListener : OnFollowClickListener? = null
    private var mCategory = category

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ProfileItemBinding>(LayoutInflater.from(parent.context), R.layout.profile_item, parent, false)
        return FollowViewHolder(binding, onFollowListener!!)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as FollowViewHolder
        val item = getItem(position)
        val model = FollowViewModel(mCategory, item.email)
        model.setFollowLiveData(item)
        holder.setViewModel(model, item, mCategory)
    }

    fun setOnFollowClickListener(onFollowListener : OnFollowClickListener){
        this.onFollowListener = onFollowListener
    }

    private class FollowViewHolder(binding: ProfileItemBinding, onFollowListener : OnFollowClickListener) : RecyclerView.ViewHolder(binding.root) {
        private val binding : ProfileItemBinding = binding
        private val mOnFollowListener = onFollowListener

        fun setViewModel(model : FollowViewModel, follow: Follow, category: String) {
             binding.followModel = model
             binding.executePendingBindings()

            setEvent(follow, category)
        }

        fun setEvent(follow: Follow, category: String){
            binding.profileLayout.setOnClickListener {v->
                mOnFollowListener.onProfileClicked(v as ConstraintLayout, follow)}
            binding.followBtn.setOnClickListener {v->
                mOnFollowListener.onButtonClicked(v as Button, category, follow) }
        }
    }
}