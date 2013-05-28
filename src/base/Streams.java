package base;

import java.io.*;

/**
 * Utility class for streams.
 *
 */
public class Streams {
  public static boolean isApplet() {
    return appletFlag;
  }

  static boolean appletFlag;
  static StreamFromPath streamFromPath;

  /**
   * Get a buffered InputStream for reading from a file.  If running in
   * an applet context, uses file in AppletFileList.
   * If no path specified, and running in applet context, reads
   *  from Console Workspace #1, if it exists; if not, throws exception
   * @param path : path of file, or null to read from System.in.
   * @param alwaysInJAR : if true, assumes file is always in the JAR file,
   *   even if it's not running as an applet
   * @return OutputStream
   * @throws IOException
   */
  public static InputStream inputStream(String path, boolean alwaysInJAR) throws
      IOException {
    boolean db = false;
    if (db) {
      System.out.println("Streams.inputStream for " + Tools.d(path) +
                         ", applet=" + isApplet() + ", streamFromPath=" +
                         streamFromPath);
    }
    InputStream ret = null;

    // give emulation a chance to find intput stream for this path

    if (!alwaysInJAR && streamFromPath != null) {
      ret = streamFromPath.getInputStream(path);
    }
    else {
      if (path == null) {
        ret = in;
      }
      else {
        if (alwaysInJAR) {
          ret = openResource(path);
        }
        else {
          ret = new BufferedInputStream(new FileInputStream(path));
        }
      }
    }
    return ret;
  }

  /**
   * Get a buffered InputStream for reading from a file.  If running in
   * an applet context, uses file in AppletFileList.
   * If no path specified, and running in applet context, reads
   * from Console's stdin text area, or from Console main text area
   * if no stdin exists.
   * @param path : path of file, or null to read from System.in.
   * @return OutputStream
   * @throws IOException
   */
  public static InputStream inputStream(String path) throws IOException {
    return inputStream(path, false);
  }

  /**
   * Get a buffered Reader for a file.
   * If running in an applet context, gets reader for file in
   * the AppletFileList.
   * If no path specified, and running in applet context, reads
   * from Console's stdin text area, or from Console main text area
   * if no stdin exists.
   *
   * @param path String : path of file, or null to read from System.in
   * @return Reader
   */
  public static Reader reader(String path, boolean alwaysInJAR) throws
      IOException {
    return new InputStreamReader(inputStream(path, alwaysInJAR));
  }

  /**
   * Get a buffered Reader for a file.
   * If running in an applet context, gets reader for file in
   * the AppletFileList.
   * If no path specified, and running in applet context, reads
   * from Console's stdin text area, or from Console main text area
   * if no stdin exists.
   *
   * @param path String : path of file, or null to read from System.in
   * @return Reader
   */
  public static Reader reader(String path) throws IOException {
    return reader(path, false);
  }

  /**
   * Get a buffered OutputStream for writing to a file.  If running in
   * an applet context, gets writer for file in AppletFileList.
   * @param path : path of file
   * @return OutputStream
   * @throws IOException
   */
  public static OutputStream outputStream(String path) throws IOException {
    OutputStream r = null;
    do {

      // give emulation a chance to find output stream for this path

      if (streamFromPath != null) {
        r = streamFromPath.getOutputStream(path);
      }
      else {
        // if path undefined, use system.out
        if (path == null) {
          r = out;
        }
        else {
          r = new BufferedOutputStream(new FileOutputStream(path));
        }
      }
    }
    while (false);
    return r;
  }

  /**
   * Get a buffered Writer for writing to a file.
   * If running in an applet context, gets writer for file in AppletFileList.
   * @param path : path of file, or null to construct a
   *   non-closing writer for System.out
   * @return Writer
   * @throws IOException
   */
  public static Writer writer(String path) throws IOException {

    Writer w = null;
    if (path == null) {
      w = new NCOutputStreamWriter(outputStream(path));
    }
    else {
      w = new OutputStreamWriter(outputStream(path));
    }
    return w;
  }

  /**
   * Get an input stream to a data file, which may be stored in the class
   * folder or one of its subfolders.  This is how to access files in a jar.
   * @param path String : name of file
   * @return BufferedInputStream
   */
  public static BufferedInputStream openResource(String path) throws
      IOException {
    return openResource(mainClass, path);
  }

  /**
   * Set the class that defines where resources are located.  If left undefined,
   * will attempt to load data files from the current directory.
   * @param c Class
   */
  public static void loadResources(Object main) {
    if (mainClass == null) {
      if (main instanceof Class) {
        mainClass = (Class) main;
      }
      else {
        mainClass = main.getClass();
      }
    }
  }

  private static Class mainClass;

  /**
   * Get an input stream to a data file, which is stored in the
   * class folder (or one of its subfolders)
   * @param path String : path to file
   * @return BufferedInputStream
   * @throws IOException
   */
  public static BufferedInputStream openResource(Class c, String path) throws
      IOException {

    BufferedInputStream out = null;
    if (c == null) {
      out = new BufferedInputStream(new FileInputStream(path));
    }
    else {
      InputStream is = c.getResourceAsStream(path);
      if (is == null) {
        throw new FileNotFoundException("openResource failed: " + path);
      }
      out = new BufferedInputStream(is);
    }
    return out;
  }

  private static class NonClosingSystemOut
      extends PrintStream {
    public NonClosingSystemOut() {
      super(System.out, true);
    }

    public void close() {
      flush();
    }
  }

  private static class NonClosingSystemIn
      extends BufferedInputStream {
    public NonClosingSystemIn() {
      super(System.in);
    }

    public void close() {
    }
  }

  static void redirectOutput(PrintStream newOutput) {
    out = newOutput;
  }

  // InputStream to use instead of System.in
  public static InputStream in = new NonClosingSystemIn();
  // PrintStream to use instead of System.out
  public static PrintStream out = new NonClosingSystemOut();
}
