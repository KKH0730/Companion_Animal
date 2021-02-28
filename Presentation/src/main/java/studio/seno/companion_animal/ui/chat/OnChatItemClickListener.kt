package studio.seno.companion_animal.ui.chat

import android.widget.ImageView
import studio.seno.domain.model.Chat

interface OnChatItemClickListener {
    fun onChatItemClicked(chat : Chat, checkDotImage : ImageView)
    fun onExitButtonClicked(chat : Chat)
    fun onImageClicked(chat:Chat, position:Int)
}