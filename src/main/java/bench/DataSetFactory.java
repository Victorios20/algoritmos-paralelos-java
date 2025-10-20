package bench;

import java.util.Random;

public final class DataSetFactory {
  private static final Random R = new Random(7);

  public static int[] random(int n) {
    int[] a = new int[n];
    for (int i = 0; i < n; i++) a[i] = R.nextInt(n * 10);
    return a;
  }

  public static int[] reversed(int n) {
    int[] a = new int[n];
    for (int i = 0; i < n; i++) a[i] = n - i;
    return a;
  }

  public static int[] nearlySorted(int n) {
    int[] a = new int[n];
    for (int i = 0; i < n; i++) a[i] = i;
    for (int i = 0; i < Math.max(1, n / 100); i++) {
      int x = R.nextInt(n), y = R.nextInt(n);
      int t = a[x]; a[x] = a[y]; a[y] = t;
    }
    return a;
  }

  public static int[] manyDuplicates(int n) {
    int[] a = new int[n];
    for (int i = 0; i < n; i++) a[i] = R.nextInt(10);
    return a;
  }

  public static boolean isSorted(int[] a) {
    for (int i = 1; i < a.length; i++) if (a[i] < a[i - 1]) return false;
    return true;
  }
}
