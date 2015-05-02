package org.bagrounds.java.image.processors;

import org.bagrounds.java.image.EasyImage;

/**
 * Created by bryan on 4/28/15.
 */
public abstract class Processor {
  protected EasyImage image;

  public Processor(EasyImage i) {
    image = i;
  }

}
