package image.processors;

import image.EasyImage;

/**
 * Created by bryan on 4/28/15.
 */
public abstract class Processor {
  protected EasyImage image;

  public Processor(EasyImage i) {
    image = i;
  }

}
