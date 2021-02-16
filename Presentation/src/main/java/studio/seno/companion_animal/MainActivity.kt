package studio.seno.companion_animal

import android.annotation.SuppressLint
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import me.ibrahimsn.lib.OnItemSelectedListener
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.companion_animal.databinding.ActivityMainBinding
import studio.seno.companion_animal.ui.feed.FeedDetailActivity
import studio.seno.companion_animal.ui.main_ui.*
import studio.seno.datamodule.Repository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import studio.seno.domain.util.PrefereceManager

class MainActivity : AppCompatActivity() , DialogInterface.OnDismissListener{
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel : MainViewModel by viewModels()
    private lateinit var homeFragment: HomeFragment
    private lateinit var searchFragment: SearchFragment
    private lateinit var notificationFragment: NotificationFragment
    private lateinit var chatFragment: ChatFragment
    private lateinit var timeLineFragment: TimeLineFragment




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        init()

        loadUserInfo()
        navigateView()

        supportFragmentManager.beginTransaction().replace(R.id.container, homeFragment).commit()

        if(intent.getStringExtra("from") != null && intent.getStringExtra("from") == "notification") {
            Repository().loadFeed(intent.getStringExtra("target_path"), object : LongTaskCallback<Feed>{
                override fun onResponse(result: Result<Feed>) {
                    if(result is Result.Success){
                        startActivity<FeedDetailActivity>("feed" to result.data)
                    } else if(result is Result.Error) {
                        Log.e("error", "MainActivity notification intent error: ${result.exception}")
                    }
                }
            })
        }
    }

    fun init(){
        homeFragment = HomeFragment.newInstance()
        searchFragment = SearchFragment.newInstance()
        notificationFragment = NotificationFragment.newInstance()
        chatFragment = ChatFragment.newInstance()
        timeLineFragment = TimeLineFragment.newInstance()
    }

    private fun loadUserInfo(){
        if(PrefereceManager.getString(this, "email") == "isEmpty") {
            mainViewModel.requestUserData(FirebaseAuth.getInstance().currentUser?.email.toString(), object: LongTaskCallback<User>{
                override fun onResponse(result: Result<User>) {
                    if(result is Result.Success){
                        val user = result.data
                        PrefereceManager.setUserInfo(applicationContext, user.email, user.nickname, user.follower,
                            user.following, user.feedCount, user.token)
                    }
                }
            })
        }
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            mainViewModel.requestUpdateToken(
                it.token,
                FirebaseAuth.getInstance().currentUser?.email.toString(),
            )
        }

    }

    private fun navigateView(){
        binding.bottomBar.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelect(pos: Int): Boolean {
                when(pos) {
                    0 -> supportFragmentManager.beginTransaction().replace(R.id.container, homeFragment).commit()
                    1 -> supportFragmentManager.beginTransaction().replace(R.id.container, SearchFragment.newInstance()).commit()
                    2 -> supportFragmentManager.beginTransaction().replace(R.id.container, NotificationFragment.newInstance()).commit()
                    3 -> supportFragmentManager.beginTransaction().replace(R.id.container, ChatFragment.newInstance()).commit()
                    4 -> supportFragmentManager.beginTransaction().replace(R.id.container, TimeLineFragment.newInstance()).commit()
                }
                return true
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        if (PrefereceManager.getString(applicationContext, "mode") == "feed_modify") {
            homeFragment.onDismissed("feed_modify")
        } else if (PrefereceManager.getString(applicationContext, "mode") == "feed_delete") {
            homeFragment.onDismissed("feed_delete")
        } else if(PrefereceManager.getString(applicationContext, "mode") == "follow") {
            homeFragment.onDismissed("follow")
        } else if(PrefereceManager.getString(applicationContext, "mode") == "unfollow") {
            homeFragment.onDismissed("unfollow")
        }
    }
}