package org.bagrounds.java.easyimage;

import org.bagrounds.java.easyimage.geometry.BoundingBox;
import org.bagrounds.java.easyimage.geometry.Interval;
import org.bagrounds.java.easyimage.math.EasyVector;
import org.bagrounds.java.easyimage.processors.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Image object class encapsulating various image processing techniques.
 * I developed this library for use in a personal project. I recently refactored it out of that project into this stand-
 * alone library, so it may be a bit disorganized and incomplete, but it should get better over time.
 *
 * Created by bagrounds on 12/5/14.
 */
public class EasyImage {

    public int width;
    public int height;
    public boolean hasAlphaChannel;
    public boolean isGrayScale;
    public boolean isBW;
    public int pixelLength;
    public byte[] pixelData;

    public IOProcessor ioProcessor = new IOProcessor(this);
    public SearchProcessor searchProcessor = new SearchProcessor(this);
    public MorphologicalProcessor morphologicalProcessor = new MorphologicalProcessor(this);
    public ConnectedComponentProcessor connectedComponentProcessor = new ConnectedComponentProcessor(this);
    public ComparisonProcessor comparisonProcessor = new ComparisonProcessor(this);
    public ColorProcessor colorProcessor = new ColorProcessor(this);
    public StatisticProcessor statisticProcessor = new StatisticProcessor(this);


    public EasyImage() {
        width = 1;
        height = 1;
        hasAlphaChannel = false;
        isGrayScale = false;
        isBW = true;
        pixelData = new byte[]{0};
    }

    public EasyImage(BufferedImage image) {
        boolean dataIsInt = image.getRaster().getDataBuffer() instanceof DataBufferInt;
        width = image.getWidth();
        height = image.getHeight();
        hasAlphaChannel = image.getAlphaRaster() != null;
        pixelData = new byte[width * height * 3];


        if (dataIsInt) {
            DataBufferInt dataBufferInt = (DataBufferInt) image.getRaster().getDataBuffer();
            int[] intData = dataBufferInt.getData();

            int pos = 0;
            for (int i = 0; pos < intData.length; i += 3, pos++) {
                pixelData[i] = (byte) ((intData[pos] >> 16) & 0xFF); //red
                pixelData[i + 1] = (byte) ((intData[pos] >> 8) & 0xFF); //green
                pixelData[i + 2] = (byte) (intData[pos] & 0xFF); //blue
            }
        } else {
            pixelData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        }

        pixelLength = 3;
        image.flush();
    }

    public EasyImage(EasyImage image) {
        width = image.width;
        height = image.height;
        hasAlphaChannel = image.hasAlphaChannel;
        isGrayScale = image.isGrayScale;
        isBW = image.isBW;

        pixelLength = image.pixelLength;
        pixelData = image.pixelData.clone();

    }

    public void addBorder(int thickness) {
        int newWidth = width + thickness * 2;
        int newHeight = height + thickness * 2;
        int newSize = (newWidth) * (newHeight) * pixelLength;

        byte[] newPixelData = new byte[newSize];
        EasyImage newImage = new EasyImage();
        newImage.pixelData = newPixelData;
        newImage.pixelLength = pixelLength;
        newImage.width = newWidth;
        newImage.height = newHeight;

        for (int i = thickness, k = 0; i < width + thickness; i++, k++)
            for (int j = thickness, l = 0; j < height + thickness; j++, l++) {
                newImage.setPixelArray(i, j, this.getPixelArray(k, l));
            }
        pixelData = newImage.pixelData;
        width = newWidth;
        height = newHeight;
    }

    public byte[] getPixelArray(int x, int y) {
        byte[] pixel = new byte[pixelLength];

        int pos = (y * pixelLength * width) + (x * pixelLength);

        for (int i = 0; i < pixelLength; i++) {
            pixel[i] = pixelData[pos];
            pos++;
        }
        return pixel;
    }

    public void setPixelArray(int x, int y, byte[] value) {
        if (value.length == pixelLength) {
            for (int i = 0; i < pixelLength; i++) {
                pixelData[x * pixelLength + y * pixelLength * width + i] = value[i];
            }
        } else System.err.println("pixel length mismatch");
    }

