package org.bagrounds.java.easyimage.processors;

import org.bagrounds.java.easyimage.EasyImage;
import org.bagrounds.java.easyimage.math.EasyVector;

/**
 * Created by bryan on 4/28/15.
 */
public class MorphologicalProcessor extends Processor {
  public MorphologicalProcessor(EasyImage i) {
    super(i);
  }

  public void open(int iterations) {
    image.colorProcessor.invert();
    close(iterations);
    image.colorProcessor.invert();
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
          image.setPixelArray(i, j, temp.statisticProcessor.borderlessNeighborhoodStat(i, j, 1, EasyVector.Stat.DILATION));
        }
      }
    }
  }

  public void erode(int n) {
    image.colorProcessor.invert();
    dilate(n);
    image.colorProcessor.invert();
  }


}
