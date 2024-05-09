package com.example.robotfoci;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class View extends Pane {
    private Circle player1Circle;
    private Circle player2Circle;
    private Circle ball;
    private PrintWriter outputWriter;

    public View() {
        initializeUI();
        connectToServer();
        startUpdateLoop();
    }

    private void initializeUI() {
        player1Circle = new Circle(500, 300, 10, Color.RED);
        player2Circle = new Circle(300, 300, 10, Color.BLUE);
        ball = new Circle(400, 300, 5, Color.BLACK);
        ball.setTranslateX(2); // Initial horizontal velocity
        ball.setTranslateY(3); // Initial vertical velocity

        getChildren().addAll(player1Circle, player2Circle, ball);

        player1Circle.setOnMouseDragged(this::movePlayer);
        player2Circle.setOnMouseDragged(this::movePlayer);
        startUpdateLoop();
    }

    private void checkCollisionAndKickBall(Circle player) {
        double dx = ball.getCenterX() - player.getCenterX();
        double dy = ball.getCenterY() - player.getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Assuming the radius for the player is 10 and for the ball is 5
        if (distance <= 15) { // Simple sum of radii for collision detection
            double kickStrength = 5; // Modify as needed for desired gameplay
            double angle = Math.atan2(dy, dx);

            // Applying a kick force to the ball
            ball.setTranslateX(kickStrength * Math.cos(angle));
            ball.setTranslateY(kickStrength * Math.sin(angle));
        }
    }

    private void handlePlayerBallCollision() {
        checkCollisionAndKickBall(player1Circle);
        checkCollisionAndKickBall(player2Circle);
    }

    private void movePlayer(MouseEvent event) {
        Circle playerCircle = (Circle) event.getSource();
        double newX = event.getX();
        double newY = event.getY();
        playerCircle.setCenterX(newX);
        playerCircle.setCenterY(newY);
        sendMessageToServer(String.format("MOVE %s %.1f %.1f", getPlayerId(playerCircle), newX, newY));
    }

    private String getPlayerId(Circle playerCircle) {
        return playerCircle == player1Circle ? "player1" : "player2";
    }

    private void sendMessageToServer(String message) {
        if (outputWriter != null) {
            outputWriter.println(message);
            outputWriter.flush();
        }
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            outputWriter = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        processMessage(inputLine);
                    }
                } catch (IOException e) {
                    System.err.println("Error receiving message from server: " + e.getMessage());
                }
            }).start();

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    private void updateBallPosition(double x, double y) {
        Platform.runLater(() -> {
            ball.setCenterX(x);
            ball.setCenterY(y);
            checkBallBoundaries();
            handlePlayerBallCollision();  // Ensure collisions are checked with updated positions
        });
    }

    private void processMessage(String inputLine) {
        String[] parts = inputLine.split(" ");
        switch (parts[0]) {
            case "MOVE":
                updatePlayerPosition(parts[1], Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
                break;
            case "BALL":
                updateBallPosition(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
                break;
        }
    }

    private void checkBallBoundaries() {
        double x = ball.getCenterX();
        double y = ball.getCenterY();

        if (x <= 50 || x >= 750) {
            ball.setTranslateX(ball.getTranslateX() * -1);
        }
        if (y <= 50 || y >= 550) {
            ball.setTranslateY(ball.getTranslateY() * -1);
        }
    }


    public void updatePlayerPosition(String playerId, double x, double y) {
        Platform.runLater(() -> {
            Circle playerCircle = "player1".equals(playerId) ? player1Circle : player2Circle;
            playerCircle.setCenterX(x);
            playerCircle.setCenterY(y);
        });
    }

    private void startUpdateLoop() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateBallPosition();
            }
        };
        timer.start();
    }

    private void updateBallPosition() {
        Platform.runLater(() -> {
            double newX = ball.getCenterX() + ball.getTranslateX();
            double newY = ball.getCenterY() + ball.getTranslateY();

            // Check boundaries to reverse direction or stop
            if (newX <= 50 || newX >= 750) {
                ball.setTranslateX(ball.getTranslateX() * -1);
            }
            if (newY <= 50 || newY >= 550) {
                ball.setTranslateY(ball.getTranslateY() * -1);
            }

            ball.setCenterX(newX);
            ball.setCenterY(newY);
            handlePlayerBallCollision();
        });
    }
}
