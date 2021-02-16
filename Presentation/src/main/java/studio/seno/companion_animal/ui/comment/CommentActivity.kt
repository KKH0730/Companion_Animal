package studio.seno.companion_animal.ui.comment

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import okhttp3.ResponseBody
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivityCommentBinding
import studio.seno.companion_animal.module.CommentModule
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.companion_animal.ui.main_ui.MainViewModel
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.Repository
import studio.seno.datamodule.api.ApiClient
import studio.seno.datamodule.api.ApiInterface
import studio.seno.datamodule.model.NotificationModel
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.util.PrefereceManager
import studio.seno.domain.model.Comment
import studio.seno.domain.model.Feed
import studio.seno.domain.model.NotificationData
import studio.seno.domain.model.User
import java.sql.Timestamp

class CommentActivity : AppCompatActivity(), View.OnClickListener,
    DialogInterface.OnDismissListener {
    private lateinit var binding: ActivityCommentBinding
    private val commentListViewModel: CommentListViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val commentAdapter = CommentAdapter()
    private var answerMode = false
    private var modifyMode = false
    private var backKeyPressedTime = 0L
    private lateinit var commentCountText: TextView
    private var curComment: Comment? = null
    private var answerComment: Comment? = null
    private var answerPosition = 0
    private var commentPosition = 0
    private val feed: Feed by lazy { intent.getParcelableExtra<Feed>("feed") }
    private val commentModule : CommentModule by lazy {
        CommentModule(
            mainViewModel, commentListViewModel, feed,
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            PrefereceManager.getString(applicationContext, "nickName")!!,
            applicationContext, commentAdapter
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment)
        binding.lifecycleOwner = this
        binding.model = commentListViewModel

        initView()
        commentEvent()
        observe()
    }

    private fun initView() {
        binding.header.findViewById<TextView>(R.id.title).text = getString(R.string.header_title)
        commentCountText = binding.header.findViewById(R.id.comment_count)
        binding.header.findViewById<TextView>(R.id.comment_count).apply {
            visibility = View.VISIBLE
            text = intent.getIntExtra("commentCount", 0).toString()
        }

        if (intent.getParcelableExtra<Feed>("feed") != null)
            commentListViewModel.requestLoadComment(feed.email, feed.timestamp)

        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        binding.modeCloseBtn.setOnClickListener(this)
        binding.commentBtn.setOnClickListener(this)
        binding.commentRecyclerView.adapter = commentAdapter
    }

    fun commentEvent() {
        commentAdapter.setOnEventListener(object : OnCommentEventListener {
            override fun onReadAnswerClicked(readAnswer: Button, targetComment: Comment) {
                if (targetComment.getChildren()!!.size != 0)
                    commentModule.showComment(readAnswer, targetComment)
                else {
                    commentModule.hideComment(readAnswer, targetComment)
                }
            }

            override fun onWriteAnswerClicked(targetComment: Comment, position: Int) {
                curComment = targetComment
                commentPosition = position

                commentModule.setHint(binding.comment, binding.modeTitle, 1)
                answerMode = true
                binding.modeLayout.visibility = View.VISIBLE
            }

            override fun onMenuClicked(comment: Comment, position: Int) {
                var menuDialog = MenuDialog.newInstance(comment.email, false)

                if (comment.type == Constants.PARENT) {
                    menuDialog.show(supportFragmentManager, "comment")
                    answerMode = false
                    curComment = comment
                    commentPosition = position
                } else {
                    menuDialog.show(supportFragmentManager, "comment_answer")
                    answerMode = true
                    answerComment = comment
                    answerPosition = position
                }

            }
        })
    }


    private fun observe() {
        commentListViewModel.getCommentListLiveData().observe(this, {
            commentAdapter.submitList(it)
        })
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.back_btn) {
            finish()
        } else if (v?.id == R.id.mode_close_btn) {
            initVariable()
        } else if (v?.id == R.id.comment_btn) {
            val timestamp = Timestamp(System.currentTimeMillis()).time

            if (answerMode) { // 답글쓰기 모드
                if (modifyMode && answerComment != null) { //답글 수정 모드
                        commentModule.submitCommentAnswer(
                            commentModule.findParentComment(answerPosition)!!, timestamp, modifyMode, answerComment!!, answerPosition,
                            commentPosition, binding.comment
                        )
                } else if (!modifyMode && curComment != null) { //일반 답글 모드
                    commentModule.submitCommentAnswer(
                        curComment!!, timestamp, modifyMode, answerComment,
                        answerPosition, commentPosition, binding.comment
                    )
                    commentModule.sendNotification(curComment!!.email, binding.comment.text.toString(), timestamp)
                }
            } else {
                if (modifyMode) { //댓글 수정 모드
                    commentModule.submitComment(
                        curComment!!.timestamp, modifyMode, curComment, commentPosition,
                        binding.header.findViewById(R.id.comment_count), binding.comment
                    )
                } else { // 일반 댓글 모드
                    commentModule.submitComment(
                        timestamp, modifyMode, curComment, commentPosition,
                        binding.header.findViewById(R.id.comment_count), binding.comment
                    )
                    commentModule.sendNotification(feed.email, binding.comment.text.toString(), timestamp)
                }
            }
            initVariable()
        }
    }

    fun initVariable() {
        answerMode = false
        curComment = null
        answerComment = null
        modifyMode = false
        CommonFunction.closeKeyboard(applicationContext, binding.comment)
        binding.comment.setText("")
        commentModule.setHint(binding.comment, binding.modeTitle, 0)
        binding.modeLayout.visibility = View.INVISIBLE
    }

    override fun onBackPressed() {
        if (binding.modeLayout.visibility == View.VISIBLE) {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis()
                initVariable()
            } else {
                finish()
            }
        } else
            finish()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        if (PrefereceManager.getString(applicationContext, "mode") == "comment_modify") {
            binding.modeLayout.visibility = View.VISIBLE
            modifyMode = true
            commentModule.setHint(binding.comment, binding.modeTitle, 3)
            CommonFunction.showKeyboard(this)
        } else if (PrefereceManager.getString(
                applicationContext,
                "mode"
            ) == "comment_answer_modify"
        ) {
            binding.modeLayout.visibility = View.VISIBLE
            modifyMode = true
            commentModule.setHint(binding.comment, binding.modeTitle, 2)
            CommonFunction.showKeyboard(this)
        } else if (PrefereceManager.getString(applicationContext, "mode") == "comment_delete") {
            commentModule.deleteComment(
                curComment, answerComment, commentPosition, answerPosition,
                answerMode, binding.header.findViewById(R.id.comment_count)
            )
        } else {

        }
    }

    /*
    fun submitComment(timestamp: Long) {
        val currentCommentList = commentAdapter.currentList.toMutableList()
        val content = binding.comment.text.toString()

        if(modifyMode) {
            curComment!!.content = content
            currentCommentList[commentPosition] = curComment!!

            commentListViewModel.setCommentListLiveData(currentCommentList.toList())
        } else {
            Repository().loadRemoteProfileImage(
                FirebaseAuth.getInstance().currentUser?.email.toString(),
                object : LongTaskCallback<String>{

                override fun onResponse(result: Result<String>) {
                    if(result is Result.Success) {
                        currentCommentList.add(Comment(
                            Constants.PARENT,
                            myEmail,
                            PrefereceManager.getString(applicationContext, "nickName")!!,
                            content,
                            result.data,
                            timestamp
                        ))
                        commentListViewModel.setCommentListLiveData(currentCommentList.toList())
                    }
                }
            })
        }

        //일반 댓글 모드
        commentListViewModel.requestUploadComment(
            feed.email,
            feed.timestamp,
            Constants.PARENT,
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            PrefereceManager.getString(this, "nickName")!!,
            content,
            timestamp
        )
        //총 댓글 수 업로드
        commentListViewModel.requestUploadCommentCount(
            feed.email,
            feed.timestamp,
            commentCountText.text.toString().toLong(),
            true
        )
        commentCountText.text = (commentCountText.text.toString().toLong() + 1L).toString()
    }
 */
    /*
fun submitCommentAnswer(parentComment: Comment, answerTimestamp: Long) {
    val currentCommentList = commentAdapter.currentList.toMutableList()
    val content = binding.comment.text.toString()

    if(modifyMode) {
        answerComment!!.content = content
        currentCommentList.set(answerPosition, answerComment!!)
        commentListViewModel.setCommentListLiveData(currentCommentList)
    } else {
        Repository().loadRemoteProfileImage(myEmail, object :LongTaskCallback<String>{
            override fun onResponse(result: Result<String>) {
                if(result is Result.Success) {

                    currentCommentList.add(
                        findNextParentComment(commentPosition),
                        Comment(
                            Constants.CHILD,
                            myEmail,
                            PrefereceManager.getString(applicationContext, "nickName")!!,
                            content,
                            result.data,
                            answerTimestamp
                        ))
                    commentListViewModel.setCommentListLiveData(currentCommentList)
                }
            }
        })
    }


    //답글 업로드
    commentListViewModel.requestUploadCommentAnswer(
        feed.email,
        feed.timestamp,
        parentComment.email,
        parentComment.timestamp,
        Constants.CHILD,
        FirebaseAuth.getInstance().currentUser?.email.toString(),
        PrefereceManager.getString(this, "nickName")!!,
        content,
        answerTimestamp
    )
}
 */

    /*
fun deleteComment() {
    val type: String?
    val list = commentAdapter.currentList.toMutableList()

    if (answerMode) {
        list.removeAt(answerPosition)
        curComment = findParentComment()
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

        commentListViewModel.requestUploadCommentCount(
            feed.email,
            feed.timestamp,
            commentCountText.text.toString().toLong(),
            false
        )

        commentCountText.text = (commentCountText.text.toString().toLong() - 1L).toString()
        type = "parent"
    }

    commentListViewModel.requestDeleteComment(
        feed.email,
        feed.timestamp,
        curComment!!,
        answerComment,
        type,
        list
    )
}
 */

    /*
fun findParentComment(): Comment? {
    var list = commentAdapter.currentList
    for (i in answerPosition downTo 0) {
        if (list[i].type == Constants.PARENT)
            return list[i]
    }
    return null
}

fun findNextParentComment(commentPosition: Int): Int {
    var list = commentAdapter.currentList
    for (i in commentPosition + 1 until list.size) {
        if (list[i].type == Constants.PARENT) {
            return i
        } else if (i == list.size - 1) {
            return i + 1
        }
    }
    return commentPosition + 1
}
 */


    /*
    fun showComment(readAnswer: Button, targetComment: Comment) {
        var currentCommentList = commentAdapter.currentList.toMutableList()
        var pos = currentCommentList.indexOf(targetComment)
        var index = pos + 1


        for (element in targetComment.getChildren()!!) {
            currentCommentList.add(index, element)
            index++
        }


        targetComment.initChildren()
        currentCommentList.set(pos, targetComment)
        commentListViewModel.setCommentListLiveData(currentCommentList.toList())
        //readAnswer.visibility = View.GONE
        readAnswer.text = applicationContext.getString(R.string.comment_fold_answer)
    }

    fun hideComment(readAnswer: Button, targetComment: Comment) {
        //targetcomment의 child에 넣어야함
        var currentCommentList = commentAdapter.currentList.toMutableList()
        var childList = mutableListOf<Comment>()
        var pos = currentCommentList.indexOf(targetComment)

        while (currentCommentList.size > pos + 1 && currentCommentList[pos + 1].type == Constants.CHILD) {
            childList.add(currentCommentList.removeAt(pos + 1))
        }

        targetComment.setChildren(childList)
        currentCommentList.set(currentCommentList.indexOf(targetComment), targetComment)
        commentListViewModel.setCommentListLiveData(currentCommentList.toList())
        readAnswer.text = applicationContext.getString(R.string.comment_read_answer)
    }
 */


    /*
fun sendNotification(targetEmail: String, content: String, currentTimestamp: Long) {
    //댓글을 작성하면 notification 알림이 전송
    mainViewModel.requestUserData(targetEmail, object : LongTaskCallback<User> {
        override fun onResponse(result: Result<User>) {
            if (result is Result.Success) {

                val notificationModel = NotificationModel(
                    result.data.token,
                    NotificationData(
                        PrefereceManager.getString(applicationContext, "nickName")!!,
                        "${feed.email + currentTimestamp} $content",
                        null,
                        null,
                        true
                    )
                )

                var apiService = ApiClient.getClient().create(ApiInterface::class.java)
                var responseBodyCall: retrofit2.Call<ResponseBody> =
                    apiService.sendNotification(notificationModel)
                responseBodyCall.enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onResponse(
                        call: retrofit2.Call<ResponseBody>,
                        response: retrofit2.Response<ResponseBody>
                    ) {
                        Log.d("hi", "success")
                    }

                    override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                        Log.d("hi", "onFailure")
                    }

                })
            }
        }
    })
}
 */
}