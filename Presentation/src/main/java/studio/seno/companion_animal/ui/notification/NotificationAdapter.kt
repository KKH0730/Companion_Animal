package studio.seno.companion_animal.ui.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.CommentParentBinding
import studio.seno.companion_animal.databinding.FragmentNotificationBinding
import studio.seno.companion_animal.databinding.NotificationItemBinding
import studio.seno.domain.model.NotificationData

class NotificationAdapter : ListAdapter<NotificationData, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<NotificationData>(){
        override fun areItemsTheSame(
            oldItem: NotificationData,
            newItem: NotificationData
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: NotificationData,
            newItem: NotificationData
        ): Boolean {
            return oldItem == newItem
        }

    }
) {
    private var notificationListener : OnNotificationClickedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<NotificationItemBinding>(LayoutInflater.from(parent.context), R.layout.notification_item, parent, false)
        return NotificationViewHolder(binding, notificationListener!!)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as NotificationViewHolder
        val item = getItem(position)
        val model = NotificationViewModel()
        model.setNotificationLiveData(item)
        holder.setViewModel(model, item)
    }

    fun setOnNotificationListener(notificationListener : OnNotificationClickedListener){
        this.notificationListener = notificationListener
    }


    private class NotificationViewHolder(binding : NotificationItemBinding, notificationListener : OnNotificationClickedListener) : RecyclerView.ViewHolder(binding.root) {
        private val binding : NotificationItemBinding = binding
        private val mNotificationlistener = notificationListener

        fun setViewModel(model : NotificationViewModel, item : NotificationData) {
            binding.model = model
            binding.executePendingBindings()

            setEvent(item)
        }

        fun setEvent(item : NotificationData){
            binding.notiLayout.setOnClickListener { mNotificationlistener.onNotificationClicked(binding.notiLayout, item) }
            binding.notiMenu.setOnClickListener { mNotificationlistener.onDeleteClicked(item) }
        }
    }


}