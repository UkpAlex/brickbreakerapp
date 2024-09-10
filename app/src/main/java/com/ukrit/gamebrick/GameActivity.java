package com.ukrit.gamebrick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the GameSurface
        GameSurface gameSurface = new GameSurface(this);
        setContentView(gameSurface);
    }

    class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

        private GameThread gameThread;
        private float ballX, ballY, ballSpeedX, ballSpeedY;
        private float paddleX, paddleWidth;

        public GameSurface(Context context) {
            super(context);

            // Get holder and attach a callback
            getHolder().addCallback(this);

            // Initialize game objects
            ballX = 100;
            ballY = 100;
            ballSpeedX = 5;
            ballSpeedY = 5;
            paddleWidth = 200;
            paddleX = getWidth() / 2 - paddleWidth / 2;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            gameThread = new GameThread(getHolder());
            gameThread.setRunning(true);
            gameThread.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            boolean retry = true;
            while (retry) {
                try {
                    gameThread.setRunning(false);
                    gameThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            paddleX = event.getX() - paddleWidth / 2;
            return true;
        }

        class GameThread extends Thread {
            private SurfaceHolder surfaceHolder;
            private boolean running = false;
            private Paint paint;

            public GameThread(SurfaceHolder holder) {
                surfaceHolder = holder;
                paint = new Paint();
            }

            public void setRunning(boolean running) {
                this.running = running;
            }

            @Override
            public void run() {
                while (running) {
                    Canvas canvas = null;
                    try {
                        canvas = surfaceHolder.lockCanvas();
                        synchronized (surfaceHolder) {
                            updateGame();
                            drawGame(canvas);
                        }
                    } finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            }

            private void updateGame() {
                // Update ball position
                ballX += ballSpeedX;
                ballY += ballSpeedY;

                // Check for collision with the edges of the screen
                if (ballX <= 0 || ballX >= getWidth() - 20) {
                    ballSpeedX = -ballSpeedX;
                }
                if (ballY <= 0 || ballY >= getHeight() - 20) {
                    ballSpeedY = -ballSpeedY;
                }

                // Check for collision with the paddle
                if (ballY >= getHeight() - 40 && ballX >= paddleX && ballX <= paddleX + paddleWidth) {
                    ballSpeedY = -ballSpeedY;
                    score += 10;
                }
            }

            private void drawGame(Canvas canvas) {
                // Clear the screen
                canvas.drawColor(Color.parseColor("#FFF8E1"));

                // Draw the ball
                paint.setColor(Color.parseColor("#FFEB3B"));
                canvas.drawCircle(ballX, ballY, 10, paint);

                // Draw the paddle
                paint.setColor(Color.parseColor("#F48FB1"));
                canvas.drawRect(paddleX, getHeight() - 40, paddleX + paddleWidth, getHeight() - 20, paint);

                // Draw the score
                paint.setColor(Color.parseColor("#755B5B"));
                paint.setTextSize(40);
                canvas.drawText("Score: " + score, 50, 50, paint);
            }
        }
    }
}
