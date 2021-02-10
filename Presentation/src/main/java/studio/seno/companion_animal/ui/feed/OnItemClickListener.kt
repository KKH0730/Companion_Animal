package studio.seno.companion_animal.ui.feed

import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import studio.seno.companion_animal.ui.feed.FeedViewModel
import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed

interface OnItemClickListener {
    fun onCommentBtnClicked(feed : Feed, commentEdit: EditText, commentCount: TextView, model : FeedViewModel)
    fun onCommentShowClicked(commentCount : TextView, feed : Feed)
    fun OnMenuClicked(feed : Feed, position : Int)
}