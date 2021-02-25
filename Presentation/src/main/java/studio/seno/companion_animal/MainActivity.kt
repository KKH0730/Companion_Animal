package studio.seno.companion_animal

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import me.ibrahimsn.lib.OnItemSelectedListener
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import studio.seno.commonmodule.BaseActivity
import studio.seno.companion_animal.databinding.ActivityMainBinding
import studio.seno.companion_animal.ui.chat.ChatActivity
import studio.seno.companion_animal.ui.feed.FeedDetailActivity
import studio.seno.companion_animal.ui.main_ui.*
import studio.seno.datamodule.LocalRepository
import studio.seno.datamodule.RemoteRepository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.database.AppDatabase
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import studio.seno.domain.util.PreferenceManager

class MainActivity : BaseActivity() , DialogInterface.OnDismissListener{
    private lateinit var binding: ActivityMainBinding
    private lateinit var db :AppDatabase
    private val mainViewModel : MainViewModel by viewModels()
    private lateinit var homeFragment: HomeFragment
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
        pendingIntent()
    }

    fun init(){
        db = AppDatabase.getInstance(this)!!
        homeFragment = HomeFragment.newInstance("feed_list", 0, FirebaseAuth.getInstance().currentUser?.email.toString())
        notificationFragment = NotificationFragment.newInstance()
        chatFragment = ChatFragment.newInstance()
        timeLineFragment = TimeLineFragment.newInstance(FirebaseAuth.getInstance().currentUser?.email.toString())
    }

    private fun loadUserInfo(){
            mainViewModel.requestUserData(FirebaseAuth.getInstance().currentUser?.email.toString(), object: LongTaskCallback<User>{
                override fun onResponse(result: Result<User>) {
                    if(result is Result.Success){
                        val user = result.data

                        LocalRepository(applicationContext).getUserInfo(lifecycleScope, object : LongTaskCallback<User>{
                            override fun onResponse(result: Result<User>) {
                                if(result is Result.Success){
                                    if(result.data == null)
                                        LocalRepository(applicationContext).InsertUserInfo(lifecycleScope, user)
                                    else
                                        LocalRepository(applicationContext).updateUserInfo(lifecycleScope, user)

                                } else if(result is Result.Error) {
                                    Log.e("error", "timeline userInfoSet error : ${result.exception}")
                                }
                            }
                        })
                    }
                }
            })

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            mainViewModel.requestUpdateToken(it.token)
        }

    }

    private fun navigateView(){
        binding.bottomBar.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelect(pos: Int): Boolean {
                when(pos) {
                    0 -> supportFragmentManager.beginTransaction().replace(R.id.container, homeFragment).commit()
                    1 -> supportFragmentManager.beginTransaction().replace(R.id.container, notificationFragment).commit()
                    2 -> supportFragmentManager.beginTransaction().replace(R.id.container, chatFragment).commit()
                    3 -> supportFragmentManager.beginTransaction().replace(R.id.container, timeLineFragment).commit()
                }
                return true
            }
        }
    }

    private fun pendingIntent(){
        if(intent.getStringExtra("from") != null){
            Log.d("hi", "from not null")
            if(intent.getStringExtra("from") == "notification") {
                Log.d("hi", "notification")
                RemoteRepository.getInstance()!!.loadFeed(intent.getStringExtra("target_path")!!, object : LongTaskCallback<Feed>{
                    override fun onResponse(result: Result<Feed>) {
                        if(result is Result.Success){
                            if(result.data != null)
                                startActivity<FeedDetailActivity>("feed" to result.data)
                            else
                                startActivity<ErrorActivity>()
                        } else if(result is Result.Error) {
                            Log.e("error", "MainActivity notification intent error: ${result.exception}")
                        }
                    }
                })
            } else if(intent.getStringExtra("from") == "chat") {
                Log.d("hi", "chat")
                startActivity<ChatActivity>(
                    "targetEmail" to intent.getStringExtra("targetRealEmail"),
                    "targetProfileUri" to intent.getStringExtra("targetEmail"),
                    "targetNickname" to intent.getStringExtra("targetNickname"),
                    "targetRealEmail" to intent.getStringExtra("targetProfileUri"),
                )
            }
        }

    }

    override fun onDismiss(dialog: DialogInterface?) {
        if (PreferenceManager.getString(applicationContext, "mode") == "feed_modify") {
            homeFragment.onDismissed("feed_modify")
        } else if (PreferenceManager.getString(applicationContext, "mode") == "feed_delete") {
            homeFragment.onDismissed("feed_delete")
        } else if(PreferenceManager.getString(applicationContext, "mode") == "follow") {
            homeFragment.onDismissed("follow")
        } else if(PreferenceManager.getString(applicationContext, "mode") == "unfollow") {
            homeFragment.onDismissed("unfollow")
        }
    }


}