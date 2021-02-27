package studio.seno.companion_animal.ui.follow

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentNotificationBinding
import studio.seno.companion_animal.ui.feed.ShowFeedActivity
import studio.seno.datamodule.LocalRepository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Follow
import studio.seno.domain.model.User

class FollowActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: FragmentNotificationBinding
    private var category : String? = null
    private val followListViewModel: FollowListViewModel by viewModels()
    private lateinit var followAdapter: FollowAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.fragment_notification)
        binding.lifecycleOwner = this
        binding.followListModel = followListViewModel
        init()
        followItemEvent()

        if (category == "follower") {
            followListViewModel.requestLoadFollower()
        } else if (category == "following") {
            followListViewModel.requestLoadFollowing()
        }

        observe()

    }

    fun init() {
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

    fun followItemEvent() {
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

    fun updateFollow(follow : Follow, isAdd : Boolean, isDeleted : Boolean){
        LocalRepository.getInstance(applicationContext)!!.getUserInfo(lifecycleScope, object : LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if(result is Result.Success) {
                    followListViewModel.requestUpdateFollower(follow, isAdd, result.data.nickname, result.data.profileUri, isDeleted)
                    LocalRepository.getInstance(applicationContext)!!.updateFollowing(lifecycleScope, isAdd)

                } else if(result is Result.Error) {
                    Log.e("error", "Homefragment follow error : ${result.exception}")
                }
            }
        })
    }

    fun observe() {
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