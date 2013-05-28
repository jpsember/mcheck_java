package base;

import java.io.*;

public class Streams2 {
  /**
   * Get a FileChooser appropriate to the applet/application context.
   * @return FileChooser
   */
  public static FileChooser fileChooser() {
    FileChooser c = null;
    if (Streams.streamFromPath != null) {
      c = ( (StreamFromPathEm) Streams.streamFromPath).getFileChooser();
    }
    if (c == null) {
      c = new ApplicationFileChooser();
    }
    return c;
  }

  /**
   * Get all files of a particular type
   * @param dir : directory to examine
   * @param extension : extension to filter by
   * @return DArray : an array of strings
   */
  public static DArray getFileList(String dir, String extension) {
    DArray list = null;
    if (Streams.streamFromPath != null) {
      list = ( (StreamFromPathEm) Streams.streamFromPath).getFileList(dir,
          extension);
    }

    if (list == null) {
      File f = new File(dir);
      if (!f.isDirectory()) {
        f = f.getParentFile();
      }

      list = Path.getFileList(f, extension, true);
    }
    return list;
  }

  static void ensureApplet() {
    if (!Streams.isApplet()) {
      throw new RuntimeException("Not an applet");
    }
  }

  /**
   * Copy a file.
   * @param inp InputStream containing file
   * @param dest : file to write
   */
  public static void copyFile(InputStream inp, File dest,
                              boolean overwriteExisting) throws IOException {

    if (!overwriteExisting && dest.exists()) {
      throw new IOException("Cannot overwrite " + dest.getAbsolutePath());
    }

    OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
    final int BUFF_SIZE = 4096;
    byte[] buff = new byte[BUFF_SIZE];
    while (true) {
      int len = inp.read(buff);
      if (len < 0) {
        break;
      }
      out.write(buff, 0, len);
    }
    out.close();
  }

  /**
   * Read a file into a string
   * @param path String
   * @return String
   */
  public static String readTextFile(String path) throws IOException {
    return Streams2.readTextFile(path, false);
  }

  public static String readTextFile(String path, boolean withinJAR) throws
      IOException {
    StringBuffer sb = new StringBuffer();
    Reader r = Streams.reader(path, withinJAR);
    while (true) {
      int c = r.read();
      if (c < 0) {
        break;
      }
      sb.append( (char) c);
    }
    r.close();
    return sb.toString();
  }

  public static PrintStream printStream(String path) throws IOException {
    OutputStream out = Streams.outputStream(path);
    return (path == null) ? new NCPrintStream(out) :
        new PrintStream(out);
  }

  public static void writeTextFile(String path, String content) {
    try {
      Writer w = Streams.writer(path);
      w.write(content);
      w.close();
    }
    catch (IOException e) {
      ScanException.toss(e);
    }
  }

  /**
   * Read a line of text from System.in, or from the console if appropriate
   * @return String
   */
  public static String readLine(String prompt, CommandHistory history) {
    String s = null;

    if (Streams.streamFromPath != null) {
      s = ( (StreamFromPathEm) Streams.streamFromPath).readLineFromConsole(
          prompt, history);
    }
    else {
      Streams.out.print(prompt);
      StringBuffer sb = new StringBuffer();
      try {
        while (true) {
          int c = Streams.in.read();
          if (c < 0) {
            break;
          }
          Streams.out.print( (char) c);
          if (c == '\n') {
            break;
          }
          sb.append( (char) c);
        }
      }
      catch (IOException e) {
        Streams.out.println("readLine IOException: " + e);
      }
      s = sb.toString().trim();
    }
    return s;
  }

}
