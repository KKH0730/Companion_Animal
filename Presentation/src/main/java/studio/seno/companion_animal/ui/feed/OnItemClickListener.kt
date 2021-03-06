package studio.seno.companion_animal.ui.feed

import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import studio.seno.domain.model.Feed

interface OnItemClickListener {
    fun onCommentBtnClicked(feed : Feed, commentEdit: EditText, commentCount: TextView, container : LinearLayout)
    fun onCommentShowClicked(commentCount : TextView, feed : Feed, position: Int)
    fun onMenuClicked(feed : Feed, position : Int)
    fun onHeartClicked(feed : Feed, heartCount : TextView, heart_btn : ImageButton)
    fun onBookmarkClicked(feed : Feed, bookmark_btn : ImageButton)
    fun onDetailClicked(feed : Feed, position: Int)
    fun onImageBtnClicked(feed: Feed)
    fun onProfileLayoutClicked(feed : Feed)
    fun onShareButtonClicked(feed : Feed)

}