    public EasyImage getSubImage(BoundingBox box) {
        EasyImage subImage = new EasyImage(this);
        subImage.crop(box);
        return subImage;
    }

    public void crop(BoundingBox b) {
        int newWidth = b.xMax - b.xMin;
        int newHeight = b.yMax - b.yMin;
        int newSize = (newWidth) * (newHeight) * pixelLength;
        byte[] cropped = new byte[newSize];

        //System.out.println( b + "area = " + b.area() );
        //System.out.println( "pixel length = " + pixelLength );
        //System.out.println("newWidth = " + newWidth + ", newHeight = " + newHeight + " newSize = " + newSize + " nw*nh = " + newWidth*newHeight );
        //System.out.println("pixeldata length = " + pixelData.length);

        int p1;
        int p2;

        for (int j = b.yMin, l = 0; j < b.yMax; j++, l++) {
            for (int i = b.xMin, k = 0; i < b.xMax; i++, k++) {
                for (int a = 0; a < pixelLength; a++) {
                    p1 = ((k) + l * newWidth) * pixelLength + a;
                    p2 = ((i) + j * width) * pixelLength + a;
                    //System.out.print("(" + k + ", " + l/* + "..." +p1 + "," + p2*/ + ").");
                    cropped[p1] = pixelData[p2];
                }
            }
            //System.out.println();
        }
        this.width = newWidth;
        this.height = newHeight;
        this.pixelData = cropped;
    }

    public void setSubImage(BoundingBox box, EasyImage img) {
        int w = box.width();
        int h = box.height();

        for (int i = 0; i < w + box.xMin; i++) {
            for (int j = 0; j <= h + box.yMin; j++) {
                setPixelArray(i + w, j + h, img.getPixelArray(i, j));
            }
        }
    }

    public void setSubImage(BoundingBox box, ColorPixel color) {
        int w = box.width();
        int h = box.height();

        for (int i = 0; i < w + box.xMin; i++) {
            for (int j = 0; j <= h + box.yMin; j++) {
                setPixelArray(i + w, j + h, color.byteArrayValue());
            }
        }
    }

    int getPixel(int x, int y) {
        int pos = (y * pixelLength * width) + (x * pixelLength);

        int argb = -16777216; // 255 alpha

        int p = pixelLength;

        if (hasAlphaChannel && !isGrayScale && !isBW) {
            argb = (((int) pixelData[pos++] & 0xff) << 24); // alpha
            p = 3;
        }

        for (int i = 0; i < p; i++) {
            argb += (((int) pixelData[pos++] & 0xff) << i * 8);
        }
        return argb;
    }

    public void addNoise(int spacing) {
        byte[] white = new byte[pixelLength];

        for (int i = 0; i < pixelLength; i++)
            white[i] = (byte) 255;

        for (int i = 0; i < width; i += spacing)
            for (int j = 0; j < height; j += spacing) {
                setPixelArray(i, j, white);
            }
    }

    public void keepPixelsWithValues(byte[] values) {
        boolean pixelIsValue = false;

        for (int i = 0; i < pixelData.length; i++) {
            if (pixelData[i] != 0) {
                for (byte value : values)
                    if (pixelData[i] == value) {
                        pixelIsValue = true;
                        break;
                    }
                if (!pixelIsValue) pixelData[i] = (byte) 0;
                pixelIsValue = false;
            }
        }
    }

    public void logicalAnd(EasyImage img) {
        if (img.width * img.height != width * height) throw new InvalidParameterException();

        byte[] black = new byte[pixelLength];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                if (img.isColor(img.getPixelArray(i, j), (byte) 0)) this.setPixelArray(i, j, black);
    }

    public boolean isColor(byte[] pixel, byte value) {
        boolean isColor = true;
        for (byte b : pixel) if (b != value) isColor = false;
        return isColor;
    }

