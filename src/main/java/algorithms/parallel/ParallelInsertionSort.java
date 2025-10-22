package algorithms.parallel;

import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

public final class ParallelInsertionSort {
  public static void sort(int[] a, int threads) {
    if (a.length < 2) return;
    int t = Math.max(1, threads);
    int n = a.length;
    int blocks = t;
    int chunk = (n + blocks - 1) / blocks;

    ForkJoinPool pool = new ForkJoinPool(t);
    pool.submit(() -> IntStream.range(0, blocks).parallel().forEach(b -> {
      int l = b * chunk;
      int r = Math.min(n, l + chunk) - 1;
      if (l < r) insertionRange(a, l, r);
    })).join();

    int[] aux = new int[n];
    int runSize = chunk;
    while (runSize < n) {
      int step = runSize << 1;
      for (int start = 0; start < n; start += step) {
        int mid = Math.min(start + runSize - 1, n - 1);
        int end = Math.min(start + step - 1, n - 1);
        if (mid < end) merge(a, aux, start, mid, end);
      }
      runSize = step;
    }
    pool.shutdown();
  }

  private static void insertionRange(int[] a, int l, int r) {
    for (int i = l + 1; i <= r; i++) {
      int key = a[i], j = i - 1;
      while (j >= l && a[j] > key) { a[j + 1] = a[j]; j--; }
      a[j + 1] = key;
    }
  }

  private static void merge(int[] a, int[] aux, int l, int m, int r) {
    int i = l, j = m + 1, k = l;
    while (i <= m && j <= r) aux[k++] = (a[i] <= a[j]) ? a[i++] : a[j++];
    while (i <= m) aux[k++] = a[i++];
    while (j <= r) aux[k++] = a[j++];
    System.arraycopy(aux, l, a, l, r - l + 1);
  }
}
