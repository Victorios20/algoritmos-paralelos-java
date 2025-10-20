package algorithms;

public final class MergeSort {
  public static void sort(int[] a) {
    if (a.length < 2) return;
    int[] aux = new int[a.length];
    sort(a, aux, 0, a.length - 1);
  }
  private static void sort(int[] a, int[] aux, int l, int r) {
    if (l >= r) return;
    int m = (l + r) >>> 1;
    sort(a, aux, l, m);
    sort(a, aux, m + 1, r);
    merge(a, aux, l, m, r);
  }
  private static void merge(int[] a, int[] aux, int l, int m, int r) {
    int i = l, j = m + 1, k = l;
    while (i <= m && j <= r) aux[k++] = (a[i] <= a[j]) ? a[i++] : a[j++];
    while (i <= m) aux[k++] = a[i++];
    while (j <= r) aux[k++] = a[j++];
    for (i = l; i <= r; i++) a[i] = aux[i];
  }
}
