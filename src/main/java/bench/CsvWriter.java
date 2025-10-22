package bench;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class CsvWriter implements Closeable, Flushable {
  private final BufferedWriter bw;

  /** Construtor antigo (compat) — modo append por padrão. */
  public CsvWriter(String path, List<String> header) throws IOException {
    this(path, header, true);
  }

  /** Novo construtor: controla se abre em append (true) ou sobrescreve (false). */
  public CsvWriter(String path, List<String> header, boolean append) throws IOException {
    Path p = Path.of(path);
    boolean exists = Files.exists(p);

    if (append) {
      // cria se não existe; escreve cabeçalho só na primeira vez
      this.bw = Files.newBufferedWriter(
          p,
          exists ? new StandardOpenOption[]{StandardOpenOption.APPEND}
                 : new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE}
      );
      if (!exists) writeRow(header);
    } else {
      // sempre sobrescreve e reescreve o cabeçalho
      this.bw = Files.newBufferedWriter(
          p,
          StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
      );
      writeRow(header);
    }
  }

  public void writeRow(List<String> cols) throws IOException {
    bw.write(String.join(",", cols));
    bw.newLine();
  }

  @Override public void flush() throws IOException { bw.flush(); }
  @Override public void close() throws IOException { bw.close(); }
}
