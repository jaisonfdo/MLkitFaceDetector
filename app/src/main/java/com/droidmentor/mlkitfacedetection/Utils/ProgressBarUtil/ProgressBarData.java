package com.droidmentor.mlkitfacedetection.Utils.ProgressBarUtil;

import android.graphics.Color;

/**
 * Created by Jaison.
 */
public class ProgressBarData
{
    int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#FFFFFF");
    int DEFAULT_TINT_COLOR = Color.parseColor("#2D9CDB");
    int DEFAULT_TEXT_COLOR = Color.parseColor("#2D9CDB");
    boolean isCancelable;
    int backgroundViewColor;
    int progressbarTintColor;
    int progressMessageColor;
    String progressMessage;

    public ProgressBarData() {
    }

    public ProgressBarData(boolean isCancelable, int backgroundViewColor, int progressbarTintColor, int progressMessageColor, String progressMessage) {
        this.isCancelable = isCancelable;
        this.backgroundViewColor = backgroundViewColor;
        this.progressbarTintColor = progressbarTintColor;
        this.progressMessageColor = progressMessageColor;
        this.progressMessage = progressMessage;
    }

    public int getBackgroundViewColor() {
        return backgroundViewColor != 0 ? backgroundViewColor : DEFAULT_BACKGROUND_COLOR;
    }

    public void setBackgroundViewColor(int backgroundViewColor) {
        this.backgroundViewColor = backgroundViewColor;
    }

    public int getProgressbarTintColor() {

        return progressbarTintColor != 0 ? progressbarTintColor : DEFAULT_TINT_COLOR;
    }

    public void setProgressbarTintColor(int progressbarTintColor) {
        this.progressbarTintColor = progressbarTintColor;
    }

    public int getProgressMessageColor() {
        return progressMessageColor != 0 ? progressMessageColor : DEFAULT_TEXT_COLOR;
    }

    public void setProgressMessageColor(int progressMessageColor) {
        this.progressMessageColor = progressMessageColor;
    }

    public boolean isCancelable() {
        return isCancelable;
    }

    public void setCancelable(boolean isCancelable) {
        this.isCancelable = isCancelable;
    }

    public String getProgressMessage() {
        return progressMessage;
    }

    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }

    public static class ProgressBarBuilder {
        boolean isCancelable;
        int backgroundViewColor;
        int progressbarTintColor;
        int progressMessageColor;
        String progressMessage;

        public ProgressBarBuilder setBackgroundViewColor(int backgroundViewColor) {
            this.backgroundViewColor = backgroundViewColor;
            return this;
        }

        public ProgressBarBuilder setProgressbarTintColor(int progressbarTintColor) {
            this.progressbarTintColor = progressbarTintColor;
            return this;
        }

        public ProgressBarBuilder setProgressMessageColor(int progressMessageColor) {
            this.progressMessageColor = progressMessageColor;
            return this;
        }

        public ProgressBarBuilder setCancelable(boolean isCancelable) {
            this.isCancelable = isCancelable;
            return this;
        }

        public ProgressBarBuilder setProgressMessage(String progressMessage) {
            this.progressMessage = progressMessage;
            return this;
        }

        public ProgressBarData build() {
            return new ProgressBarData(isCancelable, backgroundViewColor, progressbarTintColor, progressMessageColor, progressMessage);
        }
    }

}
