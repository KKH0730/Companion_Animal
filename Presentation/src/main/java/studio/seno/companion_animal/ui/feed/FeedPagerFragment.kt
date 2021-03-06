package studio.seno.companion_animal.ui.feed

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.FragmentFeedPagerBinding


class FeedPagerFragment : Fragment() {
    private var imageUri: String? = null
    private var from : String? = null
    private var binding : FragmentFeedPagerBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageUri = it.getString("uri")
            from = it.getString("from")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_feed_pager, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(from == "FeedImageActivity") {
            Glide.with(this)
                .load(Uri.parse(imageUri))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding!!.imageview)
        } else {
            Glide.with(this)
                .load(Uri.parse(imageUri))
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding!!.imageview)
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(uri : String, from : String) =
            FeedPagerFragment().apply {
                arguments = Bundle().apply {
                    putString("uri", uri)
                    putString("from", from)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding = null
    }
}