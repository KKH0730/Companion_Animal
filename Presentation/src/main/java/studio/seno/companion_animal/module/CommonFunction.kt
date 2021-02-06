package studio.seno.companion_animal.module

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object CommonFunction {
    fun closeKeyboard(context : Context, editText: EditText) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}