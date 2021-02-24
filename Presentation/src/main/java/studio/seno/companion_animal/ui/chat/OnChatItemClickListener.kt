package studio.seno.companion_animal.ui.chat

import studio.seno.domain.model.Chat

interface OnChatItemClickListener {
    fun onChatItemClicked(chat : Chat)
    fun onExitButtonClicked(chat : Chat)
}