package studio.seno.companion_animal.ui.comment

import androidx.lifecycle.MutableLiveData
import studio.seno.domain.model.Comment

class CommentParentViewModel {
    private var parentCommentLiveData : MutableLiveData<Comment> = MutableLiveData()

    fun getParentCommentLiveData() : MutableLiveData<Comment> {
        return parentCommentLiveData
    }

    fun setParentCommentLiveData(comment : Comment) {
        parentCommentLiveData.value = comment
    }
}