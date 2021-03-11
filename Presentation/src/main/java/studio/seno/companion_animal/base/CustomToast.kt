package studio.seno.companion_animal.base

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import studio.seno.companion_animal.R


class CustomToast(context : Context, content : String) : Toast(context){
    init{
        val view = LayoutInflater.from(context).inflate(R.layout.custom_toast_layout, null)
        view.findViewById<TextView>(R.id.TextVIew_Toast).apply{
            text = content
        }
        setView(view)
        duration = LENGTH_SHORT
    }
}