package image;

import java.util.Arrays;

/**
 * Tests Created by bryan on 4/28/15.
 */
public class Tester {

  public static void main(String[] args) {

    byte[] b = new byte[3];
    b[0] = 1;
    b[1] = (byte) 127;
    b[2] = (byte) 256;
    System.out.println(Arrays.toString(b));
    change(b);
    System.out.println(Arrays.toString(b));

    EasyImage image = EasyImage.loadImage("/Users/bryan/Desktop/houdini.JPG");
    image.decimate(4);
    image.displayImage("houdini");
    image.dilate(1);
    image.displayImage("dilated");

  }

  private static void change(byte[] b) {
    b[0] = 0;
    b[1] = 2;
  }
}
