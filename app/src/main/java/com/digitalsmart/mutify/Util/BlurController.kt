package com.digitalsmart.mutify

import android.view.View
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import no.danielzeller.blurbehindlib.BlurBehindLayout


//custom PanelSlideListener that blurs the background
class BlurController(view: View?, private val blurLayout: BlurBehindLayout) : SlidingUpPanelLayout.PanelSlideListener {
    override fun onPanelSlide(view: View, v: Float) {
        blurLayout.enable()
        if (v > 0.0f) {
            blurLayout.updateForMilliSeconds(0.001.toLong())
            blurLayout.alpha = v * 5
        }
    }

    override fun onPanelStateChanged(view: View, panelState: PanelState, panelState1: PanelState) {
    }

    init {
        blurLayout.viewBehind = view
    }
}