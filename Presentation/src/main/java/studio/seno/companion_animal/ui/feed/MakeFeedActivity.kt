package studio.seno.companion_animal.ui.feed

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import com.kroegerama.imgpicker.BottomSheetImagePicker
import dagger.hilt.android.AndroidEntryPoint
import studio.seno.companion_animal.base.CustomToast
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivityMakeFeedBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.ui.ChipView
import studio.seno.datamodule.repository.local.LocalRepository
import studio.seno.domain.util.LongTaskCallback
import studio.seno.domain.util.Result
import studio.seno.domain.model.Feed
import studio.seno.domain.model.User
import java.sql.Timestamp

@AndroidEntryPoint
class MakeFeedActivity : AppCompatActivity(), View.OnClickListener,
    BottomSheetImagePicker.OnImagesSelectedListener, RadioGroup.OnCheckedChangeListener {
    private var binding: ActivityMakeFeedBinding? = null
    private var selectedImageAdapter: SelectedImageAdapter? = null
    private val feedListViewModel: FeedListViewModel by viewModels()
    private val localRepository = LocalRepository(this)
    private var feed : Feed? = null
    private var mode = "write"
    private var currentChecked: String? = null
    private var hashTags = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_make_feed)
        init()
        setImageRecycler()

        if(feed != null) {
            setModifyInfo()
        }


        selectedImageAdapter!!.setOnDeleteItemListener(object :
            OnItemDeleteListener {
            override fun onDeleted(position: Int) {
                val deleteItem : String = selectedImageAdapter!!.getItem(position)
                    selectedImageAdapter!!.removeItem(deleteItem)
                selectedImageAdapter!!.notifyDataSetChanged()
            }
        })
    }

    private fun init() {
        selectedImageAdapter = SelectedImageAdapter(applicationContext)
        binding!!.cameraBtn.setOnClickListener(this)
        binding!!.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        binding!!.header.findViewById<TextView>(R.id.title2).text = getString(R.string.ShowAnimal_title)
        binding!!.radioGroup.setOnCheckedChangeListener(this)
        binding!!.hashTagBtn.setOnClickListener(this)
        binding!!.submitBtn.setOnClickListener(this)

        feed = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("feed", Feed::class.java)
        } else {
            intent.getParcelableExtra<Feed>("feed")
        }
        if(feed != null)
            mode = intent.getStringExtra("mode") ?: ""
    }

    fun setImageRecycler() {
        binding!!.imageRecyclerView.adapter = selectedImageAdapter
        val itemHelper = ItemTouchHelper(ItemTouchHelperCallback(selectedImageAdapter!!))
        itemHelper.attachToRecyclerView(binding!!.imageRecyclerView)
    }

    fun setModifyInfo(){
        for(element in feed!!.getLocalUri()) {
            selectedImageAdapter!!.addItem(element)
        }


        //반려동물 종류
        when(feed!!.getSort()) {
            "dog" -> {
                binding!!.dog.isChecked = true
                currentChecked = "dog"
            }
            "cat" -> {
                binding!!.cat.isChecked = true
                currentChecked = "cat"
            }
            else -> {
                binding!!.etc.isChecked = true
                binding!!.etcContent.setText(feed!!.getSort())
                currentChecked = feed!!.getSort()
            }
        }
        //해시태그
        for(element in feed!!.getHashTags().toMutableList()) {
            makeHashTag(element)
        }

        //내용
        binding!!.content.setText(feed!!.getContent())

        if(mode == "modify") {
            binding!!.submitBtn.text = getString(R.string.modify)
        } else if(mode == "delete") {
            binding!!.submitBtn.text = getString(R.string.delete)
        }

    }


    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        for (element in uris) {
            if (selectedImageAdapter!!.getItems().size == 10) {
                CustomToast(applicationContext, getString(R.string.ShowAnimal_toast2)).show()
                break
            }
            selectedImageAdapter!!.addItem(element.toString())
            selectedImageAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.camera_btn) {
            BottomSheetImagePicker.Builder(getString(R.string.file_provider))
                .multiSelect(1, 10)
                .multiSelectTitles(
                    R.plurals.pick_multi,
                    R.plurals.pick_multi_more,
                    R.string.pick_multi_limit
                )
                .peekHeight(R.dimen.peekHeight)
                .columnSize(R.dimen.columnSize)
                .show(supportFragmentManager, null)
        } else if (v?.id == R.id.back_btn) {
            finish()
        } else if (v?.id == R.id.hashTag_btn) {
            if (hashTags.size == 5) {
                CustomToast(applicationContext, getString(R.string.ShowAnimal_toast1)).show()
                return
            }

            makeHashTag(binding!!.hashTagContent.text.toString().trim())
            binding!!.hashTagContent.setText("")
            binding!!.hashTagContent.hint = getString(R.string.ShowAnimal_hint1)
            CommonFunction.getInstance()!!.closeKeyboard(applicationContext, binding!!.hashTagContent)


        } else if (v?.id == R.id.submit_btn) {
            if (currentChecked == null) {
                CustomToast(applicationContext, getString(R.string.ShowAnimal_toast3)).show()
                return
            } else if (currentChecked == "etc" && binding!!.etcContent.text.isEmpty()) {
                CustomToast(applicationContext, getString(R.string.ShowAnimal_toast4)).show()
                return
            } else if(selectedImageAdapter!!.getItems().size == 0) {
                CustomToast(applicationContext, getString(R.string.ShowAnimal_toast5)).show()
                return
            }

            CommonFunction.getInstance()!!.lockTouch(window!!)
            binding!!.progressBar.visibility = View.VISIBLE


            if(currentChecked == "etc")
                currentChecked = binding!!.etcContent.text.toString()

            if(feed == null && mode == "write") {
                localRepository.updateFeedCount(lifecycleScope, true)
                submitResult(Timestamp(System.currentTimeMillis()).time)
            } else if(feed != null && mode != "write") {
                if(mode == "modify") {
                    submitResult(feed!!.getTimestamp())
                } else {
                    localRepository.updateFeedCount(lifecycleScope, false)
                    feedListViewModel.requestDeleteFeed(feed!!)
                }
            }

            feedListViewModel.getFeedListSaveStatus().observe(this@MakeFeedActivity, {
                if(it) {
                    CommonFunction.getInstance()!!.unlockTouch(window!!)
                    binding!!.progressBar.visibility = View.GONE

                    finish()
                }
            })
        }
    }


    fun submitResult(timestamp: Long){
        LocalRepository.getInstance(this)!!.getUserInfo(lifecycleScope, object :
            LongTaskCallback<User> {
            override fun onResponse(result: Result<User>) {
                if (result is Result.Success) {

                    feedListViewModel.requestUploadFeed(
                        result.data.email, result.data.nickname, currentChecked!!,
                        hashTags, selectedImageAdapter!!.getItems(), binding!!.content.text.toString(),
                        timestamp, object: LongTaskCallback<Any> {
                            override fun onResponse(result: Result<Any>) {
                                if(result is Result.Success) {
                                    feed = result.data as Feed
                                }
                            }

                        }
                    )
                }
            }})
    }


    fun makeHashTag(str : String){
        var sb = StringBuilder()
        val chipView = ChipView(this)
        if(str.length == 0) {
            sb.append("#")
        } else if(str.length != 0 && str[0] != '#'){
            sb.append("#")
            sb.append(str)
        } else {
            sb.append(str)
        }

        chipView.setLabel(sb.toString())
//        chipView.setDeletable(true)
        chipView.setPadding(30, 0, 0, 0)
        chipView.setChipBackgroundColor(getColor(R.color.main_color))
        chipView.setLabelColor(getColor(R.color.white))
        binding!!.hashTagContainer.addView(chipView)
//        hashTags.add(chipView.label)
//
//        chipView.setOnDeleteClicked {
//            binding!!.hashTagContainer.removeView(chipView)
//            hashTags.remove(chipView.label)
//        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        if (checkedId == R.id.dog) {
            currentChecked = "dog"
        } else if (checkedId == R.id.cat) {
            currentChecked = "cat"
        } else {
            currentChecked = "etc"
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        binding = null
        selectedImageAdapter = null
    }
}

