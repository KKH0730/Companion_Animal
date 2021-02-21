package studio.seno.companion_animal.ui.feed

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivityShowFeedBinding
import studio.seno.companion_animal.ui.main_ui.HomeFragment
import studio.seno.domain.model.Feed

class ShowFeedActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityShowFeedBinding
    private var homeFragment : HomeFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_feed)

        init()
        move()
    }

    fun init(){
        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        binding.header.findViewById<TextView>(R.id.title).text = getString(R.string.feed_title)
    }

    fun move(){
        val sort = intent.getStringExtra("sort")
        val position = intent.getIntExtra("position", 0)

        if(sort != null) {
            homeFragment = HomeFragment.newInstance("my_feed_list", position)
            supportFragmentManager.beginTransaction().replace(R.id.container, homeFragment!!).commit()
        } else
            finish()
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.back_btn)
            finish()
    }
}