package com.example.challengeminijeu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;
    private float centerX, centerY, radius;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    private int x = 0;

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas == null) return;

        canvas.drawColor(ContextCompat.getColor(getContext(), R.color.white));

        radius = getWidth() / 4.5f;
        centerX = getWidth() / 2f;
        centerY = -radius;

        drawRoulette(canvas);
        drawIndicatorTriangle(canvas);
        drawSideCircles(canvas);
    }

    private void drawRoulette(Canvas canvas) {
        int[] rouletteColors = {
                ContextCompat.getColor(getContext(), R.color.red),
                ContextCompat.getColor(getContext(), R.color.blue),
                ContextCompat.getColor(getContext(), R.color.green),
                ContextCompat.getColor(getContext(), R.color.yellow)
        };

        float startAngle = 0;
        RectF rect = new RectF(centerX - radius, centerY, centerX + radius, centerY + 2 * radius);

        Paint fillPaint = createPaint(Paint.Style.FILL, true, 0);

        for (int color : rouletteColors) {
            fillPaint.setColor(color);
            canvas.drawArc(rect, startAngle, 180f / rouletteColors.length, true, fillPaint);
            startAngle += 180f / rouletteColors.length;
        }

        Paint borderPaint = createPaint(Paint.Style.STROKE, true, 8);
        borderPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
        canvas.drawArc(rect, 0, 180, true, borderPaint);

        Paint linePaint = createPaint(Paint.Style.STROKE, false, 5);
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.dark_grey));
        canvas.drawLine(centerX - radius, centerY + radius, centerX + radius, centerY + radius, linePaint);
    }

    private void drawSideCircles(Canvas canvas) {
        Paint circlePaint = createPaint(Paint.Style.FILL, true, 0);
        circlePaint.setColor(ContextCompat.getColor(getContext(), R.color.black));

        float smallRadius = radius / 2.5f;
        float spacing = 50;
        float verticalOffset = 40;
        float circleCenterY = centerY + 2 * radius - smallRadius - verticalOffset;

        float leftX = centerX - radius - smallRadius - spacing;
        float rightX = centerX + radius + smallRadius + spacing;

        canvas.drawCircle(leftX, circleCenterY, smallRadius, circlePaint);
        canvas.drawCircle(rightX, circleCenterY, smallRadius, circlePaint);
    }

    private void drawIndicatorTriangle(Canvas canvas) {
        Paint trianglePaint = createPaint(Paint.Style.FILL_AND_STROKE, true, 0);
        trianglePaint.setColor(ContextCompat.getColor(getContext(), R.color.black));

        float triangleWidth = 80;
        float triangleHeight = 60;

        float tipY = centerY + 2 * radius;
        float baseY = tipY + triangleHeight;

        float[] points = {
                centerX - triangleWidth / 2, baseY,
                centerX + triangleWidth / 2, baseY,
                centerX, tipY
        };

        canvas.drawPath(createTrianglePath(points), trianglePaint);
    }

    private Paint createPaint(Paint.Style style, boolean antiAlias, float strokeWidth) {
        Paint paint = new Paint();
        paint.setStyle(style);
        paint.setAntiAlias(antiAlias);
        paint.setStrokeWidth(strokeWidth);
        return paint;
    }

    private Path createTrianglePath(float[] points) {
        Path path = new Path();
        path.moveTo(points[0], points[1]);
        path.lineTo(points[2], points[3]);
        path.lineTo(points[4], points[5]);
        path.close();
        return path;
    }

    public void update() {
        x = (x + 1) % 300;
    }
}
