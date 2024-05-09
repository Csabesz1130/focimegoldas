package com.example.robotfoci;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Ball {
    public Circle circle;
    private double x;
    private double y;
    private double vx;
    private double vy;

    public Ball(double x, double y, double vx, double vy) {
        this.circle = new Circle(5, Color.BLACK);
        this.x = x;
        this.y = y;
        this.vx = 1;  // Set initial X velocity to 1 for testing
        this.vy = 1;  // Set initial Y velocity to 1 for testing
        updateCirclePosition();
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public void updateCirclePosition() {
        System.out.println("Before Update - X: " + x + ", Y: " + y + ", VX: " + vx + ", VY: " + vy);

        if (x <= 50 || x >= 750) {
            vx *= -1;
        }
        if (y <= 50 || y >= 550) {
            vy *= -1;
        }
        x += vx;
        y += vy;

        circle.setCenterX(x);
        circle.setCenterY(y);

        System.out.println("After Update - X: " + x + ", Y: " + y + ", VX: " + vx + ", VY: " + vy);
    }

}
