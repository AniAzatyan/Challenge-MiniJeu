package com.example.challengeminijeu.models;

public class Button {
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
}