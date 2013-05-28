package base;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class EmulationConsole
    implements StreamFromPath, StreamFromPathEm {

  static final boolean DQ = false;

  public String getCommand() {
    return console2.getCommand(">", null);
  }

  /**
   * Get input stream from stdin TextArea, if one exists; otherwise,
   * return null
   * @return InputStreamC
   */
  InputStream workspaceInputStream(int index) {
    InputStream out = null;
    JTextArea ta = workspaceTextArea(index);
    if (ta != null) {
      out = new StringInputStream(ta.getText());
    }
    return out;
  }

  /**
   * Get the output PrintStream (the main TextArea of the console)
   * @return PrintStream
   */
  public PrintStream getPrintStream() {
    return console2.getPrintStream();
  }

  public PrintStream workspaceOutputStream(int index) {
    return workspaces[index].printStream();
  }

//  public void setReady() {
//    console2.setReady();
//  }

  /**
   * Get the component containing the console and its related components
   * @return Component
   */
  public Component component() {
    return component;
  }

  public WorkTextArea textArea() {
    return console2.textArea();
  }

  /**
   * Get reference to the singleton console
   * @return EmulationConsole
   */
  public static EmulationConsole console() {
    return theConsole;
  }

  /**
   * Get applet file list.  Should be called within a simulated application
   * context to get the list of files available to the applet.
   * @return AppletFileList
   */
  public static AppletFileList appletFileList() {
    return theConsole.afList;
  }

  public static void buildConsole(boolean shown, String[] wsFiles,
                                  boolean includeTitle) throws IOException {
    new EmulationConsole(shown, wsFiles, includeTitle);
  }

  /**
   * Construct a Console
   * @param wsFiles : if not null, includes workspace windows, with
   *  this an array of filenames containing initial contents of these windows
   */
  private EmulationConsole(boolean shown, String[] wsFiles, boolean includeTitle) throws
      IOException {

    final boolean db = false;

    if (db) {
      System.out.println("Constructing console");
    }

    Streams.streamFromPath = this;
    theConsole = this;

if (shown) {
    console2 = new Console(includeTitle ? "Console" : null, null);
    Streams.redirectOutput(console2.getPrintStream());
//Streams.out.println("Just redirected output to console...");
}

    // have file system attempt to find and parse a JAR directory file.
    afList.processDirFile();

if (shown) {
    if (wsFiles != null) {

      workspaces = new Workspace[wsFiles.length];
      JComponent c = null;
      for (int i = 0; i < wsFiles.length; i++) {
        String f = wsFiles[i];
        if (db) {
          System.out.println(" workspace file " + i + " is " + f);
        }
        if (f == null) {
          continue;
        }

        Workspace w = new Workspace(i, null);
        workspaces[i] = w;

        if (!f.equals("_")) {
          if (db) {
            System.out.println("Attempting to read " + f +
                               " from file to workspace");
          }

          w.readFromFile(f);
        }

        JComponent cc = w.component();

        if (c == null) {
          c = cc;
        }
        else {
          JSplitPane splitPane = new JSplitPane(
              JSplitPane.HORIZONTAL_SPLIT, false, c, cc);

          splitPane.setResizeWeight(.5);
          // Provide minimum sizes for the two components in the split pane.
          Dimension minimumSize = new Dimension(50, 50);
          c.setMinimumSize(minimumSize);
          cc.setMinimumSize(minimumSize);
          splitPane.setMinimumSize(new Dimension(120, 50));
          c = splitPane;
        }

        if (c != null) {
          JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                                false, c, console2.component());

          //Provide minimum sizes for the two components in the split pane.
          c.setMinimumSize(new Dimension(50, 100));
          console2.component().setMinimumSize(new Dimension(50, 200));
          splitPane.setResizeWeight(.7);
          splitPane.setOneTouchExpandable(true);

          component = splitPane;
        }
      }
    }
    else {
      component = console2.component();
    }
  }
      }


  /**
   * Get JTextArea for workspace
   * @return JTextArea
   */
  public JTextArea workspaceTextArea(int wIndex) {
    JTextArea ta = null;
    if (workspaces[wIndex] != null) {
      ta = workspaces[wIndex].textArea();
    }
    return ta;
  }

  public boolean withWorkspaces() {
    return workspaces != null;
  }

  /**
   * Determine which workspace, if any, is associated with a filename.
   * @param path String
   * @param defaultWS : index of workspace to use if path is null
   * @return Integer : null if no association, else 0..n-1
   */
  public Integer workspace(String path, int defaultWS) {
    Integer out = null;
    if (path == null) {
      path = Integer.toString(1 + defaultWS);
    }
    if (path.length() == 1
        && workspaces != null) {
      int w = path.charAt(0) - '1';
      if (w >= 0 && w < workspaces.length) {
        out = new Integer(w);
      }
    }
    return out;
  }

  /**
   * If workspace hasn't already been initialized to some contents,
   * set it to a string.
   * @param s String
   */
  public void initializeWorkspace(int index, String s) {
    if (!stdInInitFlag) {
      stdInInitFlag = true;
      JTextArea t = workspaceTextArea(index);

      if (t != null) {
        t.append(s);
      }
    }
  }

  // ---------------------------------------------------------------
  // StreamFromPath interface
  // ---------------------------------------------------------------
  public InputStream getInputStream(String path) throws IOException {

    boolean db = false;
    if (db) {
      System.out.println("getInputStream for " + path);
    }
    InputStream ret = null;

    Integer wnum = null;
    // If no console exists yet, don't perform test.
    wnum = workspace(path, 0);
    if (db) {
      System.out.println(" wnum is " + wnum);
    }

    if (wnum != null) {
      ret = workspaceInputStream(wnum.intValue());
    }
    else {
      ret = appletFileList().getInputStream(path);
    }
    return ret;
  }

  public FileChooser getFileChooser() {
    return new AppletFileChooser();
  }

  public DArray getFileList(String dir, String extension) {
    DArray list = new DArray();
    SimFile[] f = appletFileList().getFiles();
    for (int i = 0; i < f.length; i++) {
      SimFile s = f[i];
      if (s.hidden()) {
        continue;
      }
      if (Path.getExtension(s.name()).equals(extension)) {
        list.add(s.name());
      }
    }
    return list;
  }

  public OutputStream getOutputStream(String path) throws IOException {
    OutputStream r = null;
    do {
      if (withWorkspaces()) {
        Integer w = workspace(path, 1);
        if (w != null) {
          r = workspaceOutputStream(w.intValue());
          break;
        }
      }
      if (path == null) {
        r = Streams.out;
      }
      else {
        r = appletFileList().getOutputStream(path);
      }
    }
    while (false);
    return r;
  }
  public String readLineFromConsole(String prompt, CommandHistory history) {
    return console2.getCommand(prompt, history);
  }

  // ---------------------------------------------------------------

  // reference to singleton console
  private static EmulationConsole theConsole;
  // the file structure for applets running application simulation
//  private static AppletFileList afList;

  //    ensureApplet();
  private AppletFileList afList = new AppletFileList();

  private Workspace[] workspaces;
  private boolean stdInInitFlag;

  private Container component;
  private Console console2;
}
