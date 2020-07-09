package com.ducktapedapps.updoot.ui.navDrawer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ViewBottomNavDrawerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*

class BottomNavigationDrawer @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val binding = ViewBottomNavDrawerBinding.inflate(LayoutInflater.from(context), this)

    private val bottomNavDrawerCallback = BottomNavDrawerCallback()

    private lateinit var bottomNavigationDrawerBehaviour: BottomSheetBehavior<LinearLayout>

    init {
        binding.apply {
            root.post {
                bottomNavigationDrawerBehaviour = from(binding.root as LinearLayout).apply {
                    peekHeight = context.dimensionFromAttribute(R.attr.actionBarSize)
                    isFitToContents = false
                    halfExpandedRatio = 0.4f
                    addBottomSheetCallback(bottomNavDrawerCallback)
                }
            }
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (position > 0) expand()
                }
            })
        }
    }

    private fun Context.dimensionFromAttribute(attribute: Int): Int {
        val attributes = obtainStyledAttributes(intArrayOf(attribute))
        val dimension = attributes.getDimensionPixelSize(0, 0)
        attributes.recycle()
        return dimension
    }


    /**
     * public methods
     */

    fun hide() {
        collapse()
        visibility = View.GONE
    }

    fun show() {
        visibility = View.VISIBLE
    }

    fun toggleState() {
        bottomNavigationDrawerBehaviour.state =
                when (bottomNavigationDrawerBehaviour.state) {
                    STATE_COLLAPSED -> STATE_HALF_EXPANDED
                    else -> STATE_COLLAPSED
                }
    }

    fun isInFocus() = bottomNavigationDrawerBehaviour.state != STATE_COLLAPSED

    fun expand() {
        bottomNavigationDrawerBehaviour.state = STATE_EXPANDED
    }

    fun collapse() {
        bottomNavigationDrawerBehaviour.state = STATE_COLLAPSED
    }

    fun addOnSlideAction(action: OnSlideAction) = bottomNavDrawerCallback.addOnSlideAction(action)

    fun addOnStateChangeAction(action: OnStateChangeAction) = bottomNavDrawerCallback.addOnStateChangeAction(action)

    private companion object {
        const val TAG = "NavDrawerFragment"
    }
}