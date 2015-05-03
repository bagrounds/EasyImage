package org.bagrounds.java.easyimage.geometry;

import java.util.Arrays;

public class BoundingBox {
    public int xMin, xMax, yMin, yMax;

    public BoundingBox() {
        this.xMin = 0;
        this.xMax = 0;
        this.yMin = 0;
        this.yMax = 0;

    }

    public BoundingBox(int xMin, int xMax, int yMin, int yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        if (xMin > xMax || yMin > yMax) throw new IllegalArgumentException(this.toString());
    }

    @Override
    public String toString() {
        return Arrays.toString(new int[]{xMin, xMax, yMin, yMax});
    }

    public int area() {
        return (xMax - xMin) * (yMax - yMin);
    }

    public int width() {
        return (xMax - xMin);
    }

    public int height() {
        return (yMax - yMin);
    }

    public void shiftRelative(BoundingBox superBox) {
        shift(superBox.xMin, superBox.yMin);
    }

    public void shift(int xShift, int yShift) {
        this.xMin += xShift;
        this.xMax += xShift;
        this.yMin += yShift;
        this.yMax += yShift;
    }

    public void shrinkCenter(int numPixels) {
        if (numPixels * 2 > xMax - xMin || numPixels * 2 > yMax - yMin)
            throw new IllegalArgumentException("can't shrink that much!");

        xMax -= numPixels;
        yMax -= numPixels;
        xMin += numPixels;
        yMin += numPixels;
    }

    public boolean contains(BoundingBox b) {
        boolean thisContainsB = false;

        if (b.xMin >= xMin && b.xMax <= xMax && b.yMin >= yMin && b.yMax <= yMax)
            thisContainsB = true;

        return thisContainsB;
    }
}
