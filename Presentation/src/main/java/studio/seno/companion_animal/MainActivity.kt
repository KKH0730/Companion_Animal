package studio.seno.companion_animal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import me.ibrahimsn.lib.OnItemSelectedListener
import studio.seno.companion_animal.databinding.ActivityMainBinding
import studio.seno.companion_animal.ui.main_ui.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        navigateView()
        supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment.newInstance()).commit()


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