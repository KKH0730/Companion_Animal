package studio.seno.companion_animal.ui.comment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import studio.seno.datamodule.Repository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed
import java.util.*

class CommentListViewModel : ViewModel() {
    private var commentListLiveData: MutableLiveData<List<Comment>> = MutableLiveData()
    private val repository = Repository()
    private val mapper = Mapper()

    fun getCommentListLiveData(): MutableLiveData<List<Comment>> {
        return commentListLiveData
    }

    fun setCommentListLiveData(list: List<Comment>) {
        commentListLiveData.value = list
    }

    fun requestLoadComment(targetEmail: String, targetTimestamp: Long) {
        repository.loadComment(
            targetEmail,
            targetTimestamp,
            object : LongTaskCallback<List<Comment>> {
                override fun onResponse(result: Result<List<Comment>>) {
                    if (result is Result.Success) {
                        var commentList = result.data
                        Collections.sort(commentList)
                        commentListLiveData.value = null
                        commentListLiveData.value = commentList
                    }
                }
            })
    }

    fun requestUploadComment(
        targetEmail: String,
        targetTimestamp: Long,
        type: Long,
        email: String,
        nickname: String,
        content: String,
        timestamp: Long
    ) {
        var comment = mapper.mapperToComment(type, email, nickname, content, null, timestamp)
        repository.uploadComment(
            targetEmail,
            targetTimestamp,
            comment,
            object : LongTaskCallback<Boolean> {
                override fun onResponse(result: Result<Boolean>) {
                    if (result is Result.Success) {
                        //댓글 업로드 후, 댓글 로드하여 라이브 데이터 갱신
                        requestLoadComment(targetEmail, targetTimestamp)
                    } else {
                        Log.e("error", "comment upload error")
                    }
                }
            })
    }

    fun requestUploadCommentAnswer(
        feedEmail: String,
        feedTimestamp: Long,
        targetEmail: String,
        targetTimestamp: Long,
        type: Long,
        email: String,
        nickname: String,
        content: String,
        timestamp: Long
    ) {
        var commentAnswer = mapper.mapperToComment(type, email, nickname, content, null, timestamp)
        repository.uploadCommentAnswer(feedEmail, feedTimestamp, targetEmail, targetTimestamp, commentAnswer,
        object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {
                    requestLoadComment(feedEmail, feedTimestamp)
                }else {
                    Log.e("error", "comment upload error")
                }
            }
        })
    }

    fun requestUploadCommentCount(targetEmail: String, targetTimestamp: Long, commentCount: Long) {
        repository.uploadCommentCount(targetEmail, targetTimestamp, commentCount)
    }


}