package com.ducktapedapps.updoot.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.animation.DecelerateInterpolator;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class CustomItemAnimator extends DefaultItemAnimator {
    private static final String TAG = "CustomItemAnimator";

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getLayoutPosition() == 0)
            viewHolder.itemView.setTranslationY(viewHolder.itemView.getHeight());
        else
            viewHolder.itemView.setTranslationY(viewHolder.itemView.getTop());

        viewHolder.itemView.animate()
                .translationY(0)  //move to its original y co-ordinate
                .setInterpolator(new DecelerateInterpolator(3.0f))
                .setDuration(700)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dispatchAddFinished(viewHolder);
                    }
                })
                .start();
        return false;
    }
}
