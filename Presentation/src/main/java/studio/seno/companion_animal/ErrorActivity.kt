package studio.seno.companion_animal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import studio.seno.companion_animal.databinding.ActivityErrorBinding

class ErrorActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_error)

        init()
    }

    fun init(){
        binding.header.findViewById<TextView>(R.id.title).visibility = View.GONE
        binding.header.findViewById<TextView>(R.id.title).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.back_btn) {
            finish()
        }
    }
}