package org.bagrounds.java.image.processors;

import org.bagrounds.java.image.EasyImage;

/**
 * Created by bryan on 4/28/15.
 */
public class ComparisonProcessor extends Processor {
  public ComparisonProcessor(EasyImage image) {
    super(image);
  }

  public double meanSqrtDiff(EasyImage img) {
    double value = 0;

    for (int i = 0; i < img.pixelData.length; i++)
      value += Math.pow(Math.abs(((image.pixelData[i] & 0xff) - (img.pixelData[i] & 0xff))) / 255.0, .5);
    return value;
  }

  public double imageSimilarity(EasyImage img) {
    return 1 - this.meanAbsDiff(img) / image.pixelData.length;
  }

  public double meanAbsDiff(EasyImage img) {
    double value = 0;
    double size = image.pixelData.length;

    for (int i = 0; i < image.pixelData.length; i++)
      value += Math.abs(((image.pixelData[i] & 0xff) - (img.pixelData[i] & 0xff)) / 255.0);
    return value;
  }
}
