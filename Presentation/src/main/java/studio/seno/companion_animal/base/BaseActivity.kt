package studio.seno.companion_animal.base

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import studio.seno.companion_animal.R


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