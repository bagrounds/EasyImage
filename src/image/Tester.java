package image;

/**
 * Tests Created by bryan on 4/28/15.
 */
public class Tester {

  public static void main(String[] args) {
    EasyImage image = EasyImage.loadImage("/Users/bryan/Desktop/houdini.JPG");
    image.decimate(4);
    image.displayImage("houdini");

  }
}
