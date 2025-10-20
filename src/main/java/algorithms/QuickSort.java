package algorithms;

import java.util.Random;

public final class QuickSort {
  private static final Random R = new Random(7);

  public static void sort(int[] a) {
    if (a.length < 2) return;
    sort(a, 0, a.length - 1);
  }
  private static void sort(int[] a, int l, int r) {
    if (l >= r) return;
    int p = l + R.nextInt(r - l + 1);
    int pivot = a[p]; swap(a, p, r);
    int i = l;
    for (int j = l; j < r; j++) if (a[j] <= pivot) swap(a, i++, j);
    swap(a, i, r);
    sort(a, l, i - 1);
    sort(a, i + 1, r);
  }
  private static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
}
