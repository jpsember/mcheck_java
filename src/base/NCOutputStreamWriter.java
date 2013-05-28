package base;

import java.io.*;

/**
 * Non-closing OutputStream for use with OutputStreams that are derived
 * from System.out
 */
class NCOutputStreamWriter
    extends OutputStreamWriter {
  public NCOutputStreamWriter(OutputStream s) {
    super(s);
  }

  public void close() throws IOException {
    flush();
  }

}
