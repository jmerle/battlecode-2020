package camel_case.util;

public enum Color {
  RED(255, 0, 0),
  GREEN(0, 255, 0),
  BLUE(0, 0, 255),
  YELLOW(255, 255, 0),
  PURPLE(255, 0, 255),
  WHITE(255, 255, 255),
  BLACK(0, 0, 0);

  private final int r;
  private final int g;
  private final int b;

  Color(int r, int g, int b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  public int getR() {
    return r;
  }

  public int getG() {
    return g;
  }

  public int getB() {
    return b;
  }
}
