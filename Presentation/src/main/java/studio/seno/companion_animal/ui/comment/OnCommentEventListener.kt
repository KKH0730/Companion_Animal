package studio.seno.companion_animal.ui.comment

import android.widget.Button
import studio.seno.domain.model.Comment

interface OnCommentEventListener {
    fun onReadAnswerClicked(readAnswer : Button, targetComment : Comment)
    fun onWriteAnswerClicked(targetComment : Comment, position: Int)
    fun onMenuClicked(comment : Comment, position : Int)
}