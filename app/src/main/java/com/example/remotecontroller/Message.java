package com.example.remotecontroller;


public class Message  {
    private int mKey;
    private int mX;
    private int mY;
    private int mButton;
    private boolean mPress;
    private boolean mRelease;

    public Message() {
    }

    public int getmButton() {
        return mButton;
    }

    public void setmButton(int mButton) {
        this.mButton = mButton;
    }

    public boolean ismPress() {
        return mPress;
    }

    public void setmPress(boolean mPress) {
        this.mPress = mPress;
    }

    public boolean ismRelease() {
        return mRelease;
    }

    public void setmRelease(boolean mRelease) {
        this.mRelease = mRelease;
    }

    public int getmKey() {
        return mKey;
    }

    public int getmX() {
        return mX;
    }

    public void setmX(int mX) {
        this.mX = mX;
    }

    public int getmY() {
        return mY;
    }

    public void setmY(int mY) {
        this.mY = mY;
    }

    public void setmKey(int mKey) {
        this.mKey = mKey;
    }

}
