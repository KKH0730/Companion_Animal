package studio.seno.companion_animal

import android.graphics.Point
import android.net.Uri
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.*
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.viewpager2.widget.ViewPager2
import com.aqoong.lib.expandabletextview.ExpandableTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.pchmn.materialchips.ChipView
import de.hdodenhof.circleimageview.CircleImageView
import me.relex.circleindicator.CircleIndicator3
import org.jetbrains.anko.dimen
import org.jetbrains.anko.windowManager
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.module.TextModule
import studio.seno.companion_animal.ui.feed.FeedPagerFragment
import studio.seno.companion_animal.ui.main_ui.PagerAdapter
import studio.seno.datamodule.LocalRepository
import studio.seno.datamodule.Repository
import studio.seno.domain.LongTaskCallback
import studio.seno.domain.Result
import studio.seno.domain.model.User

object BindingAdapter {
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

    @BindingAdapter("setNickname")
    @JvmStatic
    fun setNickname(view: TextView, nickName: String?) {
        try {
            if (nickName != null) {
                view.text = nickName
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @BindingAdapter("setContent")
    @JvmStatic
    fun setContent(view: ExpandableTextView, content: String?) {
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

    @BindingAdapter("setCommentContent")
    @JvmStatic
    fun setCommentContent(view: TextView, content: String?) {
        try {
            if (content != null) {
                view.text = content
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @BindingAdapter("setHeart")
    @JvmStatic
    fun setHeart(view: TextView, heart: Long) {
        try {
            view.text = heart.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @BindingAdapter("setHeartButton")
    @JvmStatic
    fun setHeartButton(imageButton: ImageButton, map: Map<String, String>) {
        imageButton.isSelected =
            map[FirebaseAuth.getInstance().currentUser!!.email.toString()] != null
    }

    @BindingAdapter("setBookmarkButton")
    @JvmStatic
    fun setBookmarkButton(imageButton: ImageButton, map: Map<String, String>) {
        imageButton.isSelected =
            map[FirebaseAuth.getInstance().currentUser!!.email.toString()] != null
    }


    @BindingAdapter("setComment")
    @JvmStatic
    fun setComment(view: TextView, comment: Long) {
        try {
            view.text = comment.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            var pagerAdapter = PagerAdapter(fm, lifecycle)
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

    @BindingAdapter("setMyProfileImage", "getLifecycleScope")
    @JvmStatic
    fun setMyProfileImage(circleImageView: CircleImageView, imageUri: String, lifecycleCoroutineScope: LifecycleCoroutineScope) {
        try {
            LocalRepository(circleImageView.context).getUserInfo(lifecycleCoroutineScope, object  : LongTaskCallback<User>{
                override fun onResponse(result: Result<User>) {
                    if(result is Result.Success) {
                        val user = result.data

                        Glide.with(circleImageView.context)
                            .load(Uri.parse(user.profileUri))
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .centerCrop()
                            .into(circleImageView)

                    } else if(result is Result.Error) {
                        Log.e("error", "databinding setMyProfileImage error : ${result.exception}")
                    }
                }
            })

        }catch (e : Exception) {

        }
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
    fun alreadyRead(imageView: ImageView, flag: Boolean) {
        if (flag)
            imageView.visibility = View.VISIBLE
        else
            imageView.visibility = View.GONE
    }

    /**
     * Search
     */
    @BindingAdapter("setImage")
    @JvmStatic
    fun setImage(imageView: ImageView, uri: List<String>) {
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

    @BindingAdapter("setFollowBtn")
    @JvmStatic
    fun setFollowBtn(button: Button, category : String) {
        try {
            if(category == "follower") {
                button.text = button.context.getString(R.string.follow_each_other)
            } else if(category == "following") {
                button.text = button.context.getString(R.string.unfollow)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}