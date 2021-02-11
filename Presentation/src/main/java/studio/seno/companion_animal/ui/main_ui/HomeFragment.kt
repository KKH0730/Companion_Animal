package studio.seno.companion_animal.ui.main_ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startActivityForResult
import seno.library.refreshlayout.OnRefreshListener
import seno.library.refreshlayout.SimpleRefreshLayout
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentHomeBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.companion_animal.ui.comment.CommentActivity
import studio.seno.companion_animal.ui.comment.CommentListViewModel
import studio.seno.companion_animal.ui.feed.*
import studio.seno.companion_animal.util.Constants
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.database.InfoManager
import studio.seno.domain.model.Feed
import java.sql.Timestamp

/**
 * HomeFragment는 FeedViewListModel과 연결.
 * FeedViewModel는 FeedListAdapter와 연결.
 */
class HomeFragment : Fragment(){
    private lateinit var binding: FragmentHomeBinding
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val commentViewModel : CommentListViewModel by viewModels()
    private var targetFeed : Feed? = null
    private val currentUserEmail  = FirebaseAuth.getInstance().currentUser?.email.toString()
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
        binding.model = feedListViewModel
        binding.feedRecyclerView.adapter = feedAdapter

        feedItemEvent()
        freshFeedList()

        binding.refreshLayout.bringToFront()
        //게시판 데이터 서버로부터 불러와서 viewmode의 livedata 업데이트
        feedListViewModel.loadFeedList(null)
        observe()


    }




    private fun observe() {
        feedListViewModel.getFeedListLiveData().observe(requireActivity(), {
            feedAdapter.submitList(it)
        })

    }

    private fun feedItemEvent() {
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
                commentViewModel.requestUploadComment(
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
                commentViewModel.requestUploadCommentCount(feed.email!!, feed.timestamp, commentCount.text.toString().toLong(), true)

                //댓글수 업데이트
                commentCount.apply {
                    var curCommentCount = Integer.valueOf(commentCount.text.toString())
                    text = (curCommentCount + 1).toString()
                }

                commentEdit.setText("")
                commentEdit.hint = requireContext().getString(R.string.comment_hint)
                CommonFunction.closeKeyboard(requireContext(), commentEdit)
            }

            override fun onCommentShowClicked(commentCount: TextView, feed: Feed) {
                startActivityForResult(intentFor<CommentActivity>(
                    "commentCount" to Integer.valueOf(commentCount.text.toString()),
                    "email" to feed.email,
                    "timestamp" to feed.timestamp
                ), Constants.COMMENT_REQUEST)
            }

            override fun onMenuClicked(feed: Feed, position: Int) {
                targetFeed = feed
                feedListViewModel.requestCheckFollow(feed, currentUserEmail, object : LongTaskCallback<Boolean>{
                    override fun onResponse(result: Result<Boolean>) {
                        var dialog : MenuDialog? = null

                        if(result is Result.Success) {
                            if(result.data)
                                dialog = MenuDialog.newInstance(feed.email, true)
                            else
                                dialog = MenuDialog.newInstance(feed.email, false)
                            dialog.show(parentFragmentManager, "feed")
                        } else if(result is Result.Error) {
                            Log.e("error", "follow check : ${result.exception}")
                        }
                    }
                })
            }

            override fun onHeartClicked(feed: Feed, heartCount : TextView, heartButton : ImageButton) {
                var map = feed.heartList?.toMutableMap()!!
                var count = feed.heart
                if(map[currentUserEmail] != null) {
                    count--
                    updateHeart(feed, count,false)
                    map.remove(currentUserEmail)
                    heartButton.isSelected = false
                } else {
                    count++
                    updateHeart(feed, count,true)
                    map[currentUserEmail] = currentUserEmail

                    heartButton.isSelected = true
                }

                feed.apply {
                    heartList = map
                    heart = count
                }
                heartCount.text = count.toString()
                feedAdapter.notifyDataSetChanged()
            }

            override fun onBookmarkClicked(feed: Feed, bookmarkButton: ImageButton) {
                var map = feed.bookmarkList?.toMutableMap()!!
                if(map[currentUserEmail] != null) { //북마크 중인 상태에서 클릭
                    updateBookmark(feed, false)
                    map.remove(currentUserEmail)
                    bookmarkButton.isSelected = false
                } else {
                    updateBookmark(feed, true)
                    map[currentUserEmail] = currentUserEmail
                    bookmarkButton.isSelected = true
                }
                feed.bookmarkList = map
                feedAdapter.notifyDataSetChanged()
            }

        })
    }

    fun updateHeart(feed: Feed, count : Long, flag : Boolean) { feedListViewModel.requestUpdateHeart(feed, count, currentUserEmail, flag) }

    fun updateBookmark(feed: Feed, flag : Boolean) { feedListViewModel.requestUpdateBookmark(feed, currentUserEmail, flag) }

    fun updateFollower(feed: Feed, flag : Boolean) { feedListViewModel.requestUpdateFollower(feed, currentUserEmail, flag)}

    fun freshFeedList(){
        binding.refreshLayout.setOnRefreshListener {
            feedListViewModel.loadFeedList(object : LongTaskCallback<List<Feed>>{
                override fun onResponse(result: Result<List<Feed>>) {
                    if(result is Result.Success) {
                        binding.refreshLayout.isRefreshing = false
                        binding.feedRecyclerView.smoothScrollToPosition(0)
                    } else if(result is Result.Error) {
                        Log.e("error", "feed refresh error : ${result.exception}")
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()

        feedAdapter.notifyDataSetChanged()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    fun onDismissed(type: String) {
        if(targetFeed != null) {
            if(type == "feed_modify") {
                startActivity<MakeFeedActivity>(
                    "feed" to targetFeed,
                    "mode" to "modify"
                )
            } else if(type == "feed_delete") {
                startActivity<MakeFeedActivity>(
                    "feed" to targetFeed,
                    "mode" to "delete"
                )
            } else if(type == "follow") {
                if(targetFeed != null)
                    feedListViewModel.requestUpdateFollower(targetFeed!!, currentUserEmail, true)
            } else if(type == "unfollow") {
                if(targetFeed != null) {
                    feedListViewModel.requestUpdateFollower(targetFeed!!, currentUserEmail, false)
                }
            }
        }
    }
}

