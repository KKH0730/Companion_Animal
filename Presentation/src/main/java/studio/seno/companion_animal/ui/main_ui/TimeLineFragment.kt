package studio.seno.companion_animal.ui.main_ui

import android.app.AlertDialog
import android.content.Context
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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import com.marcoscg.easylicensesdialog.EasyLicensesDialogCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.startActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentTimeLineBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.module.ProfileModule
import studio.seno.companion_animal.ui.chat.ChatActivity
import studio.seno.companion_animal.ui.gridLayout.FeedGridFragment
import studio.seno.companion_animal.ui.feed.MakeFeedActivity
import studio.seno.companion_animal.ui.feed.ShowFeedActivity
import studio.seno.companion_animal.ui.follow.FollowActivity
import studio.seno.companion_animal.ui.user_manage.UserManageActivity
import studio.seno.companion_animal.ui.user_manage.UserViewModel
import studio.seno.companion_animal.util.FinishActivityInterface
import studio.seno.datamodule.repository.local.LocalRepository
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import studio.seno.domain.model.User


class TimeLineFragment : Fragment(), View.OnClickListener,
    BottomSheetImagePicker.OnImagesSelectedListener {
    private lateinit var binding: FragmentTimeLineBinding
    private lateinit var localRepository: LocalRepository
    private lateinit var finishActivityInterface : FinishActivityInterface
    private val mainViewModel: MainViewModel by viewModel()
    private val userViewModel : UserViewModel by viewModel()
    private var profileEmail : String? = null // Feed, follow, 댓글 등에서 프로필 클릭시 상대방의 프로필을 보여주기 위한 email
    private var targetNickname : String? = null
    private var targetProfileUri : String? = null
    private val profileModule : ProfileModule by lazy {
        ProfileModule(profileEmail, mainViewModel)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(context is FinishActivityInterface)
            finishActivityInterface = context
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            profileEmail = it.getString("profileEmail")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(profileEmail: String?) =
            TimeLineFragment().apply { arguments = Bundle().apply {
                putString("profileEmail", profileEmail)
            } }
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

        init()
        userInfoSet()

        childFragmentManager.beginTransaction().replace(
            R.id.container, FeedGridFragment.newInstance(
                null,
                "feed_timeline",
                profileEmail
            )
        ).commit()


    }

    private fun init() {
        localRepository = LocalRepository.getInstance(requireContext())!!

        if(profileEmail != FirebaseAuth.getInstance().currentUser?.email) {
            binding.infoModifyBtn.visibility = View.GONE
            binding.bookmarkBtn.visibility = View.GONE
            binding.header.findViewById<ImageButton>(R.id.add).visibility = View.GONE
            binding.followBtn.visibility = View.VISIBLE
            binding.messageBtn.visibility = View.VISIBLE
            binding.followBtn.setOnClickListener(this)
            binding.messageBtn.setOnClickListener(this)

        } else {
            binding.bookmarkBtn.setOnClickListener(this)
            binding.timelineProfileImageView.setOnClickListener(this)
            binding.header.findViewById<LinearLayout>(R.id.menu_set).visibility = View.VISIBLE
            binding.header.findViewById<TextView>(R.id.title).text = getString(R.string.timeline_title)
            binding.header.findViewById<ImageButton>(R.id.add).setOnClickListener(this)
            binding.header.findViewById<ImageButton>(R.id.setting).setOnClickListener(this)
            binding.infoModifyBtn.setOnClickListener(this)
        }

        binding.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
        binding.header.findViewById<ImageButton>(R.id.search).visibility = View.GONE
        binding.header.findViewById<ImageButton>(R.id.refresh).visibility = View.GONE
        binding.header.findViewById<ImageButton>(R.id.scroll_up).visibility = View.GONE
        binding.header.findViewById<ImageButton>(R.id.filter).visibility = View.GONE
        binding.followerBtn.setOnClickListener(this)
        binding.followingBtn.setOnClickListener(this)
        binding.nickNameEdit.isEnabled = false
    }

    private fun userInfoSet() {
        if(profileEmail == FirebaseAuth.getInstance().currentUser?.email.toString()) {
            localRepository.getUserInfo(lifecycleScope, object : LongTaskCallback<User> {
                override fun onResponse(result: Result<User>) {
                    if (result is Result.Success) {
                        profileModule.userInfoSet(
                            result.data,
                            binding.nickNameEdit,
                            binding.feedCount,
                            binding.followerBtn,
                            binding.followingBtn,
                            binding.timelineProfileImageView
                        )
                    } else if (result is Result.Error) {
                        Log.e("error", "timeline userInfoSet error : ${result.exception}")
                    }
                }
            })
        } else {
            mainViewModel.requestUserInfo(profileEmail!!, object : LongTaskCallback<User> {
                override fun onResponse(result: Result<User>) {
                    if (result is Result.Success) {
                        profileModule.userInfoSet(
                            result.data,
                            binding.nickNameEdit,
                            binding.feedCount,
                            binding.followerBtn,
                            binding.followingBtn,
                            binding.timelineProfileImageView
                        )

                        targetNickname = result.data.nickname
                        targetProfileUri = result.data.profileUri
                    } else if (result is Result.Error) {
                        Log.e("error", "timeline userInfoSet error : ${result.exception}")
                    }
                }
            })

            mainViewModel.checkFollow(
                profileEmail!!,
                object : LongTaskCallback<Boolean> {
                    override fun onResponse(result: Result<Boolean>) {
                        if (result is Result.Success) {
                            if (result.data == true) {
                                setFollowButton(true)
                            } else {
                                setFollowButton(false)
                            }
                        } else if (result is Result.Error) {
                            Log.e(
                                "error",
                                "timeline fragment userInfoSet error : ${result.exception}"
                            )
                        }
                    }
                })
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.add) {
            startActivity<MakeFeedActivity>()
        } else if (v?.id == R.id.info_modify_btn) {
            if (binding.infoModifyBtn.text == getString(R.string.info_complete)) {
                binding.nickNameEdit.isEnabled = false
                binding.infoModifyBtn.text = getString(R.string.info_modify)

                mainViewModel.requestUpdateNickname(binding.nickNameEdit.text.toString())
                localRepository.updateNickname(lifecycleScope, binding.nickNameEdit.text.toString())

            } else {
                binding.nickNameEdit.isEnabled = true
                binding.nickNameEdit.requestFocus()
                binding.nickNameEdit.setSelection(binding.nickNameEdit.text.length)
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
        } else if(v?.id == R.id.bookmark_btn) {
            startActivity<ShowFeedActivity>(
                "feedSort" to "feed_bookmark",
                "feedPosition" to 0
            )

        } else if(v?.id == R.id.follow_btn) {
            if(binding.followBtn.text == getString(R.string.follow_ing)) {
                localRepository.getUserInfo(lifecycleScope, object : LongTaskCallback<User> {
                    override fun onResponse(result: Result<User>) {
                        if (result is Result.Success) {
                            profileModule.requestUpdateFollow(
                                profileEmail!!, targetNickname!!, targetProfileUri!!,
                                false, result.data.nickname, result.data.profileUri
                            )

                            setFollowButton(false)

                        } else if (result is Result.Error) {
                            Log.e("error", "timeline follow_btn error : ${result.exception}")
                        }
                    }
                })


            } else if(binding.followBtn.text == getString(R.string.follow)){
                localRepository.getUserInfo(lifecycleScope, object : LongTaskCallback<User> {
                    override fun onResponse(result: Result<User>) {
                        if (result is Result.Success) {
                            profileModule.requestUpdateFollow(
                                profileEmail!!, targetNickname!!, targetProfileUri!!,
                                true, result.data.nickname, result.data.profileUri
                            )

                            setFollowButton(true)

                        } else if (result is Result.Error) {
                            Log.e("error", "timeline followBtn error : ${result.exception}")
                        }
                    }
                })
            }
        } else if(v?.id == R.id.message_btn) {
            startActivity<ChatActivity>(
                "targetEmail" to profileEmail,
                "targetProfileUri" to targetProfileUri,
                "targetNickname" to targetNickname,
                "targetRealEmail" to profileEmail
            )
        } else if(v?.id == R.id.setting) {
            makeAlertDialog()
        }
    }

    private fun makeAlertDialog(){

        val info = arrayOf<CharSequence>("라이센스", "로그아웃")
        val builder= AlertDialog.Builder(requireContext())

        builder.setItems(info) { dialog, which ->
            when (which) {
                0 -> {
                    EasyLicensesDialogCompat(requireContext())
                        .setTitle("Licenses")
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
                1 -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity<UserManageActivity>()
                    finishActivityInterface.finishCurrentActivity()
                }
            }
            dialog.dismiss()
        }
        builder.show()
    }



    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        CommonFunction.getInstance()!!.lockTouch(activity?.window!!)
        binding.progressBar.visibility = View.VISIBLE

        userViewModel.uploadProfileImage(uris[0], object : LongTaskCallback<Boolean> {
            override fun onResponse(result: Result<Boolean>) {
                if (result is Result.Success) {

                    userViewModel.loadProfileUri(
                        FirebaseAuth.getInstance().currentUser?.email.toString(),
                        object : LongTaskCallback<String> {
                            override fun onResponse(result: Result<String>) {
                                if (result is Result.Success) {
                                    val profileUri = result.data

                                    userViewModel.updateRemoteProfileUri(profileUri)
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
                                        "error",
                                        "timeline onImagesSelected error : ${result.exception}"
                                    )
                                }
                            }
                        })
                } else if (result is Result.Error)
                    Log.e("error", "timeline on ImagesSelected error : ${result.exception}")
            }
        })
    }

    private fun setFollowButton(flag: Boolean){
        if(flag) {
            context?.getColor(R.color.main_color)?.let { binding.followBtn.setBackgroundColor(it) }
            context?.getColor(R.color.white)?.let { binding.followBtn.setTextColor(it) }
            binding.followBtn.text = context?.getString(R.string.follow_ing)
        } else {
            context?.getColor(R.color.white)?.let { binding.followBtn.setBackgroundColor(it) }
            context?.getColor(R.color.black)?.let { binding.followBtn.setTextColor(it) }
            binding.followBtn.text = context?.getString(R.string.follow)
        }
    }
}

