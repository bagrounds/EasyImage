package org.bagrounds.java.easyimage.math;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.*;

/**
 * EasyVector encapsulates some vector math, which is often useful in easyimage processing techniques.
 * <p/>
 * Created by bagrounds on 12/6/14.
 */
public class EasyVector extends ArrayList<Number> {

    public EasyVector(byte[] array) {
        for (byte a : array) {
            add((double) (a & 0xff));
        }
    }

    public EasyVector(int[] array) {
        for (int a : array) {
            add(a);
        }
    }

    public EasyVector() {
        super();
    }

    private boolean[] mask() {
        if (isEmpty() || size() % 2 == 0) {
            return new boolean[size()];
        }
        double sqrt = sqrt(size());

        if (sqrt - floor(sqrt) > .000000001) {
            return new boolean[size()];
        }

        int side = (int) floor(sqrt);
        boolean[] mask = new boolean[size()];

        int mid = (int) ceil(side / 2.0);
        for (int i = 0; i < side; i++)
            for (int j = 0; j < side; j++) {
                if (i == mid || j == mid) mask[i + j * side] = true;
            }
        return mask;
    }

    private int[] gradientMask() {
        return new int[]{0, -1, 0, -1, 0, 1, 0, 1, 0};
    }

    private int[] cornerMask(int corner) {
        int[] mask = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        switch (corner) {
            case 0:
                mask = new int[]{0, 0, 0,
                        0, 3, -1,
                        0, -1, -1};
                break;
            case 1:
                mask = new int[]{0, 0, 0,
                        -1, 3, 0,
                        -1, -1, 0};
                break;
            case 2:
                mask = new int[]{0, -1, -1,
                        0, 3, -1,
                        0, 0, 0};
                break;
            case 3:
                mask = new int[]{-1, -1, 0,
                        -1, 3, 0,
                        0, 0, 0};
                break;
        }
        return mask;
    }

    public double stat(Stat s) {
        if (size() < 1) return 0;
        if (size() == 1) return get(0).doubleValue();


        double result = 0;
        switch (s) {
            case MEAN:
                result = mean();
                break;
            case MEDIAN:
                result = median();
                break;
            case MODE:
                result = mode();
                break;
            case MAX:
                result = max();
                break;
            case MIN:
                result = min();
                break;
            case RANDOM:
                result = get(new Random().nextInt(size() - 1)).doubleValue();
                break;
            case DILATION:
                result = dilation(255);
                break;
            case GRADIENT:
                result = gradient();
                break;
            case SUM:
                result = sum();
                break;
            case NORM1:
                result = norm(1);
                break;
            case NORM2:
                result = norm(2);
                break;
            case NORMINF:
                result = norm(100);
                break;
            case BRIDGE:
                result = bridge();
                break;
            case MAX_GRADIENT:
                result = maxGradient();
                break;
            case CORNERS:
                result = corners();
                break;
        }
        return result;
    }

    private double dilation(double value) {
        boolean[] mask = mask();
        for (int i = 0; i < size(); i++)
            if (mask[i] && get(i).doubleValue() > 0)
                return value;
        return 0;
    }

    private double bridge() {
        double result = get(4).doubleValue();
        boolean[] b = new boolean[size()];
        for (int i = 0; i < size(); i++) {
            if (get(i).intValue() > 0) b[i] = true;
        }

        for (int i = 0; i < size(); i++)
            if (b[i]) set(i, (int) Math.pow(2, i));

        if (sum() == 8 + 16 + 32 ||
                sum() == 1 + 8 + 16 + 32 ||
                sum() == 1 + 8 + 16 + 32 + 64 ||
                sum() == 8 + 16 + 32 + 64 ||
                sum() == 4 + 8 + 16 + 32 ||
                sum() == 4 + 8 + 16 + 32 + 256 ||
                sum() == 8 + 16 + 32 + 256) {
            result = 0;
        }
        return result;
    }


