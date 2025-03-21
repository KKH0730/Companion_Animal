package studio.seno.companion_animal

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import me.ibrahimsn.lib.OnItemSelectedListener
import studio.seno.companion_animal.base.BaseActivity
import studio.seno.companion_animal.databinding.ActivityMainBinding
import studio.seno.companion_animal.extension.startActivity
import studio.seno.companion_animal.ui.ReportActivity
import studio.seno.companion_animal.ui.chat.ChatActivity
import studio.seno.companion_animal.ui.feed.FeedDetailActivity
import studio.seno.companion_animal.ui.main_ui.*
import studio.seno.companion_animal.util.FinishActivityInterface
import studio.seno.datamodule.database.AppDatabase
import studio.seno.datamodule.repository.local.LocalRepository
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.PreferenceManager
import studio.seno.domain.util.Result

@AndroidEntryPoint
class MainActivity : BaseActivity() , DialogInterface.OnDismissListener, FinishActivityInterface {
    private val mainViewModel : MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var homeFragment: HomeFragment
    private lateinit var notificationFragment: NotificationFragment
    private lateinit var chatFragment: ChatFragment
    private lateinit var timeLineFragment: TimeLineFragment
    private lateinit var db :AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        init()
        loadUserInfo()
        navigateView()
        notificationClicked()
        kakaoSharedClicked()
        supportFragmentManager.beginTransaction().replace(R.id.container, homeFragment).commit()
    }

    fun init(){
        db = AppDatabase.getInstance(this)!!
        homeFragment = HomeFragment.newInstance("feed_list", 0, FirebaseAuth.getInstance().currentUser?.email.toString())
        notificationFragment = NotificationFragment.newInstance()
        chatFragment = ChatFragment.newInstance()
        timeLineFragment = TimeLineFragment.newInstance(FirebaseAuth.getInstance().currentUser?.email.toString(), true)
    }

    //앱이 실행되면 회원정보를 Remote DB에서 Local DB에 저장
    private fun loadUserInfo(){
            mainViewModel.requestUserInfo(FirebaseAuth.getInstance().currentUser?.email.toString(), object:
                LongTaskCallback<Any> {
                override fun onResponse(result: Result<Any>) {
                    if(result is Result.Success){
                        val user = result.data as User

                        LocalRepository(applicationContext).getUserInfo(lifecycleScope, object :
                            LongTaskCallback<User> {
                            override fun onResponse(result: Result<User>) {
                                if(result is Result.Success){
                                    if(result.data == null)
                                        LocalRepository(applicationContext).insertUserInfo(lifecycleScope, user)

                                    else
                                        LocalRepository(applicationContext).updateUserInfo(lifecycleScope, user, null)
                                } else if(result is Result.Error) {
                                    Log.e("error", "timeline userInfoSet error : ${result.exception}")
                                }
                            }
                        })
                    }
                }
            })

        FirebaseMessaging.getInstance().token.addOnSuccessListener { mainViewModel.requestUpdateToken(it) }
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

    //notification 클릭시 DetailFeedActivity 혹은 ChatActivity 이동
    private fun notificationClicked(){
        if(intent.getStringExtra("from") != null){
            if(intent.getStringExtra("from") == "notification") {
                mainViewModel.getFeed(intent.getStringExtra("target_path")!!, object :
                    LongTaskCallback<Any> {
                    override fun onResponse(result: Result<Any>) {
                        if(result is Result.Success){
                            if(result.data != null)
                                startActivity(FeedDetailActivity::class.java) {
                                    putExtra("feed", result.data as Feed)
                                }
                            else
                                startActivity(ReportActivity::class.java)
                        } else if(result is Result.Error) {
                            Log.e("error", "MainActivity notification intent error: ${result.exception}")
                        }
                    }
                })
            } else if(intent.getStringExtra("from") == "chat") {
                startActivity(ChatActivity::class.java) {
                    putExtra("targetEmail", intent.getStringExtra("targetRealEmail"))
                    putExtra("targetProfileUri", intent.getStringExtra("targetEmail"))
                    putExtra("targetNickname", intent.getStringExtra("targetNickname"))
                    putExtra("targetRealEmail", intent.getStringExtra("targetProfileUri"))
                }
            }
        }
    }

    //kakao 공유하기 클릭시 FeedDetailActivity 이동
    private fun kakaoSharedClicked(){
        if(intent.action == Intent.ACTION_VIEW) {
            val path = intent.data?.getQueryParameter("path")

            if (path != null) {
                mainViewModel.getFeed(path, object : LongTaskCallback<Any> {
                    override fun onResponse(result: Result<Any>) {
                        if(result is Result.Success){
                            if(result.data != null)
                                startActivity(FeedDetailActivity::class.java) {
                                    putExtra("feed", result.data as Feed)
                                }
                            else
                                startActivity(ReportActivity::class.java)
                        }else if(result is Result.Error) {
                            Log.e("error", "MainActivity kakao share error: ${result.exception}")
                        }
                    }
                })
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
        } else if(PreferenceManager.getString(applicationContext, "mode") == "report") {
            homeFragment.onDismissed("report")
        }
    }

    override fun finishCurrentActivity() {
        finish()
    }
    
}