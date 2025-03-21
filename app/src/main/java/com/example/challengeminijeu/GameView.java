package com.example.challengeminijeu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread thread;
    private Paint paint;

    private static final int NUM_COLUMNS = 4;
    private static final int NUM_CIRCLES = 4;
    private static final int CIRCLE_RADIUS = 40
            ;
    private int[][] colors;
    private int cellWidth;
    private int canvasHeight;

    private final int RED = Color.RED;
    private final int GREEN = Color.GREEN;
    private final int BLUE = Color.BLUE;
    private final int YELLOW = Color.YELLOW;

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
            canvas.drawColor(Color.WHITE); // Background color
            drawColumns(canvas);
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