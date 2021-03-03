package studio.seno.companion_animal.ui.main_ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.*
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.commonmodule.CustomToast
import studio.seno.companion_animal.ErrorActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentHomeBinding
import studio.seno.companion_animal.module.FeedModule
import studio.seno.companion_animal.ui.comment.CommentActivity
import studio.seno.companion_animal.ui.comment.CommentListViewModel
import studio.seno.companion_animal.ui.feed.*
import studio.seno.companion_animal.ui.search.SearchActivity
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.LocalRepository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import studio.seno.domain.util.PreferenceManager
import java.util.logging.Logger

/**
 * HomeFragment는 FeedViewListModel과 연결.
 * FeedViewModel는 FeedListAdapter와 연결.
 */
class HomeFragment : Fragment(), View.OnClickListener{
    private lateinit var binding: FragmentHomeBinding
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val commentViewModel : CommentListViewModel by viewModels()
    private var filter1  = true
    private var filter2  = true
    private var filter3  = true
    private var feedSort : String? = null
    private var feedPosition : Int? = null
    private var timeLineEmail : String? = null
    private val feedModule : FeedModule by lazy {
        FeedModule(feedListViewModel, commentViewModel)
    }
    private var targetFeed : Feed? = null
    private val feedAdapter: FeedListAdapter by lazy { FeedListAdapter(parentFragmentManager, lifecycle, lifecycleScope) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            feedSort = it.getString("feedSort")
            feedPosition = it.getInt("feedPosition")
            timeLineEmail = it.getString("timeLineEmail")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(feedSort : String, feedPosition : Int, timeLineEmail : String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString("feedSort", feedSort)
                    putInt("feedPosition", feedPosition)
                    putString("timeLineEmail", timeLineEmail)
                }
            }
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

        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = feedListViewModel
        feedAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.feedRecyclerView.adapter = feedAdapter

        init()

