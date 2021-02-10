package studio.seno.companion_animal.module

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import java.util.*

object CommonFunction {

    fun closeKeyboard(context: Context, editText: EditText) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun showKeyboard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun calTime(millisecond: Long): String? {
        val result = System.currentTimeMillis() - millisecond
        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = System.currentTimeMillis()

        val pastCalendar = Calendar.getInstance()
        pastCalendar.timeInMillis = millisecond

        if(result in 0..1000L) {
            return "1초"
        } else if(result in 1000L..59999L) {
            val second = currentCalendar[Calendar.SECOND] - pastCalendar[Calendar.SECOND]
            return if(second < 0) "${60 - pastCalendar[Calendar.SECOND] + currentCalendar[Calendar.SECOND]} 초" else "${second.toString()}  초"

        } else if (result in 60000L..3599999L) {
            val minute = currentCalendar[Calendar.MINUTE] - pastCalendar[Calendar.MINUTE]
            return if (minute < 0) "${60 - pastCalendar[Calendar.MINUTE] + currentCalendar[Calendar.MINUTE]}  분" else "${minute.toString()}  분"

        } else if (result in 3600000L..86399999L) {
            val hour = currentCalendar[Calendar.HOUR_OF_DAY] - pastCalendar[Calendar.HOUR_OF_DAY]
            return if (hour < 0) "${24 - pastCalendar[Calendar.HOUR_OF_DAY] + currentCalendar[Calendar.HOUR_OF_DAY]}  시간" else "${hour.toString()}  시간"

        } else if (result in 86400000L..2591999999L) {
            val day = currentCalendar[Calendar.DATE] - pastCalendar[Calendar.DATE]
            return if (day < 0) "${30 - pastCalendar[Calendar.DATE] + currentCalendar[Calendar.DATE]}  일" else "${day.toString()}  일"

        } else if (result in 2592000000L..31535999999L) {
            val month = currentCalendar[Calendar.MONTH] - pastCalendar[Calendar.MONTH]
            return if (month < 0) "${12 - pastCalendar[Calendar.MONTH] + currentCalendar[Calendar.MONTH]}  달" else "${month.toString()}  달"

        } else {
            val year = currentCalendar[Calendar.YEAR] - pastCalendar[Calendar.YEAR]
            return "${year.toString()}  년"
        }
    }
}