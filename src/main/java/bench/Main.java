package bench;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
  public static void main(String[] args) throws Exception {
    // Gera nome com timestamp para não sobrescrever resultados anteriores
    String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String csv = "results_" + stamp + ".csv";

    System.out.println("Iniciando benchmark (serial + paralelo)...");
    BenchmarkRunner.runAllToCsv(csv);
    System.out.println("Concluído. CSV gerado em " + csv);
  }
}
