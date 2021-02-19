package studio.seno.companion_animal.ui.follow

import studio.seno.domain.model.Follow

interface OnFollowClickListener {
    fun onProfileClicked(follow : Follow)
}