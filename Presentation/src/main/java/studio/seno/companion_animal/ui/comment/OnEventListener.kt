package studio.seno.companion_animal.ui.comment

import android.widget.Button
import studio.seno.domain.model.Comment

interface OnEventListener {
    fun OnReadAnswerClicked(readAnswer : Button, targetComment : Comment)
    fun OnWriteAnswerCilcked(targetComment : Comment)
    fun OnMenuClicked(comment : Comment, position : Int)
}