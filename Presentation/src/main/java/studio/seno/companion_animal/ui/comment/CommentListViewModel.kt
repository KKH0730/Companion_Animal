package studio.seno.companion_animal.ui.comment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.datamodule.RemoteRepository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Comment
import java.util.*

class CommentListViewModel : ViewModel() {
    private var commentListLiveData: MutableLiveData<List<Comment>> = MutableLiveData()
    private val remoteRepository = RemoteRepository.getInstance()!!

    fun getCommentListLiveData(): MutableLiveData<List<Comment>> {
        return commentListLiveData
    }

    fun setCommentListLiveData(list: List<Comment>) {
        commentListLiveData.value = null
        commentListLiveData.value = list
    }

    fun requestLoadComment(targetEmail: String, targetTimestamp: Long) {
        remoteRepository.loadComment(
            targetEmail,
            targetTimestamp,
            object : LongTaskCallback<List<Comment>> {
                override fun onResponse(result: Result<List<Comment>>) {
                    if (result is Result.Success) {
                        var commentList = result.data
                        Collections.sort(commentList)
                        //commentListLiveData.value = null
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
        var comment = Mapper.getInstance()!!.mapperToComment(type, myEmail, nickname, content, null, timestamp)
        remoteRepository.uploadComment(targetEmail, targetTimestamp, comment)
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
        var commentAnswer = Mapper.getInstance()!!.mapperToComment(type, myEmail, myNickname, content, null, timestamp)
        remoteRepository.uploadCommentAnswer(feedEmail, feedTimestamp, targetEmail, targetTimestamp, commentAnswer)
    }

    fun requestUploadCommentCount(targetEmail: String, targetTimestamp: Long, commentCount: Long, flag : Boolean) {
        remoteRepository.uploadCommentCount(targetEmail, targetTimestamp, commentCount, flag)
    }

    fun requestDeleteComment(
        feedEmail: String,
        feedTimestamp: Long,
        parentComment: Comment,
        childComment: Comment?,
        type: String,
        list: MutableList<Comment>
    ){
        remoteRepository.deleteComment(feedEmail, feedTimestamp, parentComment, childComment, type)
        commentListLiveData.value = list
    }


}