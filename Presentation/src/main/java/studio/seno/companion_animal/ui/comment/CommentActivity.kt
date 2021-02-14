package studio.seno.companion_animal.ui.comment

import android.content.DialogInterface
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
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.companion_animal.ui.main_ui.MainViewModel
import studio.seno.companion_animal.util.Constants
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
    private val viewModel: CommentListViewModel by viewModels()
    private val mainViewModel : MainViewModel by viewModels()
    private val commentAdapter = CommentAdapter()
    private var answerMode = false
    private var modifyMode = false
    private var backKeyPressedTime = 0L
    private lateinit var commentCountText: TextView
    private var curComment: Comment? = null
    private var answerComment : Comment? = null
    private var answerPosition = 0
    private val feed : Feed by lazy {intent.getParcelableExtra<Feed>("feed")}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment)
        binding.lifecycleOwner = this
        binding.model = viewModel

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
            viewModel.requestLoadComment(feed.email, feed.timestamp)

        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        binding.modeCloseBtn.setOnClickListener(this)
        binding.commentBtn.setOnClickListener(this)
        binding.commentRecyclerView.adapter = commentAdapter

    }

    fun commentEvent() {
        commentAdapter.setOnEventListener(object : OnEventListener {
            override fun onReadAnswerClicked(readAnswer: Button, targetComment: Comment) {
                var currentCommentList = commentAdapter.currentList.toMutableList()
                var pos = currentCommentList.indexOf(targetComment)
                var index = pos + 1

                if (targetComment.getChildren() != null) {
                    for (element in targetComment.getChildren()!!) {
                        currentCommentList.add(index, element)
                        index++
                    }
                }
                targetComment.initChildren()
                currentCommentList.set(pos, targetComment)
                viewModel.setCommentListLiveData(currentCommentList.toList())
                readAnswer.visibility = View.GONE
            }

            override fun onWriteAnswerCilcked(targetComment: Comment) {
                curComment = targetComment

                setHint(1)
                answerMode = true
                binding.modeLayout.visibility = View.VISIBLE
            }

            override fun onMenuClicked(comment: Comment, position : Int) {
                var menuDialog = MenuDialog.newInstance(comment.email, false)

                if (comment.type == Constants.PARENT) {
                    menuDialog.show(supportFragmentManager, "comment")
                    answerMode = false
                    curComment = comment
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
        viewModel.getCommentListLiveData().observe(this, {
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
               if(modifyMode && answerComment != null) { //답글 수정 모드
                   findParentComment()?.let { submitCommentAnswer(it,answerComment!!.timestamp) }

               } else if(!modifyMode && curComment != null){ //일반 답글 모드
                   submitCommentAnswer(curComment!!, timestamp)
                   sendNotification(curComment!!.email, binding.comment.text.toString(), timestamp)
               }
            } else {
                if (modifyMode) { //댓글 수정 모드
                    submitComment(curComment!!.timestamp)
                } else { // 일반 댓글 모드
                    submitComment(timestamp)
                    sendNotification(feed.email, binding.comment.text.toString(), timestamp)
                }
            }
            initVariable()
        }
    }

    fun submitComment(timestamp: Long) {
        //일반 댓글 모드
        viewModel.requestUploadComment(
            feed.email,
            feed.timestamp,
            Constants.PARENT,
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            PrefereceManager.getString(this, "nickName")!!,
            binding.comment.text.toString(),
            timestamp
        )
        //총 댓글 수 업로드
        viewModel.requestUploadCommentCount(
            feed.email,
            feed.timestamp,
            commentCountText.text.toString().toLong(),
            true
        )
        commentCountText.text = (commentCountText.text.toString().toLong() + 1L).toString()
    }

    fun submitCommentAnswer(parentComment : Comment, answerTimestamp: Long){
        //답글 업로드
        viewModel.requestUploadCommentAnswer(
            feed.email,
            feed.timestamp,
            parentComment.email,
            parentComment.timestamp,
            Constants.CHILD,
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            PrefereceManager.getString(this, "nickName")!!,
            binding.comment.text.toString(),
            answerTimestamp
        )
    }

    fun deleteComment(){
        var type : String? = null
        if(answerMode) {
            curComment = findParentComment()
            type = "child"
        } else{
            viewModel.requestUploadCommentCount(
                feed.email,
                feed.timestamp,
                commentCountText.text.toString().toLong(),
                false
            )

            commentCountText.text = (commentCountText.text.toString().toLong() - 1L).toString()
            type = "parent"
        }

        viewModel.requestDeleteComment(
            feed.email,
            feed.timestamp,
            curComment!!,
            answerComment,
            type
        )
    }

    fun sendNotification(targetEmail : String, content : String, currentTimestamp : Long){
        //댓글을 작성하면 notification 알림이 전송
        mainViewModel.requestUserData(targetEmail, object : LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if(result is Result.Success) {

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
                    var responseBodyCall: retrofit2.Call<ResponseBody> = apiService.sendNotification(notificationModel)
                    responseBodyCall.enqueue(object : retrofit2.Callback<ResponseBody> {
                        override fun onResponse(
                            call: retrofit2.Call<ResponseBody>,
                            response: retrofit2.Response<ResponseBody>
                        ) {
                            Log.d("hi","success")
                        }

                        override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                            Log.d("hi","onFailure")
                        }

                    })
                }
            }
        })
    }

    fun findParentComment() : Comment?{
        var list = commentAdapter.currentList
        for(i in answerPosition downTo 0){
            if(list[i].type == Constants.PARENT)
                return list[i]
        }
        return null
    }

    fun setHint(method: Int) {
        if (method == 0) {
            binding.comment.setHint(R.string.comment_hint)
        } else if (method == 1) {
            binding.comment.setHint(R.string.answer_write_hint)
            binding.modeTitle.setText(R.string.answer_write_hint)
        } else if (method == 2) {
            binding.comment.setHint(R.string.answer_modify_ing)
            binding.modeTitle.setText(R.string.answer_modify_ing)
        } else if (method == 3) {
            binding.comment.setHint(R.string.comment_modify_ing)
            binding.modeTitle.setText(R.string.comment_modify_ing)
        }
    }

    fun initVariable(){
        answerMode = false
        curComment = null
        answerComment = null
        modifyMode = false
        CommonFunction.closeKeyboard(applicationContext, binding.comment)
        binding.comment.setText("")
        setHint(0)
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
            setHint(3)
            CommonFunction.showKeyboard(this)
        } else if (PrefereceManager.getString(applicationContext, "mode") == "comment_answer_modify") {
            binding.modeLayout.visibility = View.VISIBLE
            modifyMode = true
            setHint(2)
            CommonFunction.showKeyboard(this)
        } else if (PrefereceManager.getString(applicationContext, "mode") == "comment_delete") {
            deleteComment()
        } else {

        }
    }
}