package base;

import java.lang.Double;

public class FPoint2
    extends java.awt.geom.Point2D.Double {
  public static void add(FPoint2 a, FPoint2 b, FPoint2 d) {
    d.x = a.x + b.x;
    d.y = a.y + b.y;
  }

  public FPoint2(double x, double y) {
    super(x, y);
  }

  public FPoint2 add(FPoint2 pt) {
    return add(pt.x, pt.y);
  }

  public FPoint2 add(double x, double y) {
    this.x += x;
    this.y += y;
    return this;
  }

  public FPoint2 subtract(double x, double y) {
    this.x -= x;
    this.y -= y;
    return this;
  }

  public FPoint2 subtract(FPoint2 pt) {
    return subtract(pt.x, pt.y);
  }

  public FPoint2(FPoint2 src) {
    super(src.x, src.y);
  }

  public static FPoint2 interpolate(FPoint2 p1, FPoint2 p2, double t) {
    return new FPoint2(p1.x + t * (p2.x - p1.x),
                       p1.y + t * (p2.y - p1.y));
  }

  public static FPoint2 midPoint(FPoint2 p1, FPoint2 p2) {
    return interpolate(p1, p2, .5);
//  return new FPoint2(.5 * (p1.x+p2.x),.5*(p1.y+p2.y));
  }

  public boolean isValid() {

    return! (java.lang.Double.isInfinite(x) || java.lang.Double.isInfinite(y)
             || java.lang.Double.isNaN(x) || java.lang.Double.isNaN(y));
  }

  /**
   * Align point to a grid
   * @param gridSize : size of grid
   */
  public void alignToGrid(double gridSize) {
    alignToGrid(gridSize,gridSize);
  }
  /**
   * Align point to a grid
   * @param gridSize : size of grid
   */
  public void alignToGrid(double gridX, double gridY) {
    double iGrid = 1 / gridX;
    x = Math.round(x * iGrid) * gridX;
    iGrid = 1 / gridY;
   y = Math.round(y * iGrid) * gridY;
  }

  public FPoint2() {}

  public boolean clamp(double x0, double y0, double x1, double y1) {
    boolean valid = true;
    if (x < x0 || x > x1) {
      valid = false;
      x = Tools.clamp(x, x0, x1);
    }
    if (y < y0 || y > y1) {
      valid = false;
      y = Tools.clamp(y, y0, y1);
    }
    return valid;
  }

//  public boolean clamp(FRect r) {
//    return clamp(r.x, r.y, r.x + r.width, r.y + r.height);
//  }
  public static double distance(FPoint2 a, FPoint2 b) {
    return FPoint2.distance(a.x, a.y, b.x, b.y);
  }

  public static double distanceSquared(FPoint2 a, FPoint2 b) {
    return distanceSq(a.x, a.y, b.x, b.y);
  }

//  public String dump(boolean withComma) {
//    return Tools.f(x) + (withComma ? "," : " ") + Tools.f(y);
//  }

//public String dump() { // plotInfo
//  return dump(false);
//}

  /**
   * Dump point as x and y (rounded), with leading space before each
   * @return String
   */
  public String toString() {
    return toString(false, true);
  }

  /**
  * Dump point
  * @param allDigits : if true, results are not rounded
  * @param numbersOnly : if true, returns ' xxxxx yyyy '; otherwise, returns '(xxx,yyy)'
  * @return String
  */
 public String toString(boolean allDigits, boolean numbersOnly) {
    if (!numbersOnly) {
      if (allDigits) {
        return "(" + x + "," + y + ")";
      }
      else {
        return "(" + Tools.fz(x) + "," + Tools.fz(y) + ")";
      }
    }
    else {
      if (allDigits) {
        return " " + x + " " + y + " ";
      }
      else {
        return " " + Tools.f(x) + " " + Tools.f(y) + " ";
      }
    }
  }


}
