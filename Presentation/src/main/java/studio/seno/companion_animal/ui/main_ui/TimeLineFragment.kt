package studio.seno.companion_animal.ui.main_ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentTimeLineBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.companion_animal.ui.feed.FeedListViewModel
import studio.seno.companion_animal.ui.feed.ShowFeedActivity
import studio.seno.companion_animal.ui.follow.FollowActivity
import studio.seno.companion_animal.ui.search.OnSearchItemClickListener
import studio.seno.companion_animal.ui.search.SearchResultAdapter
import studio.seno.datamodule.LocalRepository
import studio.seno.datamodule.RemoteRepository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import studio.seno.domain.util.PrefereceManager


class TimeLineFragment : Fragment(), View.OnClickListener,
    BottomSheetImagePicker.OnImagesSelectedListener {
    private lateinit var binding: FragmentTimeLineBinding
    private lateinit var localRepository: LocalRepository
    private val remoteRepository: RemoteRepository = RemoteRepository.getInstance()!!
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val searchResultAdapter = SearchResultAdapter()


    companion object {
        @JvmStatic
        fun newInstance() =
            TimeLineFragment().apply { arguments = Bundle().apply {} }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_time_line, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = requireActivity()
        binding.feedViewModel = feedListViewModel
        binding.timelineRecyclerView.adapter = searchResultAdapter

        init()
        userInfoSet()
        setFeedList()
        feedItemEvent()
        observe()
    }

    fun init() {
        localRepository = LocalRepository.getInstance(requireContext())!!

        binding.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
        binding.header.findViewById<TextView>(R.id.title).text = getString(R.string.timeline_title)
        binding.header.findViewById<LinearLayout>(R.id.menu_set).visibility = View.VISIBLE
        binding.header.findViewById<ImageButton>(R.id.search).visibility = View.GONE
        binding.header.findViewById<ImageButton>(R.id.refresh).visibility = View.GONE

        binding.header.findViewById<ImageButton>(R.id.add).setOnClickListener(this)
        binding.timelineProfileImageView.setOnClickListener(this)
        binding.infoModifyBtn.setOnClickListener(this)
        binding.followerBtn.setOnClickListener(this)
        binding.followingBtn.setOnClickListener(this)
        binding.nickNameEdit.setText(PrefereceManager.getString(requireContext(), "nickName"))
        binding.nickNameEdit.isEnabled = false
    }

    fun userInfoSet() {
        localRepository.getUserInfo(lifecycleScope, object : LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if (result is Result.Success) {
                    binding.nickNameEdit.setText(result.data.nickname)
                    binding.feedCount.text = String.format(getString(R.string.timeLine_menu1), result.data.feedCount)
                    binding.followerBtn.text = String.format(getString(R.string.timeLine_menu2), result.data.follower)
                    binding.followingBtn.text = String.format(getString(R.string.timeLine_menu3), result.data.following)

                    Glide.with(requireContext())
                        .load(Uri.parse(result.data.profileUri))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.timelineProfileImageView)
                } else if (result is Result.Error) {
                    Log.e("error", "timeline userInfoSet error : ${result.exception}")
                }
            }
        })
    }

    fun setFeedList() {
        feedListViewModel.clearFeedList()
        feedListViewModel.requestLoadFeedList(
            null, "myFeed", FirebaseAuth.getInstance().currentUser?.email.toString(),
            binding.timelineRecyclerView, null
        )
    }

    fun feedItemEvent(){
        searchResultAdapter.setOnItemClickListener(object : OnSearchItemClickListener{
            override fun onSearchItemClicked(feed: Feed, position : Int) {
                startActivity<ShowFeedActivity>(
                    "sort" to "my_feed_list",
                    "position" to position
                )
            }
        })
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.add) {
            var menuDialog = MenuDialog.newInstance("null", false)
            menuDialog.show(parentFragmentManager, "write")
        } else if (v?.id == R.id.info_modify_btn) {
            if (binding.infoModifyBtn.text == getString(R.string.info_complete)) {
                binding.nickNameEdit.isEnabled = false
                binding.infoModifyBtn.text = getString(R.string.info_modify)

                localRepository.updateNickname(lifecycleScope, binding.nickNameEdit.text.toString())
                mainViewModel.requestUpdateNickname(binding.nickNameEdit.text.toString())
            } else {
                binding.nickNameEdit.isEnabled = true
                binding.nickNameEdit.requestFocus()
                binding.infoModifyBtn.text = getString(R.string.info_complete)
            }

        } else if (v?.id == R.id.timeline_profileImageView) {
            BottomSheetImagePicker.Builder(getString(R.string.file_provider))
                .cameraButton(ButtonType.Button)
                .galleryButton(ButtonType.Button)
                .singleSelectTitle(R.string.pick_single)
                .peekHeight(R.dimen.peekHeight)
                .columnSize(R.dimen.columnSize)
                .requestTag("single")
                .show(childFragmentManager, null)
        } else if (v?.id == R.id.follower_btn) {
            startActivity<FollowActivity>("category" to "follower")
        } else if (v?.id == R.id.following_btn) {
            startActivity<FollowActivity>("category" to "following")
        }
    }

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        CommonFunction.getInstance()!!.lockTouch(activity?.window!!)
        binding.progressBar.visibility = View.VISIBLE

        remoteRepository.uploadInItProfileImage(uris[0], object : LongTaskCallback<Boolean> {
            override fun onResponse(result: Result<Boolean>) {
                if (result is Result.Success) {

                    remoteRepository.loadRemoteProfileImage(
                        FirebaseAuth.getInstance().currentUser?.email.toString(),
                        object : LongTaskCallback<String> {
                            override fun onResponse(result: Result<String>) {
                                if (result is Result.Success) {
                                    val profileUri = result.data

                                    remoteRepository.updateRemoteProfileUri(profileUri)
                                    LocalRepository(context!!).updateProfileUri(
                                        lifecycleScope,
                                        profileUri,
                                        object : LongTaskCallback<User> {
                                            override fun onResponse(result: Result<User>) {
                                                if (result is Result.Success) {
                                                    Glide.with(requireContext())
                                                        .load(uris[0])
                                                        .centerCrop()
                                                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                                        .into(binding.timelineProfileImageView)

                                                    CommonFunction.getInstance()!!
                                                        .unlockTouch(activity?.window!!)
                                                    binding.progressBar.visibility = View.GONE
                                                }
                                            }
                                        })

                                } else if (result is Result.Error) {
                                    Log.e(
                                        "eror",
                                        "timeline onImagesSelected error : ${result.exception}"
                                    )
                                }
                            }
                        })
                } else if (result is Result.Error)
                    Log.e("eror", "timeline onImagesSelected error : ${result.exception}")
            }
        })
    }

    private fun observe() {
        feedListViewModel.getFeedListLiveData().observe(viewLifecycleOwner, {
            searchResultAdapter.submitList(it)
        })
    }
}

