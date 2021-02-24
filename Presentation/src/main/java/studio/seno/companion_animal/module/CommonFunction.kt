package studio.seno.companion_animal.module

import android.content.Context
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.Room
import studio.seno.domain.database.AppDatabase
import java.lang.StringBuilder
import java.util.*

object CommonFunction {
    private var commonFunction : CommonFunction? = null

    fun getInstance() : CommonFunction? {
        if(commonFunction == null) {
            synchronized(CommonFunction::class.java) {
                commonFunction = this
            }
        }
        return commonFunction
    }

    fun closeKeyboard(context: Context, editText: EditText) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun showKeyboard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun lockTouch(window : Window){
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    fun unlockTouch(window : Window){
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    fun makeChatPath(email : String) : String{
        var sb = StringBuilder()

        for(i in email.indices) {
            if(email[i] != '.')
                sb.append(email[i])
        }
        return sb.toString()
    }



    fun calTime(millisecond: Long): String? {
        val result = System.currentTimeMillis() - millisecond
        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = System.currentTimeMillis()

        val pastCalendar = Calendar.getInstance()
        pastCalendar.timeInMillis = millisecond

        if(result in 0..1000L) {
            return "1초 전"
        } else if(result in 1000L..59999L) {
            val second = currentCalendar[Calendar.SECOND] - pastCalendar[Calendar.SECOND]
            return if(second < 0) "${60 - pastCalendar[Calendar.SECOND] + currentCalendar[Calendar.SECOND]}초 전" else "${second.toString()}초 전"

        } else if (result in 60000L..3599999L) {
            val minute = currentCalendar[Calendar.MINUTE] - pastCalendar[Calendar.MINUTE]
            return if (minute < 0) "${60 - pastCalendar[Calendar.MINUTE] + currentCalendar[Calendar.MINUTE]}분 전" else "${minute.toString()}분 전"

        } else if (result in 3600000L..86399999L) {
            val hour = currentCalendar[Calendar.HOUR_OF_DAY] - pastCalendar[Calendar.HOUR_OF_DAY]
            return if (hour < 0) "${24 - pastCalendar[Calendar.HOUR_OF_DAY] + currentCalendar[Calendar.HOUR_OF_DAY]}시간 전" else "${hour.toString()}시간 전"

        } else if (result in 86400000L..2591999999L) {
            val day = currentCalendar[Calendar.DATE] - pastCalendar[Calendar.DATE]
            return if (day < 0) "${30 - pastCalendar[Calendar.DATE] + currentCalendar[Calendar.DATE]}일 전" else "${day.toString()}일 전"

        } else if (result in 2592000000L..31535999999L) {
            val month = currentCalendar[Calendar.MONTH] - pastCalendar[Calendar.MONTH]
            return if (month < 0) "${12 - pastCalendar[Calendar.MONTH] + currentCalendar[Calendar.MONTH]}달 전" else "${month.toString()}달 전"

        } else {
            val year = currentCalendar[Calendar.YEAR] - pastCalendar[Calendar.YEAR]
            return "${year.toString()}년 전"
        }
    }
}