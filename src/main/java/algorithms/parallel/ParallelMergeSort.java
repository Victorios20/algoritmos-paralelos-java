package algorithms.parallel;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public final class ParallelMergeSort {
  public static void sort(int[] a, int threads) {
    if (a.length < 2) return;
    ForkJoinPool pool = new ForkJoinPool(Math.max(1, threads));
    int[] aux = new int[a.length];
    pool.invoke(new MergeTask(a, aux, 0, a.length - 1));
    pool.shutdown();
  }

  private static final int THRESHOLD = 10_000;

  private static final class MergeTask extends RecursiveAction {
    private final int[] a, aux; private final int l, r;
    MergeTask(int[] a, int[] aux, int l, int r) { this.a=a; this.aux=aux; this.l=l; this.r=r; }
    @Override protected void compute() {
      int n = r - l + 1;
      if (n <= THRESHOLD) { Arrays.sort(a, l, r + 1); return; }
      int m = (l + r) >>> 1;
      MergeTask left = new MergeTask(a, aux, l, m);
      MergeTask right = new MergeTask(a, aux, m + 1, r);
      invokeAll(left, right);
      merge(a, aux, l, m, r);
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
