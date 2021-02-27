package studio.seno.companion_animal.module

import android.net.Uri
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import studio.seno.companion_animal.R
import studio.seno.datamodule.LocalRepository
import studio.seno.datamodule.RemoteRepository
import studio.seno.datamodule.mapper.Mapper
import studio.seno.domain.model.User

class ProfileModule(email : String?) {
    private val mEmail = email

    fun userInfoSet(user : User, nickNameEdit: EditText, feedCount : TextView, followerBtn : TextView, followingBtn : TextView, profileImageView : CircleImageView){
        if(mEmail != null) {
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

    fun requestUpdateFollower(targetEmail : String, targetNickname: String, targetProfileUri : String, flag : Boolean, myNickName : String, myProfileUri : String) {
        val targetFollow = Mapper.getInstance()!!.mapperToFollow(targetEmail, targetNickname, targetProfileUri)
        val myFollow = Mapper.getInstance()!!.mapperToFollow(FirebaseAuth.getInstance().currentUser?.email.toString(), myNickName, myProfileUri)
        RemoteRepository.getInstance()!!.requestUpdateFollower(targetEmail, flag, myFollow, targetFollow)
    }
}