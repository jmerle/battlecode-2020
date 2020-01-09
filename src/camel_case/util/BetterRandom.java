package camel_case.util;

public class BetterRandom {
  public static int nextInt(int maxExclusive) {
    return (int) Math.floor(Math.random() * maxExclusive);
  }
}