    public void printImage() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++)
                System.out.print(Arrays.toString(getPixelArray(i, j)) + "\t");
            System.out.println();
        }
    }

    public void histNormalize() {
        //if (!isGrayScale) throw new InvalidParameterException();

        double max = max();
        double min = min();
        max = max - min;
        for (int i = 0; i < pixelData.length; i++) {
            double value = pixelData[i] & 0xff;
            pixelData[i] = (byte) Math.floor((value - min) * 255 / max);
        }
    }

    public int min() {
        return statisticProcessor.min();
    }

    public int max() {
        return statisticProcessor.max();
    }

    public void decimate(int factor) {
        byte[] newPixelData = new byte[pixelData.length / factor];
        EasyImage newImage = new EasyImage();
        newImage.pixelData = newPixelData;
        newImage.pixelLength = pixelLength;
        newImage.width = width / factor;
        newImage.height = height / factor;

        for (int i = 0; i < width / factor; i++) {
            for (int j = 0; j < height / factor; j++) {
                newImage.setPixelArray(i, j, getPixelArray(i * factor, j * factor));
            }
        }
        width /= factor;
        height /= factor;
        pixelData = newImage.pixelData;
    }

    public void displayImage(String title) {
        BufferedImage img = getBufferedImage();
        ImageIcon icon = new ImageIcon(img);
        JFrame frame = new JFrame();
        frame.setTitle(title);
        frame.setLayout(new FlowLayout());
        frame.setSize(width, height);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.pack();
        frame.setVisible(true);
    }

    public BufferedImage getBufferedImage() {
        int w = this.width;
        int h = this.height;
        BufferedImage bufferedImage;

        if (isGrayScale) {
            bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            bufferedImage.getRaster().setPixels(0, 0, w, h, getIntRGBRasterArray());
        } else if (isBW) {
            bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
            bufferedImage.getRaster().setPixels(0, 0, w, h, getIntRGBRasterArray());
        } else {
            bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
            bufferedImage.getRaster().setPixels(0, 0, w, h, getIntRGBRasterArray());
        }

        return bufferedImage;
    }

    private int[] getIntRGBRasterArray() {
        int[] raster;
        if (isBW) {
            System.out.println("length = " + pixelData.length);
            raster = new int[pixelData.length * 3];

            for (int i = 0, j = 0; i < pixelData.length; i++) {
                raster[j++] = pixelData[i] & 0xff;
                raster[j++] = pixelData[i] & 0xff;
                raster[j++] = pixelData[i] & 0xff;
            }
        } else {
            raster = new int[pixelData.length];

            for (int i = 0; i < pixelData.length; i++) {
                raster[i] = pixelData[i] & 0xff;
            }
        }
        return raster;
    }

    public LinkedList<Interval> getHorizontalGaps(int gapSize) {

        LinkedList<Interval> intervals = new LinkedList<Interval>();
        LinkedList<Interval> largeEnoughIntervals = new LinkedList<Interval>();

        for (int i = 0; i < width; i++) if (getColumn(i).stat(EasyVector.Stat.SUM) == 0) intervals.add(new Interval(i, i));

        for (int i = 0; i < intervals.size() - 1; i++)
            if (intervals.get(i).isAdjacentTo(intervals.get(i + 1)))
                intervals.set(i + 1, new Interval(intervals.get(i).min, intervals.get(i + 1).max));


        for (Interval i : intervals)
            if (i.length() >= gapSize) largeEnoughIntervals.add(i);


        return largeEnoughIntervals;
    }

    // comparison processing

    public EasyVector getColumn(int col) {
        EasyVector vector = new EasyVector();
        if (col < 0 || col > width - 1) throw new IllegalArgumentException("coloumn out of easyimage bounds");
        if (!isBW && !isGrayScale) throw new IllegalArgumentException("unsupported for color images");

        for (int j = 0; j < height; j++) {
            vector.add(getPixelArray(col, j)[0] & 0xff);
        }
        return vector;
    }

    public void flipVertical() {
        byte[] temp;

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height / 2; j++) {
                temp = getPixelArray(i, j);
                setPixelArray(i, j, getPixelArray(width - 1 - i, height - 1 - j));
                setPixelArray(width - 1 - i, height - 1 - j, temp);
            }
    }

    public boolean equals(EasyImage obj) {
        return Arrays.equals(pixelData, obj.pixelData);
    }
}

