package com.example.coupledaysinlove.dialogs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

public class TouchCropImageView extends AppCompatImageView implements View.OnTouchListener {

    private final Matrix matrix = new Matrix();
    private final Matrix savedMatrix = new Matrix();

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private final PointF start = new PointF();
    private final PointF mid = new PointF();

    private final float[] m = new float[9];
    private float minScale = 1f;
    private static final float MAX_SCALE = 5f;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private Bitmap originalBitmap;

    public TouchCropImageView(Context context) {
        super(context);
        init(context);
    }

    public TouchCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TouchCropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(this);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener());
    }

    public void setBitmap(Bitmap bitmap) {
        this.originalBitmap = bitmap;
        setImageBitmap(bitmap);
        post(this::fitImageToCenter);
    }

    public void rotateImage90() {
        if (originalBitmap == null) return;
        Matrix rotMatrix = new Matrix();
        rotMatrix.postRotate(-90); // Rotates 90 degrees right to left
        Bitmap rotated = Bitmap.createBitmap(
                originalBitmap, 0, 0,
                originalBitmap.getWidth(), originalBitmap.getHeight(),
                rotMatrix, true
        );
        setBitmap(rotated);
    }

    private void fitImageToCenter() {
        if (originalBitmap == null || getWidth() == 0 || getHeight() == 0) return;

        int viewW = getWidth();
        int viewH = getHeight();
        int bmpW = originalBitmap.getWidth();
        int bmpH = originalBitmap.getHeight();

        float scale;
        if (bmpW * viewH > bmpH * viewW) {
            scale = (float) viewH / (float) bmpH;
        } else {
            scale = (float) viewW / (float) bmpW;
        }

        minScale = scale;

        matrix.reset();
        matrix.postScale(scale, scale);
        float redundantYSpace = (float) viewH - (scale * (float) bmpH);
        float redundantXSpace = (float) viewW - (scale * (float) bmpW);

        matrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2);
        setImageMatrix(matrix);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        PointF curr = new PointF(event.getX(), event.getY());

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                start.set(event.getX(), event.getY());
                savedMatrix.set(savedMatrix);
                midPoint(mid, event);
                mode = ZOOM;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    float dx = curr.x - start.x;
                    float dy = curr.y - start.y;
                    matrix.set(savedMatrix);
                    matrix.postTranslate(dx, dy);
                    checkBounds();
                }
                break;
        }

        setImageMatrix(matrix);
        return true;
    }

    private void checkBounds() {
        if (originalBitmap == null) return;

        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];
        float scaleX = m[Matrix.MSCALE_X];
        float scaleY = m[Matrix.MSCALE_Y];

        int viewW = getWidth();
        int viewH = getHeight();
        float bmpW = originalBitmap.getWidth() * scaleX;
        float bmpH = originalBitmap.getHeight() * scaleY;

        if (bmpW >= viewW) {
            if (transX > 0) transX = 0;
            if (transX < viewW - bmpW) transX = viewW - bmpW;
        } else {
            transX = (viewW - bmpW) / 2f;
        }

        if (bmpH >= viewH) {
            if (transY > 0) transY = 0;
            if (transY < viewH - bmpH) transY = viewH - bmpH;
        } else {
            transY = (viewH - bmpH) / 2f;
        }

        m[Matrix.MTRANS_X] = transX;
        m[Matrix.MTRANS_Y] = transY;
        matrix.setValues(m);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            matrix.getValues(m);
            float currentScale = m[Matrix.MSCALE_X];

            float targetScale = currentScale * scaleFactor;
            if (targetScale < minScale) {
                scaleFactor = minScale / currentScale;
            } else if (targetScale > MAX_SCALE) {
                scaleFactor = MAX_SCALE / currentScale;
            }

            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            checkBounds();
            return true;
        }
    }

    public Bitmap getCroppedBitmap() {
        if (originalBitmap == null || getWidth() == 0 || getHeight() == 0) return null;

        Matrix inverse = new Matrix();
        matrix.invert(inverse);

        RectF viewRect = new RectF(0, 0, getWidth(), getHeight());
        RectF mappedRect = new RectF();
        inverse.mapRect(mappedRect, viewRect);

        float left = Math.max(0, mappedRect.left);
        float top = Math.max(0, mappedRect.top);
        float right = Math.min(originalBitmap.getWidth(), mappedRect.right);
        float bottom = Math.min(originalBitmap.getHeight(), mappedRect.bottom);

        float width = right - left;
        float height = bottom - top;

        if (width <= 0 || height <= 0) return originalBitmap;

        Bitmap cropped = Bitmap.createBitmap(originalBitmap, (int) left, (int) top, (int) width, (int) height);
        int targetWidth = 800;
        int targetHeight = Math.max(1, (int) (targetWidth * ((float) getHeight() / (float) getWidth())));
        return Bitmap.createScaledBitmap(cropped, targetWidth, targetHeight, true);
    }
}