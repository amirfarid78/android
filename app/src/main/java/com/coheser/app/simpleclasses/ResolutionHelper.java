package com.coheser.app.simpleclasses;

public class ResolutionHelper {

    private double height;
    private double width;

    public ResolutionHelper() {
    }

    public ResolutionHelper(double height, double width) {
        this.height = height;
        this.width = width;
    }

    public double calculateRatio() {
        return height / width;
    }

    public double calculateWidthFromRatio(double givenHeight, double ratio) {
        return givenHeight / ratio;
    }

    public double calculateHeightFromRatio(double givenWidth, double ratio) {
        return givenWidth * ratio;
    }
}