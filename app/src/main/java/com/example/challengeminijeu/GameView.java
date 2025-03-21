package com.example.challengeminijeu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.media.MediaPlayer;


import androidx.core.content.ContextCompat;

import java.util.Random;

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

    private final int[] rouletteColors;
    private float rouletteRotation = 0;
    private boolean isSpinning = false;
    private float spinSpeed = 0;

    private final int RED;
    private final int GREEN;
    private final int BLUE;
    private final int YELLOW;

    private AudioRecord audioRecord;
    private boolean isListening = false;
    private static final int SAMPLE_RATE = 8000;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    private MediaPlayer mediaPlayer;


    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        paint = new Paint();

        RED = ContextCompat.getColor(getContext(), R.color.red);
        GREEN = ContextCompat.getColor(getContext(), R.color.green);
        BLUE = ContextCompat.getColor(getContext(), R.color.blue);
        YELLOW = ContextCompat.getColor(getContext(), R.color.yellow);

        colors = new int[][]{
                {RED, RED, RED, RED},
                {GREEN, GREEN, GREEN, GREEN},
                {BLUE, BLUE, BLUE, BLUE},
                {YELLOW, YELLOW, YELLOW, YELLOW}
        };

        rouletteColors = new int[]{RED, BLUE, GREEN, YELLOW, RED, BLUE, GREEN, YELLOW};
        mediaPlayer = MediaPlayer.create(context, R.raw.wheel);
        mediaPlayer.setLooping(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
        startMicListening();
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
                stopMicListening();
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
            canvas.save();
            canvas.rotate(rouletteRotation, centerWheelX, centerWheelY + radiusWheel);
            drawRoulette(canvas);
            canvas.restore();
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
        float startAngle = 0;
        RectF rect = new RectF(centerWheelX - radiusWheel, centerWheelY, centerWheelX + radiusWheel, centerWheelY + 2 * radiusWheel);

        Paint fillPaint = createPaint(Paint.Style.FILL, true, 0);

        for (int color : rouletteColors) {
            fillPaint.setColor(color);
            canvas.drawArc(rect, startAngle, 360f / rouletteColors.length, true, fillPaint);
            startAngle += 360f / rouletteColors.length;
        }

        Paint borderPaint = createPaint(Paint.Style.STROKE, true, 8);
        borderPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
        canvas.drawArc(rect, 0, 360, true, borderPaint);
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

    private void startMicListening() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.w("GameView", "Permission micro non accordée !");
            return;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isListening = true;
        audioRecord.startRecording();

        new Thread(() -> {
            short[] buffer = new short[BUFFER_SIZE];
            while (isListening) {
                int read = audioRecord.read(buffer, 0, BUFFER_SIZE);
                if (read > 0) {
                    double sum = 0;
                    for (int i = 0; i < read; i++) {
                        sum += buffer[i] * buffer[i];
                    }
                    double amplitude = Math.sqrt(sum / read);

                    if (amplitude > 4000) {
                        float addedSpeed = (float) (amplitude / 1500);
                        spinSpeed += addedSpeed;

                        if (!isSpinning) {
                            isSpinning = true;

                            if (!mediaPlayer.isPlaying()) {
                                mediaPlayer.start();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void stopMicListening() {
        isListening = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void update() {
        if (isSpinning) {
            rouletteRotation += spinSpeed;

            spinSpeed *= 0.985f;
            float volume = Math.min(spinSpeed / 10f, 1f);
            mediaPlayer.setVolume(volume, volume);
            if (spinSpeed < 1f) {
                isSpinning = false;
                spinSpeed = 0;
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(0);
                }


                rouletteRotation = rouletteRotation % 360;

                float arrowAngle = (270 - rouletteRotation + 360) % 360;
                int sector = (int) (arrowAngle / (360f / rouletteColors.length));

                int color = rouletteColors[sector];
                String colorName = "Unknown";
                if (color == RED) colorName = "RED";
                else if (color == BLUE) colorName = "BLUE";
                else if (color == GREEN) colorName = "GREEN";
                else if (color == YELLOW) colorName = "YELLOW";

                Log.d("GameView", "La roulette s'est arrêtée sur : " + colorName);
            }
        }

    }

}
