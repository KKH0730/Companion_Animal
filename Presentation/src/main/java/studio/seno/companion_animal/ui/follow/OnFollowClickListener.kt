package studio.seno.companion_animal.ui.follow

import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import studio.seno.domain.model.Follow

interface OnFollowClickListener {
    fun onProfileClicked(layout : ConstraintLayout, follow : Follow)
    fun onButtonClicked(button : Button, category : String,  follow : Follow)
}