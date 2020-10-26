package com.digitalsmart.mutify.util

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.ViewPager
import com.skydoves.balloon.Balloon
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import com.valkriaine.factor.HomePager
import no.danielzeller.blurbehindlib.BlurBehindLayout


//custom PanelSlideListener that blurs the background
class BlurController(view: View?,
                     private val blurLayout: BlurBehindLayout,
                     private val addTile: CardView,
                     private val menuTile: CardView,
                     private val homePager: HomePager,
                     private val balloon: Balloon,
                     private val anchor: View,
                     private val offset: Int)
    : SlidingUpPanelLayout.PanelSlideListener, ViewPager.OnPageChangeListener
{
    init
    {

    }

    override fun onPanelSlide(view: View, v: Float) {
        blurLayout.enable()
        if (v > 0.0f) {
            blurLayout.updateForMilliSeconds(0.001.toLong())
            blurLayout.alpha = v * 5
        }
    }

    override fun onPanelStateChanged(view: View, panelState: PanelState, panelState1: PanelState)
    {
        balloon.dismiss()
        if (panelState1 == PanelState.EXPANDED && panelState == PanelState.DRAGGING)
        {
            addTile.animate()
                    .translationX(offset - 10f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            menuTile.animate()
                    .translationX(-1 * offset + 10f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()

            if (homePager.currentItem == 0)
            {
                addTile.animate()
                        .translationZ(50f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .start()
                menuTile.animate()
                        .translationZ(-50f)
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .start()
            }
            if (homePager.currentItem == 1)
            {
                addTile.animate()
                        .translationZ(-50f)
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .start()
                menuTile.animate()
                        .translationZ(50f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .start()
            }


        }
        if (panelState1 == PanelState.COLLAPSED && panelState == PanelState.DRAGGING)
        {
            balloon.showAlignTop(anchor)
            addTile.animate()
                    .translationX(0f)
                    .translationZ(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            menuTile.animate()
                    .translationX(0f)
                    .translationZ(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
        }
    }

    init {
        blurLayout.viewBehind = view
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
    {

    }

    override fun onPageSelected(position: Int) {
        if (position == 0) {
            addTile.animate()
                    .translationZ(50f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            menuTile.animate()
                    .translationZ(-50f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
        } else {
            addTile.animate()
                    .translationZ(-50f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            menuTile.animate()
                    .translationZ(50f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
        }
    }

    override fun onPageScrollStateChanged(state: Int)
    {

    }
}