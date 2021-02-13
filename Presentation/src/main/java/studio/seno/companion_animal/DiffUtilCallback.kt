package studio.seno.companion_animal

import androidx.recyclerview.widget.DiffUtil
import studio.seno.domain.model.Feed

class DiffUtilCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }
}