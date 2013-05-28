package base;

import java.io.*;

public class MacroReaderTest {

  /**
   * Test program for MacroReader class.
   *
   * @param args : list of filenames to read and echo with substitutions.
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    MacroReader r = null;

    for (int j = 0; j < args.length; j++) {
      r = new MacroReader(Streams.reader(args[j]), args[j], r);
      int c;
      while ( (c = r.read()) >= 0) {
        System.out.write(c);
      }
      r.close();
    }
  }
}
