package org.bagrounds.java.easyimage.processors;

import org.bagrounds.java.easyimage.EasyImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

/**
 * Created by bryan on 5/3/15.
 */
public class IOProcessor extends Processor {

  public IOProcessor(EasyImage i) {
    super(i);
  }

  public static EasyImage loadImage(String fileName) {
    File file = new File(fileName);
    if (!file.exists() || !file.canRead()) {
      System.err.println("cannot read file: " + fileName + "!");
      return new EasyImage();
    }

    EasyImage result = null;
    BufferedImage img = null;
    BufferedImage noAlpha = null;
    try {
      img = ImageIO.read(file);
      int w = img.getWidth();
      int h = img.getHeight();
      noAlpha = new BufferedImage(w, h,
          BufferedImage.TYPE_3BYTE_BGR);
      Raster raster = img.getRaster().createChild(0, 0, w, h, 0, 0, new
          int[]{2, 1, 0});
      noAlpha.setData(raster);
      result = new EasyImage(noAlpha);
    } catch (Exception e) {
      System.err.println("Error reading easyimage");
      e.printStackTrace();
      result = new EasyImage();
    } finally {
      if (img != null) img.flush();
      if (noAlpha != null) noAlpha.flush();
    }

    return result;
  }

  public static void saveImage(String fileName, EasyImage easyImage) {
    try {
      // retrieve easyimage
      File out = new File(fileName);
      ImageIO.write(easyImage.getBufferedImage(), "png", out);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
