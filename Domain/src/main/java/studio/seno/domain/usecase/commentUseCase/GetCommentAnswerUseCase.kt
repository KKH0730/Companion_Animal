package studio.seno.domain.usecase.commentUseCase

import studio.seno.domain.Repository.CommentRepository
import studio.seno.domain.util.Mapper

class GetCommentAnswerUseCase (private val commentRepository: CommentRepository){
    fun execute(
        feedEmail: String,
        feedTimeStamp: Long,
        targetEmail: String,
        targetTimestamp: Long,
        type: Long,
        myEmail: String,
        myNickname: String,
        content: String,
        timestamp: Long
    ){
        var commentAnswer = Mapper.getInstance()!!.mapperToComment(type, myEmail, myNickname, content, null, timestamp)
        commentRepository.getCommentAnswer(feedEmail, feedTimeStamp, targetEmail, targetTimestamp, commentAnswer)
    }
}