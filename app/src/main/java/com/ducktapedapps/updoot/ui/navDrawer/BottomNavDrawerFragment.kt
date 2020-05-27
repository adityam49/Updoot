package com.ducktapedapps.updoot.ui.navDrawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.ducktapedapps.updoot.databinding.FragmentBottomNavDrawerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*

class BottomNavDrawerFragment : Fragment() {

    private lateinit var binding: FragmentBottomNavDrawerBinding
    private val behaviour: BottomSheetBehavior<FrameLayout> by lazy {
        from(binding.backgroundContainer)
    }

    private val bottomNavDrawerCallback = BottomNavDrawerCallback()
    fun addOnSlideAction(action: OnSlideAction) = bottomNavDrawerCallback.addOnSlideAction(action)
    fun addOnStateChangeAction(action: OnStateChangeAction) = bottomNavDrawerCallback.addOnStateChangeAction(action)

    private val scrim: View by lazy {
        binding.scrimView.apply {
            setOnClickListener { toggleState() }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBottomNavDrawerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        behaviour.apply {
            state = STATE_HIDDEN
            addOnSlideAction(object : OnSlideAction {
                override fun onSlide(slideOffset: Float) {
                    if (slideOffset < 0) scrim.alpha = 1 + slideOffset
                }
            })
            addOnStateChangeAction(object : OnStateChangeAction {
                override fun onStateChange(newState: Int) {
                    val visibility = when (newState) {
                        STATE_HIDDEN -> GONE
                        else -> VISIBLE
                    }
                    binding.root.visibility = visibility
                    scrim.visibility = visibility
                }
            })
            addBottomSheetCallback(bottomNavDrawerCallback)
        }
    }


    fun toggleState() {
        behaviour.state = when (behaviour.state) {
            STATE_HIDDEN -> STATE_HALF_EXPANDED
            STATE_HALF_EXPANDED -> STATE_HIDDEN
            STATE_EXPANDED -> STATE_HIDDEN
            else -> STATE_HIDDEN
        }
    }

    fun hide() {
        behaviour.state = STATE_HIDDEN
    }

    private companion object {
        const val TAG = "NavDrawerFragment"
    }
}