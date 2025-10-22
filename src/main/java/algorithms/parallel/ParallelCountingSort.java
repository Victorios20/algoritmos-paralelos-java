package algorithms.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinTask;
import java.util.Arrays;

public final class ParallelCountingSort {

  private static final int MAX_RANGE_SAFE = 5_000_000;

  public static void sort(int[] a, int threads) {
    if (a.length == 0) return;

    // 1) Encontrar min/max (pode reatribuir -> não é efetivamente final)
    int min = a[0], max = a[0];
    for (int v : a) { if (v < min) min = v; if (v > max) max = v; }
    int range = max - min + 1;

    // Fallback seguro quando o range é gigante/negativo
    if (range <= 0 || range > MAX_RANGE_SAFE) {
      Arrays.sort(a);
      return;
    }

    // 2) Tornar valores capturados "final"
    final int fMin = min;
    final int fRange = range;

    int t = Math.max(1, threads);
    int blocks = t;

    int n = a.length;
    final int fN = n;
    int chunk = (n + blocks - 1) / blocks;

    // Histogramas locais
    int[][] local = new int[blocks][fRange];

    ForkJoinPool pool = new ForkJoinPool(t);
    try {
      List<RecursiveAction> tasks = new ArrayList<>(blocks);
      for (int b = 0; b < blocks; b++) {
        final int bb = b;
        final int start = bb * chunk;
        final int end = Math.min(fN, start + chunk);
        tasks.add(new RecursiveAction() {
          @Override protected void compute() {
            if (start >= end) return;
            int[] hist = local[bb];
            for (int i = start; i < end; i++) {
              hist[a[i] - fMin]++;
            }
          }
        });
      }
      // Executa as tasks no pool
      pool.submit(() -> ForkJoinTask.invokeAll(tasks)).join();
    } finally {
      pool.shutdown();
    }

    // Redução dos histogramas locais -> count global
    int[] count = new int[fRange];
    for (int r = 0; r < fRange; r++) {
      int s = 0;
      for (int b = 0; b < blocks; b++) s += local[b][r];
      count[r] = s;
    }

    // Escrita ordenada
    int idx = 0;
    for (int r = 0; r < fRange; r++) {
      int c = count[r];
      while (c-- > 0) a[idx++] = r + fMin;
    }
  }
}
