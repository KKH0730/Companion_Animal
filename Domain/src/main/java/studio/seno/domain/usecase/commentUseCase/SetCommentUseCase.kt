package studio.seno.domain.usecase.commentUseCase

import studio.seno.domain.Repository.CommentRepository
import studio.seno.domain.util.Mapper

class SetCommentUseCase (private val commentRepository: CommentRepository){
    fun execute(
        targetEmail: String,
        targetTimestamp: Long,
        type: Long,
        myEmail: String,
        nickname: String,
        content: String,
        timestamp: Long
    ){
        val comment = Mapper.getInstance()!!.mapperToComment(type, myEmail, nickname, content, null, timestamp)
        commentRepository.setComment(targetEmail, targetTimestamp, comment)
    }
}