        feedItemEvent()
        if(feedSort == "feed_list") {
            binding.refreshLayout.isEnabled = true
            refreshFeedList()
        }
        loadFeedList()
        observe()
        setListener()



    }

    private fun init(){
        binding.feedRecyclerView.itemAnimator = null

        if(feedSort == "feed_list") {
            binding.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
            binding.header.findViewById<TextView>(R.id.title).visibility = View.GONE
            binding.header.findViewById<ImageView>(R.id.logo).visibility = View.VISIBLE
            binding.header.findViewById<LinearLayout>(R.id.menu_set).visibility = View.VISIBLE
            binding.header.findViewById<ImageButton>(R.id.setting).visibility = View.GONE
            binding.header.findViewById<ConstraintLayout>(R.id.header_layout).layoutParams.height =
                requireActivity().applicationContext.resources.getDimension(R.dimen.header_layout_height).toInt()

        } else if(feedSort != null && feedSort == "feed_timeline")
            binding.header.visibility = View.GONE

        binding.refreshLayout.isEnabled = false
        binding.header.findViewById<ImageButton>(R.id.add).setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.search).setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.refresh).setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.scroll_up).setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.filter).setOnClickListener(this)
    }

    private fun refreshFeedList(){
        binding.refreshLayout.setOnRefreshListener {
            loadFeedList()
        }
    }

    fun loadFeedList(){
        //게시판 데이터 서버로부터 불러와서 viewmode의 livedata 업데이트
        feedListViewModel.clearFeedList()
        if(feedSort != null && feedSort == "feed_list")
            feedListViewModel.requestLoadFeedList(filter1, filter2, filter3, null, "feed_list",
                null, binding.feedRecyclerView, object : LongTaskCallback<List<Feed>>{
                    override fun onResponse(result: Result<List<Feed>>) {
                        if (result is Result.Success) {
                            binding.refreshLayout.isRefreshing = false
                            //feedListViewModel.setFeedListListener()

                        } else if (result is Result.Error) {
                            e("error", "feed refresh error : ${result.exception}")
                        }
                    }
                })

        else if(feedSort != null && feedSort == "feed_timeline") {
            feedListViewModel.requestLoadFeedList(
                null, null, null,
                null,
                "feed_timeline",
                timeLineEmail,
                binding.feedRecyclerView,
                object : LongTaskCallback<List<Feed>>{
                    override fun onResponse(result: Result<List<Feed>>) {
                        binding.feedRecyclerView.scrollToPosition(feedPosition!!)
                    }
                })
        }
    }

    private fun observe() {
        feedListViewModel.getFeedListLiveData().observe(requireActivity(), {
            feedAdapter.submitList(it)
        })
    }

    private fun feedItemEvent() {
        //댓글작성 버튼클릭
        feedAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onDetailClicked(feed : Feed, position : Int) {
                startActivity<FeedDetailActivity>("feed" to feed,)
            }

            override fun onImageBtnClicked(feed: Feed) {
                startActivity<FeedImageActivity>("feed" to feed)
            }

            override fun onCommentBtnClicked(feed: Feed, commentEdit: EditText, commentCount: TextView, container: LinearLayout) {
                LocalRepository.getInstance(activity?.applicationContext!!)?.getUserInfo(lifecycleScope, object : LongTaskCallback<User>{
                    override fun onResponse(result: Result<User>) {
                        if(result is Result.Success) {
                            feedModule.onCommentBtnClicked(feed, result.data.email, result.data.nickname, result.data.profileUri, commentEdit, commentCount, container, lifecycleScope)
                        }
                    }
                })
            }

            override fun onCommentShowClicked(commentCountTextView: TextView, feed: Feed, position : Int) {
                startActivity<CommentActivity>("commentCount" to Integer.valueOf(commentCountTextView.text.toString()), "feed" to feed,)
            }

            override fun onMenuClicked(feed: Feed, position: Int) {
                targetFeed = feed
                feedModule.menuButtonEvent(feed, parentFragmentManager)
            }

            override fun onHeartClicked(feed: Feed, heartCount: TextView, heartButton: ImageButton) {
                feedModule.heartButtonEvent(feed, heartCount, heartButton, feedAdapter)
            }

            override fun onBookmarkClicked(feed: Feed, bookmarkButton: ImageButton) {
                feedModule.bookmarkButtonEvent(feed, bookmarkButton, feedAdapter)
            }

            override fun onProfileLayoutClicked(feed: Feed) {
                startActivity<ShowFeedActivity>("profileEmail" to feed.getEmail(), "feedSort" to "profile")
            }

            override fun onShareButtonClicked(feed: Feed) {
                kakaoLink(feed)
            }
        })
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.add) {
            startActivityForResult(
                intentFor<MakeFeedActivity>(), Constants.FEED_MAKE_QEQUEST
            )
        } else if(v?.id == R.id.search) {
            startActivity<SearchActivity>()
        } else if(v?.id == R.id.refresh) {
            loadFeedList()
        } else if(v?.id == R.id.scroll_up) {
            binding.feedRecyclerView.smoothScrollToPosition(0)
        } else if(v?.id == R.id.filter) {
            showFilterDialog()
        }
    }


    fun onDismissed(type: String) {
        if(targetFeed != null) {
            if(type == "feed_modify") {
                startActivity<MakeFeedActivity>("feed" to targetFeed, "mode" to "modify")
            } else if(type == "feed_delete") {
                startActivity<MakeFeedActivity>("feed" to targetFeed, "mode" to "delete")
            } else if(type == "follow") {
                feedModule.onDismiss("follow", targetFeed, requireActivity(), LocalRepository.getInstance(requireActivity().applicationContext)!!, feedAdapter, lifecycleScope)
                CustomToast(requireContext(), getString(R.string.follow_toast)).show()
            } else if(type == "unfollow") {
                feedModule.onDismiss("unfollow", targetFeed, requireActivity(), LocalRepository.getInstance(requireActivity().applicationContext)!!, feedAdapter, lifecycleScope)
                CustomToast(requireContext(), getString(R.string.unfollow_toast)).show()
            } else if(type == "report") {
                startActivity<ErrorActivity>("feed" to targetFeed)
            }
        }
    }



    fun showFilterDialog(){
        val versionArray = arrayOf(
            getString(R.string.check_box1),
            getString(R.string.check_box2),
            getString(R.string.check_box3)
        )
        val checkArray = booleanArrayOf(filter1, filter2, filter3)

        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(getString(R.string.filter_title))
            .setMultiChoiceItems(versionArray, checkArray) { dialog, which, isChecked ->
                when (which) {
                    0 -> filter1 = isChecked
                    1 -> filter2 = isChecked
                    2 -> filter3 = isChecked
                }
            }

        builder.setPositiveButton(getString(R.string.submit)) { dialog, which -> loadFeedList() }
        builder.show()
    }


    fun setListener() {
        FirebaseFirestore.getInstance()
            .collection("feed")
            .addSnapshotListener { value, error ->
                if(error != null)
                    return@addSnapshotListener
                else {
                    var feed : Feed? = null
                    for(element in value?.documentChanges!!) {
                        feed = Feed(
                            element.document.getString("email"),
                            element.document.getString("nickname")!!,
                            element.document.getString("sort")!!,
                            element.document.data.get("hashTags") as MutableList<String>,
                            element.document.data.get("localUri") as MutableList<String>,
                            element.document.getString("content")!!,
                            element.document.getLong("heart")!!,
                            element.document.getLong("comment")!!,
                            element.document.getLong("timestamp")!!,
                            element.document.getString("remoteProfileUri")!!,
                            element.document.data.get("remoteUri") as MutableList<String>,
                            element.document.data.get("heartList") as Map<String, String>,
                            element.document.data.get("bookmarkList") as Map<String, String>
                        )

                        when(element.type) {
                            DocumentChange.Type.ADDED -> {}

                            DocumentChange.Type.MODIFIED ->  { modifyItem(feed) }
                            DocumentChange.Type.REMOVED -> {removeItem(feed)}
                        }


                    }
                }
            }
    }


    fun modifyItem(feed : Feed){
        val tempList = feedListViewModel.getFeedListLiveData().value?.toMutableList()
        for((position, item) in tempList?.withIndex()!!) {
            if(item.getTimestamp() == feed.getTimestamp()) {
                tempList[position] = feed
                feedListViewModel.setFeedListLiveData(tempList.toList())
                break
            }
        }
    }

    fun removeItem(feed: Feed) {
        val tempList = feedListViewModel.getFeedListLiveData().value?.toMutableList()
        for((position, item) in tempList?.withIndex()!!) {
            if(item.getTimestamp() == feed.getTimestamp()) {
                tempList.removeAt(position)
                feedListViewModel.setFeedListLiveData(tempList.toList())
                break
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == Constants.FEED_MAKE_QEQUEST && resultCode == Constants.RESULT_OK) {
            loadFeedList()
        }
    }

    fun kakaoLink(feed: Feed) {
        val params = FeedTemplate
            .newBuilder(
                ContentObject.newBuilder(
                    getString(R.string.kakao_title),
                    "https://firebasestorage.googleapis.com/v0/b/companion-animal-f0bfa.appspot.com/o/logo.png?alt=media&token=a0fa3ee2-d150-47d0-855d-ecded4306030",
                    LinkObject.newBuilder().setMobileWebUrl("https://www.naver.com").build()
                )
                    .setDescrption(getString(R.string.kakao_description))
                    .build()
            )
            .addButton(
                ButtonObject(
                    getString(R.string.kakao_description2), LinkObject.newBuilder()
                        .setMobileWebUrl("https://www.naver.com")
                        .setAndroidExecutionParams("path=${feed.getEmail()}${feed.getTimestamp()}")
                        .build()
                )
            )
            .build()

        val serverCallbackArgs: MutableMap<String, String> = HashMap()
        serverCallbackArgs["user_id"] = "\${current_user_id}"
        serverCallbackArgs["product_id"] = "\${shared_product_id}"

        KakaoLinkService.getInstance().sendDefault(
            requireContext(),
            params,
            serverCallbackArgs,
            object : ResponseCallback<KakaoLinkResponse?>() {
                override fun onFailure(errorResult: ErrorResult) {
                    e("error", "kakao share error : ${errorResult.errorMessage}")
                }

                override fun onSuccess(result: KakaoLinkResponse?) {}
            })
    }
}

