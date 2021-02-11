package studio.seno.companion_animal

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import me.ibrahimsn.lib.OnItemSelectedListener
import studio.seno.companion_animal.databinding.ActivityMainBinding
import studio.seno.companion_animal.ui.feed.FeedListViewModel
import studio.seno.companion_animal.ui.main_ui.*
import studio.seno.domain.database.InfoManager

class MainActivity : AppCompatActivity() , DialogInterface.OnDismissListener{
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel : MainViewModel by viewModels()
    private lateinit var homeFragment: HomeFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        homeFragment = HomeFragment.newInstance()
        loadUserInfo()

        navigateView()
        supportFragmentManager.beginTransaction().replace(R.id.container, homeFragment).commit()

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
        }
    }

    private fun loadUserInfo(){
        if(InfoManager.getString(this, "email") == "isEmpty") {
            mainViewModel.requestUserData(FirebaseAuth.getInstance().currentUser?.email.toString())
            mainViewModel.getUserLiveData().observe(this, {
                InfoManager.setUserInfo(this, it.email, it.nickname, it.follower, it.following, it.feedCount)
            })
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
        if (InfoManager.getString(applicationContext, "mode") == "feed_modify") {
            homeFragment.onDismissed("feed_modify")
        } else if (InfoManager.getString(applicationContext, "mode") == "feed_delete") {
            homeFragment.onDismissed("feed_delete")
        } else if(InfoManager.getString(applicationContext, "mode") == "follow") {
            homeFragment.onDismissed("follow")
        } else if(InfoManager.getString(applicationContext, "mode") == "unfollow") {
            homeFragment.onDismissed("unfollow")
        }
    }
}