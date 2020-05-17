package com.ducktapedapps.updoot.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.FragmentBottomNavDrawerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.shape.*

class NavDrawerFragment : Fragment() {

    private lateinit var binding: FragmentBottomNavDrawerBinding
    private val behaviour: BottomSheetBehavior<FrameLayout> by lazy {
        from(binding.backgroundContainer)
    }

    val scrim: View by lazy {
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
            addBottomSheetCallback(object : BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset < 0) scrim.alpha = 1 + slideOffset
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    val visibility = when (newState) {
                        STATE_HIDDEN -> GONE
                        else -> VISIBLE
                    }
                    binding.root.visibility = visibility
                    scrim.visibility = visibility
                }
            })

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

    private companion object {
        const val TAG = "NavDrawerFragment"
    }
}