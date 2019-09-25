package com.ducktapedapps.updoot.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.ducktapedapps.updoot.R;

import org.jetbrains.annotations.NotNull;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

public class SwipeUtils extends ItemTouchHelper.Callback {
    private static final String TAG = "SwipeUtils";
    private boolean swipeBack = false;
    private swipeActionCallback swipeActionCallback;
    private int performActionIndex = 0; // left = -2 ,slightLeft = -1 ,slightRight = 1 , right = 2


    private Paint paint;
    private int upVoteColor;
    private int saveContentColor;
    private int downVoteColor;
    private int optionsColor;
    private Bitmap upVoteIcon;
    private Bitmap downVoteIcon;
    private Bitmap saveIcon;

    public SwipeUtils(Context context, swipeActionCallback callback) {
        super();
        init(context);
        swipeActionCallback = callback;
    }

    private static Bitmap getBitmap(Context context, int vectorDrawable) {
        Drawable drawable = ContextCompat.getDrawable(context, vectorDrawable);
        if (drawable instanceof VectorDrawable) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }

    private void init(Context context) {
        paint = new Paint();
        upVoteIcon = getBitmap(context, R.drawable.ic_upvote_24dp);
        downVoteIcon = getBitmap(context, R.drawable.ic_downvote_24dp);
        saveIcon = getBitmap(context, R.drawable.ic_star_black_24dp);
        upVoteColor = ContextCompat.getColor(context, R.color.upVoteColor);
        downVoteColor = ContextCompat.getColor(context, R.color.downVoteColor);
        saveContentColor = ContextCompat.getColor(context, R.color.saveContentColor);
        optionsColor = ContextCompat.getColor(context, R.color.neutralColor);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeFlag(ACTION_STATE_SWIPE, LEFT | RIGHT);
    }

    @Override
    public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ACTION_STATE_SWIPE) {
            // Get RecyclerView item from the ViewHolder
            View itemView = viewHolder.itemView;
            float itemViewWidth = itemView.getWidth();
            float itemViewTop = itemView.getTop();
            float itemViewBottom = itemView.getBottom();
            float itemViewLeft = itemView.getLeft();
            float itemViewRight = itemView.getRight();
            float firstThreshold = itemViewWidth * 0.2f;
            float secondThreshold = itemViewWidth * 0.6f;

            if (dX > 0) {
                // right swipe
                paint.setColor(upVoteColor);
                if (dX < firstThreshold) {
                    paint.setAlpha((int) (255 * (dX / firstThreshold))); //right swipe cancel threshold of 20%
                    c.drawRect(itemViewLeft, itemViewTop, dX, itemViewBottom, paint);
                } else if (dX > firstThreshold && dX <= secondThreshold) {
                    c.drawRect(itemViewLeft, itemViewTop, dX, itemViewBottom, paint);
                    c.drawBitmap(upVoteIcon,
                            itemViewLeft + itemViewWidth * 0.1f,
                            itemViewTop + (itemViewBottom - itemViewTop - upVoteIcon.getHeight()) / 2,
                            null);
                } else if (dX >= secondThreshold) {
                    paint.setColor(saveContentColor); // swiped right more than 60%
                    c.drawRect(itemViewLeft, itemViewTop, dX, itemViewBottom, paint);
                    c.drawBitmap(saveIcon,
                            itemViewLeft + itemViewWidth * 0.1f,
                            itemViewTop + (itemViewBottom - itemViewTop - upVoteIcon.getHeight()) / 2,
                            null);
                }
            } else if (dX < 0) {
                // left swipe
                paint.setColor(downVoteColor);
                if (dX > -firstThreshold) {
                    paint.setAlpha((int) (255 * (-dX / firstThreshold))); // left swipe cancel threshold of 20%
                    c.drawRect(itemViewRight + dX, itemViewTop, itemViewRight, itemViewBottom, paint);
                } else if (dX < -firstThreshold && dX > -secondThreshold) {
                    c.drawRect(itemViewRight + dX, itemViewTop, itemViewRight, itemViewBottom, paint);
                    c.drawBitmap(downVoteIcon,
                            itemViewRight - itemViewWidth * 0.1f - downVoteIcon.getWidth(),
                            itemViewTop + (itemViewBottom - itemViewTop - downVoteIcon.getHeight()) / 2,
                            null);
                } else if (dX <= -secondThreshold) {
                    paint.setColor(optionsColor); // swiped left more than 60%
                    c.drawRect(itemViewRight + dX, itemViewTop, itemViewRight, itemViewBottom, paint);
                }
            } else {// when view is back to og position
                switch (performActionIndex) {
                    case -2:
                        swipeActionCallback.performLeftSwipeAction(viewHolder.getAdapterPosition());
                        break;
                    case -1:
                        swipeActionCallback.performSlightLeftSwipeAction(viewHolder.getAdapterPosition());
                        break;
                    case 1:
                        swipeActionCallback.performSlightRightSwipeAction(viewHolder.getAdapterPosition());
                        break;
                    case 2:
                        swipeActionCallback.performRightSwipeAction(viewHolder.getAdapterPosition());
                        break;
                }
                if (performActionIndex != 0) {
                    performActionIndex = 0;
                }
            }
            setTouchListener(c, recyclerView, dX);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListener(@NotNull Canvas c, @NotNull RecyclerView recyclerView, float dX) {
        recyclerView.setOnTouchListener((v, event) -> {
            swipeBack = event.getAction() == MotionEvent.ACTION_UP;
            if (swipeBack) {
                float firstThreshold = c.getWidth() * 0.2f;
                float secondThreshold = c.getWidth() * 0.6f;

                if (dX < 0) { // swipe left
                    if (dX > -firstThreshold)
                        return false; // left swipe action cancel threshold is 20%
                    if (dX > -secondThreshold) { // left swipe less than 60%
                        performActionIndex = -1;
                    } else { // left swipe more than 50%
                        performActionIndex = -2;
                    }
                } else { // swipe right
                    if (dX < firstThreshold)
                        return false; // right swipe action cancel threshold is 20%
                    if (dX > firstThreshold && dX < secondThreshold) { // right swipe less than 60%
                        performActionIndex = 1;
                    } else { // right swipe more than 60%
                        performActionIndex = 2;
                    }
                }
            }
            return false;
        });
    }


    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    }

    public interface swipeActionCallback {
        void performSlightLeftSwipeAction(int adapterPosition);

        void performSlightRightSwipeAction(int adapterPosition);

        void performLeftSwipeAction(int adapterPosition);

        void performRightSwipeAction(int adapterPosition);
    }

}
