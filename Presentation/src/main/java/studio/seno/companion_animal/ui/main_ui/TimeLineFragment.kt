package studio.seno.companion_animal.ui.main_ui

import android.graphics.drawable.Drawable
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentTimeLineBinding
import studio.seno.companion_animal.ui.MenuDialog
import studio.seno.datamodule.LocalRepository
import studio.seno.datamodule.Repository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.User
import studio.seno.domain.usecase.remote.UploadUseCase
import studio.seno.domain.util.PrefereceManager


class TimeLineFragment : Fragment(), View.OnClickListener, BottomSheetImagePicker.OnImagesSelectedListener {
    private lateinit var binding : FragmentTimeLineBinding
    private val mainViewModel : MainViewModel by viewModels()
    private val localRepository : LocalRepository by lazy{ LocalRepository(requireContext()) }


    companion object {
        @JvmStatic
        fun newInstance() =
            TimeLineFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_time_line,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        userInfoSet()
    }

    fun init(){
        binding.header.findViewById<ImageButton>(R.id.back_btn).visibility = View.GONE
        binding.header.findViewById<TextView>(R.id.title).text = getString(R.string.timeline_title)
        binding.header.findViewById<LinearLayout>(R.id.menu_set).visibility = View.VISIBLE
        binding.header.findViewById<ImageButton>(R.id.search).visibility = View.GONE
        binding.header.findViewById<ImageButton>(R.id.refresh).visibility = View.GONE

        binding.header.findViewById<ImageButton>(R.id.add).setOnClickListener(this)
        binding.timelineProfileImageView.setOnClickListener(this)
        binding.infoModifyBtn.setOnClickListener(this)
        binding.nickNameEdit.setText(PrefereceManager.getString(requireContext(), "nickName"))
        binding.nickNameEdit.isEnabled = false

        LocalRepository(requireContext()).getUserInfo(lifecycleScope, object : LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if(result is Result.Success){
                    Glide.with(requireContext())
                        .load(Uri.parse(result.data.profileUri))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.timelineProfileImageView)
                } else if(result is Result.Error) {
                    Log.e("error", "timeline profile imageLoad Fail : ${result.exception}")
                }
            }
        })
    }

    fun userInfoSet(){
        localRepository.getUserInfo(lifecycleScope, object : LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if (result is Result.Success) {
                    binding.nickNameEdit.setText(result.data.nickname)
                    binding.followerCount.text = result.data.follower.toString()
                    binding.followingCount.text = result.data.following.toString()
                    binding.feedCount.text = result.data.feedCount.toString()
                } else if (result is Result.Error) {
                    Log.e("error", "timeline userInfoSet error : ${result.exception}")
                }
            }
        })
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.add) {
            var menuDialog = MenuDialog.newInstance("null", false)
            menuDialog.show(parentFragmentManager, "write")
        } else if(v?.id == R.id.info_modify_btn) {
            if(binding.infoModifyBtn.text == getString(R.string.info_complete)) {
                binding.nickNameEdit.isEnabled = false
                binding.infoModifyBtn.text = getString(R.string.info_modify)

                localRepository.updateNickname(lifecycleScope, binding.nickNameEdit.text.toString())
                mainViewModel.requestUpdateNickname(binding.nickNameEdit.text.toString())
            } else {
                binding.nickNameEdit.isEnabled = true
                binding.nickNameEdit.requestFocus()
                binding.infoModifyBtn.text = getString(R.string.info_complete)
            }

        } else if(v?.id == R.id.timeline_profileImageView) {
            BottomSheetImagePicker.Builder(getString(R.string.file_provider))
                .cameraButton(ButtonType.Button)
                .galleryButton(ButtonType.Button)
                .singleSelectTitle(R.string.pick_single)
                .peekHeight(R.dimen.peekHeight)
                .columnSize(R.dimen.columnSize)
                .requestTag("single")
                .show(childFragmentManager, null)
        }
    }

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        Glide.with(requireContext())
            .load(uris[0])
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(binding.timelineProfileImageView)

        Repository().uploadInItProfileImage(uris[0], object : LongTaskCallback<Boolean>{
            override fun onResponse(result: Result<Boolean>) {
                if(result is Result.Success) {

                    Repository().loadRemoteProfileImage(object : LongTaskCallback<String>{
                        override fun onResponse(result: Result<String>) {
                            if(result is Result.Success) {
                                val profileUri = result.data

                                Repository().updateRemoteProfileUri(profileUri)
                                if(context != null)
                                    LocalRepository(context!!).updateProfileUri(lifecycleScope, profileUri)

                            } else if(result is Result.Error){
                                Log.e("eror", "timeline onImagesSelected error : ${result.exception}")
                            }
                        }
                    })


                } else if(result is Result.Error)
                    Log.e("eror", "timeline onImagesSelected error : ${result.exception}")
            }

        })
    }
}