package com.itikarus.miska.library.widget

import android.content.Context
import android.util.AttributeSet
import com.makeramen.roundedimageview.RoundedImageView

class RatioImageView : RoundedImageView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, (widthMeasureSpec * 1.5f).toInt())
    }
}