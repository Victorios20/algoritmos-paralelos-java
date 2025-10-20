package bench;

public final class Timer {
  public static long now() { return System.nanoTime(); }
  public static double ms(long start, long end) { return (end - start) / 1_000_000.0; }
}
