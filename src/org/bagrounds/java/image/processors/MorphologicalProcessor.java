package org.bagrounds.java.image.processors;

import org.bagrounds.java.image.EasyImage;
import org.bagrounds.java.image.math.EasyVector;

/**
 * Created by bryan on 4/28/15.
 */
public class MorphologicalProcessor extends Processor {
  public MorphologicalProcessor(EasyImage i) {
    super(i);
  }

  public void open(int iterations) {
    image.invert();
    close(iterations);
    image.invert();
  }

  public void close(int iterations) {
    dilate(iterations);
    erode(iterations);
  }

  public void dilate(int n) {
    EasyImage temp = new EasyImage(image);

    //System.out.println("width = " + width + " height = " + height + " length = " + pixelData.length + "hxw = " + width*height);
    for (int a = 0; a < n; a++) {
      for (int i = 0; i < image.width; i++) {
        for (int j = 0; j < image.height; j++) {
          image.setPixelArray(i, j, temp.borderlessNeighborhoodStat(i, j, 1, EasyVector.Stat.DILATION));
        }
      }
    }
  }

  public void erode(int n) {
    image.invert();
    dilate(n);
    image.invert();
  }


}
