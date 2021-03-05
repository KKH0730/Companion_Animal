package studio.seno.domain.Repository

import studio.seno.domain.model.Comment
import studio.seno.domain.util.LongTaskCallback

interface CommentRepository {
    fun setComment(
        targetEmail: String,
        targetTimestamp: Long,
        myComment: Comment
    )

    fun getCommentAnswer(
        feedEmail: String,
        feedTimeStamp: Long,
        targetEmail: String,
        targetTimestamp: Long,
        myCommentAnswer: Comment
    )

    fun setCommentCount(
        targetEmail: String,
        targetTimestamp: Long,
        commentCount: Long,
        flag : Boolean
    )


    fun getComment(
        email: String,
        timestamp: Long,
        callback: LongTaskCallback<List<Comment>>
    )


    fun deleteComment(
        feedEmail : String,
        feedTimestamp : Long,
        parentComment : Comment,
        childComment : Comment?,
        type : String
    )
}