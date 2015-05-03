package org.bagrounds.java.easyimage.geometry;

import java.util.HashSet;
import java.util.Set;

/**
 * Point on an image corresponding to a pixel. This class is used by the connected components algorithm.
 *
 * Created by bryan on 4/28/15.
 */
public class Point {
  public int x;
  public int y;
  public int xMax;
  public int yMax;
  private Set<Point> neighbors = null;

  public Point(int x, int y, int xMax, int yMax) {
    this.x = x;
    this.y = y;
    this.xMax = xMax;
    this.yMax = yMax;
  }

  public Set<Point> getNeighbors() {
    if (neighbors == null) {
      setNeightbors();
    }
    return neighbors;
  }

  private void setNeightbors() {
    neighbors = new HashSet<Point>();

    if (x > 0) neighbors.add(new Point(x - 1, y, xMax, yMax));
    if (y > 0) neighbors.add(new Point(x, y - 1, xMax, yMax));
    if (x < xMax) neighbors.add(new Point(x + 1, y, xMax, yMax));
    if (y < yMax) neighbors.add(new Point(x, y + 1, xMax, yMax));

    if (x > 0 && y > 0) neighbors.add(new Point(x - 1, y - 1, xMax, yMax));
    if (x < xMax && y < yMax) neighbors.add(new Point(x + 1, y + 1, xMax, yMax));
    if (x > 0 && y < yMax) neighbors.add(new Point(x - 1, y + 1, xMax, yMax));
    if (y > 0 && x < xMax) neighbors.add(new Point(x + 1, y - 1, xMax, yMax));
  }
}
