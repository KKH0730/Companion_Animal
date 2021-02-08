package studio.seno.companion_animal

import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import studio.seno.domain.model.Feed

interface OnItemClickListener {
    fun onCommentBtnClicked(feed : Feed, commentEdit: EditText, commentCount: TextView, container : LinearLayout)
    fun onCommentShowClicked(commentCount : TextView, feed : Feed)
}