package studio.seno.companion_animal.util

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan


object TextUtils {

   var setTextColorBold = {ssb: SpannableStringBuilder, context: Context, color: Int, start: Int, end: Int ->
       ssb.setSpan(ForegroundColorSpan(context.resources.getColor(color)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
       ssb.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
       ssb
   }


}


