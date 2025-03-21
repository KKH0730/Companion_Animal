package studio.seno.companion_animal.ui.main_ui

import android.app.AlertDialog
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
import dagger.hilt.android.AndroidEntryPoint
import studio.seno.companion_animal.R
import studio.seno.companion_animal.base.CustomToast
import studio.seno.companion_animal.databinding.FragmentHomeBinding
import studio.seno.companion_animal.extension.startActivity
import studio.seno.companion_animal.module.FeedModule
import studio.seno.companion_animal.ui.ReportActivity
import studio.seno.companion_animal.ui.comment.CommentActivity
import studio.seno.companion_animal.ui.comment.CommentListViewModel
import studio.seno.companion_animal.ui.feed.*
import studio.seno.companion_animal.ui.search.SearchActivity
import studio.seno.companion_animal.util.Constants
import studio.seno.datamodule.repository.local.LocalRepository
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

@AndroidEntryPoint
class HomeFragment : Fragment(), View.OnClickListener {
    private var binding: FragmentHomeBinding? = null
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val commentViewModel: CommentListViewModel by viewModels()
    private var filter1 = true
    private var filter2 = true
    private var filter3 = true
    private var feedSort: String? = null
    private var feedPosition: Int? = null
    private var timeLineEmail: String? = null
    private val feedModule: FeedModule by lazy {
        FeedModule(feedListViewModel, commentViewModel)
    }
    private var targetFeed: Feed? = null
    private var feedAdapter: FeedListAdapter? = null

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
        fun newInstance(feedSort: String, feedPosition: Int, timeLineEmail: String) =
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
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        feedItemEvent()
        if (feedSort == "feed_list") {
            binding!!.refreshLayout.isEnabled = true
            refreshFeedList()
        }
        loadFeedList()
        setListener()
        observe()
    }

    private fun init() {
        binding!!.lifecycleOwner = viewLifecycleOwner
        binding!!.model = feedListViewModel
        feedAdapter = FeedListAdapter(parentFragmentManager, lifecycle, lifecycleScope)

        feedAdapter!!.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding!!.feedRecyclerView!!.adapter = feedAdapter
        binding!!.feedRecyclerView.itemAnimator = null


        if (feedSort == "feed_list") {
            binding!!.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
            binding!!.header.findViewById<TextView>(R.id.title).visibility = View.GONE
            binding!!.header.findViewById<ImageView>(R.id.logo).visibility = View.VISIBLE
            binding!!.header.findViewById<LinearLayout>(R.id.menu_set).visibility = View.VISIBLE
            binding!!.header.findViewById<ImageButton>(R.id.setting).visibility = View.GONE
            binding!!.header.findViewById<ConstraintLayout>(R.id.header_layout).layoutParams.height =
                requireActivity().applicationContext.resources.getDimension(R.dimen.header_layout_height)
                    .toInt()

        } else if (feedSort != null && feedSort == "feed_timeline")
            binding!!.header.visibility = View.GONE

        binding!!.refreshLayout.isEnabled = false
        binding!!.header.findViewById<ImageButton>(R.id.add).setOnClickListener(this)
        binding!!.header.findViewById<ImageButton>(R.id.search).setOnClickListener(this)
        binding!!.header.findViewById<ImageButton>(R.id.refresh).setOnClickListener(this)
        binding!!.header.findViewById<ImageButton>(R.id.scroll_up).setOnClickListener(this)
        binding!!.header.findViewById<ImageButton>(R.id.filter).setOnClickListener(this)
    }

    private fun refreshFeedList() {
        binding!!.refreshLayout.setOnRefreshListener {
            loadFeedList()
        }
    }

    private fun loadFeedList() {
        //게시판 데이터 서버로부터 불러와서 viewmode의 livedata 업데이트
        feedListViewModel.clearFeedList()
        if (feedSort != null && feedSort == "feed_list")
            feedListViewModel.getPagingFeed(filter1, filter2, filter3, null, "feed_list",
                null, binding!!.feedRecyclerView, object : LongTaskCallback<Any> {
                    override fun onResponse(result: Result<Any>) {
                        if (result is Result.Success) {
                            binding?.refreshLayout?.isRefreshing = false

                        } else if (result is Result.Error) {
                            e("error", "feed refresh error : ${result.exception}")
                        }
                    }
                })
        else if (feedSort != null && feedSort == "feed_timeline") {
            feedListViewModel.getPagingFeed(
                null, null, null,
                null,
                "feed_timeline",
                timeLineEmail,
                binding!!.feedRecyclerView,
                object : LongTaskCallback<Any> {
                    override fun onResponse(result: Result<Any>) {
                        binding!!.feedRecyclerView.scrollToPosition(feedPosition!!)
                    }
                })
        }
    }

    private fun observe() {
        feedListViewModel.getFeedListLiveData().observe(requireActivity(), {
            feedAdapter?.submitList(it)
        })
    }

    private fun feedItemEvent() {
        //댓글작성 버튼클릭
        feedAdapter?.setOnItemClickListener(object : OnItemClickListener {
            override fun onDetailClicked(feed: Feed, position: Int) {
                requireContext().startActivity(FeedDetailActivity::class.java) {
                    putExtra("feed", feed)
                }
            }

            override fun onImageBtnClicked(feed: Feed) {
                requireContext().startActivity(FeedImageActivity::class.java) {
                    putExtra("feed", feed)
                }
            }

            override fun onCommentBtnClicked(
                feed: Feed,
                commentEdit: EditText,
                commentCount: TextView,
                container: LinearLayout
            ) {
                LocalRepository.getInstance(activity?.applicationContext!!)
                    ?.getUserInfo(lifecycleScope, object :
                        LongTaskCallback<User> {
                        override fun onResponse(result: Result<User>) {
                            if (result is Result.Success) {
                                feedModule.onCommentBtnClicked(
                                    feed,
                                    result.data.email,
                                    result.data.nickname,
                                    result.data.profileUri,
                                    commentEdit,
                                    commentCount,
                                    container,
                                    lifecycleScope
                                )
                            }
                        }
                    })
            }

            override fun onCommentShowClicked(
                commentCountTextView: TextView,
                feed: Feed,
                position: Int
            ) {
                requireContext().startActivity(CommentActivity::class.java) {
                    putExtra("commentCount", Integer.valueOf(commentCountTextView.text.toString()))
                    putExtra("feed", feed)
                }
            }

            override fun onMenuClicked(feed: Feed, position: Int) {
                targetFeed = feed
                feedModule.menuButtonEvent(feed, parentFragmentManager)
            }

            override fun onHeartClicked(
                feed: Feed,
                heartCount: TextView,
                heartButton: ImageButton
            ) {
                feedModule.heartButtonEvent(feed, heartCount, heartButton, feedAdapter)
            }

            override fun onBookmarkClicked(feed: Feed, bookmarkButton: ImageButton) {
                feedModule.bookmarkButtonEvent(feed, bookmarkButton, feedAdapter)
            }

            override fun onProfileLayoutClicked(feed: Feed) {
                requireContext().startActivity(ShowFeedActivity::class.java) {
                    putExtra("profileEmail", feed.getEmail())
                    putExtra("feedSort", "profile")
                }
            }

            override fun onShareButtonClicked(feed: Feed) {
                feedModule.sendShareLink(feed, requireContext(), lifecycleScope)
            }
        })
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.add) {
            val intent = Intent(requireContext(), MakeFeedActivity::class.java)
            startActivityForResult(intent, Constants.FEED_MAKE_QEQUEST)
        } else if (v?.id == R.id.search) {
            requireContext().startActivity(SearchActivity::class.java)
        } else if (v?.id == R.id.refresh) {
            loadFeedList()
        } else if (v?.id == R.id.scroll_up) {
            binding!!.feedRecyclerView.smoothScrollToPosition(0)
        } else if (v?.id == R.id.filter) {
            showFilterDialog()
        }
    }


    fun onDismissed(type: String) {
        if (targetFeed != null) {
            if (type == "feed_modify") {
                requireContext().startActivity(MakeFeedActivity::class.java) {
                    putExtra("feed", targetFeed)
                    putExtra("mode", "modify")
                }
            } else if (type == "feed_delete") {
                requireContext().startActivity(MakeFeedActivity::class.java) {
                    putExtra("feed", targetFeed)
                    putExtra("mode", "delete")
                }
            } else if (type == "follow") {
                feedModule.onDismiss(
                    "follow",
                    targetFeed,
                    requireActivity(),
                    LocalRepository.getInstance(requireActivity().applicationContext)!!,
                    feedAdapter,
                    lifecycleScope
                )
                CustomToast(requireContext(), getString(R.string.follow_toast)).show()
            } else if (type == "unfollow") {
                feedModule.onDismiss(
                    "unfollow",
                    targetFeed,
                    requireActivity(),
                    LocalRepository.getInstance(requireActivity().applicationContext)!!,
                    feedAdapter,
                    lifecycleScope
                )
                CustomToast(requireContext(), getString(R.string.unfollow_toast)).show()
            } else if (type == "report") {
                requireContext().startActivity(ReportActivity::class.java) {
                    putExtra("feed", targetFeed)
                }
            }
        }
    }


    private fun showFilterDialog() {
        val versionArray = arrayOf(
            getString(R.string.check_box1),
            getString(R.string.check_box2),
            getString(R.string.check_box3)
        )
        val checkArray = booleanArrayOf(filter1, filter2, filter3)

        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(getString(R.string.filter_title))
            .setMultiChoiceItems(versionArray, checkArray) { _, which, isChecked ->
                when (which) {
                    0 -> filter1 = isChecked
                    1 -> filter2 = isChecked
                    2 -> filter3 = isChecked
                }
            }

        builder.setPositiveButton(getString(R.string.submit)) { dialog, which -> loadFeedList() }
        builder.show()
    }


    private fun setListener() {
        FirebaseFirestore.getInstance()
            .collection("feed")
            .addSnapshotListener { value, error ->
                if (error != null)
                    return@addSnapshotListener
                else {
                    var feed: Feed? = null
                    for (element in value?.documentChanges!!) {
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

                        when (element.type) {
                            DocumentChange.Type.ADDED -> {
                                addFeedItem(feed)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                modifyFeedItem(feed)
                            }
                            DocumentChange.Type.REMOVED -> {
                                deleteFeedItem(feed)
                            }
                        }


                    }
                }
            }
    }

    private fun addFeedItem(feed: Feed) {
        val tempList = feedListViewModel.getFeedListLiveData().value?.toMutableList()
        tempList?.let {
            it.add(feed)
            feedListViewModel.setFeedListLiveData(it.toList())
        }
    }


    private fun modifyFeedItem(feed: Feed) {
        val tempList = feedListViewModel.getFeedListLiveData().value?.toMutableList()
        if(tempList != null) {
            val idx = binarySearch(feed.getTimestamp(), tempList)
            tempList[idx] = feed
            feedListViewModel.setFeedListLiveData(tempList.toList())
        }
    }

    private fun deleteFeedItem(feed: Feed) {
        val tempList = feedListViewModel.getFeedListLiveData().value?.toMutableList()
        if(tempList != null) {
            val idx = binarySearch(feed.getTimestamp(), tempList)
            tempList.removeAt(idx)
            feedListViewModel.setFeedListLiveData(tempList.toList())
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.FEED_MAKE_QEQUEST && resultCode == Constants.RESULT_OK) {
            loadFeedList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
        feedAdapter = null
    }

    private fun binarySearch(timestamp: Long, tempList: MutableList<Feed>) : Int{
        var pl = 0
        var pr: Int = tempList.size

        do {
            val pc = (pl + pr) / 2
            if (tempList[pc].getTimestamp() == timestamp) {
                return pc
            } else if (tempList[pc].getTimestamp() < timestamp) {
                pr = pc - 1
            } else {
                pl = pc + 1
            }
        } while (pl <= pr)
        return -1
    }
}

