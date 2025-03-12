package studio.seno.domain.usecase.commentUseCase

import studio.seno.domain.repository.CommentRepository
import javax.inject.Inject

class SetCommentCountUseCase @Inject constructor(private val commentRepository: CommentRepository){
    fun execute(
        targetEmail: String,
        targetTimestamp: Long,
        commentCount: Long,
        flag : Boolean
    ){
        commentRepository.setCommentCount(targetEmail, targetTimestamp, commentCount, flag)
    }
}