package com.example.demo2;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class HelloController {

    @FXML private Canvas canvas;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button addBallButton;
    @FXML private Button resetButton;
    @FXML private Label ballCountLabel;

    private final List<Ball> balls = new ArrayList<>();
    private AnimationTimer timer;
    private boolean running = false;

    private Image ballImage;
    private Image backgroundImage;

    private static final double WALL_RESTITUTION = 1.0; // полная упругость
    private static final double SPEED_LIMIT = 400;      // ограничение скорости

    @FXML
    public void initialize() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Загружаем фон и мяч
        backgroundImage = new Image(getClass().getResourceAsStream("/com/example/demo2/images/field.png"));
        ballImage = new Image(getClass().getResourceAsStream("/com/example/demo2/images/ball2-round.png"));

        // Первый мяч
        balls.add(new Ball(canvas.getWidth() / 2, canvas.getHeight() / 2));

        // Кнопки
        startButton.setOnAction(e -> running = true);
        stopButton.setOnAction(e -> running = false);
        addBallButton.setOnAction(e -> balls.add(new Ball(Math.random() * canvas.getWidth(), 50)));
        resetButton.setOnAction(e -> {
            balls.clear();
            balls.add(new Ball(canvas.getWidth() / 2, canvas.getHeight() / 2));
        });

        // Клик мышкой добавляет новый мяч
        canvas.setOnMouseClicked(e -> balls.add(new Ball(e.getX(), e.getY())));

        // Таймер
        timer = new AnimationTimer() {
            private long last = 0;
            @Override
            public void handle(long now) {
                if (!running) { draw(gc); return; }
                if (last == 0) { last = now; return; }
                double dt = (now - last) / 1_000_000_000.0;
                last = now;

                update(dt);
                draw(gc);
            }
        };

        running = true;
        timer.start();
    }

    private void update(double dt) {
        for (Ball b : balls) {
            // Обновляем координаты
            b.x += b.dx * dt;
            b.y += b.dy * dt;

            // Отскок от стен
            if (b.y + b.radius > canvas.getHeight()) {
                b.y = canvas.getHeight() - b.radius;
                b.dy = -b.dy * WALL_RESTITUTION;
            }
            if (b.y - b.radius < 0) {
                b.y = b.radius;
                b.dy = -b.dy * WALL_RESTITUTION;
            }
            if (b.x - b.radius < 0) {
                b.x = b.radius;
                b.dx = -b.dx * WALL_RESTITUTION;
            }
            if (b.x + b.radius > canvas.getWidth()) {
                b.x = canvas.getWidth() - b.radius;
                b.dx = -b.dx * WALL_RESTITUTION;
            }

            // Ограничение скорости, чтобы не улетали слишком быстро
            if (b.dx > SPEED_LIMIT) b.dx = SPEED_LIMIT;
            if (b.dx < -SPEED_LIMIT) b.dx = -SPEED_LIMIT;
            if (b.dy > SPEED_LIMIT) b.dy = SPEED_LIMIT;
            if (b.dy < -SPEED_LIMIT) b.dy = -SPEED_LIMIT;
        }
    }

    private void draw(GraphicsContext gc) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(backgroundImage, 0, 0, canvas.getWidth(), canvas.getHeight());

        if (ballCountLabel != null) {
            ballCountLabel.setText("Мячей: " + balls.size());
        }

        for (Ball b : balls) {
            gc.setGlobalAlpha(0.3);
            gc.setFill(Color.hsb((b.x + b.y) % 360, 0.8, 1.0));
            gc.fillOval(b.x - b.radius * 1.3, b.y - b.radius * 1.3, b.radius * 2.6, b.radius * 2.6);
            gc.setGlobalAlpha(1.0);

            gc.drawImage(ballImage, b.x - b.radius, b.y - b.radius, b.radius * 2, b.radius * 2);
        }
    }

    private static class Ball {
        double x, y;
        double dx, dy;
        double radius = 25;

        Ball(double x, double y) {
            this.x = x;
            this.y = y;
            // случайная начальная скорость
            this.dx = (Math.random() - 0.5) * 400;
            this.dy = (Math.random() - 0.5) * 400;
        }
    }
}
