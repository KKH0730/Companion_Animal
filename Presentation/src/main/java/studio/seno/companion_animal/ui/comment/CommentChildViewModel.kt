package studio.seno.companion_animal.ui.comment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.domain.model.Comment

class CommentChildViewModel : ViewModel(){
    private var childCommentLiveData : MutableLiveData<Comment> = MutableLiveData()

    fun getChildCommentLiveData() : MutableLiveData<Comment> {
        return childCommentLiveData
    }

    fun setChildCommentLiveData(comment : Comment) {
        childCommentLiveData.value = comment
    }
}