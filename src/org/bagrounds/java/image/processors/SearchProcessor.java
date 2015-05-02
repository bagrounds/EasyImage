package org.bagrounds.java.image.processors;

import org.bagrounds.java.image.BoundingBox;
import org.bagrounds.java.image.EasyImage;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by bryan on 4/29/15.
 */
public class SearchProcessor extends Processor {

  public SearchProcessor(EasyImage easyImage) {
    super(easyImage);
  }

  public ArrayList<BoundingBox> findPattern(EasyImage pattern) {
    byte[] firstPixel = pattern.getPixelArray(0, 0);
    System.out.println("find pattern");
    ArrayList<BoundingBox> boxes = new ArrayList<BoundingBox>();

    for (int i = 0; i < image.width; i++) {
      for (int j = 0; j < image.height; j++) {
        if (Arrays.equals(image.getPixelArray(i, j), firstPixel)) {
          BoundingBox patternBox = new BoundingBox(i, i + pattern.width, j, j + pattern.height);
          EasyImage subImage = image.getSubImage(patternBox);
          if (subImage.equals(pattern)) {
            System.out.println("match found!");
            boxes.add(patternBox);
          }
        }
      }
    }
    System.out.println("done finding patterns");
    return boxes;
  }
}
