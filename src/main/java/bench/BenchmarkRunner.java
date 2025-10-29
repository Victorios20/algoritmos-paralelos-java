package bench;

import algorithms.CountingSort;
import algorithms.InsertionSort;
import algorithms.MergeSort;
import algorithms.QuickSort;
import algorithms.parallel.ParallelCountingSort;
import algorithms.parallel.ParallelMergeSort;
import algorithms.parallel.ParallelQuickSort;
import algorithms.parallel.ParallelInsertionSort;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Locale;

public final class BenchmarkRunner {

  // ===== Configuração dos testes =====
  private static final String[] ALGOS    = {"Insertion","Counting","Merge","Quick"};
  private static final String[] DATASETS = {"random","reversed","nearly","dupes"};
  private static final int[] SIZES       = {10_000, 50_000, 100_000};
  private static final int[] THREADS     = {2, 4, 8};
  private static final int WARMUPS       = 3;
  private static final int SAMPLES       = 5;
  private static final boolean ENABLE_PARALLEL_INSERTION = true;

  public static void runAllToCsv(String csvPath) throws Exception {
    try (CsvWriter csv = new CsvWriter(csvPath, header(), /*append=*/false)) {

      // baseline: mediana serial por (algo,dataset,size)
      Map<String, Double> serialMedian = new ConcurrentHashMap<>();

      for (String algo : ALGOS) {
        for (String ds : DATASETS) {
          for (int n : SIZES) {

            // ---------- SERIAL ----------
            // warm-up serial
            for (int w = 0; w < WARMUPS; w++) {
              int[] warm = makeData(ds, n);
              sortSerial(algo, warm);
            }

            // amostras seriais
            double[] tSerial = new double[SAMPLES];
            for (int s = 1; s <= SAMPLES; s++) {
              int[] base = makeData(ds, n);
              int[] a = Arrays.copyOf(base, base.length);

              long t0 = Timer.now();
              sortSerial(algo, a);
              long t1 = Timer.now();

              boolean ok = DataSetFactory.isSorted(a);
              double ms = Timer.ms(t0, t1);
              tSerial[s - 1] = ms;

              csv.writeRow(row(algo, "serial", ds, n, 1, s, ms, "", "", ok ? "" : "NOT_SORTED"));
            }
            Arrays.sort(tSerial);
            double medSerial = tSerial[tSerial.length / 2];
            serialMedian.put(key(algo, ds, n), medSerial);
            csv.writeRow(row(algo, "serial-summary", ds, n, 1, 0, medSerial, "", "", "median"));

            // ✅ também escrevemos um summary como se fosse parallel(threads=1)
            csv.writeRow(row(algo, "parallel-summary", ds, n, 1, 0, medSerial, "1.000", "1.000", "median-serial-as-threads1"));

            // ---------- PARALELO ----------
            for (int th : THREADS) {
              // warm-up paralelo
              for (int w = 0; w < WARMUPS; w++) {
                int[] warm = makeData(ds, n);
                sortParallel(algo, warm, th);
              }

              // amostras paralelas
              double[] tPar = new double[SAMPLES];
              for (int s = 1; s <= SAMPLES; s++) {
                int[] base = makeData(ds, n);
                int[] a = Arrays.copyOf(base, base.length);

                long t0 = Timer.now();
                sortParallel(algo, a, th);
                long t1 = Timer.now();

                boolean ok = DataSetFactory.isSorted(a);
                double ms = Timer.ms(t0, t1);
                tPar[s - 1] = ms;

                Double baseMed = serialMedian.get(key(algo, ds, n));
                String speedup    = baseMed != null ? fmt(baseMed / ms) : "";
                String eficiencia = baseMed != null ? fmt((baseMed / ms) / th) : "";

                csv.writeRow(row(algo, "parallel", ds, n, th, s, ms, speedup, eficiencia, ok ? "" : "NOT_SORTED"));
              }

              // summary paralelo (mediana por threads)
              Arrays.sort(tPar);
              double medPar = tPar[tPar.length / 2];
              String spMed  = fmt(medSerial / medPar);
              String efMed  = fmt((medSerial / medPar) / th);
              csv.writeRow(row(algo, "parallel-summary", ds, n, th, 0, medPar, spMed, efMed, "median"));
            }
          }
        }
      }
      csv.flush();
    }
  }

  // ===== Helpers =====
  private static String key(String algo, String ds, int n) { return algo + "|" + ds + "|" + n; }

  private static List<String> header() {
    return List.of("algoritmo","versao","dataset","tamanho","threads","amostra","tempo_ms","speedup","eficiencia","obs");
  }

  private static List<String> row(String alg, String ver, String ds, int n, int th, int sample, double ms, String sp, String ef, String obs) {
    List<String> r = new ArrayList<>(10);
    r.add(alg); r.add(ver); r.add(ds);
    r.add(Integer.toString(n)); r.add(Integer.toString(th));
    r.add(Integer.toString(sample)); r.add(fmt(ms));
    r.add(sp == null ? "" : sp);
    r.add(ef == null ? "" : ef);
    r.add(obs == null ? "" : obs);
    return r;
  }

  private static String fmt(double v) {
    return String.format(Locale.US, "%.3f", v);
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

  private static void sortParallel(String algo, int[] a, int threads) {
    switch (algo) {
      case "Insertion" -> {
        if (ENABLE_PARALLEL_INSERTION) ParallelInsertionSort.sort(a, threads);
        else InsertionSort.sort(a); // mantém serial como controle
      }
      case "Counting"  -> ParallelCountingSort.sort(a, threads);
      case "Merge"     -> ParallelMergeSort.sort(a, threads);
      case "Quick"     -> ParallelQuickSort.sort(a, threads);
      default -> throw new IllegalArgumentException("Algo desconhecido: " + algo);
    }
  }
}
