package com.example.challengeminijeu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.ContextCompat;


public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread thread;
    private Paint paint;

    private static final int NUM_COLUMNS = 4;
    private static final int NUM_CIRCLES = 4;
    private static final int CIRCLE_RADIUS = 40;
    private float centerWheelX, centerWheelY, radiusWheel;
    private int[][] colors;
    private int cellWidth;
    private int canvasHeight;

    private final int RED = ContextCompat.getColor(getContext(), R.color.red);
    private final int GREEN = ContextCompat.getColor(getContext(), R.color.green);
    private final int BLUE = ContextCompat.getColor(getContext(), R.color.blue);
    private final int YELLOW = ContextCompat.getColor(getContext(), R.color.yellow);

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        paint = new Paint();

        colors = new int[][]{
                {RED, RED, RED, RED},
                {GREEN, GREEN, GREEN, GREEN},
                {BLUE, BLUE, BLUE, BLUE},
                {YELLOW, YELLOW, YELLOW, YELLOW}
        };
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        cellWidth = width / NUM_COLUMNS;
        canvasHeight = height;
    }

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

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);

            radiusWheel = getWidth() / 4.5f;
            centerWheelX = getWidth() / 2f;
            centerWheelY = -radiusWheel;

            drawColumns(canvas);
            drawRoulette(canvas);
            drawIndicatorTriangle(canvas);
            drawSideCircles(canvas);
        }
    }

    private void drawColumns(Canvas canvas) {
        int boardTop = canvasHeight / 3;
        int boardHeight = canvasHeight * 2 / 3;

        for (int col = 0; col < NUM_COLUMNS; col++) {
            for (int row = 0; row < NUM_CIRCLES; row++) {
                float x = (col * cellWidth) + (cellWidth / 2);
                float y = boardTop + row * (boardHeight / NUM_CIRCLES) + (boardHeight / (NUM_CIRCLES * 2));

                paint.setColor(colors[col][row]);
                canvas.drawCircle(x, y, CIRCLE_RADIUS, paint);
            }
        }
    }

    private void drawRoulette(Canvas canvas) {
        int[] rouletteColors = {
                ContextCompat.getColor(getContext(), R.color.red),
                ContextCompat.getColor(getContext(), R.color.blue),
                ContextCompat.getColor(getContext(), R.color.green),
                ContextCompat.getColor(getContext(), R.color.yellow)
        };

        float startAngle = 0;
        RectF rect = new RectF(centerWheelX - radiusWheel, centerWheelY, centerWheelX + radiusWheel, centerWheelY + 2 * radiusWheel);

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
        canvas.drawLine(centerWheelX - radiusWheel, centerWheelY + radiusWheel, centerWheelX + radiusWheel, centerWheelY + radiusWheel, linePaint);
    }

    private void drawSideCircles(Canvas canvas) {
        Paint circlePaint = createPaint(Paint.Style.FILL, true, 0);
        circlePaint.setColor(ContextCompat.getColor(getContext(), R.color.black));

        float smallRadius = radiusWheel / 2.5f;
        float spacing = 50;
        float verticalOffset = 0;
        float circleCenterY = centerWheelY + 2 * radiusWheel - smallRadius - verticalOffset;

        float leftX = centerWheelX - radiusWheel - smallRadius - spacing;
        float rightX = centerWheelX + radiusWheel + smallRadius + spacing;

        canvas.drawCircle(leftX, circleCenterY, smallRadius, circlePaint);
        canvas.drawCircle(rightX, circleCenterY, smallRadius, circlePaint);
    }

    private void drawIndicatorTriangle(Canvas canvas) {
        Paint trianglePaint = createPaint(Paint.Style.FILL_AND_STROKE, true, 0);
        trianglePaint.setColor(ContextCompat.getColor(getContext(), R.color.black));

        float triangleWidth = 60;
        float triangleHeight = 45;

        float tipY = centerWheelY + 2 * radiusWheel;
        float baseY = tipY + triangleHeight;

        float[] points = {
                centerWheelX - triangleWidth / 2, baseY,
                centerWheelX + triangleWidth / 2, baseY,
                centerWheelX, tipY
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            int col = (int) (touchX / cellWidth);
            int row = (int) (touchY / (canvasHeight / NUM_CIRCLES));

            if (col >= 0 && col < NUM_COLUMNS && row >= 0 && row < NUM_CIRCLES) {
                Log.d("GameView", "Touched color: " + colors[col][row]);

            }
        }
        return true;
    }

    public void update() {

    }

}