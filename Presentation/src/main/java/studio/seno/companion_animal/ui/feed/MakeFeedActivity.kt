package studio.seno.companion_animal.ui.feed

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.firebase.auth.FirebaseAuth
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.pchmn.materialchips.ChipView
import studio.seno.commonmodule.CustomToast
import studio.seno.companion_animal.R
import studio.seno.companion_animal.databinding.ActivityMakeFeedBinding
import studio.seno.companion_animal.module.CommonFunction
import studio.seno.companion_animal.util.ItemTouchHelperCallback
import studio.seno.companion_animal.util.OnItemDeleteListener
import studio.seno.domain.database.InfoManager
import studio.seno.domain.model.Feed
import java.sql.Timestamp

class MakeFeedActivity : AppCompatActivity(), View.OnClickListener,
    BottomSheetImagePicker.OnImagesSelectedListener, RadioGroup.OnCheckedChangeListener {
    private lateinit var binding: ActivityMakeFeedBinding
    private lateinit var selectedImageAdapter: SelectedImageAdapter
    private val viewModel: FeedListViewModel by viewModels()
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


        selectedImageAdapter.setOnDeleteItemListener(object : OnItemDeleteListener {
            override fun onDeleted(position: Int) {
                selectedImageAdapter.removeItem(selectedImageAdapter.getItem(position))
                selectedImageAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun init() {
        selectedImageAdapter = SelectedImageAdapter(applicationContext)
        binding.cameraBtn.setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        binding.radioGroup.setOnCheckedChangeListener(this)
        binding.hashTagBtn.setOnClickListener(this)
        binding.submitBtn.setOnClickListener(this)

        feed = intent.getParcelableExtra<Feed>("feed")
        mode = intent.getStringExtra("mode")
    }

    fun setImageRecycler() {
        binding.imageRecyclerView.adapter = selectedImageAdapter
        val itemHelper = ItemTouchHelper(ItemTouchHelperCallback(selectedImageAdapter))
        itemHelper.attachToRecyclerView(binding.imageRecyclerView)
    }

    fun setModifyInfo(){
        // 이미지 업로드
        for(element in feed!!.remoteUri!!) {
            selectedImageAdapter.addItem(element)
        }
        selectedImageAdapter.notifyDataSetChanged()

        //반려동물 종류
        when(feed!!.sort) {
            "dog" -> {
                binding.dog.isChecked = true
                currentChecked = "dog"
            }
            "cat" -> {
                binding.cat.isChecked = true
                currentChecked = "cat"
            }
            else -> {
                binding.etc.isChecked = true
                binding.etcContent.setText(feed!!.sort)
                currentChecked = feed!!.sort
            }
        }
        //해시태그
        hashTags = feed!!.hashTags!!.toMutableList()
        for(element in feed!!.hashTags!!) {
            makeHashTag(element)
        }

        //내용
        binding.content.setText(feed!!.content)

        if(mode == "modify") {
            binding.submitBtn.text = getString(R.string.modify)
        } else if(mode == "delete") {
            binding.submitBtn.text = getString(R.string.delete)
        }

    }


    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        for (element in uris) {
            if (selectedImageAdapter.getItems().size == 10) {
                CustomToast(applicationContext, getString(R.string.ShowAnimal_toast2)).show()
                break
            }
            selectedImageAdapter.addItem(element.toString())
            selectedImageAdapter.notifyDataSetChanged()
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

            makeHashTag(binding.hashTagContent.text.toString().trim())
            binding.hashTagContent.setText("")
            binding.hashTagContent.hint = getString(R.string.ShowAnimal_hint1)
            CommonFunction.closeKeyboard(applicationContext, binding.hashTagContent)


        } else if (v?.id == R.id.submit_btn) {
            if (currentChecked == null) {
                CustomToast(applicationContext, getString(R.string.ShowAnimal_toast3)).show()
                return
            } else if (currentChecked == "etc" && binding.etcContent.text.isEmpty()) {
                CustomToast(applicationContext, getString(R.string.ShowAnimal_toast4)).show()
                return
            } else if(selectedImageAdapter.getItems().size == 0) {
                CustomToast(applicationContext, getString(R.string.ShowAnimal_toast5)).show()
                return
            }

            if(currentChecked == "etc")
                currentChecked = binding.etcContent.text.toString()

            if(feed == null && mode != "write") {
                submitResult(Timestamp(System.currentTimeMillis()).time)
            } else if(feed != null && mode != "write") {
                if(mode == "modify")
                    submitResult(feed!!.timestamp)
                else
                    viewModel.requestDeleteFeed(feed!!)
            }


            viewModel.getFeedListSaveStatus().observe(this, {
                if(it) {
                    finish()
                }
            })

        }
    }

    fun submitResult(timestamp: Long){
        InfoManager.getString(this, "nickName")?.let {
            viewModel.requestUploadFeed(
                this,
                0,
                FirebaseAuth.getInstance().currentUser?.email.toString(),
                it,
                currentChecked!!,
                hashTags,
                selectedImageAdapter.getItems(),
                binding.content.text.toString(),
                timestamp
            )
        }
    }

    fun makeHashTag(str : String){
        val chipView = ChipView(this)
        chipView.label = str
        chipView.setDeletable(true)
        chipView.setPadding(30, 0, 0, 0)
        chipView.setChipBackgroundColor(getColor(R.color.main_color))
        chipView.setLabelColor(getColor(R.color.white))
        binding.hashTagContainer.addView(chipView)
        hashTags.add(chipView.label)

        chipView.setOnDeleteClicked {
            binding.hashTagContainer.removeView(chipView)
            hashTags.remove(chipView.label)
        }
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

}

