package studio.seno.commonmodule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        this.window?.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        supportActionBar?.hide()

         */
    }
}