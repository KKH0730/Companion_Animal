package studio.seno.companion_animal.ui.comment

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import studio.seno.commonmodule.CustomToast
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivityCommentBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.companion_animal.util.Constants
import studio.seno.domain.database.InfoManager
import studio.seno.domain.model.Comment
import java.sql.Timestamp

class CommentActivity : AppCompatActivity(), View.OnClickListener,
    DialogInterface.OnDismissListener {
    private lateinit var binding: ActivityCommentBinding
    private val viewModel: CommentListViewModel by viewModels()
    private val commentAdapter = CommentAdapter()
    private var answerMode = false
    private var modifyMode = false
    private var backKeyPressedTime = 0L
    private lateinit var commentCountText: TextView
    private lateinit var headerTitle: TextView
    private var curComment: Comment? = null
    private var answerComment : Comment? = null
    private var answerPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment)
        binding.lifecycleOwner = this
        binding.model = viewModel

        initView()

        if (intent.getStringExtra("email") != null && intent.getLongExtra("timestamp", 0L) != 0L)
            viewModel.requestLoadComment(
                intent.getStringExtra("email")!!, intent.getLongExtra(
                    "timestamp",
                    0
                )
            )

        commentEvent()
        observe()
    }


    private fun initView() {
        headerTitle = binding.header.findViewById(R.id.title)
        headerTitle.text = getString(R.string.header_title)

        commentCountText = binding.header.findViewById(R.id.comment_count)
        commentCountText.apply {
            visibility = View.VISIBLE
            text = intent.getIntExtra("commentCount", 0).toString()
        }


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
            if (answerMode) { // 답글쓰기 모드
               if(modifyMode && answerComment != null) { //답글 수정 모드
                   findParentComment()?.let { submitCommentAnswer(it,answerComment!!.timestamp) }

               } else if(!modifyMode && curComment != null){
                       submitCommentAnswer(curComment!!, Timestamp(System.currentTimeMillis()).time) //일반 답글 모드
               }
            } else {
                if (modifyMode) { //댓글 수정 모드
                    submitComment(curComment!!.timestamp)
                } else { // 일반 댓글 모드
                    submitComment(Timestamp(System.currentTimeMillis()).time)
                }
            }
            initVariable()
        }
    }

    fun submitComment(timestamp: Long) {
        //일반 댓글 모드
        viewModel.requestUploadComment(
            intent.getStringExtra("email"),
            intent.getLongExtra("timestamp", 0L),
            Constants.PARENT,
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            InfoManager.getString(this, "nickName")!!,
            binding.comment.text.toString(),
            timestamp
        )
        //총 댓글 수 업로드
        viewModel.requestUploadCommentCount(
            intent.getStringExtra("email"),
            intent.getLongExtra("timestamp", 0L),
            commentCountText.text.toString().toLong(),
            true
        )
        commentCountText.text = (commentCountText.text.toString().toLong() + 1L).toString()
    }

    fun submitCommentAnswer(parentComment : Comment, answerTimestamp: Long){
        //답글 업로드
        viewModel.requestUploadCommentAnswer(
            intent.getStringExtra("email"),
            intent.getLongExtra("timestamp", 0L),
            parentComment.email,
            parentComment.timestamp,
            Constants.CHILD,
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            InfoManager.getString(this, "nickName")!!,
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
                intent.getStringExtra("email"),
                intent.getLongExtra("timestamp", 0L),
                commentCountText.text.toString().toLong(),
                false
            )

            commentCountText.text = (commentCountText.text.toString().toLong() - 1L).toString()
            type = "parent"
        }

        viewModel.requestDeleteComment(
            intent.getStringExtra("email"),
            intent.getLongExtra("timestamp", 0L),
            curComment!!,
            answerComment,
            type
        )
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
        if (InfoManager.getString(applicationContext, "mode") == "comment_modify") {
            binding.modeLayout.visibility = View.VISIBLE
            modifyMode = true
            setHint(3)
            CommonFunction.showKeyboard(this)
        } else if (InfoManager.getString(applicationContext, "mode") == "comment_answer_modify") {
            binding.modeLayout.visibility = View.VISIBLE
            modifyMode = true
            setHint(2)
            CommonFunction.showKeyboard(this)
        } else if (InfoManager.getString(applicationContext, "mode") == "comment_delete") {
            deleteComment()
        } else {

        }
    }
}