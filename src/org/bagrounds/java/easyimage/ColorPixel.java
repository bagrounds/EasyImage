package org.bagrounds.java.easyimage;

import org.bagrounds.java.easyimage.math.EasyVector;

import java.awt.*;
import java.util.Arrays;

/**
 * ColorPixel encapsulates common color processing functionality at the pixel level.
 * <p/>
 * Created by bagrounds on 12/10/14.
 */
public class ColorPixel {
    public static final ColorPixel RED = new ColorPixel(new byte[]{(byte) 255, 0, 0});
    public static final ColorPixel REDYELLOW = new ColorPixel(new byte[]{(byte) 255, (byte) 128, 0});
    public static final ColorPixel YELLOW = new ColorPixel(new byte[]{(byte) 255, (byte) 255, 0});
    public static final ColorPixel YELLOWGREEN = new ColorPixel(new byte[]{(byte) 128, (byte) 255, 0});
    public static final ColorPixel GREEN = new ColorPixel(new byte[]{0, (byte) 255, 0});
    public static final ColorPixel GREENCYAN = new ColorPixel(new byte[]{0, (byte) 255, (byte) 128});
    public static final ColorPixel CYAN = new ColorPixel(new byte[]{0, (byte) 255, (byte) 255});
    public static final ColorPixel CYANBLUE = new ColorPixel(new byte[]{0, (byte) 128, (byte) 255});
    public static final ColorPixel BLUE = new ColorPixel(new byte[]{0, 0, (byte) 255});
    public static final ColorPixel BLUEMAGENTA = new ColorPixel(new byte[]{(byte) 128, 0, (byte) 255});
    public static final ColorPixel MAGENTA = new ColorPixel(new byte[]{(byte) 255, 0, (byte) 255});
    public static final ColorPixel MAGENTARED = new ColorPixel(new byte[]{(byte) 255, 0, (byte) 128});

    public static final ColorPixel BLACK = new ColorPixel(new byte[]{0, 0, 0});
    public static final ColorPixel LIGHTGRAY = new ColorPixel(new byte[]{(byte) 64, (byte) 64, (byte) 64});
    public static final ColorPixel DARKGRAY = new ColorPixel(new byte[]{(byte) 128, (byte) 128, (byte) 128});
    public static final ColorPixel GRAYWHITE = new ColorPixel(new byte[]{(byte) 192, (byte) 192, (byte) 192});
    public static final ColorPixel WHITE = new ColorPixel(new byte[]{(byte) 255, (byte) 255, (byte) 255});

    private int r;
    private int g;
    private int b;


    public ColorPixel(byte[] values) {
        if (values.length != 3) throw new IllegalArgumentException(Arrays.toString(values));

        this.r = values[0] & 0xff;
        this.g = values[1] & 0xff;
        this.b = values[2] & 0xff;
    }

    public ColorPixel(int r, int g, int b) {

        this.r = r & 0xff;
        this.g = g & 0xff;
        this.b = b & 0xff;
    }




    public static byte[] rgbFromHsi(double h, double s, double i) {
        int r;
        int b;
        int g;

        if (h < 120) {
            r = (int) Math.round(i * (1 + s * Math.cos(Math.toRadians(h)) / Math.cos(Math.toRadians(60 - h))));
            b = (int) Math.round(i * (1 - s));
            g = (int) Math.round(3 * i - (r + b));
        } else if (h < 240) {
            h = h - 120;
            r = (int) Math.round(i * (1 - s));
            g = (int) Math.round(i * (1 + s * Math.cos(Math.toRadians(h)) / Math.cos(Math.toRadians(60 - h))));
            b = (int) Math.round(3 * i - (r + g));
        } else {
            h = h - 240;
            g = (int) Math.round(i * (1 - s));
            b = (int) Math.round(i * (1 + s * Math.cos(Math.toRadians(h)) / Math.cos(Math.toRadians(60 - h))));
            r = (int) Math.round(3 * i - (g + b));
        }
        r *= 255;
        g *= 255;
        b *= 255;
        return new byte[]{(byte) r, (byte) g, (byte) b};
    }

    /**
     * returns the euclidean distance in RGB space normalized to be a value between 0 and 1.
     *
     * @param p - another pixel to calculate distance from
     * @return distance between this and p
     */
    public double rgbDist(ColorPixel p) {
        EasyVector v = rgbVector();
        EasyVector u = p.rgbVector();

        v = v.minus(u);
        v.dividedBy(255);

        return v.stat(EasyVector.Stat.NORM2) / Math.sqrt(3);
    }

    public EasyVector rgbVector() {
        return new EasyVector(new int[]{r, g, b});
    }

    public double hueDist(ColorPixel p) {
        double diff = Math.abs(this.getHue() - p.getHue());
        if (diff > 180) diff = 360 - diff;
        return diff;
    }

    public double getHue() {
        return rgbToHue(new byte[]{(byte) this.r, (byte) this.g, (byte) this.b});
    }

    public static double rgbToHue(byte[] rgb) {
        double hue;
        double red = (rgb[0] & 0xff) / 255.0;
        double green = (rgb[1] & 0xff) / 255.0;
        double blue = (rgb[2] & 0xff) / 255.0;

        double n = .5 * ((red - green) + (red - blue));
        double d = Math.sqrt(Math.pow(red - green, 2) + (red - blue) * (green - blue));
        if (d == 0) d = .000001;
        double theta = Math.toDegrees(Math.acos(n / d));

        if (blue <= green) hue = theta;
        else hue = 360 - theta;

        return hue;
    }

    public int intValue() {
        return color().getRGB();
    }

    public Color color() {
        return new Color(r, g, b);
    }

    public byte[] byteArrayValue() {
        byte[] array = new byte[3];
        for (int i = 0; i < 3; i++) {
            array[i] = rgbVector().get(i).byteValue();
        }
        return array;
    }

    public double getSaturation() {
        EasyVector rgb = rgbVector();
        rgb.dividedBy(255);
        double sum = rgb.stat(EasyVector.Stat.SUM);
        if (sum == 0) sum = .0000001;
        return 1.0 - 3.0 * rgb.stat(EasyVector.Stat.MIN) / sum;
    }

    public double getIntensity() {
        return rgbVector().stat(EasyVector.Stat.MEAN) / 255.0;
    }

    @Override
    public String toString() {
        return Arrays.toString(new int[]{r, g, b});
    }
}
