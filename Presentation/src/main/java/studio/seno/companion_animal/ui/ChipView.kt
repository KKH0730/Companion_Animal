package studio.seno.companion_animal.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import studio.seno.companion_animal.R

class ChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val chipTextView: TextView
    private val cardView: CardView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_chip, this, true)
        chipTextView = findViewById(R.id.chip_text)
        cardView = findViewById(R.id.cardView)
    }

    // 텍스트 설정 메서드
    fun setText(text: String) {
        chipTextView.text = text
    }

    // 배경 색상을 변경하는 메서드
    fun setChipBackgroundColor(color: Int) {
        // ChipView 자체의 배경색 변경 (기존 배경 Drawable을 덮어씁니다.)
        cardView.setCardBackgroundColor(color)
    }

    fun setLabelColor(color: Int) {
        chipTextView.setTextColor(color)
    }

    fun setLabel(label: String) {
        chipTextView.text = label
    }
}

