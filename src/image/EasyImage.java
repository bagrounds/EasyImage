package image;

import image.math.EasyVector;
import image.processors.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * EasyImage is an image class that encapsulates a wide array of image processing functionality. The intent is to keep execution as simple as possible.
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

    private MorphologicalProcessor morphologicalProcessor = new MorphologicalProcessor(this);
    private ConnectedComponentProcessor connectedComponentProcessor = new ConnectedComponentProcessor(this);
    private ComparisonProcessor comparisonProcessor = new ComparisonProcessor(this);
    private ColorProcessor colorProcessor = new ColorProcessor(this);
    private StatisticProcessor statisticProcessor = new StatisticProcessor(this);

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

    public static EasyImage loadImage(String fileName) {
        File file = new File(fileName);
        if (!file.exists() || !file.canRead()) {
            System.err.println("cannot read file: " + fileName + "!");
            return new EasyImage();
        }

        EasyImage result = null;
        BufferedImage img = null;
        BufferedImage noAlpha = null;
        try {
            img = ImageIO.read(file);
            int w = img.getWidth();
            int h = img.getHeight();
            noAlpha = new BufferedImage(w, h,
                    BufferedImage.TYPE_3BYTE_BGR);
            Raster raster = img.getRaster().createChild(0, 0, w, h, 0, 0, new
                    int[]{2, 1, 0});
            noAlpha.setData(raster);
            result = new EasyImage(noAlpha);
        } catch (Exception e) {
            System.err.println("Error reading image");
            e.printStackTrace();
            result = new EasyImage();
        } finally {
            if (img != null) img.flush();
            if (noAlpha != null) noAlpha.flush();
        }

        return result;
    }

    public void saveImage(String fileName) {
        try {
            // retrieve image
            File out = new File(fileName);
            ImageIO.write(getBufferedImage(), "png", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getBufferedImage() {
        int w = this.width;
        int h = this.height;
        BufferedImage bufferedImage;

        if (isGrayScale) {
            bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        } else if (isBW) {
            bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        } else {
            bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        }
        bufferedImage.getRaster().setPixels(0, 0, w, h, getIntRGBRasterArray());

        return bufferedImage;
    }

    private int[] getIntRGBRasterArray() {
        int[] raster = new int[pixelData.length];

        for (int i = 0; i < pixelData.length; i++) {
            raster[i] = pixelData[i] & 0xff;
        }
        return raster;
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

    public boolean isColor(byte[] pixel, byte value) {
        boolean isColor = true;
        for (byte b : pixel) if (b != value) isColor = false;
        return isColor;
    }

    public void logicalAnd(EasyImage img) {
        if (img.width * img.height != width * height) throw new InvalidParameterException();

        byte[] black = new byte[pixelLength];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                if (img.isColor(img.getPixelArray(i, j), (byte) 0)) this.setPixelArray(i, j, black);
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

    public EasyVector getColumn(int col) {
        EasyVector vector = new EasyVector();
        if (col < 0 || col > width - 1) throw new IllegalArgumentException("coloumn out of image bounds");
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

    // comparison processing

    public double meanSqrtDiff(EasyImage image) {
        return comparisonProcessor.meanSqrtDiff(image);
    }

    public double imageSimilarity(EasyImage image) {
        return comparisonProcessor.imageSimilarity(image);
    }

    public double meanAbsDiff(EasyImage image) {
        return comparisonProcessor.meanAbsDiff(image);
    }

    // statistic processing

    public int[] hueHistogram() {
        return statisticProcessor.hueHistogram();
    }

    public byte mode() {
        return statisticProcessor.mode();
    }

    public int[] histogram() {
        return statisticProcessor.histogram();
    }

    public double norm2() {
        return statisticProcessor.norm2();

    }

    public int min() {
        return statisticProcessor.min();
    }

    public int max() {
        return statisticProcessor.max();
    }

    public byte[] borderlessNeighborhoodStat(int x, int y, int r, EasyVector.Stat stat) {
        return statisticProcessor.borderlessNeighborhoodStat(x, y, r, stat);
    }

    public void filter(EasyVector.Stat stat, int r) {
        statisticProcessor.filter(stat, r);
    }

    // morphological processing

    public void dilate(int n) {
        morphologicalProcessor.dilate(n);
    }

    public void erode(int n) {
        morphologicalProcessor.erode(n);
    }

    public void close(int iterations) {
        morphologicalProcessor.close(iterations);
    }

    public void open(int iterations) {
        morphologicalProcessor.open(iterations);
    }

    // connected component processing

    public BoundingBox[] connectedComponents() {
        return connectedComponentProcessor.computeConnectedComponents();
    }

    public void keepLargestComponent() {
        connectedComponentProcessor.keepLargestComponent();
    }

    public void filterComponentsBySize(double smallFraction, double largeFraction) {
        connectedComponentProcessor.filterComponentsBySize(smallFraction, largeFraction);
    }

    public void filterComponentsByBoundingBoxArea(double smallFraction, double largeFraction) {
        connectedComponentProcessor.filterComponentsByBoundingBoxArea(smallFraction, largeFraction);
    }

    public void filterComponentsByBoundingBoxHeight(int minPixelHeight, int maxPixelHeight) {
        connectedComponentProcessor.filterComponentsByBoundingBoxHeight(minPixelHeight, maxPixelHeight);
    }

    public void filterComponentsByBoundingBoxWidth(int minPixelWidth, int maxPixelWidth) {
        connectedComponentProcessor.filterComponentsByBoundingBoxWidth(minPixelWidth, maxPixelWidth);
    }

    // color processing

    public void convertToBW(int thresh) {
        colorProcessor.convertToBW(thresh);
    }

    public void convertToGrayScale() {
        colorProcessor.convertToGrayScale();
    }

    public int grayValue(byte[] pixelArray) {
        return colorProcessor.grayValue(pixelArray);
    }

    public void threshold(int keepBelow, int keepAbove) {
        colorProcessor.threshold(keepBelow, keepAbove);
    }

    public void invert() {
        colorProcessor.invert();
    }

    public void colorKeeper(ColorPixel keep, ColorPixel discardColor, double maxDist) {
        colorProcessor.colorKeeper(keep, discardColor, maxDist);
    }

    public void hueKeeper(ColorPixel color, double hueDistance) {
        colorProcessor.hueKeeper(color, hueDistance);
    }

    public void quantize3Bit() {
        colorProcessor.quantize3Bit();
    }

    public void quantize8Bit() {
        colorProcessor.quantize8Bit();
    }
}

