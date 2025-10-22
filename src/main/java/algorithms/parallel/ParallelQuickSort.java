package algorithms.parallel;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public final class ParallelQuickSort {
  public static void sort(int[] a, int threads) {
    if (a.length < 2) return;
    ForkJoinPool pool = new ForkJoinPool(Math.max(1, threads));
    pool.invoke(new QTask(a, 0, a.length - 1));
    pool.shutdown();
  }

  private static final int THRESHOLD = 10_000;
  private static final Random R = new Random(7);

  private static final class QTask extends RecursiveAction {
    private final int[] a; private final int l, r;
    QTask(int[] a, int l, int r) { this.a=a; this.l=l; this.r=r; }
    @Override protected void compute() {
      if (l >= r) return;
      if (r - l + 1 <= THRESHOLD) { Arrays.sort(a, l, r + 1); return; }
      int p = l + R.nextInt(r - l + 1);
      int pivot = a[p];
      swap(a, l, p);
      int lt = l, i = l + 1, gt = r;
      while (i <= gt) {
        if (a[i] < pivot) swap(a, lt++, i++);
        else if (a[i] > pivot) swap(a, i, gt--);
        else i++;
      }
      QTask left = new QTask(a, l, lt - 1);
      QTask right = new QTask(a, gt + 1, r);
      invokeAll(left, right);
    }
  }

  private static void swap(int[] a, int i, int j) { int t=a[i]; a[i]=a[j]; a[j]=t; }
}
