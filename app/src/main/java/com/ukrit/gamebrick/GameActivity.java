package com.ukrit.gamebrick;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private int lives = 3;  // Initialize player lives
    private int score = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the GameSurface
        GameSurface gameSurface = new GameSurface(this);
        setContentView(gameSurface);
    }

    private void gameOver() {
        Intent intent = new Intent(GameActivity.this, GameOverActivity.class);
        intent.putExtra("score", score);  // Pass the current score to the GameOverActivity
        startActivity(intent);
        finish();  // End the current game activity
    }

    class Brick {
        private float left, top, right, bottom;
        private boolean isVisible;

        public Brick(float left, float top, float right, float bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.isVisible = true;
        }

        public boolean isVisible() {
            return isVisible;
        }

        public void setVisible(boolean visible) {
            this.isVisible = visible;
        }

        public void draw(Canvas canvas, Paint paint) {
            if (isVisible) {
                paint.setColor(Color.RED);
                canvas.drawRect(left, top, right, bottom, paint);
            }
        }

        public boolean checkCollision(float ballX, float ballY, float ballRadius) {
            return isVisible && ballX + ballRadius >= left && ballX - ballRadius <= right &&
                    ballY + ballRadius >= top && ballY - ballRadius <= bottom;
        }
    }

    class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

        private GameThread gameThread;
        private float ballX, ballY, ballSpeedX, ballSpeedY;
        private float paddleX, paddleWidth, paddleHeight;
        private List<Brick> bricks = new ArrayList<>();  // Use ArrayList only

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
            paddleHeight = 20;
            paddleX = getWidth() / 2 - paddleWidth / 2;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // Initialize bricks after the surface is created and dimensions are available
            int brickRowCount = 5;  // Number of rows of bricks
            int brickColumnCount = 7;  // Number of columns of bricks
            float brickWidth = getWidth() / brickColumnCount;
            float brickHeight = 60;

            bricks.clear();  // Clear any old bricks
            for (int row = 0; row < brickRowCount; row++) {
                for (int col = 0; col < brickColumnCount; col++) {
                    float left = col * brickWidth;
                    float top = row * brickHeight;
                    float right = left + brickWidth;
                    float bottom = top + brickHeight;
                    bricks.add(new Brick(left, top, right, bottom));
                }
            }

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

                // Ball collision with screen borders
                if (ballX <= 0 || ballX >= getWidth()) {
                    ballSpeedX = -ballSpeedX;
                }
                if (ballY <= 0) {
                    ballSpeedY = -ballSpeedY;
                }

                // Check if the ball falls below the paddle
                if (ballY > getHeight()) {
                    lives--;  // Decrease lives
                    if (lives <= 0) {
                        gameOver();  // Game over when lives reach 0
                    } else {
                        resetBall();  // Reset ball for the next life
                    }
                }

                // Ball collision with paddle
                if (ballY >= getHeight() - paddleHeight - 20 && ballX >= paddleX && ballX <= paddleX + paddleWidth) {
                    ballSpeedY = -ballSpeedY;
                }

                // Ball collision detection with bricks
                for (Brick brick : bricks) {
                    if (brick.checkCollision(ballX, ballY, 10)) {
                        ballSpeedY = -ballSpeedY;  // Reverse ball direction
                        brick.setVisible(false);  // Hide the brick
                        score += 50;  // Add score
                        break;
                    }
                }
            }

            private void resetBall() {
                // Place the ball in the center of the paddle
                ballX = paddleX + paddleWidth / 2;  // Center the ball on the paddle
                ballY = getHeight() - paddleHeight - 30;  // Place the ball just above the paddle
                ballSpeedX = 5;
                ballSpeedY = -5;  // Ensure the ball starts moving upwards
                gameThread = new GameThread(getHolder());
                gameThread.setRunning(true);
                gameThread.start();

                resetBall();
            }

            private void drawGame(Canvas canvas) {
                // Clear the screen
                canvas.drawColor(Color.parseColor("#FFF8E1"));

                // Draw the bricks
                for (Brick brick : bricks) {
                    brick.draw(canvas, paint);
                }

                // Draw the ball
                paint.setColor(Color.parseColor("#FFEB3B"));
                canvas.drawCircle(ballX, ballY, 10, paint);

                // Draw the paddle
                paint.setColor(Color.parseColor("#F48FB1"));
                canvas.drawRect(paddleX, getHeight() - 40, paddleX + paddleWidth, getHeight() - 20, paint);

                // Draw lives
                paint.setColor(Color.BLACK);
                paint.setTextSize(40);
                canvas.drawText("Lives: " + lives, 50, 100, paint);

                // Draw the score
                paint.setColor(Color.parseColor("#755B5B"));
                paint.setTextSize(40);
                canvas.drawText("Score: " + score, 50, 50, paint);
            }
        }
    }
}
