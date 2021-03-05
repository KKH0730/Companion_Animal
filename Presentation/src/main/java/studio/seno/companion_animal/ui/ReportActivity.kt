package studio.seno.companion_animal.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import studio.seno.commonmodule.CustomToast
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivityReportBinding
import studio.seno.datamodule.repository.remote.ReportRepositoryImpl
import studio.seno.domain.model.Feed

/**
 * 1. NotificationFragment 에서 알림 클릭시 피드가 삭제된 경우
 * 2. Feed 메뉴에서 신고하기 선택된 경우
 */

class ReportActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityReportBinding
    private var feed : Feed? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_report)

        init()
    }

    fun init(){
        binding.header.findViewById<TextView>(R.id.title).visibility = View.GONE
        binding.header.findViewById<TextView>(R.id.title).setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        if(intent.getParcelableExtra<Feed>("feed") != null)
            feed = intent.getParcelableExtra<Feed>("feed")

        if(feed == null)
            binding.noFeedLayout.visibility = View.VISIBLE
        else {
            binding.reportLayout1.setOnClickListener(this)
            binding.reportLayout2.setOnClickListener(this)
            binding.reportLayout3.setOnClickListener(this)
            binding.reportLayout.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.back_btn) {
            finish()
        } else if(v?.id == R.id.report_layout1) {
            feed?.let { ReportRepositoryImpl().reportFeed(it, 1) }
            CustomToast(applicationContext, getString(R.string.report_toast)).show()
            finish()
        } else if(v?.id == R.id.report_layout2) {
            feed?.let { ReportRepositoryImpl().reportFeed(it, 2) }
            CustomToast(applicationContext, getString(R.string.report_toast)).show()
            finish()
        } else if(v?.id == R.id.report_layout3) {
            feed?.let { ReportRepositoryImpl().reportFeed(it, 3) }
            CustomToast(applicationContext, getString(R.string.report_toast)).show()
            finish()
        }
    }
}