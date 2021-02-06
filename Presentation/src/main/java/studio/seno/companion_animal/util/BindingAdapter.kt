package studio.seno.companion_animal.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.lang.Exception

object BindingAdapter {
    @BindingAdapter("setContent")
    @JvmStatic
    fun setContent(view : TextView, content : String?) {
        try{
            if(content != null) {
                view.text = content
            }
        }catch (e : Exception) {
            e.printStackTrace()
        }
    }
}