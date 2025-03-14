package studio.seno.companion_animal.ui.user_manage

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import studio.seno.companion_animal.base.BaseActivity
import studio.seno.companion_animal.R
import studio.seno.companion_animal.util.FinishActivityInterface

@AndroidEntryPoint
class UserManageActivity : BaseActivity(), FinishActivityInterface {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_manage)
    }

    override fun finishCurrentActivity() {
        finish()
    }
}