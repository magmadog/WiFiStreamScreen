package com.sarbaevartur.wifistreamscreen.ui.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sarbaevartur.wifistreamscreen.R


class CurvedBottomNavigationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    private val path: Path = Path()
    private var backgroundShapeColor: Int =
        ContextCompat.getColor(getContext(), R.color.colorNavigationBackground)

    init {
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        path.apply {
            reset()
            moveTo(0f, 0f)
            lineTo(w.toFloat(), 0f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }

        val shape = PathShape(path, w.toFloat(), h.toFloat())
        background = ShapeDrawable(shape).apply {
            colorFilter = PorterDuffColorFilter(backgroundShapeColor, PorterDuff.Mode.SRC_IN)
        }
    }
}