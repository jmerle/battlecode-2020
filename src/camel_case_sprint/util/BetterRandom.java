package camel_case_sprint.util;

public class BetterRandom {
  public static int nextInt(int maxExclusive) {
    return (int) Math.floor(Math.random() * maxExclusive);
  }
}
