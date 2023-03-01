package com.example.vkinternshipaudionotes.presentation.customView

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class RecorderWaveformView: View {

    private lateinit var amplitudes: ArrayList<Int>
    private lateinit var spikes: ArrayList<RectF>
    private lateinit var paintRead: Paint
    private var w : Float = 9f
    private var d : Float = 4f
    private var sw : Int = 0
    private var maxSpikes : Int = 0
    private var maxAmp : Int = 200

    constructor(context: Context?) : super(context){
        init(null)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        init(attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        init(attrs)
    }


    private fun init(attrs: AttributeSet?){
        amplitudes = ArrayList()
        paintRead = Paint()
        paintRead.color = Color.rgb(29, 127, 255)

        val displayMetrics = resources.displayMetrics
        sw = displayMetrics.widthPixels

        maxSpikes = (sw/(w+d)).toInt()
        spikes = ArrayList()
    }

    fun reset(){
        amplitudes.clear()
        spikes.clear()
        invalidate()
    }

    fun updateAmps(amp: Int){

        val norm  = Math.min(amp/7, maxAmp)
        amplitudes.add(norm)
        val amps = amplitudes.takeLast(maxSpikes)

        spikes.clear()

        for(i in amps.indices){
            val delta = maxAmp.toFloat()
            val top = delta - amps[i]
            val bottom = top + amps[i] as Int
            val rectUp = RectF(sw-i*(w+d), top, sw-i*(w+d) - w, bottom)
            val rectDown = RectF(sw-i*(w+d), delta-2, sw-i*(w+d) - w, delta+amps[i])
            spikes.add(rectUp)
            spikes.add(rectDown)
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        spikes.forEach {
            canvas?.drawRoundRect(it, 6f, 6f,paintRead)
        }
    }
}