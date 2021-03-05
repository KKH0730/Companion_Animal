package studio.seno.domain.usecase.commentUseCase

import studio.seno.domain.Repository.CommentRepository
import studio.seno.domain.model.Comment

class DeleteCommentUseCase (private val commentRepository: CommentRepository){
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