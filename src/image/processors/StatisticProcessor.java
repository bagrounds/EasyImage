package image.processors;

import image.ColorPixel;
import image.EasyImage;
import image.math.EasyVector;

import static java.lang.Math.pow;

/**
 * Created by bryan on 4/28/15.
 */
public class StatisticProcessor extends Processor {
  public StatisticProcessor(EasyImage image) {
    super(image);
  }

  public int[] hueHistogram() {
    int[] histogram = new int[256];
    double hue;

    for (int i = 0; i < image.width; i++)
      for (int j = 0; j < image.height; j++) {
        hue = ColorPixel.rgbToHue(image.getPixelArray(i, j));
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

    for (byte b : image.pixelData) histogram[b & 0xff]++;

    return histogram;
  }

  public double norm2() {
    double value = 0;

    double max = max();

    for (byte b : image.pixelData) value += pow((b & 0xff) / max, 2);

    value = Math.sqrt(value) * max;
    return value;
  }

  public int max() {
    //if (!isGrayScale) throw new InvalidParameterException();

    int max = 0;

    for (byte b : image.pixelData) if ((b & 0xff) > max) max = (b & 0xff);

    return max;
  }

  public byte[] borderlessNeighborhoodStat(int x, int y, int r, EasyVector.Stat stat) {
    byte[] result = new byte[image.pixelLength];

    EasyVector neighborhood = new EasyVector();

    for (int a = 0; a < image.pixelLength; a++) {
      for (int i = x - r; i <= x + r; i++)
        for (int j = y - r; j <= y + r; j++) {
          byte[] pixelArray;
          try {
            pixelArray = image.getPixelArray(i, j);
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

  public int min() {
    //if (!isGrayScale) throw new InvalidParameterException();

    int min = 255;

    for (byte b : image.pixelData) if ((b & 0xff) < min) min = (b & 0xff);

    return min;
  }

  public void filter(EasyVector.Stat stat, int r) {
    EasyImage temp = new EasyImage(image);

    for (int i = 0; i < image.width; i++) {
      for (int j = 0; j < image.height; j++) {
        image.setPixelArray(i, j, temp.borderlessNeighborhoodStat(i, j, r, stat));
      }
    }
  }
}
