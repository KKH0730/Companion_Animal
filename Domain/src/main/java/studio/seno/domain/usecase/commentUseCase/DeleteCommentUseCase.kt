package studio.seno.domain.usecase.commentUseCase

import studio.seno.domain.repository.CommentRepository
import studio.seno.domain.model.Comment
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(private val commentRepository: CommentRepository){
    fun execute(
        feedEmail : String,
        feedTimestamp : Long,
        parentComment : Comment,
        childComment : Comment?,
        type : String
    ){
        commentRepository.deleteComment(feedEmail, feedTimestamp, parentComment, childComment, type)
    }
}