package bench;

public class Main {
  public static void main(String[] args) throws Exception {
    System.out.println("Iniciando benchmark serial...");
    BenchmarkRunner.runSerialToCsv("results.csv");
    System.out.println("Concluído. CSV gerado em results.csv");
  }
}
