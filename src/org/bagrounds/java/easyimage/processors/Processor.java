package org.bagrounds.java.easyimage.processors;

import org.bagrounds.java.easyimage.EasyImage;

/**
 * Created by bryan on 4/28/15.
 */
public abstract class Processor {
  protected EasyImage image;

  public Processor(EasyImage i) {
    image = i;
  }

}
