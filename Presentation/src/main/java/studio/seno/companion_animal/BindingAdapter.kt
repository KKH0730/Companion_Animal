package studio.seno.companion_animal

import android.graphics.Point
import android.net.Uri
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import com.aqoong.lib.expandabletextview.ExpandableTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.makeramen.roundedimageview.RoundedImageView
import com.pchmn.materialchips.ChipView
import de.hdodenhof.circleimageview.CircleImageView
import me.relex.circleindicator.CircleIndicator3
import org.jetbrains.anko.windowManager
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.module.TextModule
import studio.seno.companion_animal.ui.feed.FeedPagerFragment
import studio.seno.companion_animal.ui.main_ui.PagerAdapter
import studio.seno.datamodule.repository.remote.FollowRepositoryImpl
import studio.seno.datamodule.repository.remote.UploadRepositoryImpl
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result

object BindingAdapter {

    /**
     * Feed
     */
    @BindingAdapter("setProfileImage")
    @JvmStatic
    fun setProfileImage(imageView: CircleImageView, profileUri: String?) {
        try {
            if (profileUri != null) {
                Glide.with(imageView.context)
                    .load(Uri.parse(profileUri))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(imageView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @BindingAdapter("setText")
    @JvmStatic
    fun setText(view: TextView, text: String?) {
        try {
            if (text != null) {
                view.text = text
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @BindingAdapter("setExpandableText")
    @JvmStatic
    fun setExpandableText(view: ExpandableTextView, content: String?) {
        try {
            if (content != null) {
                val context = view.context
                view.setText(content, context.getString(R.string.more))
                view.state = ExpandableTextView.STATE.COLLAPSE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @BindingAdapter("setLong")
    @JvmStatic
    fun setLong(view: TextView, count: Long) {
        try {
            view.text = count.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @BindingAdapter("setHeartButton")
    @JvmStatic
    fun setHeartButton(imageButton: ImageButton, map: Map<String, String>) {
        imageButton.isSelected =
            map[FirebaseAuth.getInstance().currentUser.email.toString()] != null
    }

    @BindingAdapter("setBookmarkButton")
    @JvmStatic
    fun setBookmarkButton(imageButton: ImageButton, map: Map<String, String>) {
        imageButton.isSelected =
            map[FirebaseAuth.getInstance().currentUser.email.toString()] != null
    }


    @BindingAdapter("setHashTag")
    @JvmStatic
    fun setHashTag(linearLayout: LinearLayout, hashTags: List<String>?) {
        try {
            if (hashTags != null) {
                linearLayout.removeAllViews()

                for (element in hashTags) {
                    var chipView = ChipView(linearLayout.context)

                    chipView.setChipBackgroundColor(linearLayout.context.getColor(R.color.main_color))
                    chipView.setLabelColor(linearLayout.context.getColor(R.color.white))
                    chipView.setPadding(20, 0, 0, 0)
                    chipView.label = element
                    linearLayout.addView(chipView)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @BindingAdapter("setFeedImages", "getLifecycle", "getFm", "getIndicator")
    @JvmStatic
    fun setFeedImages(
        viewPager: ViewPager2, remoteUri: List<String>,
        lifecycle: Lifecycle, fm: FragmentManager, indicator: CircleIndicator3
    ) {
        try {
            val pagerAdapter = PagerAdapter(fm, lifecycle)
            for (element in remoteUri) {
                pagerAdapter.addItem(FeedPagerFragment.newInstance(element, "BindingAdapter"))
                viewPager.adapter = pagerAdapter
                indicator.setViewPager(viewPager)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @BindingAdapter("setTime")
    @JvmStatic
    fun setTime(textView: TextView, timestamp: Long) {
        try {
            textView.text = CommonFunction.calTime(timestamp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @BindingAdapter("setMyProfileUri")
    @JvmStatic
    fun setMyProfileUri(circleImageView: CircleImageView, imageUri: String) {

        UploadRepositoryImpl().getRemoteProfileImage(
            FirebaseAuth.getInstance().currentUser?.email.toString(),
            object: LongTaskCallback<String> {
                override fun onResponse(result: Result<String>) {
                    if(result is Result.Success) {
                        if(result.data == null)
                            return

                        Glide.with(circleImageView.context)
                            .load(Uri.parse(result.data))
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .centerCrop()
                            .into(circleImageView)
                    } else if(result is Result.Error) {
                        Log.e("error", "BindingAdapter setMyProfileUri Error : ${result.exception}")
                    }
                }
            })
    }


    /**
     * NotificationList
     */
    @BindingAdapter("setNotificationTitle")
    @JvmStatic
    fun setNotificationTitle(view: TextView, title: String?) {
        try {
            if (title != null) {
                SpannableStringBuilder(title).apply {
                    TextModule().setTextColorBold(
                        this,
                        view.context,
                        R.color.black,
                        0,
                        title.length
                    )
                    append(" ${view.context.getString(R.string.noti_title)}")
                    view.text = this
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @BindingAdapter("alreadyRead")
    @JvmStatic
    fun alreadyRead(constraintLayout: ConstraintLayout, flag: Boolean) {
        if (flag)
            constraintLayout.setBackgroundColor(constraintLayout.context.getColor(R.color.bottom_tab_color))
        else
            constraintLayout.setBackgroundColor(constraintLayout.context.getColor(R.color.white))
    }

    /**
     * Search
     */
    @BindingAdapter("setGridImage")
    @JvmStatic
    fun setGridImage(imageView: RoundedImageView, uri: List<String>) {
        try {
            var display = imageView.context.windowManager.defaultDisplay
            val size = Point()
            display.getRealSize(size)
            val width = size.x

            Glide.with(imageView.context)
                .load(Uri.parse(uri[0]))
                .override(width / 3, width / 3)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imageView)


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * follower and following
     */
    @BindingAdapter("setFollowBtn", "getTargetEmail")
    @JvmStatic
    fun setFollowBtn(button: Button, category : String, targetEmail : String) {
        try {
            if(category == "follower") {
                FollowRepositoryImpl().checkFollow(targetEmail, object :
                    LongTaskCallback<Boolean> {
                    override fun onResponse(result: Result<Boolean>) {
                        if(result is Result.Success) {
                            if(result.data) {
                                button.text = button.context.getString(R.string.follow_ing)
                                button.setBackgroundColor(button.context.getColor(R.color.main_color))
                                button.setTextColor(button.context.getColor(R.color.white))
                            } else {
                                button.text = button.context.getString(R.string.follow_each_other)
                                button.setBackgroundColor(button.context.getColor(R.color.white))
                                button.setTextColor(button.context.getColor(R.color.black))
                            }
                        } else if(result is Result.Error) {
                            Log.e("error", "BindingAdapter setFollowBtn error : ${result.exception}")
                        }
                    }
                })

                button.text = button.context.getString(R.string.follow_each_other)
            } else if(category == "following") {
                button.text = button.context.getString(R.string.unfollow)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @BindingAdapter("setRemoteProfileUri")
    @JvmStatic
    fun setRemoteProfileUri(circleImageView: CircleImageView, email: String) {

        UploadRepositoryImpl().getRemoteProfileImage(email, object:
            LongTaskCallback<String> {
                override fun onResponse(result: Result<String>) {
                    if(result is Result.Success) {

                        Glide.with(circleImageView.context)
                            .load(Uri.parse(result.data))
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .centerCrop()
                            .into(circleImageView)
                    } else if(result is Result.Error) {
                        Log.e("hi", "BindingAdapter setRemoteProfileUri Error : ${result.exception}")
                    }
                }
            })
    }


    /**
     * Chat
     */
    @BindingAdapter("setChatListNickname", "getMyNickname", "getTargetNickname")
    @JvmStatic
    fun setChatListNickname(textView: TextView, myEmail: String?, myNickname: String?, targetNickname : String?) {
        if(myEmail ==  CommonFunction.getInstance()!!.makeChatPath(FirebaseAuth.getInstance().currentUser?.email.toString())) {
            textView.text = targetNickname
        } else {
            textView.text = myNickname
        }
    }

    @BindingAdapter("setChatListProfileUri", "getMyProfileUri", "getTargetProfileUri")
    @JvmStatic
    fun setChatListProfileUri(circleImageView: CircleImageView, myEmail: String?, myProfileUri : String?, targetProfileUri : String?) {
        if(myEmail ==  CommonFunction.getInstance()!!.makeChatPath(FirebaseAuth.getInstance().currentUser?.email.toString())) {
            Glide.with(circleImageView.context)
                .load(Uri.parse(targetProfileUri))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .into(circleImageView)

            if(circleImageView.resources == null) {
                Glide.with(circleImageView.context)
                    .load(ContextCompat.getDrawable(circleImageView.context, R.drawable.menu_profile))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .into(circleImageView)
            }
        } else {
            Glide.with(circleImageView.context)
                .load(Uri.parse(myProfileUri))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .into(circleImageView)
        }
    }
}