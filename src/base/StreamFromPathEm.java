package base;
import java.io.*;

/**
 * This interface allows paths to be given as input and
 * produce Input/Output streams, and is used for Applet application emulation.
 */
interface StreamFromPathEm  {
//  public InputStream getInputStream(String path) throws IOException;
//  public OutputStream getOutputStream(String path) throws IOException;
  public DArray getFileList(String dir, String extension);
  public FileChooser getFileChooser();
  public String readLineFromConsole(String prompt, CommandHistory history);
}
