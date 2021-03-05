package studio.seno.companion_animal.ui.comment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import studio.seno.domain.model.Comment
import studio.seno.domain.usecase.commentUseCase.*
import studio.seno.domain.usecase.uploadUseCase.GetProfileImageUseCase
import java.util.*

class CommentListViewModel (
    private val getProfileImageUseCase: GetProfileImageUseCase,
    private val setCommentUseCase: SetCommentUseCase,
    private val getCommentAnswerUseCase: GetCommentAnswerUseCase,
    private val setCommentCountUseCase: SetCommentCountUseCase,
    private val getCommentUseCase: GetCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase

    ): ViewModel() {
    private var commentListLiveData: MutableLiveData<List<Comment>> = MutableLiveData()

    fun getCommentListLiveData(): MutableLiveData<List<Comment>> {
        return commentListLiveData
    }

    fun setCommentListLiveData(list: List<Comment>) {
        commentListLiveData.value = null
        commentListLiveData.value = list
    }

    fun requestLoadComment(targetEmail: String, targetTimestamp: Long) {
        getCommentUseCase.execute(
            targetEmail,
            targetTimestamp,
            object : LongTaskCallback<List<Comment>> {
                override fun onResponse(result: Result<List<Comment>>) {
                    if (result is Result.Success) {
                        var commentList = result.data
                        Collections.sort(commentList)

                        commentListLiveData.value = commentList
                    } else if(result is Result.Error) {
                        Log.e("error", "comment load error : ${result.exception}")
                    }
                }
            })
    }

    fun requestUploadComment(
        targetEmail: String,
        targetTimestamp: Long,
        type: Long,
        myEmail: String,
        nickname: String,
        content: String,
        timestamp: Long
    ) {
        setCommentUseCase.execute(targetEmail, targetTimestamp, type, myEmail, nickname, content, timestamp)
    }

    fun requestUploadCommentAnswer(
        feedEmail: String,
        feedTimestamp: Long,
        targetEmail: String,
        targetTimestamp: Long,
        type: Long,
        myEmail: String,
        myNickname: String,
        content: String,
        timestamp: Long
    ) {
        getCommentAnswerUseCase.execute(feedEmail, feedTimestamp, targetEmail, targetTimestamp, type, myEmail, myNickname, content, timestamp)
    }

    fun requestUploadCommentCount(targetEmail: String, targetTimestamp: Long, commentCount: Long, flag : Boolean) {
        setCommentCountUseCase.execute(targetEmail, targetTimestamp, commentCount, flag)
    }

    fun requestDeleteComment(
        feedEmail: String,
        feedTimestamp: Long,
        parentComment: Comment,
        childComment: Comment?,
        type: String,
        list: MutableList<Comment>
    ){
        deleteCommentUseCase.execute(feedEmail, feedTimestamp, parentComment, childComment, type)
        commentListLiveData.value = list
    }

    fun loadProfileUri(email: String, callback: LongTaskCallback<String>){
        getProfileImageUseCase.execute(email, object  : LongTaskCallback<String>{
            override fun onResponse(result: Result<String>) {
                if(result is Result.Success)
                    callback.onResponse(Result.Success(result.data))
                else if(result is Result.Error) {
                    Log.e("error", "UserViewModel loadProfileUri error: ${result.exception}")
                }
            }
        })
    }

}