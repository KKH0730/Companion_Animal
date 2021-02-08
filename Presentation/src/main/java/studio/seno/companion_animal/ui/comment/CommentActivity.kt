package studio.seno.companion_animal.ui.comment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivityCommentBinding

class CommentActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityCommentBinding
    private val viewModel : CommentListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment)

        initView()

        if(intent.getStringExtra("email") != null && intent.getLongExtra("timestamp", 0L) != 0L) {
            viewModel.requestLoadComment(intent.getStringExtra("email")!!, intent.getLongExtra("timestamp", 0))
        }

    }

    private fun initView(){
        binding.header.findViewById<TextView>(R.id.title).text = getString(R.string.header_title)
        binding.header.findViewById<TextView>(R.id.comment_count).apply{
            visibility = View.VISIBLE
            text = intent.getIntExtra("commentCount", 0).toString()
        }

        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.back_btn) {
            finish()
        }
    }
}