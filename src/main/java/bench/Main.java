package bench;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.SwingUtilities;

public class Main {
  public static void main(String[] args) throws Exception {
    // 1) Gera nome com timestamp para não sobrescrever
    String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String csv = "results_" + stamp + ".csv";

    System.out.println("Iniciando benchmark (serial + paralelo)...");
    BenchmarkRunner.runAllToCsv(csv);
    System.out.println("Concluído. CSV gerado em " + csv);

    // 2) Abre o viewer de gráficos já com o CSV recém-gerado
    try {
      var rows = ResultCharts.load(csv);
      SwingUtilities.invokeLater(() -> new ResultCharts(rows).setVisible(true));
    } catch (Exception e) {
      System.err.println("Falha ao abrir gráficos: " + e.getMessage());
      // não interrompe; só informa no console
    }
  }
}
