package studio.seno.companion_animal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import me.ibrahimsn.lib.OnItemSelectedListener
import studio.seno.companion_animal.databinding.ActivityMainBinding
import studio.seno.companion_animal.ui.main_ui.*
import studio.seno.domain.database.InfoManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        loadUserInfo()

        navigateView()
        supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment.newInstance()).commit()

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
                    0 -> supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment.newInstance()).commit()
                    1 -> supportFragmentManager.beginTransaction().replace(R.id.container, SearchFragment.newInstance()).commit()
                    2 -> supportFragmentManager.beginTransaction().replace(R.id.container, NotificationFragment.newInstance()).commit()
                    3 -> supportFragmentManager.beginTransaction().replace(R.id.container, ChatFragment.newInstance()).commit()
                    4 -> supportFragmentManager.beginTransaction().replace(R.id.container, TimeLineFragment.newInstance()).commit()
                }
                return true
            }
        }
    }


}