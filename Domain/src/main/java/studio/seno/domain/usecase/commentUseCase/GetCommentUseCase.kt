package studio.seno.domain.usecase.commentUseCase

import studio.seno.domain.repository.CommentRepository
import studio.seno.domain.model.Comment
import studio.seno.domain.util.LongTaskCallback
import javax.inject.Inject

class GetCommentUseCase @Inject constructor(private val commentRepository: CommentRepository){
    fun execute(
        email: String,
        timestamp: Long,
        callback: LongTaskCallback<List<Comment>>
    ){
        commentRepository.getComment(email, timestamp, callback)
    }
}