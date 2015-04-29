package image;

import java.lang.IllegalArgumentException;import java.lang.Override;import java.lang.String;import java.util.Arrays;

/**
 * Describes an interval in 1 dimension.
 * <p/>
 * Created by bryan on 12/13/14.
 */
public class Interval {
    public int min, max;

    public Interval() {
        this.min = 0;
        this.max = 0;
    }

    public Interval(int Min, int Max) {
        if (Min > Max) throw new IllegalArgumentException();
        this.min = Min;
        this.max = Max;
    }

    public int length() {
        return (max - min);
    }

    @Override
    public String toString() {
        return Arrays.toString(new int[]{min, max});
    }

    public void shift(int xShift) {
        this.min += xShift;
        this.max += xShift;
    }

    public boolean isAdjacentTo(Interval i) {
        boolean isAdjacent = false;
        if (this.max == i.min - 1 || this.min == i.max + 1) isAdjacent = true;
        return isAdjacent;
    }

    public void shrinkCenter(int shiftAmount) {
        if (shiftAmount * 2 > max - min)
            throw new IllegalArgumentException("can't shrink that much!");

        max -= shiftAmount;
        min += shiftAmount;
    }

}
