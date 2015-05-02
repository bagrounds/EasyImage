package org.bagrounds.java.image.processors;

import org.bagrounds.java.image.BoundingBox;
import org.bagrounds.java.image.EasyImage;
import org.bagrounds.java.image.Point;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by bryan on 4/28/15.
 */
public class ConnectedComponentProcessor extends Processor {
  public ConnectedComponentProcessor(EasyImage image) {
    super(image);
  }

  public void keepLargestComponent() {
    image.connectedComponents();
    int[] histogram = new int[256];

    for (byte b : image.pixelData) histogram[b & 0xff]++;

    int max = 0;
    int maxIndex = 0;

    for (int i = 0; i < histogram.length; i++)
      if (histogram[i] > max) {
        max = histogram[i];
        maxIndex = i;
      }

    image.keepPixelsWithValues(new byte[]{(byte) maxIndex});
    image.isBW = false;
    image.isGrayScale = true;
    image.convertToBW(0);
  }

  public BoundingBox[] computeConnectedComponents() {

    byte[] connected = new byte[image.width * image.height];

    Queue<Point> q = new LinkedList<Point>();

    int currentLabel = 1;

    for (int i = 1; i < image.width - 2; i++) {
      for (int j = 1; j < image.height - 2; j++) {

        if (image.isColor(image.getPixelArray(i, j), (byte) 255) && connected[i + j * image.width] == 0) {
          Point point = new Point(i, j, image.width - 1, image.height - 1);
          q.add(point);
          connected[i + j * image.width] = (byte) currentLabel;
          while (!q.isEmpty()) {
            Point member = q.remove();
            Set<Point> neighbors = member.getNeighbors();
            for (Point n : neighbors) {
              if (image.isColor(image.getPixelArray(n.x, n.y), (byte) 255) && connected[n.x + n.y * image.width] == 0) {
                connected[n.x + n.y * image.width] = (byte) currentLabel;
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

    int xMin = image.width;
    int xMax = 0;
    int yMin = image.height;
    int yMax = 0;

    for (int a = 1; a < currentLabel; a++) {
      for (int i = 0; i < image.width; i++)
        for (int j = 0; j < image.height; j++) {
          int value = connected[i + j * image.width] & 0xff;
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
      xMin = image.width;
      xMax = 0;
      yMin = image.height;
      yMax = 0;
    }

    image.pixelData = connected;
    image.isGrayScale = true;
    image.isBW = false;

    return bounds;
  }

  public void filterComponentsBySize(double smallFraction, double largeFraction) {
    int size = image.pixelData.length;
    int small = (int) Math.floor(size * smallFraction);
    int large = (int) Math.floor(size * largeFraction);

    EasyImage temp = new EasyImage(image);
    BoundingBox[] boxes = temp.connectedComponents();
    int componentSize = 0;

    for (int a = 0; a <= boxes.length; a++) {

      for (int i = 0; i < size; i++)
        if (temp.pixelData[i] == a) componentSize++;

      if (componentSize > large || componentSize < small)
        for (int i = 0; i < size; i++)
          if (temp.pixelData[i] == a) image.pixelData[i] = 0;

      componentSize = 0;
    }
  }

  public void filterComponentsByBoundingBoxArea(double smallFraction, double largeFraction) {
    int size = image.pixelData.length;
    int small = (int) Math.floor(size * smallFraction);
    int large = (int) Math.floor(size * largeFraction);
    int area;

    EasyImage temp = new EasyImage(image);
    BoundingBox[] boxes = temp.connectedComponents();

    for (int a = 0; a <= boxes.length; a++) {
      area = boxes[a].area();

      if (area > large || area < small)
        for (int i = 0; i < size; i++)
          if (temp.pixelData[i] == a) image.pixelData[i] = 0;
    }
  }

  public void filterComponentsByBoundingBoxHeight(int minPixelHeight, int maxPixelHeight) {
    EasyImage temp = new EasyImage(image);
    BoundingBox[] boxes = temp.connectedComponents();
    int height;


    for (int a = 0; a <= boxes.length; a++) {
      height = boxes[a].yMax - boxes[a].yMin;

      if (height > maxPixelHeight || height < minPixelHeight)
        for (int i = 0; i < image.pixelData.length; i++)
          if (temp.pixelData[i] == a) image.pixelData[i] = 0;
    }
  }

  public void filterComponentsByBoundingBoxWidth(int minPixelWidth, int maxPixelWidth) {
    EasyImage temp = new EasyImage(image);
    BoundingBox[] boxes = temp.connectedComponents();
    int width;


    for (int a = 0; a < boxes.length; a++) {
      width = boxes[a].xMax - boxes[a].xMin;

      if (width > maxPixelWidth || width < minPixelWidth)
        for (int i = 0; i < image.pixelData.length; i++)
          if (temp.pixelData[i] == a) image.pixelData[i] = 0;
    }
  }


}
