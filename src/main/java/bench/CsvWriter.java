package bench;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class CsvWriter implements Closeable, Flushable {
  private final BufferedWriter bw;

  public CsvWriter(String path, List<String> header) throws IOException {
    Path p = Path.of(path);
    boolean exists = Files.exists(p);
    this.bw = Files.newBufferedWriter(p, exists ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
    if (!exists) writeRow(header);
  }

  public void writeRow(List<String> cols) throws IOException {
    bw.write(String.join(",", cols));
    bw.newLine();
  }

  public void flush() throws IOException { bw.flush(); }
  public void close() throws IOException { bw.close(); }
}
