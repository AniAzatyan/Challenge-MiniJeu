package com.example.challengeminijeu.models;

public class Button {
    private int finger;
    private int main;
    private int col;
    private int row;
    private int color;
    private boolean pressed;

    public Button(int col, int row, int color) {
        this.col = col;
        this.row = row;
        this.color = color;
        pressed = false;
    }

    public int getColor() {
        return color;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void press() {
        pressed = true;
    }

    public void release() {
        pressed = false;
    }

    public int getMain() {
        return main;
    }

    public void setMain(int main) {
        this.main = main;
    }

    public int getFinger() {
        return finger;
    }

    public void setFinger(int finger) {
        this.finger = finger;
    }
}