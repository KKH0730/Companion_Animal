package studio.seno.companion_animal.ui.user_manage

import android.os.Bundle
import studio.seno.commonmodule.BaseActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.util.ViewControlListener

class UserManageActivity : BaseActivity(), ViewControlListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_manage)
    }

    override fun finishCurrentActivity() {
        finish()
    }
}