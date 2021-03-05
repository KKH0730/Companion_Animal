package studio.seno.companion_animal.ui.follow

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentNotificationBinding
import studio.seno.companion_animal.ui.feed.ShowFeedActivity
import studio.seno.datamodule.repository.local.LocalRepository
import studio.seno.domain.model.Follow
import studio.seno.domain.model.User
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

class FollowActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: FragmentNotificationBinding
    private var category : String? = null
    private val followListViewModel: FollowListViewModel by viewModel()
    private lateinit var followAdapter: FollowAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_notification)
        binding.lifecycleOwner = this
        binding.followListModel = followListViewModel
        init()
        followItemEvent()

        if (category == "follower") {
            followListViewModel.requestLoadFollower("follower")
        } else if (category == "following") {
            followListViewModel.requestLoadFollower("following")
        }

        observe()

    }

    private fun init() {
        category = intent.getStringExtra("category")
        if (category != null)
            followAdapter = FollowAdapter(category!!)
        binding.notiRecyclerView.adapter = followAdapter

        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        if (category == "follower")
            binding.header.findViewById<TextView>(R.id.title2).text = getString(R.string.follower)
        else
            binding.header.findViewById<TextView>(R.id.title2).text = getString(R.string.following)
    }

    private fun followItemEvent() {
        followAdapter.setOnFollowClickListener(object : OnFollowClickListener {
            override fun onProfileClicked(layout: ConstraintLayout, follow: Follow) {
                startActivity<ShowFeedActivity>(
                    "profileEmail" to follow.email,
                    "feedSort" to "profile"
                )
            }

            override fun onButtonClicked(button: Button, category: String, follow: Follow) {
                if(category == "follower"){
                    if(button.text == getString(R.string.follow_each_other)) {
                        button.text = getString(R.string.follow_ing)
                        button.setBackgroundColor(getColor(R.color.main_color))
                        button.setTextColor(getColor(R.color.white))
                        updateFollow(follow, true, false)
                    } else {
                        button.text = getString(R.string.follow_each_other)
                        button.setBackgroundColor(getColor(R.color.white))
                        button.setTextColor(getColor(R.color.black))
                        updateFollow(follow, false, false)
                    }
                } else if(category == "following") {
                    updateFollow(follow, false, true)
                }
            }
        })
    }

    private fun updateFollow(follow : Follow, isAdd : Boolean, isDeleted : Boolean){
        LocalRepository.getInstance(this)!!.getUserInfo(lifecycleScope, object :
            LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if(result is Result.Success) {
                    followListViewModel.requestUpdateFollow(follow, isAdd, result.data.nickname, result.data.profileUri, isDeleted)
                    LocalRepository.getInstance(this@FollowActivity)!!.updateFollowing(lifecycleScope, isAdd)

                } else if(result is Result.Error)
                    Log.e("error", "FollowActivity follow error : ${result.exception}")
            }
        })
    }

    private fun observe() {
        followListViewModel.getFollowListLiveData().observe(this, {
            followAdapter.submitList(it)
        })
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.back_btn){
            finish()
        }
    }


}