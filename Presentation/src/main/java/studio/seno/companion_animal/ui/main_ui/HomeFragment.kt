package studio.seno.companion_animal.ui.main_ui

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import okhttp3.ResponseBody
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentHomeBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.module.TextModule
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.companion_animal.ui.comment.CommentActivity
import studio.seno.companion_animal.ui.comment.CommentListViewModel
import studio.seno.companion_animal.ui.feed.FeedListAdapter
import studio.seno.companion_animal.ui.feed.FeedListViewModel
import studio.seno.companion_animal.ui.feed.MakeFeedActivity
import studio.seno.companion_animal.ui.feed.OnItemClickListener
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.api.ApiClient
import studio.seno.datamodule.api.ApiInterface
import studio.seno.domain.model.NotificationData
import studio.seno.datamodule.model.NotificationModel
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import studio.seno.domain.util.PrefereceManager
import java.sql.Timestamp

/**
 * HomeFragment는 FeedViewListModel과 연결.
 * FeedViewModel는 FeedListAdapter와 연결.
 */
class HomeFragment : Fragment(){
    private lateinit var binding: FragmentHomeBinding
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val commentViewModel : CommentListViewModel by viewModels()
    private val mainViewModel : MainViewModel by viewModels()
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
                container: LinearLayout
            ) {
                //피드에 보여지는 댓글의 라이브 데이터 업데이트
                //model.setFeedCommentLiveData(commentEdit.text.toString())
                val textView = TextView(requireContext())
                val nickname = PrefereceManager.getString(requireContext(), "nickName")
                val commentContent = commentEdit.text.toString()
                    container.apply{
                    removeAllViews()
                    SpannableStringBuilder(nickname).apply {

                        TextModule().setTextColorBold(
                            this,
                            requireContext(),
                            R.color.black,
                            0,
                            nickname!!.length
                        )
                        append("  $commentContent")
                        textView.text = this
                    }
                    addView(textView)
                }


                //댓글을 서버에 업로드
                commentViewModel.requestUploadComment(
                    feed.email!!,
                    feed.timestamp,
                    Constants.PARENT,
                    FirebaseAuth.getInstance().currentUser?.email.toString(),
                    PrefereceManager.getString(
                        requireContext(),
                        "nickName"
                    )!!,
                    commentEdit.text.toString(),
                    Timestamp(System.currentTimeMillis()).time
                )

                //서버에 댓글 개수 업로드
                commentViewModel.requestUploadCommentCount(
                    feed.email,
                    feed.timestamp,
                    commentCount.text.toString().toLong(),
                    true
                )

                //댓글수 업데이트
                commentCount.apply {
                    var curCommentCount = Integer.valueOf(commentCount.text.toString())
                    text = (curCommentCount + 1).toString()
                }

                commentEdit.setText("")
                commentEdit.hint = requireContext().getString(R.string.comment_hint)
                CommonFunction.closeKeyboard(requireContext(), commentEdit)

                //댓글을 작성하면 notification 알림이 전송
                mainViewModel.requestUserData(feed.email, object : LongTaskCallback<User> {
                    override fun onResponse(result: Result<User>) {
                        if(result is Result.Success) {

                            val notificationModel = NotificationModel(
                                result.data.token,
                                NotificationData(
                                    nickname!!,
                                    "${feed.email + Timestamp(System.currentTimeMillis()).time} $commentContent",
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

            override fun onCommentShowClicked(commentCount: TextView, feed: Feed) {
                startActivityForResult(
                    intentFor<CommentActivity>(
                        "commentCount" to Integer.valueOf(commentCount.text.toString()),
                        "feed" to feed,
                    ), Constants.COMMENT_REQUEST
                )
            }

            override fun onMenuClicked(feed: Feed, position: Int) {
                targetFeed = feed
                feedListViewModel.requestCheckFollow(
                    feed,
                    currentUserEmail,
                    object : LongTaskCallback<Boolean> {
                        override fun onResponse(result: Result<Boolean>) {
                            var dialog: MenuDialog? = null

                            if (result is Result.Success) {
                                if (result.data)
                                    dialog = MenuDialog.newInstance(feed.email, true)
                                else
                                    dialog = MenuDialog.newInstance(feed.email, false)
                                dialog.show(parentFragmentManager, "feed")
                            } else if (result is Result.Error) {
                                Log.e("error", "follow check : ${result.exception}")
                            }
                        }
                    })
            }

            override fun onHeartClicked(
                feed: Feed,
                heartCount: TextView,
                heartButton: ImageButton
            ) {
                var map = feed.heartList?.toMutableMap()!!
                var count = feed.heart
                if (map[currentUserEmail] != null) {
                    count--
                    updateHeart(feed, count, false)
                    map.remove(currentUserEmail)
                    heartButton.isSelected = false
                } else {
                    count++
                    updateHeart(feed, count, true)
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
                if (map[currentUserEmail] != null) { //북마크 중인 상태에서 클릭
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

    fun updateHeart(feed: Feed, count: Long, flag: Boolean) { feedListViewModel.requestUpdateHeart(
        feed,
        count,
        currentUserEmail,
        flag
    ) }

    fun updateBookmark(feed: Feed, flag: Boolean) { feedListViewModel.requestUpdateBookmark(
        feed,
        currentUserEmail,
        flag
    ) }

    fun updateFollower(feed: Feed, flag: Boolean) { feedListViewModel.requestUpdateFollower(
        feed,
        currentUserEmail,
        flag
    )}

    fun freshFeedList(){
        binding.refreshLayout.setOnRefreshListener {
            feedListViewModel.loadFeedList(object : LongTaskCallback<List<Feed>> {
                override fun onResponse(result: Result<List<Feed>>) {
                    if (result is Result.Success) {
                        binding.refreshLayout.isRefreshing = false
                        binding.feedRecyclerView.smoothScrollToPosition(0)
                    } else if (result is Result.Error) {
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

