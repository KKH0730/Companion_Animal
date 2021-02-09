package studio.seno.companion_animal.ui.comment

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
import studio.seno.companion_animal.util.Constants
import studio.seno.domain.database.InfoManager
import studio.seno.domain.model.Comment
import java.sql.Timestamp

class CommentActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityCommentBinding
    private val viewModel : CommentListViewModel by viewModels()
    private val commentAdapter = CommentAdapter()
    private var answerMode = false
    private var backKeyPressedTime = 0L
    private lateinit var commentCountText : TextView
    private lateinit var headerTitle : TextView
    private var curTargetComment : Comment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment)
        binding.lifecycleOwner = this
        binding.model = viewModel

        initView()

        if(intent.getStringExtra("email") != null && intent.getLongExtra("timestamp", 0L) != 0L)
            viewModel.requestLoadComment(
                intent.getStringExtra("email")!!, intent.getLongExtra(
                    "timestamp",
                    0
                )
            )

        commentEvent()
        observe()
    }


    private fun initView(){
        headerTitle =  binding.header.findViewById(R.id.title)
        headerTitle.text = getString(R.string.header_title)

        commentCountText = binding.header.findViewById(R.id.comment_count)
        commentCountText.apply{
            visibility = View.VISIBLE
            text = intent.getIntExtra("commentCount", 0).toString()
        }


        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        binding.modeCloseBtn.setOnClickListener(this)
        binding.commentBtn.setOnClickListener(this)
        binding.commentRecyclerView.adapter = commentAdapter

    }

    fun commentEvent(){
        commentAdapter.setOnEventListener(object : OnEventListener {
            override fun OnReadAnswerClicked(readAnswer: Button, targetComment : Comment) {
                var currentCommentList = commentAdapter.currentList.toMutableList()
                var pos = currentCommentList.indexOf(targetComment)
                var index = pos + 1

                if(targetComment.getChildren() != null) {
                    for(element in targetComment.getChildren()!!) {
                        currentCommentList.add(index, element)
                        index++
                    }
                }
                targetComment.initChildren()
                currentCommentList.set(pos, targetComment)
                viewModel.setCommentListLiveData(currentCommentList.toList())
                readAnswer.visibility = View.GONE
            }

            override fun OnWriteAnswerCilcked(targetComment : Comment) {
                curTargetComment = targetComment

                setHint(1)
                answerMode = true
                binding.modeLayout.visibility = View.VISIBLE
            }
        })
    }

    private fun observe(){
        viewModel.getCommentListLiveData().observe(this, {
            commentAdapter.submitList(it)
        })
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.back_btn) {
            finish()
        } else if(v?.id == R.id.mode_close_btn){
            setHint(0)
            binding.modeLayout.visibility = View.INVISIBLE
            answerMode = false
        } else if(v?.id == R.id.comment_btn) {
            if(answerMode) { // 답글쓰기 모드
                if(curTargetComment != null) {
                    //댓글 업로드
                    viewModel.requestUploadCommentAnswer(
                        intent.getStringExtra("email"),
                        intent.getLongExtra("timestamp", 0L),
                        curTargetComment!!.email,
                        curTargetComment!!.timestamp,
                        Constants.CHILD,
                        FirebaseAuth.getInstance().currentUser?.email.toString(),
                        InfoManager.getString(this,"nickName")!!,
                        binding.comment.text.toString(),
                        Timestamp(System.currentTimeMillis()).time
                    )
                    //댓글 수 업로드
                    viewModel.requestUploadCommentCount(
                        intent.getStringExtra("email"),
                        intent.getLongExtra("timestamp", 0L),
                        commentCountText.text.toString().toLong()
                    )
                    commentCountText.text = (commentCountText.text.toString().toLong() + 1L).toString()
                }
            } else {
                //댓글 업로드
                viewModel.requestUploadComment(
                    intent.getStringExtra("email"),
                    intent.getLongExtra("timestamp", 0L),
                    Constants.PARENT,
                    FirebaseAuth.getInstance().currentUser?.email.toString(),
                    InfoManager.getString(this,"nickName")!!,
                    binding.comment.text.toString(),
                    Timestamp(System.currentTimeMillis()).time
                )
                //댓글 수 업로드
                viewModel.requestUploadCommentCount(
                    intent.getStringExtra("email"),
                    intent.getLongExtra("timestamp", 0L),
                    commentCountText.text.toString().toLong()
                )
                commentCountText.text = (commentCountText.text.toString().toLong() + 1L).toString()
            }
            CommonFunction.closeKeyboard(this, binding.comment)
            binding.comment.setText("")
            binding.modeLayout.visibility = View.INVISIBLE
            setHint(0)
        }
    }

    fun setHint(method: Int) {
        if (method == 0) {
            binding.comment.setHint(R.string.comment_hint)

        } else if (method == 1) {
            binding.comment.setHint(R.string.answer_write_hint)
            binding.modeTitle.setText(R.string.answer_write_hint)
        } else if (method == 2) {
            binding.comment.setText(R.string.answer_modify_ing)
            binding.modeTitle.setText(R.string.answer_modify_ing)
        } else if (method == 3) {
            binding.comment.setText(R.string.comment_modify_ing)
            binding.modeTitle.setText(R.string.comment_modify_ing)
        }
    }

    override fun onBackPressed() {
        if(binding.modeLayout.visibility == View.VISIBLE) {
            if(System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis()
                binding.modeLayout.visibility = View.INVISIBLE
                setHint(0)
            } else {
                finish()
            }
        } else
            finish()
    }
}