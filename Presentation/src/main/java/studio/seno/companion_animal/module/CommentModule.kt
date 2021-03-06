package studio.seno.companion_animal.module

import android.content.Context
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import studio.seno.companion_animal.R
import studio.seno.companion_animal.ui.comment.CommentAdapter
import studio.seno.companion_animal.ui.comment.CommentListViewModel
import studio.seno.companion_animal.util.Constants
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed

class CommentModule(
    commentListViewModel: CommentListViewModel, feed : Feed, email : String,
    nickname : String, context: Context, commentAdapter : CommentAdapter
) {
    private val mCommentListViewModel = commentListViewModel
    private val mFeed = feed
    private val mContext = context
    private val myEmail = email
    private val myNickname = nickname
    private val mAdapter = commentAdapter

    fun submitComment(timestamp: Long, modifyMode : Boolean, curComment : Comment?,
                      commentPosition: Int, countTextView : TextView, commentTextView : EditText) {
        val currentCommentList = mAdapter.currentList.toMutableList()
        val content = commentTextView.text.toString()
        var tempComment = curComment

        if(modifyMode) {
            tempComment?.content = content
            currentCommentList[commentPosition] = tempComment!!
            mCommentListViewModel.setCommentListLiveData(currentCommentList.toList())

        } else {
            mCommentListViewModel.loadProfileUri(myEmail, object :
                LongTaskCallback<Any> {
                    override fun onResponse(result: Result<Any>) {
                        if(result is Result.Success) {

                            currentCommentList.add(Comment(
                                Constants.PARENT, myEmail, myNickname,
                                content, result.data as String, timestamp
                            ))

                            mCommentListViewModel.setCommentListLiveData(currentCommentList.toList())
                        }
                    }
                })
        }

        //일반 댓글 업로드
        mCommentListViewModel.requestUploadComment(
            mFeed.getEmail()!!, mFeed.getTimestamp(), Constants.PARENT,
            myEmail, myNickname, content, timestamp
        )
        //총 댓글 수 업로드
        updateCommentCount(mFeed, countTextView, true)
        countTextView.text = (countTextView.text.toString().toLong() + 1L).toString()
    }

    fun submitCommentAnswer(parentComment: Comment, answerTimestamp: Long, modifyMode: Boolean,
                            answerComment : Comment?, answerPosition: Int, commentPosition: Int,
                            commentTextView: TextView) {
        val currentCommentList = mAdapter.currentList.toMutableList()
        val content = commentTextView.text.toString()
        var tempComment = answerComment

        if(modifyMode) {
            tempComment?.content = content
            currentCommentList.set(answerPosition, tempComment!!)
            mCommentListViewModel.setCommentListLiveData(currentCommentList)
        } else {
            mCommentListViewModel.loadProfileUri(myEmail, object :
                LongTaskCallback<Any> {
                override fun onResponse(result: Result<Any>) {
                    if(result is Result.Success) {

                        currentCommentList.add(findNextParentComment(commentPosition),
                            Comment(
                                Constants.CHILD, myEmail, myNickname,
                                content, result.data as String, answerTimestamp
                            ))
                        mCommentListViewModel.setCommentListLiveData(currentCommentList)
                    } } })
        }

        //답글 업로드
        mCommentListViewModel.requestUploadCommentAnswer(
            mFeed.getEmail()!!, mFeed.getTimestamp(), parentComment.email, parentComment.timestamp,
            Constants.CHILD, myEmail, myNickname, content, answerTimestamp
        )
    }

    fun updateCommentCount(feed : Feed, countTextView : TextView, addFlag : Boolean){
        mCommentListViewModel.requestUploadCommentCount(
            feed.getEmail()!!,
            feed.getTimestamp(),
            countTextView.text.toString().toLong(),
            addFlag
        )
    }

    fun deleteComment(curComment: Comment?, answerComment: Comment?,
                      commentPosition: Int, answerPosition: Int,
                      answerMode: Boolean, countTextView: TextView) {
        val type: String?
        val list = mAdapter.currentList.toMutableList()
        var parentComment = curComment

        if (answerMode) {
            list.removeAt(answerPosition)
            parentComment = findParentComment(answerPosition)!!
            type = "child"
        } else {
            var size = findNextParentComment(commentPosition) - 1
            var idx = 1
            for (i in commentPosition..size) {
                if (i != commentPosition)
                    list.removeAt(i - idx++)
                else
                    list.removeAt(i)
            }

            updateCommentCount(mFeed, countTextView, false)
            mCommentListViewModel.requestUploadCommentCount(
                mFeed.getEmail()!!, mFeed.getTimestamp(), countTextView.text.toString().toLong(), false
            )

            countTextView.text = (countTextView.text.toString().toLong() - 1L).toString()
            type = "parent"
        }

        mCommentListViewModel.requestDeleteComment(
            mFeed.getEmail()!!, mFeed.getTimestamp(), parentComment!!,
            answerComment, type, list
        )
    }


    fun showComment(readAnswer: Button, targetComment: Comment) {
        var currentCommentList = mAdapter.currentList.toMutableList()
        var pos = currentCommentList.indexOf(targetComment)
        var index = pos + 1


        for (element in targetComment.getChildren()!!) {
            currentCommentList.add(index, element)
            index++
        }


        targetComment.initChildren()
        currentCommentList.set(pos, targetComment)
        mCommentListViewModel.setCommentListLiveData(currentCommentList.toList())
        readAnswer.text = mContext.getString(R.string.comment_fold_answer)
    }

    fun hideComment(readAnswer: Button, targetComment: Comment) {
        var currentCommentList = mAdapter.currentList.toMutableList()
        var childList = mutableListOf<Comment>()
        var pos = currentCommentList.indexOf(targetComment)

        while (currentCommentList.size > pos + 1 && currentCommentList[pos + 1].type == Constants.CHILD) {
            childList.add(currentCommentList.removeAt(pos + 1))
        }

        targetComment.setChildren(childList)
        currentCommentList.set(currentCommentList.indexOf(targetComment), targetComment)
        mCommentListViewModel.setCommentListLiveData(currentCommentList.toList())
        readAnswer.text = mContext.getString(R.string.comment_read_answer)
    }

    fun findParentComment(answerPosition : Int): Comment? {
        var list = mAdapter.currentList
        for (i in answerPosition downTo 0) {
            if (list[i].type == Constants.PARENT)
                return list[i]
        }
        return null
    }

    fun findNextParentComment(commentPosition: Int): Int {
        var list = mAdapter.currentList
        for (i in commentPosition + 1 until list.size) {
            if (list[i].type == Constants.PARENT) {
                return i
            } else if (i == list.size - 1) {
                return i + 1
            }
        }
        return commentPosition + 1
    }

    fun setHint(commentEditText : EditText, modeTitleTextView : TextView, method: Int) {
        if (method == 0) {
            commentEditText.setHint(R.string.comment_hint)
        } else if (method == 1) {
            commentEditText.setHint(R.string.answer_write_ing)
            modeTitleTextView.setText(R.string.answer_write_ing)
        } else if (method == 2) {
            commentEditText.setHint(R.string.answer_modify_ing)
            modeTitleTextView.setText(R.string.answer_modify_ing)
        } else if (method == 3) {
            commentEditText.setHint(R.string.comment_modify_ing)
            modeTitleTextView.setText(R.string.comment_modify_ing)
        }
    }


}