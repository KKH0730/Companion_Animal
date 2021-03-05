package studio.seno.domain.usecase.commentUseCase

import studio.seno.domain.Repository.CommentRepository

class SetCommentCountUseCase (private val commentRepository: CommentRepository){
    fun execute(
        targetEmail: String,
        targetTimestamp: Long,
        commentCount: Long,
        flag : Boolean
    ){
        commentRepository.setCommentCount(targetEmail, targetTimestamp, commentCount, flag)
    }
}