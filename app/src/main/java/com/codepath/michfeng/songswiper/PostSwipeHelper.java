package com.codepath.michfeng.songswiper;

import static android.graphics.PorterDuff.Mode.CLEAR;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class PostSwipeHelper extends ItemTouchHelper.SimpleCallback {

    private final int intrinsicWidth;
    private final int intrinsicHeight;
    private final int swipeLeftColor;
    private final int swipeRightColor;
    private final Paint clearPaint;
    private final Drawable swipeRightIcon;
    private final Drawable swipeLeftIcon;
    private final ColorDrawable background = new ColorDrawable();

    public PostSwipeHelper(@ColorInt int swipeRightColor, @ColorInt int swipeLeftColor,
                          @DrawableRes int swipeRightIconResource, @DrawableRes int swipeLeftIconResource, Context context) {

        super(0, LEFT|RIGHT);
        this.swipeLeftColor = swipeLeftColor;
        this.swipeRightColor = swipeRightColor;

        this.swipeRightIcon = ContextCompat.getDrawable(context, swipeRightIconResource);
        this.swipeLeftIcon =  ContextCompat.getDrawable(context, swipeLeftIconResource);

        // Called if swiping is cancelled by user.
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(CLEAR));

        /* intrinsicHeight = swipeRightIcon.getIntrinsicHeight();
        intrinsicWidth = swipeRightIcon.getIntrinsicWidth();*/
        intrinsicHeight = 200;
        intrinsicWidth = 200;
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        // Gets item height, check if cancelled.
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();
        boolean isCanceled = (dX == 0f) && !isCurrentlyActive;
        if (isCanceled) {
            clearCanvas(c, itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false);
            return;
        }

        // Swipe left.
        if (dX < 0) {
            background.setColor(swipeLeftColor);
            background.setBounds((int) (itemView.getRight() + dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
            background.draw(c);
            int itemTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int itemMargin = (itemHeight - intrinsicHeight) / 2;
            int itemLeft = itemView.getRight() - itemMargin - intrinsicWidth;
            int itemRight = itemView.getRight() - itemMargin;
            int itemBottom = itemTop + intrinsicHeight;

            swipeLeftIcon.setBounds(itemLeft, itemTop, itemRight, itemBottom);
            swipeLeftIcon.draw(c);
        } else { // Swipe right.
            background.setColor(swipeRightColor);
            background.setBounds((int) (itemView.getLeft() + dX), itemView.getTop(), itemView.getLeft(), itemView.getBottom());
            background.draw(c);
            int itemTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int itemMargin = (itemHeight - intrinsicHeight) / 2;
            int itemLeft = itemView.getLeft() + itemMargin;
            int itemRight = itemView.getLeft() + itemMargin + intrinsicWidth;
            int itemBottom = itemTop + intrinsicHeight;

            swipeRightIcon.setBounds(itemLeft, itemTop, itemRight, itemBottom);
            swipeRightIcon.draw(c);
        }
    }

    private void clearCanvas(Canvas c, float left, float top, float right, float bottom) {
        if(c != null) c.drawRect(left, top, right, bottom, clearPaint);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }
}
