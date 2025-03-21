package com.example.challengeminijeu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;
import com.example.challengeminijeu.models.Button;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import java.util.Random;
import java.util.Random;


public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread thread;
    private Paint paint;
    private Vibrator vibrator;
    private Button[][] buttons;

    private static final int NUM_COLUMNS = 4;
    private static final int NUM_CIRCLES = 4;
    private static final int CIRCLE_RADIUS = 70;
    private float centerWheelX, centerWheelY, radiusWheel;
    private int cellWidth;
    private int canvasHeight;

    private final int[] rouletteColors;
    private float rouletteRotation = 0;
    private boolean isSpinning = false;
    private float spinSpeed = 0;


    private SoundPool soundPool;
    private int soundReleasedId;
    private AudioRecord audioRecord;
    private boolean isListening = false;
    private static final int SAMPLE_RATE = 8000;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    private int[] currentFingerButton = new int[15];
    private int[][] colors;

    private Bitmap[] leftHandImages;
    private Bitmap[] rightHandImages;
    private Bitmap currentLeftHand;
    private Bitmap currentRightHand;

    private int currentPlayer = 0;
    private int numPlayers = 5;

    private final int RED = ContextCompat.getColor(getContext(), R.color.red);
    private final int GREEN = ContextCompat.getColor(getContext(), R.color.green);
    private final int BLUE = ContextCompat.getColor(getContext(), R.color.blue);
    private final int YELLOW = ContextCompat.getColor(getContext(), R.color.yellow);
    private int fingers;
    private int hands;
    public GameView(Context context,  int fingers, int hands) {
        super(context);
        this.numPlayers = Math.max(1, Math.min(numPlayers, 5));
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        paint = new Paint();


        initializeButtons(context);
        initializeSoundPool(context);
        initializeVibrator(context);

        colors = new int[][]{
                {RED, RED, RED, RED},
                {GREEN, GREEN, GREEN, GREEN},
                {BLUE, BLUE, BLUE, BLUE},
                {YELLOW, YELLOW, YELLOW, YELLOW}
        };
        rouletteColors = new int[]{RED, BLUE, GREEN, YELLOW, RED, BLUE, GREEN, YELLOW};

        loadHandImages();
        changeHandAndFinger();

        this.fingers = fingers;
        this.hands = hands;
    }

    private void loadHandImages() {
        int[] leftHandIds = {
                R.drawable.right_hand_1,
                R.drawable.right_hand_2,
                R.drawable.right_hand_3,
                R.drawable.right_hand_4,
                R.drawable.right_hand_5
        };

        int[] rightHandIds = {
                R.drawable.right_hand_index,
                R.drawable.right_hand_majeur,
                R.drawable.right_hand_pouce
        };

        leftHandImages = new Bitmap[leftHandIds.length];
        rightHandImages = new Bitmap[rightHandIds.length];

        for (int i = 0; i < leftHandIds.length; i++) {
            leftHandImages[i] = getBitmapFromVector(leftHandIds[i]);
        }

        for (int i = 0; i < rightHandIds.length; i++) {
            rightHandImages[i] = getBitmapFromVector(rightHandIds[i]);
        }
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
        soundPool.release();

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
                float y = boardTop + row * (boardHeight / NUM_CIRCLES) + (boardHeight / NUM_CIRCLES / 2);

                paint.setColor(buttons[col][row].getColor());
                if (buttons[col][row].isPressed()) {
                    paint.setAlpha(150);
                } else {
                    paint.setAlpha(255);
                }
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

        //canvas.drawCircle(leftX, circleCenterY, smallRadius, circlePaint);
        //canvas.drawCircle(rightX, circleCenterY, smallRadius, circlePaint);

        if (currentLeftHand != null) {
            canvas.drawBitmap(currentLeftHand, leftX - (currentLeftHand.getWidth() / 2), circleCenterY - (currentLeftHand.getHeight() / 2), null);
        }

        if (currentRightHand != null) {
            canvas.drawBitmap(currentRightHand, rightX - (currentRightHand.getWidth() / 2), circleCenterY - (currentRightHand.getHeight() / 2), null);
        }
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

    public void changeTurn() {
        if (leftHandImages == null || leftHandImages.length == 0 || rightHandImages == null || rightHandImages.length == 0) {
            Log.e("GameView", "Les images ne sont pas chargées !");
            return;
        }

        currentPlayer = (currentPlayer + 1) % numPlayers;
        currentLeftHand = leftHandImages[currentPlayer];

        changeHandAndFinger();

        Log.d("GameView", "Tour du joueur: " + currentPlayer);
    }

    public void changeHandAndFinger() {
        if (leftHandImages != null && leftHandImages.length > 0) {
            currentLeftHand = leftHandImages[currentPlayer];
        }
        if (rightHandImages != null && rightHandImages.length > 0) {
            int rightIndex = new Random().nextInt(rightHandImages.length);
            currentRightHand = rightHandImages[rightIndex];
        }
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
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        float touchX = event.getX(pointerIndex);
        float touchY = event.getY(pointerIndex);
        int col = (int) (touchX / cellWidth);
        int row = (int) ((touchY - (canvasHeight / 3)) / (canvasHeight * 2 / 3 / NUM_CIRCLES));

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (col >= 0 && col < NUM_COLUMNS && row >= 0 && row < NUM_CIRCLES) {
                    if (!buttons[col][row].isPressed()) {
                        buttons[col][row].press();
                        currentFingerButton[event.getPointerId(pointerIndex)] = col * NUM_CIRCLES + row;
                        Log.d("GameView", "Finger " + event.getPointerId(pointerIndex) + " pressed Button [" + col + "][" + row + "]");
                        if (vibrator != null) {
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (col >= 0 && col < NUM_COLUMNS && row >= 0 && row < NUM_CIRCLES) {
                    if (buttons[col][row].isPressed()) {
                        buttons[col][row].release();
                        Log.d("GameView", "Finger " + event.getPointerId(pointerIndex) + " released Button [" + col + "][" + row + "]");
                        soundPool.play(soundReleasedId, 1, 1, 0, 0, 1);
                        if (vibrator != null) {
                            vibrator.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (col >= 0 && col < NUM_COLUMNS && row >= 0 && row < NUM_CIRCLES) {
                    if (!buttons[col][row].isPressed()) {
                        buttons[col][row].press();
                        currentFingerButton[event.getPointerId(pointerIndex)] = col * NUM_CIRCLES + row;
                        Log.d("GameView", "Finger " + event.getPointerId(pointerIndex) + " pressed Button [" + col + "][" + row + "]");
                        if (vibrator != null) {
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)); // Vibrate for 100 ms
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                int pointerId = event.getPointerId(pointerIndex);
                int buttonIndex = currentFingerButton[pointerId];
                if (buttonIndex != 0) {
                    int buttonCol = buttonIndex / NUM_CIRCLES;
                    int buttonRow = buttonIndex % NUM_CIRCLES;
                    if (buttons[buttonCol][buttonRow].isPressed()) {
                        buttons[buttonCol][buttonRow].release();
                        Log.d("GameView", "Finger " + pointerId + " released Button [" + buttonCol + "][" + buttonRow + "]");
                        soundPool.play(soundReleasedId, 1, 1, 0, 0, 1);
                        if (vibrator != null) {
                            vibrator.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE)); // Vibrate for 100 ms
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                if (col >= 0 && col < NUM_COLUMNS && row >= 0 && row < NUM_CIRCLES) {
                    if (buttons[col][row].isPressed()) {
                        buttons[col][row].release();
                        Log.d("GameView", "Finger " + event.getPointerId(pointerIndex) + " canceled Button [" + col + "][" + row + "]");
                    }
                }
                break;

            default:
                break;
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

                    if (amplitude > 3000) {
                        float addedSpeed = (float) (amplitude / 1500);
                        spinSpeed += addedSpeed;

                        if (!isSpinning) {
                            isSpinning = true;
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
    }

    public void update() {
        if (isSpinning) {
            rouletteRotation += spinSpeed;

            spinSpeed *= 0.985f;

            if (spinSpeed < 1f) {
                isSpinning = false;
                spinSpeed = 0;

                rouletteRotation = rouletteRotation % 360;

                float arrowAngle = (270 - rouletteRotation + 360) % 360;
                int sector = (int) (arrowAngle / (360f / rouletteColors.length));

                int color = rouletteColors[sector];
                String colorName = "Unknown";
                if (color == RED) colorName = "RED";
                else if (color == BLUE) colorName = "BLUE";
                else if (color == GREEN) colorName = "GREEN";
                else if (color == YELLOW) colorName = "YELLOW";

                changeTurn();

                Log.d("GameView", "La roulette s'est arrêtée sur : " + colorName);
            }
        }

    }
    private void initializeVibrator(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    private void initializeButtons(Context context) {
        buttons = new Button[NUM_COLUMNS][NUM_CIRCLES];
        int[] colors = {
                ContextCompat.getColor(context, R.color.red),
                ContextCompat.getColor(context, R.color.green),
                ContextCompat.getColor(context, R.color.blue),
                ContextCompat.getColor(context, R.color.yellow)
        };

        for (int col = 0; col < NUM_COLUMNS; col++) {
            for (int row = 0; row < NUM_CIRCLES; row++) {
                buttons[col][row] = new Button(col, row, colors[col]);
            }
        }
    }
    private void initializeSoundPool(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(NUM_COLUMNS * NUM_CIRCLES)
                .setAudioAttributes(audioAttributes)
                .build();

        soundReleasedId = soundPool.load(context, R.raw.fiasco, 1);
    }

    private Bitmap getBitmapFromVector(int resId) {
        Drawable drawable = AppCompatResources.getDrawable(getContext(), resId);
        if (drawable == null) {
            Log.e("GameView", "Drawable non trouvé pour le resId: " + resId);
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}