package algorithms;

public final class CountingSort {
  public static void sort(int[] a) {
    if (a.length == 0) return;
    int max = a[0], min = a[0];
    for (int v : a) { if (v > max) max = v; if (v < min) min = v; }
    int range = max - min + 1;
    int[] count = new int[range];
    for (int v : a) count[v - min]++;
    int idx = 0;
    for (int i = 0; i < range; i++) {
      int c = count[i];
      while (c-- > 0) a[idx++] = i + min;
    }
  }
}
