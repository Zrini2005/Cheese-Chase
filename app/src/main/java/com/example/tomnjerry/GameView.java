package com.example.tomnjerry;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class GameView extends SurfaceView implements Runnable, SensorEventListener {
    private Thread gameThread;
    private boolean isPlaying;
    private Paint paint;
    private SurfaceHolder surfaceHolder;
    private Jerry jerry;
    private Tom tom;

    private long tomAppearTime;
    private boolean tomFollowing;
    private List<Obstacle> obstacles;
    private List<Cheese> cheeses;
    private List<Key> keys;
    private List<Bullet> bullets;
    private Random random;
    private int cheesePoints;
    private boolean hasGun;
    private int score;
    private int highScore;
    private int obstaclesHit;
    private long lastObstacleTime;
    private long lastKeyTime;
    private long lastCheeseTime;
    private long ObstacleTime=2000;
    private long CheeseTime=5000;
    private float initialX;
    private float initialY;
    private int laneWidth;
    private int separatorWidth;
    private int separatorColor = Color.YELLOW;
    private TextView scoreTextView;
    private MediaPlayer mediaPlayer;
    private Bitmap laneImage;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private long lastTapTime = 0;
    private List<Explosion> explosions;
    private Bitmap[] roadSegments;
    private int roadSegment;
    private long lastRoadTime;
    private boolean isPaused = false;
    private int lastScoreIncrement = 0;
    private float speedMultiplier = 1.0f;
    private float sp ;
    private long ob;
    private long ch;

    private int keyPoints;
    private boolean isJerryImmune;
    private long immunityEndTime;
    private SoundPool soundPool;
    private int pows;
    private int swipe;
    private int explo;


    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        surfaceHolder = getHolder();
        paint = new Paint();
        jerry = new Jerry(getContext());
        tom = new Tom(getContext());
        obstacles = new ArrayList<>();
        random = new Random();
        score = 0;
        highScore = 0;
        obstaclesHit = 0;
        lastObstacleTime = System.currentTimeMillis();
        tomAppearTime = 0;
        tomFollowing = false;

        laneWidth = getResources().getDisplayMetrics().widthPixels / 3;
        separatorWidth = 20;
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();
        swipe = soundPool.load(this.getContext(), R.raw.jerryjmp, 1);
        pows = soundPool.load(this.getContext(), R.raw.powerup, 1);

        explo=soundPool.load(this.getContext(), R.raw.explosionsound, 1);

        roadSegments = new Bitmap[4];
        roadSegments[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.road1);
        roadSegments[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.road2);
        roadSegments[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.road3);
        roadSegments[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.road4);
        roadSegments[0] = Bitmap.createScaledBitmap(roadSegments[0], laneWidth, getResources().getDisplayMetrics().heightPixels, false);
        roadSegments[1] = Bitmap.createScaledBitmap(roadSegments[1], laneWidth, getResources().getDisplayMetrics().heightPixels, false);
        roadSegments[2] = Bitmap.createScaledBitmap(roadSegments[2], laneWidth, getResources().getDisplayMetrics().heightPixels, false);
        roadSegments[3] = Bitmap.createScaledBitmap(roadSegments[3], laneWidth, getResources().getDisplayMetrics().heightPixels, false);
        roadSegment=0;


        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
        cheesePoints = 0;
        hasGun = false;
        cheeses = new ArrayList<>();
        keyPoints=0;

        bullets = new ArrayList<>();
        lastCheeseTime = System.currentTimeMillis();
        keys = new ArrayList<>();
        lastKeyTime = System.currentTimeMillis();
        lastRoadTime = System.currentTimeMillis();
        explosions = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        highScore = sharedPreferences.getInt("HighScore", 0);
        SharedPreferences sharedPreferences2 = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE);
        keyPoints = sharedPreferences2.getInt("key_points", 0);
        isJerryImmune = false;
        immunityEndTime = 0;
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            control();
        }
    }
    private void saveHighScore(int highScore) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("HighScore", highScore);
        editor.apply();
    }
    public void makeJerryImmune() {
        isJerryImmune = true;
        immunityEndTime = System.currentTimeMillis() + 10000;
        soundPool.play(pows, 1, 1, 0, 0, 1);

    }
    public void giveJerryCheeseAndGun() {
        cheesePoints += 25;
        hasGun = true;
        jerry.setHasGun(true);
        soundPool.play(pows, 1, 1, 0, 0, 1);
    }

    public void setScoreTextView(TextView scoreTextView) {
        this.scoreTextView = scoreTextView;
    }


    private void update() {
        long currentTime = System.currentTimeMillis();
        if (isJerryImmune && currentTime > immunityEndTime) {
            isJerryImmune = false;
        }

        if (score >= 15 && score / 15 > lastScoreIncrement) {
            lastScoreIncrement = score / 15;
            speedMultiplier += 0.1f;
            ObstacleTime-=100;
            CheeseTime-=100;
        }
        if(currentTime-lastRoadTime>200){
            roadSegment++;
            if(roadSegment==4){
                roadSegment=0;
            }
            lastRoadTime=currentTime;
        }
        if (currentTime - lastKeyTime > 30000) {
            int lane;
            boolean isValidLane = false;
            Key key = null;
            do {
                lane = random.nextInt(3);
                boolean isLaneValid = true;
                for (Obstacle obstacle : obstacles) {
                    if (obstacle.getLane() == lane && Math.abs(obstacle.getY() - 0) < 100) {
                        isLaneValid = false;
                        break;
                    }
                }
                if (isLaneValid) {
                    key = new Key(getContext(), lane, 0);
                    isValidLane = true;
                } else {
                    isValidLane = false;
                }
            } while (!isValidLane);
            keys.add(key);
            lastKeyTime = currentTime;
        }
        if(isJerryImmune && currentTime<immunityEndTime-2000) {
            if (currentTime - lastObstacleTime > 250) {
                int lane = random.nextInt(3);
                Obstacle obstacle = new Obstacle(getContext(), lane, 0);
                obstacles.add(obstacle);
                lastObstacleTime = currentTime;
            }
        } else{
            if (currentTime - lastObstacleTime > ObstacleTime) {
                int lane = random.nextInt(3);
                Obstacle obstacle = new Obstacle(getContext(), lane, 0);
                obstacles.add(obstacle);
                lastObstacleTime = currentTime;
            }
        }
        long currentTime1 = System.currentTimeMillis();
        if(isJerryImmune && currentTime1<immunityEndTime-2000) {
            if (currentTime1 - lastCheeseTime > 1000) {
                int lane;
                boolean isValidLane = false;
                Cheese cheese = null;
                do {
                    lane = random.nextInt(3);
                    boolean isLaneValid = true;
                    for (Obstacle obstacle : obstacles) {
                        if (obstacle.getLane() == lane && Math.abs(obstacle.getY() - 0) < 100) {
                            isLaneValid = false;
                            break;
                        }
                    }

                    if (isLaneValid) {
                        cheese = new Cheese(getContext(), lane, 0);
                        isValidLane = true;
                    } else {
                        isValidLane = false;
                    }
                } while (!isValidLane);
                cheeses.add(cheese);
                lastCheeseTime = currentTime1;
            }
        }else{
            if (currentTime1 - lastCheeseTime > CheeseTime) {
                int lane;
                boolean isValidLane = false;
                Cheese cheese = null;
                do {
                    lane = random.nextInt(3);
                    boolean isLaneValid = true;
                    for (Obstacle obstacle : obstacles) {
                        if (obstacle.getLane() == lane && Math.abs(obstacle.getY() - 0) < 100) {
                            isLaneValid = false;
                            break;
                        }
                    }

                    if (isLaneValid) {
                        cheese = new Cheese(getContext(), lane, 0);
                        isValidLane = true;
                    } else {
                        isValidLane = false;
                    }
                } while (!isValidLane);
                cheeses.add(cheese);
                lastCheeseTime = currentTime1;
            }
        }
        Iterator<Key> keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            Key key = keyIterator.next();
            key.update(speedMultiplier);
            if (key.getY() > 2000) {
                keyIterator.remove();
            } else if (key.isColliding(jerry)) {
                keyPoints++;
                keyIterator.remove();
                SharedPreferences sharedPreferences2 = getContext().getSharedPreferences("game_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor2 = sharedPreferences2.edit();
                editor2.putInt("key_points", keyPoints);
                editor2.apply();
            }
        }

        Iterator<Obstacle> iterator = obstacles.iterator();
        while (iterator.hasNext()) {
            Obstacle obstacle = iterator.next();
            if(isJerryImmune && currentTime<immunityEndTime-2000) {
                obstacle.update(5);
            }else{
                obstacle.update(speedMultiplier);
            }
            if (obstacle.getY() > 2000) {
                iterator.remove();
                score++;
            } else if (obstacle.isColliding(jerry) && !isJerryImmune) {
                obstaclesHit++;
                iterator.remove();
                if (obstaclesHit == 2) {
                    tom.appearBottom(jerry.getX());
                    tomFollowing = false;
                    isPlaying = false;
                    gameOver();
                } else {
                    tomAppearTime = currentTime;
                    tomFollowing = true;
                }
            }else if(obstacle.isColliding(jerry) && isJerryImmune ){
                score++;
                iterator.remove();
            }
        }
        Iterator<Cheese> cheeseIterator = cheeses.iterator();
        while (cheeseIterator.hasNext()) {
            Cheese cheese = cheeseIterator.next();
            if(isJerryImmune && currentTime<immunityEndTime-2000) {
                cheese.update(5);
            }else{
                cheese.update(speedMultiplier);
            }

            if (cheese.getY() > 2000) {
                cheeseIterator.remove();
            } else if (cheese.isColliding(jerry)) {
                cheesePoints++;
                cheeseIterator.remove();
                if (cheesePoints >= 5) {
                    hasGun = true;
                    jerry.setHasGun(true);
                }

            }
        }
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update();

            boolean hit = false;
            Iterator<Obstacle> obstacleItr = obstacles.iterator();
            while (obstacleItr.hasNext()) {
                Obstacle obstacle = obstacleItr.next();
                if (bullet.isColliding(obstacle)) {
                    obstacleItr.remove();
                    bulletIterator.remove();
                    explosions.add(new Explosion(getContext(), obstacle.getX()-120, obstacle.getY()));
                    soundPool.play(explo, 1, 1, 0, 0, 1);
                    hit = true;
                    break;
                }
            }

            if (!hit && bullet.getY() < 0) {
                bulletIterator.remove();
            }
        }
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update();
            if (explosion.isFinished()) {
                explosionIterator.remove();
            }
        }


        if (tomFollowing) {
            if (currentTime - tomAppearTime <= 5000) {
                tom.followJerry(jerry);
            } else {
                tom.retreat();
                tomFollowing = false;
            }
        }

        if (score > highScore) {
            highScore = score;
            saveHighScore(highScore);

        }

        if (scoreTextView != null) {
            post(() -> scoreTextView.setText("Score: " + score + "      Cheese : " + cheesePoints));
        }
    }


    private void draw() {
        if (surfaceHolder != null && surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                for (int i = 0; i < 3; i++) {
                    canvas.drawBitmap(roadSegments[roadSegment], i * laneWidth, 0, paint);
                    if (i < 2) {
                        paint.setColor(separatorColor);
                        canvas.drawRect((i + 1) * laneWidth - separatorWidth, 0, (i + 1) * laneWidth, getHeight(), paint);
                    }
                }

                paint.setColor(Color.BLUE);
                jerry.draw(canvas, paint);

                paint.setColor(Color.BLACK);
                for (Obstacle obstacle : obstacles) {
                    obstacle.draw(canvas, paint);
                }
                for (Cheese cheese : cheeses) {
                    cheese.draw(canvas, paint);
                }
                for (Key key : keys) {
                    key.draw(canvas, paint);
                }

                for (Bullet bullet : bullets) {
                    bullet.draw(canvas, paint);
                }
                for (Explosion explosion : explosions) {
                    explosion.draw(canvas, paint);
                }

                paint.setColor(Color.CYAN);
                tom.draw(canvas, paint);

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void gameOver() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            final Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.game_over_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(false);
            dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
            TextView scoreTextView = dialog.findViewById(R.id.score_text_view);
            TextView highScoreTextView = dialog.findViewById(R.id.high_score_text_view);
            scoreTextView.setText("Your score: " + score);
            highScoreTextView.setText("High score: " + highScore);

            Button playAgainButton = dialog.findViewById(R.id.play_again_button);
            Button exitButton = dialog.findViewById(R.id.exit_button);
            exitButton.setText("Home");
            Button revive=dialog.findViewById(R.id.revive);
            if(cheesePoints>=5){
                revive.setVisibility(View.VISIBLE);
            }
            revive.setOnClickListener(v -> {
                isPlaying=true;
                tom.reset();
                cheesePoints-=5;
                jerry.setHasGun(cheesePoints >= 5);

                tomAppearTime = 0;
                obstaclesHit = 0;
                obstacles.clear();
                cheeses.clear();
                keys.clear();
                gameThread = new Thread(GameView.this);
                gameThread.start();
                dialog.dismiss();

            });

            playAgainButton.setOnClickListener(v -> {

                score = 0;
                cheesePoints=0;
                hasGun=false;


                tomAppearTime = 0;
                obstaclesHit = 0;
                obstacles.clear();
                cheeses.clear();
                keys.clear();
                tom.reset();
                tomFollowing = false;
                isPlaying = true;
                gameThread = new Thread(GameView.this);
                gameThread.start();
                dialog.dismiss();
                if (getContext() instanceof MainActivity) {
                    if(keyPoints>=2) {
                        ((MainActivity) getContext()).showPowerUpButtons();
                    }
                }
            });

            exitButton.setOnClickListener(v -> {

                Intent intent = new Intent((Activity) getContext(), MainActivity2.class);
                ((Activity) getContext()).startActivity(intent);

                dialog.dismiss();
            });

            dialog.show();
        });
    }

    private void control() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTapTime < 400) {
                    if ( cheesePoints>=5 &&  hasGun) {
                        Bullet bullet = new Bullet(getContext(), jerry.getX(), jerry.getY());
                        bullets.add(bullet);
                        cheesePoints-=5;
                        if(cheesePoints<5){
                            hasGun=false;
                            jerry.setHasGun(false);
                        }
                    }
                    break;
                } else {
                    initialX = event.getX();
                    lastTapTime = currentTime;
                }
            case MotionEvent.ACTION_UP:

                float finalX = event.getX();
                if (initialX+50 < finalX) {
                    jerry.moveRight();
                    soundPool.play(swipe, 1, 1, 0, 0, 1);
                } else if (initialX > finalX+50) {
                    jerry.moveLeft();
                    soundPool.play(swipe, 1, 1, 0, 0, 1);
                }
                break;

        }
        return true;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float rotationY = event.values[1];

            if (rotationY > 1f) {
                jerry.moveRight();
                soundPool.play(swipe, 1, 1, 0, 0, 1);
            } else if (rotationY < -1f) {
                jerry.moveLeft();
                soundPool.play(swipe, 1, 1, 0, 0, 1);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void pause() {
        isPlaying = false;
        isPaused = true;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    public void resume() {
        isPlaying = true;
        isPaused = false;
        gameThread = new Thread(this);
        gameThread.start();

        if (sensorManager != null && gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    public boolean isPaused() {
        return isPaused;
    }
}
