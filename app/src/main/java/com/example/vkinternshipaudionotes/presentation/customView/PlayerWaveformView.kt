package com.example.vkinternshipaudionotes.presentation.customView

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View

class PlayerWaveformView : View {

    private lateinit var spikes: Array<RectF>
    private lateinit var paintRead: Paint
    private var w: Int = 18
    private var d: Int = 4
    private var sw: Int = 0
    private var delta = 320
    private var nbSpikes = 30

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }


    private fun init(attrs: AttributeSet?) {
        spikes = Array(nbSpikes) { RectF() }

        paintRead = Paint()
        paintRead.color = Color.rgb(29, 127, 255)

        val displayMetrics = resources.displayMetrics
        sw = displayMetrics.widthPixels
    }


    fun updateAmps(amp: Int) {

        val norm = amp
        for (i in spikes.indices) {
            val bottom: Float = (Math.random() * norm).toFloat()
            val top = delta - bottom

            val rectUp = RectF(i * (w + d) * 1f, top, i * (w + d) + w * 1f, bottom)
            spikes[i] = rectUp

        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        spikes.forEach {
            Log.d("waveform", it.bottom.toString())
            canvas?.drawRoundRect(it, 10f, 10f, paintRead)
        }
    }
}