package studio.seno.companion_animal.ui.feed

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
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
import java.sql.Timestamp

class MakeFeedActivity : AppCompatActivity(), View.OnClickListener,
    BottomSheetImagePicker.OnImagesSelectedListener,
    RadioGroup.OnCheckedChangeListener {
    private lateinit var binding: ActivityMakeFeedBinding
    private lateinit var selectedImageAdapter: SelectedImageAdapter
    private val viewModel: FeedListViewModel by viewModels()


    private var currentChecked: String? = null
    private var hashTags = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_make_feed)
        initView()
        setImageRecycler()

        selectedImageAdapter.setOnDeleteItemListener(object : OnItemDeleteListener {
            override fun onDeleted(position: Int) {
                selectedImageAdapter.removeItem(selectedImageAdapter.getItem(position))
                selectedImageAdapter.notifyDataSetChanged()
            }
        })
    }

    fun setImageRecycler() {
        binding.imageRecyclerView.adapter = selectedImageAdapter
        val itemHelper = ItemTouchHelper(ItemTouchHelperCallback(selectedImageAdapter))
        itemHelper.attachToRecyclerView(binding.imageRecyclerView)
    }

    private fun initView() {
        selectedImageAdapter = SelectedImageAdapter(applicationContext)
        binding.cameraBtn.setOnClickListener(this)
        binding.header.findViewById<ImageButton>(R.id.back_btn).setOnClickListener(this)
        binding.radioGroup.setOnCheckedChangeListener(this)
        binding.hashTagBtn.setOnClickListener(this)
        binding.submitBtn.setOnClickListener(this)

    }


    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        for (uri in 0..(uris.size - 1)) {
            if (selectedImageAdapter.getItems().size == 10) {
                CustomToast(applicationContext, getString(R.string.ShowAnimal_toast2)).show()
                break
            }
            selectedImageAdapter.addItem(uris[uri].toString())
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

            val chipView = ChipView(this)
            chipView.label = binding.hashTagContent.text.toString().trim()
            chipView.setDeletable(true)
            chipView.setPadding(30, 0, 0, 0)
            chipView.setChipBackgroundColor(getColor(R.color.main_color))
            chipView.setLabelColor(getColor(R.color.white))
            binding.hashTagContainer.addView(chipView)
            hashTags.add(chipView.label)
            binding.hashTagContent.setText("")
            binding.hashTagContent.hint = getString(R.string.ShowAnimal_hint1)
            CommonFunction.closeKeyboard(applicationContext, binding.hashTagContent)

            chipView.setOnDeleteClicked {
                binding.hashTagContainer.removeView(chipView)
                hashTags.remove(chipView.label)
            }
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

            viewModel.requestUploadFeed(
                FirebaseAuth.getInstance()?.currentUser?.email.toString(),
                "test_nickname",
                currentChecked!!,
                hashTags,
                selectedImageAdapter.getItems(),
                binding.content.text.toString(),
                Timestamp(System.currentTimeMillis()).time
            )

            viewModel.getFeedListSaveStatus().observe(this, {
                if(it)
                    finish()
                else
                    CustomToast(this, getString(R.string.upload_fail)).show()
            })

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

