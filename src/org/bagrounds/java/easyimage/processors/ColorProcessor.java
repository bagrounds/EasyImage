package org.bagrounds.java.easyimage.processors;

import org.bagrounds.java.easyimage.ColorPixel;
import org.bagrounds.java.easyimage.EasyImage;

/**
 * Created by bryan on 4/28/15.
 */
public class ColorProcessor extends Processor {
  public ColorProcessor(EasyImage image) {
    super(image);
  }

  public void quantize8Bit() {
    ColorPixel pixel;
    double hue, saturation, intensity;
    double dHue, dSaturation, dIntensity;

    for (int i = 0; i < image.width; i++) {
      for (int j = 0; j < image.height; j++) {
        pixel = new ColorPixel(image.getPixelArray(i, j));
        hue = pixel.getHue();
        intensity = pixel.getIntensity();
        saturation = pixel.getSaturation();

        if (saturation < .2) { // 16 Grayscale
          dIntensity = 255 * Math.round(intensity * 16) / 16.0;
          image.setPixelArray(i, j, new byte[]{(byte) dIntensity, (byte) dIntensity, (byte) dIntensity});
        } else {
          dSaturation = Math.ceil(saturation * 5) / 5.0;
          dIntensity = Math.round(intensity * 10) / 10.0;
          dHue = Math.round(hue * 6 / 360) * 360.0 / 6.0;
          image.setPixelArray(i, j, ColorPixel.rgbFromHsi(dHue, dSaturation, dIntensity));
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

    for (int i = 0; i < image.width; i++)
      for (int j = 0; j < image.height; j++) {
        p = new ColorPixel(image.getPixelArray(i, j));
        intensity = p.getIntensity();
        saturation = p.getSaturation();

        if (saturation < .2) {
          if (intensity < .2) image.setPixelArray(i, j, ColorPixel.BLACK.byteArrayValue());
          else if (intensity < .4) image.setPixelArray(i, j, ColorPixel.DARKGRAY.byteArrayValue());
          else if (intensity < .6) image.setPixelArray(i, j, ColorPixel.LIGHTGRAY.byteArrayValue());
          else if (intensity < .8) image.setPixelArray(i, j, ColorPixel.GRAYWHITE.byteArrayValue());
          else image.setPixelArray(i, j, ColorPixel.WHITE.byteArrayValue());
        } else {
          hue = p.getHue();

          if (hue >= 345 || hue < 15)
            image.setPixelArray(i, j, ColorPixel.RED.byteArrayValue());
          else if (hue < 45)
            image.setPixelArray(i, j, ColorPixel.REDYELLOW.byteArrayValue());
          else if (hue < 75)
            image.setPixelArray(i, j, ColorPixel.YELLOW.byteArrayValue());
          else if (hue < 105)
            image.setPixelArray(i, j, ColorPixel.YELLOWGREEN.byteArrayValue());
          else if (hue < 135)
            image.setPixelArray(i, j, ColorPixel.GREEN.byteArrayValue());
          else if (hue < 165)
            image.setPixelArray(i, j, ColorPixel.GREENCYAN.byteArrayValue());
          else if (hue < 195)
            image.setPixelArray(i, j, ColorPixel.CYAN.byteArrayValue());
          else if (hue < 225)
            image.setPixelArray(i, j, ColorPixel.CYANBLUE.byteArrayValue());
          else if (hue < 255)
            image.setPixelArray(i, j, ColorPixel.BLUE.byteArrayValue());
          else if (hue < 285)
            image.setPixelArray(i, j, ColorPixel.BLUEMAGENTA.byteArrayValue());
          else if (hue < 315)
            image.setPixelArray(i, j, ColorPixel.MAGENTA.byteArrayValue());
          else if (hue < 345)
            image.setPixelArray(i, j, ColorPixel.MAGENTARED.byteArrayValue());
        }
      }
  }

  public void quantizeMod(int m) {
    for (int i = 0; i < image.pixelData.length; i++) {
      image.pixelData[i] /= m;
      image.pixelData[i] *= m;
    }
  }


  public void hueKeeper(ColorPixel color, double hueDistance) {
    for (int i = 0; i < image.width; i++)
      for (int j = 0; j < image.height; j++) {
        if (!(new ColorPixel(image.getPixelArray(i, j)).hueDist(color) < hueDistance))
          image.setPixelArray(i, j, ColorPixel.BLACK.byteArrayValue());
      }
  }

  public void colorKeeper(ColorPixel keep, ColorPixel discardColor, double maxDist) {
    if (image.isBW || image.isGrayScale) throw new IllegalArgumentException("this is not a color easyimage");
    ColorPixel temp;
    for (int i = 0; i < image.width; i++) {
      for (int j = 0; j < image.height; j++) {
        temp = new ColorPixel(image.getPixelArray(i, j));
        //System.out.print(Arrays.toString(temp.byteArrayValue()) + ":" + keep.rgbDist(temp )+ "\t\t\t" );
        if (keep.rgbDist(temp) > maxDist) image.setPixelArray(i, j, discardColor.byteArrayValue());
      }
      //System.out.println();
    }
  }

  public void colorFilter(ColorPixel filter, ColorPixel discardColor, double maxDist) {
    if (image.isBW || image.isGrayScale) throw new IllegalArgumentException("this is not a color easyimage");
    ColorPixel temp;
    for (int i = 0; i < image.width; i++) {
      for (int j = 0; j < image.height; j++) {
        temp = new ColorPixel(image.getPixelArray(i, j));
        //System.out.print(Arrays.toString(temp.byteArrayValue()) + ":" + keep.rgbDist(temp )+ "\t\t\t" );
        if (filter.rgbDist(temp) < maxDist) image.setPixelArray(i, j, discardColor.byteArrayValue());
      }
      //System.out.println();
    }
  }

  public void convertToBW(int thresh) {
    if (!image.isBW) {
      if (!image.isGrayScale)
        convertToGrayScale();
      for (int i = 0; i < image.width; i++)
        for (int j = 0; j < image.height; j++) {
          int pos = i + j * image.width;
          image.pixelData[pos] = (byte) (((image.pixelData[pos] & 0xff) > thresh) ? 255 : 0);
        }
      image.isBW = true;
      image.isGrayScale = false;
    }
  }

  public void convertToGrayScale() {
    if (!image.isGrayScale && !image.isBW) {
      byte[] pixelsGray = new byte[image.width * image.height];

      for (int i = 0; i < image.width; i++) {
        for (int j = 0; j < image.height; j++) {
          pixelsGray[i + j * image.width] = (byte) grayValue(image.getPixelArray(i, j));
        }
      }
      image.pixelData = pixelsGray;
      image.isGrayScale = true;
      image.hasAlphaChannel = false;
      image.pixelLength = 1;
    }
  }

  public int grayValue(byte[] pixelArray) {
    int red = pixelArray[0] & 0xff;
    int green = pixelArray[1] & 0xff;
    int blue = pixelArray[2] & 0xff;

    return (int) (.2126 * red + .7152 * green + .0722 * blue);
  }

  public void threshold(int keepBelow, int keepAbove) {

    for (int i = 0; i < image.pixelData.length; i++) {
      if (image.pixelData[i] < keepBelow || image.pixelData[i] > keepAbove) image.pixelData[i] = (byte) 255;
      else image.pixelData[i] = 0;
    }

  }

  public void invert() {
    if (image.hasAlphaChannel)
      for (int i = 0; i < image.pixelData.length; i++) {
        if (i % 4 != 0)
          image.pixelData[i] = (byte) (255 - image.pixelData[i]);
      }
    else
      for (int i = 0; i < image.pixelData.length; i++) {
        image.pixelData[i] = (byte) (255 - image.pixelData[i]);
      }
  }


}
