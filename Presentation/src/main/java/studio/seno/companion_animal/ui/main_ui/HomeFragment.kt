package studio.seno.companion_animal.ui.main_ui

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.companion_animal.OnItemClickListener
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentHomeBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.ui.comment.CommentActivity
import studio.seno.companion_animal.ui.feed.FeedListAdapter
import studio.seno.companion_animal.ui.feed.FeedListViewModel
import studio.seno.companion_animal.ui.feed.FeedViewModel
import studio.seno.companion_animal.util.Constants
import studio.seno.companion_animal.util.TextUtils.setTextColorBold
import studio.seno.domain.database.InfoManager
import studio.seno.domain.model.Feed
import java.sql.Timestamp

/**
 * HomeFragment는 FeedViewListModel과 연결.
 * FeedViewModel는 FeedListAdapter와 연결.
 */
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: FeedListViewModel by viewModels()
    private val feedAdapter: FeedListAdapter by lazy {
        FeedListAdapter(
            parentFragmentManager,
            lifecycle,
            viewLifecycleOwner
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.model = viewModel
        binding.feedRecyclerView.adapter = feedAdapter


        itemEvent()
    }


    private fun observe() {
        viewModel.getFeedListLiveData().observe(requireActivity(), {
            feedAdapter.submitList(it)
        })
    }

    private fun itemEvent() {
        //댓글작성 버튼클릭
        feedAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onCommentBtnClicked(
                feed: Feed,
                commentEdit: EditText,
                commentCount: TextView,
                model : FeedViewModel
            ) {
                //피드에 보여지는 댓글의 라이브 데이터 업데이트
                model.setFeedCommentLiveData(commentEdit.text.toString())

                //댓글을 서버에 업로드
                viewModel.requestUploadComment(
                    feed.email!!,
                    feed.timestamp,
                    Constants.PARENT,
                    FirebaseAuth.getInstance().currentUser?.email.toString(),
                    InfoManager.getString(
                        requireContext(),
                        "nickName"
                    )!!,
                    commentEdit.text.toString(),
                    Timestamp(System.currentTimeMillis()).time
                )

                //서버에 댓글 개수 업로드
                viewModel.requestUploadCommentCount(feed.email!!, feed.timestamp, commentCount.text.toString().toLong(), true)

                //댓글수 업데이트
                commentCount.text.apply {
                    var curCommentCount = Integer.valueOf(commentCount.text.toString())
                    (curCommentCount + 1).toString()
                }


                commentEdit.setText("")
                commentEdit.hint = requireContext().getString(R.string.comment_hint)
                CommonFunction.closeKeyboard(requireContext(), commentEdit)
            }

            override fun onCommentShowClicked(commentCount: TextView, feed: Feed) {
                startActivity<CommentActivity>(
                    "commentCount" to Integer.valueOf(commentCount.text.toString()),
                    "email" to feed.email,
                    "timestamp" to feed.timestamp
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()

        //게시판 데이터 서버로부터 불러와서 viewmode의 livedata 업데이트
        viewModel.loadFeedList()
        observe()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}