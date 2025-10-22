package bench;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ResultCharts extends JFrame {
  private static final long serialVersionUID = 1L;

  static final class Row {
    String algoritmo, versao, dataset;
    int tamanho, threads, amostra;
    double tempo_ms;
    Double speedup, eficiencia;
  }

  private final java.util.List<Row> rows;
  private final JComboBox<String> algoBox;
  private final JComboBox<String> dataBox;
  private final JComboBox<Integer> sizeBox;
  private final JPanel chartPanel;
  private final JRadioButton rbSpeedup, rbTempo;

  public ResultCharts(java.util.List<Row> rows) {
    super("Gráficos - Serial vs Paralelo");
    this.rows = rows;
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1000, 640);
    setLocationRelativeTo(null);

    // filtros
    var algos = rows.stream().map(r -> r.algoritmo).distinct().sorted().toArray(String[]::new);
    var datasets = rows.stream().map(r -> r.dataset).distinct().sorted().toArray(String[]::new);
    var sizes = rows.stream().map(r -> r.tamanho).distinct().sorted().collect(Collectors.toList());

    algoBox = new JComboBox<>(algos);
    dataBox = new JComboBox<>(datasets);
    sizeBox = new JComboBox<>(sizes.toArray(new Integer[0]));

    rbSpeedup = new JRadioButton("Speedup × Threads", true);
    rbTempo = new JRadioButton("Tempo(ms) × Threads");
    var bg = new ButtonGroup(); bg.add(rbSpeedup); bg.add(rbTempo);

    var top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(new JLabel("Algoritmo:")); top.add(algoBox);
    top.add(new JLabel("Dataset:")); top.add(dataBox);
    top.add(new JLabel("Tamanho:")); top.add(sizeBox);
    top.add(rbSpeedup); top.add(rbTempo);
    add(top, BorderLayout.NORTH);

    chartPanel = new JPanel() {
      @Override public Dimension getPreferredSize() { return new Dimension(1000, 560); }
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawChart((Graphics2D) g);
      }
    };
    add(chartPanel, BorderLayout.CENTER);

    var listener = (java.awt.event.ItemListener) e -> chartPanel.repaint();
    algoBox.addItemListener(listener);
    dataBox.addItemListener(listener);
    sizeBox.addItemListener(listener);
    rbSpeedup.addItemListener(listener);
    rbTempo.addItemListener(listener);
  }

  private void drawChart(Graphics2D g2) {
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    String algo = (String) algoBox.getSelectedItem();
    String ds = (String) dataBox.getSelectedItem();
    Integer n = (Integer) sizeBox.getSelectedItem();
    if (algo == null || ds == null || n == null) return;

    // agrega por threads (média das amostras)
    Map<Integer, List<Row>> byThreads = rows.stream()
      .filter(r -> r.algoritmo.equals(algo) && r.dataset.equals(ds) && r.tamanho == n && r.versao.equals("parallel"))
      .collect(Collectors.groupingBy(r -> r.threads));

    java.util.List<Integer> xThreads = byThreads.keySet().stream().sorted().collect(Collectors.toList());
    if (xThreads.isEmpty()) return;

    java.util.List<Double> yValues = new ArrayList<>();
    boolean plotSpeedup = rbSpeedup.isSelected();
    for (Integer th : xThreads) {
      List<Row> rs = byThreads.get(th);
      double v = rs.stream().mapToDouble(r -> plotSpeedup && r.speedup != null ? r.speedup : r.tempo_ms).average().orElse(Double.NaN);
      yValues.add(v);
    }

    // layout simples
    int W = chartPanel.getWidth(), H = chartPanel.getHeight();
    int left = 80, right = 40, top = 40, bottom = 60;
    int w = W - left - right, h = H - top - bottom;

    // fundo
    g2.setColor(Color.white); g2.fillRect(0,0,W,H);
    g2.setColor(Color.black);
    g2.drawString(algo + " / " + ds + " / N=" + n + (plotSpeedup ? "  (Speedup)" : "  (Tempo ms)"), left, 20);

    // eixo
    double maxY = yValues.stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
    if (maxY <= 0) maxY = 1.0;
    // um pouco de margem
    maxY *= 1.1;

    g2.drawLine(left, H - bottom, left, top);
    g2.drawLine(left, H - bottom, W - right, H - bottom);

    // grades horizontais
    g2.setColor(new Color(230,230,230));
    int grid = 5;
    for (int i=1;i<=grid;i++){
      int y = (int)(H - bottom - (i*(h/(double)grid)));
      g2.drawLine(left, y, W - right, y);
    }

    // eixo Y labels
    g2.setColor(Color.black);
    for (int i=0;i<=grid;i++){
      double val = (i*maxY/grid);
      int y = (int)(H - bottom - (i*(h/(double)grid)));
      g2.drawString(String.format(Locale.US, "%.2f", val), 10, y+5);
    }

    // desenhar barras/linha
    int npts = xThreads.size();
    if (plotSpeedup) {
      // barras
      int barW = Math.max(20, (int)(w/(npts*1.5)));
      int gap = (w - npts*barW) / Math.max(1, npts+1);
      int x = left + gap;
      g2.setColor(new Color(120,160,255));
      for (int i=0;i<npts;i++){
        double val = yValues.get(i);
        int barH = (int)(val * h / maxY);
        int y = H - bottom - barH;
        g2.fillRect(x, y, barW, barH);
        g2.setColor(Color.black);
        g2.drawRect(x, y, barW, barH);
        g2.drawString("t="+xThreads.get(i), x, H - bottom + 15);
        g2.setColor(new Color(120,160,255));
        x += barW + gap;
      }
    } else {
      // linha
      g2.setColor(new Color(80,140,240));
      int prevX = -1, prevY = -1;
      for (int i=0;i<npts;i++){
        double val = yValues.get(i);
        int x = (int)(left + (i*(w/(double)(npts-1))));
        int y = (int)(H - bottom - (val * h / maxY));
        g2.fill(new Ellipse2D.Double(x-3, y-3, 6, 6));
        if (prevX >= 0) g2.drawLine(prevX, prevY, x, y);
        prevX = x; prevY = y;
        g2.setColor(Color.black);
        g2.drawString("t="+xThreads.get(i), x-10, H - bottom + 15);
        g2.setColor(new Color(80,140,240));
      }
    }
  }

  // ===== CSV loader =====
  public static java.util.List<Row> load(String path) throws IOException {
    var rows = new ArrayList<Row>();
    try (var br = Files.newBufferedReader(Path.of(path))) {
      String head = br.readLine(); // header
      String line;
      while ((line = br.readLine()) != null) {
        String[] c = line.split(",", -1);
        if (c.length < 10) continue;
        Row r = new Row();
        r.algoritmo = c[0]; r.versao = c[1]; r.dataset = c[2];
        r.tamanho = Integer.parseInt(c[3]); r.threads = Integer.parseInt(c[4]);
        r.amostra = Integer.parseInt(c[5]); r.tempo_ms = parseD(c[6]);
        r.speedup = c[7].isEmpty() ? null : parseD(c[7]);
        r.eficiencia = c[8].isEmpty() ? null : parseD(c[8]);
        rows.add(r);
      }
    }
    return rows;
  }
  private static double parseD(String s){ return Double.parseDouble(s.replace(",", ".")); }

  public static void main(String[] args) throws Exception {
    String file = args.length > 0 ? args[0] : "results.csv";
    java.util.List<Row> rows = load(file);
    SwingUtilities.invokeLater(() -> new ResultCharts(rows).setVisible(true));
  }
}
