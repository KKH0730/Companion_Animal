package studio.seno.commonmodule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

open class BaseActivity : AppCompatActivity() {
    private var backKeyPressedTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 3000) {
            backKeyPressedTime = System.currentTimeMillis()

            CustomToast(this, getString(R.string.toast1)).show()
        } else {
            finish()
        }
    }
}