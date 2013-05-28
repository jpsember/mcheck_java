package base;

public class Inf {
  public Inf(String message, int limit) {
    msg = message;
    this.limit = limit;
  }

  public void update() {
    if (++counter >= limit) {
      throw new Error("Infinite loop (" + counter + "): " + msg);
    }
  }

  String msg;
  int limit;
  int counter;
}
