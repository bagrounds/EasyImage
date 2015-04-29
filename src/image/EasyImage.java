package image;

import image.math.EasyVector;

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
import java.util.*;
import java.util.Queue;

import static java.lang.Math.pow;

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

    public double meanSqrtDiff(EasyImage image) {
        double value = 0;
        double size = pixelData.length;

        for (int i = 0; i < pixelData.length; i++)
            value += Math.pow(Math.abs(((pixelData[i] & 0xff) - (image.pixelData[i] & 0xff))) / 255.0, .5);
        return value;
    }

    public double imageSimilarity(EasyImage b) {
        return 1 - this.meanAbsDiff(b) / pixelData.length;
    }

    public double meanAbsDiff(EasyImage image) {
        double value = 0;
        double size = pixelData.length;

        for (int i = 0; i < pixelData.length; i++)
            value += Math.abs(((pixelData[i] & 0xff) - (image.pixelData[i] & 0xff)) / 255.0);
        return value;
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

    public int[] hueHistogram() {
        int[] histogram = new int[256];
        double hue;

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                hue = ColorPixel.rgbToHue(getPixelArray(i, j));
                hue = hue * 255 / 360;
                histogram[(int) Math.floor(hue)]++;
            }
        return histogram;
    }

    public byte mode() {
        int[] histogram = histogram();

        int max = 0;
        int maxIndex = 0;

        for (int i = 0; i < histogram.length; i++)
            if (histogram[i] > max) {
                max = histogram[i];
                maxIndex = i;
            }

        return (byte) maxIndex;
    }

    public int[] histogram() {
        int[] histogram = new int[256];

        for (byte b : pixelData) histogram[b & 0xff]++;

        return histogram;
    }

    public double norm2() {
        double value = 0;

        double max = max();

        for (byte b : pixelData) value += pow((b & 0xff) / max, 2);

        value = Math.sqrt(value) * max;
        return value;
    }

    public int max() {
        //if (!isGrayScale) throw new InvalidParameterException();

        int max = 0;

        for (byte b : pixelData) if ((b & 0xff) > max) max = (b & 0xff);

        return max;
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

    public void filter(EasyVector.Stat stat, int r) {
        EasyImage temp = new EasyImage(this);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                setPixelArray(i, j, temp.borderlessNeighborhoodStat(i, j, r, stat));
            }
        }
    }

    public byte[] borderlessNeighborhoodStat(int x, int y, int r, EasyVector.Stat stat) {
        byte[] result = new byte[pixelLength];

        EasyVector neighborhood = new EasyVector();

        for (int a = 0; a < pixelLength; a++) {
            for (int i = x - r; i <= x + r; i++)
                for (int j = y - r; j <= y + r; j++) {
                    byte[] pixelArray;
                    try {
                        pixelArray = getPixelArray(i, j);
                    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                        pixelArray = null;
                    }
                    if (pixelArray != null) neighborhood.add(pixelArray[a] & 0xff);
                }
            result[a] = (byte) neighborhood.stat(stat);
            neighborhood.clear();
        }
        return result;
    }

    public void colorKeeper(ColorPixel keep, ColorPixel discardColor, double maxDist) {
        if (isBW || isGrayScale) throw new IllegalArgumentException("this is not a color image");
        ColorPixel temp;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                temp = new ColorPixel(getPixelArray(i, j));
                //System.out.print(Arrays.toString(temp.byteArrayValue()) + ":" + keep.rgbDist(temp )+ "\t\t\t" );
                if (keep.rgbDist(temp) > maxDist) setPixelArray(i, j, discardColor.byteArrayValue());
            }
            //System.out.println();
        }
    }

    public void dilate(int n) {
        EasyImage temp = new EasyImage(this);

        //System.out.println("width = " + width + " height = " + height + " length = " + pixelData.length + "hxw = " + width*height);
        for (int a = 0; a < n; a++) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    setPixelArray(i, j, temp.borderlessNeighborhoodStat(i, j, 1, EasyVector.Stat.DILATION));
                }
            }
        }
    }

    public void erode(int n) {
        invert();
        dilate(n);
        invert();
    }

    public void close(int iterations) {
        dilate(iterations);
        erode(iterations);
    }

    public void open(int iterations) {
        invert();
        close(iterations);
        invert();
    }

    public void threshold(int keepBelow, int keepAbove) {
        if (!isGrayScale) throw new IllegalArgumentException();

        for (int i = 0; i < pixelData.length; i++) {
            if (pixelData[i] > keepBelow && pixelData[i] < keepAbove) pixelData[i] = 0;
            else pixelData[i] = (byte) 255;
        }

    }

    public void invert() {
        if (hasAlphaChannel)
            for (int i = 0; i < pixelData.length; i++) {
                if (i % 4 != 0)
                    pixelData[i] = (byte) (255 - pixelData[i]);
            }
        else
            for (int i = 0; i < pixelData.length; i++) {
                pixelData[i] = (byte) (255 - pixelData[i]);
            }
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

    public void keepLargestComponent() {
        connectedComponents();
        int[] histogram = new int[256];

        for (byte b : pixelData) histogram[b & 0xff]++;

        int max = 0;
        int maxIndex = 0;

        for (int i = 0; i < histogram.length; i++)
            if (histogram[i] > max) {
                max = histogram[i];
                maxIndex = i;
            }

        keepPixelsWithValues(new byte[]{(byte) maxIndex});
        isBW = false;
        isGrayScale = true;
        convertToBW(0);
    }

    public void convertToBW(int thresh) {
        if (!isBW) {
            if (!isGrayScale)
                convertToGrayScale();
            for (int i = 0; i < width; i++)
                for (int j = 0; j < height; j++) {
                    int pos = i + j * width;
                    pixelData[pos] = (byte) (((pixelData[pos] & 0xff) > thresh) ? 255 : 0);
                }
            isBW = true;
            isGrayScale = false;
        }
    }

    public void convertToGrayScale() {
        if (!isGrayScale && !isBW) {
            byte[] pixelsGray = new byte[width * height];

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    pixelsGray[i + j * width] = (byte) grayValue(getPixelArray(i, j));
                }
            }
            pixelData = pixelsGray;
            isGrayScale = true;
            hasAlphaChannel = false;
            pixelLength = 1;
        }
    }

    public int grayValue(byte[] pixelArray) {
        return grayValue(pixelArray[0], pixelArray[1], pixelArray[2]);
    }

    public int grayValue(byte r, byte g, byte b) {
        int red = r & 0xff;
        int green = g & 0xff;
        int blue = b & 0xff;

        return (int) (.2126 * red + .7152 * green + .0722 * blue);
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

    public BoundingBox[] connectedComponents() {

        byte[] connected = new byte[width * height];

        Queue<Point> q = new LinkedList<Point>();

        int currentLabel = 1;

        for (int i = 1; i < width - 2; i++) {
            for (int j = 1; j < height - 2; j++) {

                if (isColor(getPixelArray(i, j), (byte) 255) && connected[i + j * width] == 0) {
                    Point point = new Point(i, j, width - 1, height - 1);
                    q.add(point);
                    connected[i + j * width] = (byte) currentLabel;
                    while (!q.isEmpty()) {
                        Point member = q.remove();
                        Set<Point> neighbors = member.getNeighbors();
                        for (Point n : neighbors) {
                            if (isColor(getPixelArray(n.x, n.y), (byte) 255) && connected[n.x + n.y * width] == 0) {
                                connected[n.x + n.y * width] = (byte) currentLabel;
                                q.add(n);
                            }
                        }
                    }
                    currentLabel = ((currentLabel + 1) % 256);
                }
            }
        }

        BoundingBox[] bounds = new BoundingBox[currentLabel - 1];
        BoundingBox b;

        int xMin = width;
        int xMax = 0;
        int yMin = height;
        int yMax = 0;

        for (int a = 1; a < currentLabel; a++) {
            for (int i = 0; i < width; i++)
                for (int j = 0; j < height; j++) {
                    int value = connected[i + j * width] & 0xff;
                    if (value == a) {
                        xMin = (xMin < i) ? xMin : i;
                        xMax = (xMax > i) ? xMax : i;
                        yMin = (yMin < j) ? yMin : j;
                        yMax = (yMax > j) ? yMax : j;
                    }
                }

            try {
                b = new BoundingBox(xMin, xMax, yMin, yMax);
                bounds[a - 1] = b;
            } catch (IllegalArgumentException e) {
                System.err.println("illegal bounding box parameters in c.c.");
            }
            xMin = width;
            xMax = 0;
            yMin = height;
            yMax = 0;
        }

        this.pixelData = connected;
        this.isGrayScale = true;
        this.isBW = false;

        return bounds;
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

    public void filterComponentsBySize(double smallFraction, double largeFraction) {
        int size = this.pixelData.length;
        int small = (int) Math.floor(size * smallFraction);
        int large = (int) Math.floor(size * largeFraction);

        EasyImage temp = new EasyImage(this);
        BoundingBox[] boxes = temp.connectedComponents();
        int componentSize = 0;

        for (int a = 0; a <= boxes.length; a++) {

            for (int i = 0; i < size; i++)
                if (temp.pixelData[i] == a) componentSize++;

            if (componentSize > large || componentSize < small)
                for (int i = 0; i < size; i++)
                    if (temp.pixelData[i] == a) this.pixelData[i] = 0;

            componentSize = 0;
        }
    }

    public void filterComponentsByBoundingBoxArea(double smallFraction, double largeFraction) {
        int size = this.pixelData.length;
        int small = (int) Math.floor(size * smallFraction);
        int large = (int) Math.floor(size * largeFraction);
        int area;

        EasyImage temp = new EasyImage(this);
        BoundingBox[] boxes = temp.connectedComponents();

        for (int a = 0; a <= boxes.length; a++) {
            area = boxes[a].area();

            if (area > large || area < small)
                for (int i = 0; i < size; i++)
                    if (temp.pixelData[i] == a) this.pixelData[i] = 0;
        }
    }

    public void filterComponentsByBoundingBoxHeight(int minPixelHeight, int maxPixelHeight) {
        EasyImage temp = new EasyImage(this);
        BoundingBox[] boxes = temp.connectedComponents();
        int height;


        for (int a = 0; a <= boxes.length; a++) {
            height = boxes[a].yMax - boxes[a].yMin;

            if (height > maxPixelHeight || height < minPixelHeight)
                for (int i = 0; i < pixelData.length; i++)
                    if (temp.pixelData[i] == a) this.pixelData[i] = 0;
        }
    }

    public void filterComponentsByBoundingBoxWidth(int minPixelWidth, int maxPixelWidth) {
        EasyImage temp = new EasyImage(this);
        BoundingBox[] boxes = temp.connectedComponents();
        int width;


        for (int a = 0; a < boxes.length; a++) {
            width = boxes[a].xMax - boxes[a].xMin;

            if (width > maxPixelWidth || width < minPixelWidth)
                for (int i = 0; i < pixelData.length; i++)
                    if (temp.pixelData[i] == a) this.pixelData[i] = 0;
        }
    }

    public void quantize8Bit() {
        ColorPixel pixel;
        double hue, saturation, intensity;
        double dHue, dSaturation, dIntensity;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixel = new ColorPixel(getPixelArray(i, j));
                hue = pixel.getHue();
                intensity = pixel.getIntensity();
                saturation = pixel.getSaturation();

                if (saturation < .2) { // 16 Grayscale
                    dIntensity = 255 * Math.round(intensity * 16) / 16.0;
                    setPixelArray(i, j, new byte[]{(byte) dIntensity, (byte) dIntensity, (byte) dIntensity});
                } else {
                    dSaturation = Math.ceil(saturation * 5) / 5.0;
                    dIntensity = Math.round(intensity * 10) / 10.0;
                    dHue = Math.round(hue * 6 / 360) * 360.0 / 6.0;
                    setPixelArray(i, j, ColorPixel.rgbFromHsi(dHue, dSaturation, dIntensity));
                }
                //System.out.print(Arrays.toString(pixel.byteArrayValue()) + "/" +Arrays.toString(new double[]{hue, saturation, intensity}) + "/" +Arrays.toString( ColorPixel.rgbFromHsi(dHue,dSaturation,dIntensity)));
                pixel = null;
            }
            //System.out.println();
        }
    }

    public void quantize3Bit() {
        ColorPixel p;

        double hue;
        double intensity;
        double saturation;

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                p = new ColorPixel(getPixelArray(i, j));
                intensity = p.getIntensity();
                saturation = p.getSaturation();

                if (saturation < .2) {
                    if (intensity < .2) setPixelArray(i, j, ColorPixel.BLACK.byteArrayValue());
                    else if (intensity < .4) setPixelArray(i, j, ColorPixel.DARKGRAY.byteArrayValue());
                    else if (intensity < .6) setPixelArray(i, j, ColorPixel.LIGHTGRAY.byteArrayValue());
                    else if (intensity < .8) setPixelArray(i, j, ColorPixel.GRAYWHITE.byteArrayValue());
                    else setPixelArray(i, j, ColorPixel.WHITE.byteArrayValue());
                } else {
                    hue = p.getHue();

                    if (hue >= 345 || hue < 15)
                        setPixelArray(i, j, ColorPixel.RED.byteArrayValue());
                    else if (hue < 45)
                        setPixelArray(i, j, ColorPixel.REDYELLOW.byteArrayValue());
                    else if (hue < 75)
                        setPixelArray(i, j, ColorPixel.YELLOW.byteArrayValue());
                    else if (hue < 105)
                        setPixelArray(i, j, ColorPixel.YELLOWGREEN.byteArrayValue());
                    else if (hue < 135)
                        setPixelArray(i, j, ColorPixel.GREEN.byteArrayValue());
                    else if (hue < 165)
                        setPixelArray(i, j, ColorPixel.GREENCYAN.byteArrayValue());
                    else if (hue < 195)
                        setPixelArray(i, j, ColorPixel.CYAN.byteArrayValue());
                    else if (hue < 225)
                        setPixelArray(i, j, ColorPixel.CYANBLUE.byteArrayValue());
                    else if (hue < 255)
                        setPixelArray(i, j, ColorPixel.BLUE.byteArrayValue());
                    else if (hue < 285)
                        setPixelArray(i, j, ColorPixel.BLUEMAGENTA.byteArrayValue());
                    else if (hue < 315)
                        setPixelArray(i, j, ColorPixel.MAGENTA.byteArrayValue());
                    else if (hue < 345)
                        setPixelArray(i, j, ColorPixel.MAGENTARED.byteArrayValue());
                }
            }
    }

    public void hueKeeper(ColorPixel color, double hueDistance) {
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                if (!(new ColorPixel(getPixelArray(i, j)).hueDist(color) < hueDistance))
                    setPixelArray(i, j, ColorPixel.BLACK.byteArrayValue());
            }
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
        //if (!isGrayScale) throw new InvalidParameterException();

        int min = 255;

        for (byte b : pixelData) if ((b & 0xff) < min) min = (b & 0xff);

        return min;
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

    /*public void displayImageColorized( String s ){
        BufferedImage img = getBufferedImage();
        ImageSingleBand isb = ConvertBufferedImage.convertFromSingle(img, null, ImageUInt8.class);
        ImageUInt8 imageUInt8 = ConvertBufferedImage.convertFrom(img,(ImageUInt8)null);

        img = VisualizeImageData.grayMagnitudeTemp(isb, null, ImageStatistics.max(imageUInt8));

        displayImage(s);
    }*/

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

    class Point {
        public int x;
        public int y;
        public int xMax;
        public int yMax;
        private Set<Point> neighbors = null;

        Point(int x, int y, int xMax, int yMax) {
            this.x = x;
            this.y = y;
            this.xMax = xMax;
            this.yMax = yMax;
        }

        public Set<Point> getNeighbors() {
            if (neighbors == null) {
                setNeightbors();
            }
            return neighbors;
        }

        private void setNeightbors() {
            neighbors = new HashSet<Point>();

            if (x > 0) neighbors.add(new Point(x - 1, y, xMax, yMax));
            if (y > 0) neighbors.add(new Point(x, y - 1, xMax, yMax));
            if (x < xMax) neighbors.add(new Point(x + 1, y, xMax, yMax));
            if (y < yMax) neighbors.add(new Point(x, y + 1, xMax, yMax));

            if (x > 0 && y > 0) neighbors.add(new Point(x - 1, y - 1, xMax, yMax));
            if (x < xMax && y < yMax) neighbors.add(new Point(x + 1, y + 1, xMax, yMax));
            if (x > 0 && y < yMax) neighbors.add(new Point(x - 1, y + 1, xMax, yMax));
            if (y > 0 && x < xMax) neighbors.add(new Point(x + 1, y - 1, xMax, yMax));
        }
    }

}

