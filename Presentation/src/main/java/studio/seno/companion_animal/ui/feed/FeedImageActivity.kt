package studio.seno.companion_animal.ui.feed

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivityFeedImageBinding
import studio.seno.companion_animal.ui.main_ui.PagerAdapter
import studio.seno.domain.model.Feed

class FeedImageActivity : AppCompatActivity() {
    private var binding : ActivityFeedImageBinding? = null
    private lateinit var feed : Feed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_feed_image)

        feed = intent.getParcelableExtra("feed")

        var pagerAdapter = PagerAdapter(supportFragmentManager, lifecycle)
        for (element in feed.getRemoteUri()) {
            pagerAdapter.addItem(FeedPagerFragment.newInstance(element, "FeedImageActivity"))
            binding!!.viewpager.adapter = pagerAdapter
            binding!!.indicator.setViewPager(binding!!.viewpager)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        binding = null
    }
}