    private double corners() {
        double result;
        EasyVector corners = new EasyVector();
        int[] mask;

        double value = 0;
        for (int i = 0; i < 4; i++) {
            mask = cornerMask(i);
            for (int j = 0; j < size(); j++) {
                try {
                    value += mask[j] * get(j).doubleValue();
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println(e.getMessage());
                }
            }
            corners.add(Math.abs(value / 3));
            value = 0;
        }

        if (!(corners.max() > 128))
            result = get(4).doubleValue() / 2;
        else result = corners.max();


        return result;
    }

    private double maxGradient() {
        EasyVector gradients = new EasyVector();
        double a;
        double b;

        for (int i = 0; i < 5; i++) {
            a = 0;
            b = 0;
            try {
                a = this.get(i).doubleValue();
            } catch (IndexOutOfBoundsException e) {
            }
            try {
                b = this.get(8 - i).doubleValue();
            } catch (IndexOutOfBoundsException e) {
            }
            gradients.add(Math.abs(a - b));

        }

        return gradients.max();
    }

    private double gradient() {
        int[] mask = gradientMask();

        double value = 0;
        for (int i = 0; i < size(); i++) {
            try {
                value += mask[i] * get(i).doubleValue();
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }
        return value / 3 + 128;
    }

    private double mean() {
        double mean = 0;
        for (Number n : this) {
            mean += n.doubleValue() / size();
        }
        return mean;
    }

    private double median() {
        this.sort();
        return this.get(size() / 2).doubleValue();
    }

    private double mode() {
        this.sort();
        double mode = this.get(0).doubleValue();
        double previous = this.get(0).doubleValue();
        double current;
        int currentStreak = 1;
        int maxStreak = 1;
        for (int i = 1; i < this.size(); i++) {
            current = this.get(i).doubleValue();
            if (current == previous) currentStreak++;
            else {
                if (maxStreak < currentStreak) {
                    maxStreak = currentStreak;
                    mode = current;
                }
                currentStreak = 0;
            }
        }
        if (maxStreak > 1) return mode;
        else return median();
    }

    private double max() {
        this.sort();
        return (this.get(this.size() - 1).doubleValue());
    }

    private double min() {
        this.sort();
        return (this.get(0).doubleValue());
    }

    private double norm(double l) {
        double value = 0;
        abs();
        double max = max();

        for (Number n : this) value += pow(Math.abs(n.doubleValue() / max), l);

        value = pow(value, 1.0 / l) * Math.abs(max);
        return value;
    }

    public double[] toDoubleArray() {
        double[] array = new double[this.size()];
        for (int i = 0; i < this.size(); i++) {
            array[i] = this.get(i).doubleValue();
        }
        return array;
    }

    public void sort() {
        double[] sorted = this.toDoubleArray();
        Arrays.sort(sorted);
        this.clear();
        for (double aSorted : sorted) {
            this.add(aSorted);
        }
    }

    public void abs() {
        for (int i = 0; i < size(); i++) {
            set(i, Math.abs(get(i).doubleValue()));
        }
    }

    public void dividedBy(double scalar) {
        for (int i = 0; i < this.size(); i++) {
            set(i, get(i).doubleValue() / scalar);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EasyVector)) return false;

        return (this.minus((EasyVector) o).sum() == 0.0);
    }

    private double sum() {
        double sum = 0;
        for (Number n : this)
            sum += n.doubleValue();
        return sum;
    }

    public EasyVector minus(EasyVector v) {
        if (v.size() != size()) throw new InvalidParameterException("vector size mismatch");

        EasyVector difference = new EasyVector();

        for (int i = 0; i < size(); i++) {
            difference.add(get(i).doubleValue() - v.get(i).doubleValue());
        }
        return difference;
    }

    public enum Stat {
        MEAN,
        MEDIAN,
        MODE,
        MAX,
        MIN,
        RANDOM,
        DILATION,
        GRADIENT,
        SUM,
        NORM1,
        NORM2,
        NORMINF,
        MAX_GRADIENT,
        CORNERS, BRIDGE

    }

}
