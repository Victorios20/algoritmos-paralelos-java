package bench;

import algorithms.CountingSort;
import algorithms.InsertionSort;
import algorithms.MergeSort;
import algorithms.QuickSort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BenchmarkRunner {

  private static final String[] ALGOS = {"Insertion","Counting","Merge","Quick"};
  private static final String[] DATASETS = {"random","reversed","nearly","dupes"};
  private static final int[] SIZES = {10_000, 50_000, 100_000}; // ajuste depois
  private static final int SAMPLES = 5;

  public static void runSerialToCsv(String csvPath) throws Exception {
    try (CsvWriter csv = new CsvWriter(csvPath, header())) {
      for (String algo : ALGOS) {
        for (String ds : DATASETS) {
          for (int n : SIZES) {
            for (int s = 1; s <= SAMPLES; s++) {
              int[] base = makeData(ds, n);
              int[] a = Arrays.copyOf(base, base.length);
              long t0 = Timer.now();
              sortSerial(algo, a);
              long t1 = Timer.now();
              boolean ok = DataSetFactory.isSorted(a);
              List<String> row = row(algo, "serial", ds, n, 1, s, Timer.ms(t0, t1), ok ? "" : "NOT_SORTED");
              csv.writeRow(row);
            }
          }
        }
      }
      csv.flush();
    }
  }

  private static List<String> header() {
    return List.of("algoritmo","versao","dataset","tamanho","threads","amostra","tempo_ms","obs");
  }

  private static List<String> row(String alg, String ver, String ds, int n, int th, int sample, double ms, String obs) {
    List<String> r = new ArrayList<>(8);
    r.add(alg); r.add(ver); r.add(ds);
    r.add(Integer.toString(n)); r.add(Integer.toString(th));
    r.add(Integer.toString(sample)); r.add(String.format(java.util.Locale.US,"%.3f", ms));
    r.add(obs == null ? "" : obs);
    return r;
  }

  private static int[] makeData(String ds, int n) {
    return switch (ds) {
      case "reversed" -> DataSetFactory.reversed(n);
      case "nearly"   -> DataSetFactory.nearlySorted(n);
      case "dupes"    -> DataSetFactory.manyDuplicates(n);
      default         -> DataSetFactory.random(n);
    };
  }

  private static void sortSerial(String algo, int[] a) {
    switch (algo) {
      case "Insertion" -> InsertionSort.sort(a);
      case "Counting"  -> CountingSort.sort(a);
      case "Merge"     -> MergeSort.sort(a);
      case "Quick"     -> QuickSort.sort(a);
      default -> throw new IllegalArgumentException("Algo desconhecido: " + algo);
    }
  }
}
