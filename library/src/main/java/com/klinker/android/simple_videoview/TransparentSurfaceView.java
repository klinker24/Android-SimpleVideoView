package com.klinker.android.simple_videoview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class TransparentSurfaceView extends SurfaceView {
    public TransparentSurfaceView(Context context) {
        super(context);
        init();
    }

    public TransparentSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TransparentSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
}
