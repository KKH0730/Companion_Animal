package studio.seno.companion_animal.ui.feed

import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import studio.seno.companion_animal.ui.feed.FeedViewModel
import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed

interface OnItemClickListener {
    fun onCommentBtnClicked(feed : Feed, commentEdit: EditText, commentCount: TextView, container : LinearLayout)
    fun onCommentShowClicked(commentCount : TextView, feed : Feed)
    fun onMenuClicked(feed : Feed, position : Int)
    fun onHeartClicked(feed : Feed, heartCount : TextView, heart_btn : ImageButton)
    fun onBookmarkClicked(feed : Feed, bookmark_btn : ImageButton)
}