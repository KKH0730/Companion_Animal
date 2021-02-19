package studio.seno.companion_animal.ui.follow

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentNotificationBinding
import studio.seno.domain.model.Follow

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

        binding.header.findViewById<TextView>(R.id.title).visibility = View.GONE
        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
    }

    fun followItemEvent() {
        followAdapter.setOnFollowClickListener(object : OnFollowClickListener {
            override fun onProfileClicked(follow: Follow) {

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