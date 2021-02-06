package studio.seno.companion_animal.util

import android.os.Handler
import com.dd.processbutton.ProcessButton
import java.util.*

open class ProgressGenerator(private val mListener: OnCompleteListener) {
    interface OnCompleteListener {
        fun onComplete()
    }

    private var mProgress = 0
    fun start(button: ProcessButton) {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                mProgress += 10
                button.progress = mProgress
                if (mProgress < 100) {
                    handler.postDelayed(this, generateDelay().toLong())
                } else {
                    mListener.onComplete()
                }
            }
        }, generateDelay().toLong())
    }

    private val random = Random()
    private fun generateDelay(): Int {
        return random.nextInt(1000)
    }
}