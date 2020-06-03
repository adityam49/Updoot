package com.ducktapedapps.updoot.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.DialogFragment
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.FragmentExploreOverlayDialogBinding

class ExploreDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentExploreOverlayDialogBinding
    private var toolbarExpanded = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentExploreOverlayDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            root.post { toggleToolbar() }
            searchView.apply {
                isFocusable = true
                isIconified = false
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialog)
        dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        super.onCreate(savedInstanceState)
    }

    private fun toggleToolbar() {
        val margin =
                if (toolbarExpanded) (resources.displayMetrics.density * 16).toInt()
                else 0

        val changeBounds = ChangeBounds().apply {
            duration = 300
            addListener(object : Transition.TransitionListener {
                override fun onTransitionEnd(transition: Transition) = Unit

                override fun onTransitionResume(transition: Transition) = Unit

                override fun onTransitionPause(transition: Transition) = Unit

                override fun onTransitionCancel(transition: Transition) {
                    toolbarExpanded = !toolbarExpanded
                }

                override fun onTransitionStart(transition: Transition) {
                    ObjectAnimator.ofFloat(
                            binding.cardView,
                            "radius",
                            if (toolbarExpanded) 0f else 32f,
                            if (toolbarExpanded) 32f else 0f
                    ).apply {
                        duration = 300
                        start()
                    }
                    toolbarExpanded = !toolbarExpanded
                }
            })
        }
        TransitionManager.beginDelayedTransition(binding.root, changeBounds)

        ConstraintSet().apply {
            clone(binding.root)
            setMargin(R.id.card_view, ConstraintSet.START, margin)
            setMargin(R.id.card_view, ConstraintSet.END, margin)
            setMargin(R.id.card_view, ConstraintSet.TOP, margin)
            setMargin(R.id.card_view, ConstraintSet.BOTTOM, margin)
            applyTo(binding.root)
        }
    }

}