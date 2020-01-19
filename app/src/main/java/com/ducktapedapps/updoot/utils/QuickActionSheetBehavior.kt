package com.ducktapedapps.updoot.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior

class QuickActionSheetBehavior(context: Context, attrs: AttributeSet) : BottomSheetBehavior<NestedScrollView>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: NestedScrollView, dependency: View): Boolean {
        return dependency is BottomNavigationView
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: NestedScrollView, dependency: View): Boolean {
        if (dependency is BottomNavigationView) {
            relocateQAS(dependency, child)
            return true
        }
        return super.onDependentViewChanged(parent, child, dependency)

    }

    private fun relocateQAS(bottomNavigationView: BottomNavigationView, child: NestedScrollView) {
        child.translationY = bottomNavigationView.height * -1.0f
    }

    fun hideQAS() {
        state = STATE_COLLAPSED
    }
}