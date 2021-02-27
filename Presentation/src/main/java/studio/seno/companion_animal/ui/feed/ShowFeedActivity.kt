package studio.seno.companion_animal.ui.feed

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivityShowFeedBinding
import studio.seno.companion_animal.ui.main_ui.HomeFragment
import studio.seno.companion_animal.ui.main_ui.TimeLineFragment
import studio.seno.domain.util.PreferenceManager

class ShowFeedActivity : AppCompatActivity(), View.OnClickListener, DialogInterface.OnDismissListener {
    private lateinit var binding : ActivityShowFeedBinding
    private var homeFragment : HomeFragment? = null
    private var timeLineFragment : TimeLineFragment? = null
    private var feedGridFragment : FeedGridFragment? = null
    private var feedSort : String? = null
    private var feedPosition : Int? = null
    private var profile : String? = null
    private var profileEmail : String? = null
    private var timeLineEmail : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_feed)

        init()
        move()
    }

    fun init(){
        feedSort = intent.getStringExtra("feedSort")
        feedPosition = intent.getIntExtra("feedPosition", 0)
        profile = intent.getStringExtra("profile")
        profileEmail = intent.getStringExtra("profileEmail")
        timeLineEmail = intent.getStringExtra("timeLineEmail")


        if(feedSort == "feed_timeline")
            binding.header.findViewById<TextView>(R.id.title).visibility = View.GONE
        else if(feedSort == "feed_bookmark")
            binding.header.findViewById<TextView>(R.id.title).visibility = View.GONE
        else if(feedSort == "profile")
            binding.header.visibility = View.GONE

        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
    }

    fun move(){
        if(feedSort == "feed_timeline") {
            homeFragment = HomeFragment.newInstance(feedSort!!, feedPosition!!, timeLineEmail!!)
            supportFragmentManager.beginTransaction().replace(R.id.container, homeFragment!!).commit()
        } else if(feedSort == "feed_bookmark") {
            feedGridFragment = FeedGridFragment.newInstance(null, "feed_bookmark", null)
            supportFragmentManager.beginTransaction().replace(R.id.container, feedGridFragment!!).commit()
        } else if(feedSort == "profile") {
            timeLineFragment = TimeLineFragment.newInstance(profileEmail)
            supportFragmentManager.beginTransaction().replace(R.id.container, timeLineFragment!!).commit()
        }
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.back_btn)
            finish()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        if (PreferenceManager.getString(applicationContext, "mode") == "feed_modify") {
            homeFragment?.onDismissed("feed_modify")
        } else if (PreferenceManager.getString(applicationContext, "mode") == "feed_delete") {
            homeFragment?.onDismissed("feed_delete")
        } else if(PreferenceManager.getString(applicationContext, "mode") == "follow") {
            homeFragment?.onDismissed("follow")
        } else if(PreferenceManager.getString(applicationContext, "mode") == "unfollow") {
            homeFragment?.onDismissed("unfollow")
        }
    }
}