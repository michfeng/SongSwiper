package com.codepath.michfeng.songswiper.connectors;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;

public abstract class DoubleClickListener implements View.OnClickListener {
    /*private static final long DOUBLE_CLICK_TIME_DELTA = 500;//milliseconds
    long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();
        Log.i(TAG, "" + (clickTime - lastClickTime));
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
            onDoubleClick(v);
            lastClickTime = 0;
        } else {
            onSingleClick(v);
        }
        lastClickTime = clickTime;
    }*/

    private static final String TAG = "DoubleClickListener";
    private static final long DEFAULT_QUALIFICATION_SPAN = 5000;
    private long doubleClickQualificationSpanInMillis;
    private long timestampLastClick;

    public DoubleClickListener() {
        doubleClickQualificationSpanInMillis = DEFAULT_QUALIFICATION_SPAN;
        timestampLastClick = 0;
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "" + (SystemClock.elapsedRealtime() - timestampLastClick));
        if ((SystemClock.elapsedRealtime() - timestampLastClick) < doubleClickQualificationSpanInMillis) {
            onDoubleClick(v);
        } else {
            onSingleClick(v);
        }
        timestampLastClick = SystemClock.elapsedRealtime();
    }


    public abstract void onSingleClick(View v);
    public abstract void onDoubleClick(View v);
}
