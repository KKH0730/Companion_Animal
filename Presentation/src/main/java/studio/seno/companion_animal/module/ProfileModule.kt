package studio.seno.companion_animal.module

import android.net.Uri
import android.widget.EditText
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import de.hdodenhof.circleimageview.CircleImageView
import studio.seno.companion_animal.R
import studio.seno.companion_animal.ui.main_ui.MainViewModel
import studio.seno.domain.model.User

class ProfileModule(
    private val email : String?,
    private val mainViewModel: MainViewModel
) {


    fun userInfoSet(user : User, nickNameEdit: EditText, feedCount : TextView, followerBtn : TextView, followingBtn : TextView, profileImageView : CircleImageView){
        if(email != null) {
            nickNameEdit.setText(user.nickname)
            feedCount.text = String.format(feedCount.context.getString(R.string.timeLine_menu1), user.feedCount)
            followerBtn.text = String.format(followerBtn.context.getString(R.string.timeLine_menu2), user.follower)
            followingBtn.text = String.format(followingBtn.context.getString(R.string.timeLine_menu3), user.following)

            Glide.with(nickNameEdit.context)
                .load(Uri.parse(user.profileUri))
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(profileImageView)
        } else {

        }
    }

    fun requestUpdateFollow(targetEmail : String, targetNickname: String, targetProfileUri : String, flag : Boolean, myNickName : String, myProfileUri : String) {
        mainViewModel.requestUpdateFollow(targetEmail, targetNickname, targetProfileUri, flag, myNickName, myProfileUri)
    }
}