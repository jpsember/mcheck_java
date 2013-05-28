package base;

import javax.swing.*;
import java.util.*;
import java.io.*;

/**
 * Application class.
 *
 * [] For GUI applications, supports running applications as applets,
 * with option for displaying program in applet's location on the web page, or
 * using that space to display a button that when pressed causes the program to
 * appear in a frame.
 *
 * [] For console (non-GUI) applications, supports application emulation for
 * applets by creating a frame that contains a console window, a command line
 * input, and optionally, a window containing a user-editable text file that
 * can be treated as if it were a file on the disk.
 *
 */
public abstract class EmApplication
    extends Application {
  /**
   * Determine string to display in console at start
   * @return String
   */
  protected String welcomeMsg() {
    return null;
  }

  /**
   */
  protected void doInit() {
    super.doInit();

    // if running as an applet,
    // we must construct a console even if we're not emulating one,
    // since it is used as the Streams.streamFromPath object.
    if (isApplet()) {
      try {
        EmulationConsole.buildConsole(consoleEmulation, workspaceFiles, true);
      }
      catch (IOException e) {
        ScanException.toss(e);
      }
    }
    if (consoleEmulation) {
      createCmds();
    }

    if (!inFrameFlag && consoleEmulation) {
      // Add a panel to contain applet contents
      JPanel p = new JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
      JLabel title = new JLabel("", JLabel.CENTER);
      p.add(title);
      getAppContentPane().add(p);
      appletPanel = p;
      appletTitleLabel = title;
    }
    updateTitle();

    if (consoleEmulation) {
      buildConsoleComponents();
      if (EmulationConsole.console() != null) {
        String s = welcomeMsg();
        if (s != null) {
          Streams.out.println(s);
        }
      }
      new CmdProcThread(this);

      showApp();
      appFrame.toFront();
    }
  }

  private void buildConsoleComponents() {
    if (appletPanel != null) {
      appletPanel.add(EmulationConsole.console().component(),
                      SwingConstants.CENTER);
    }
    else {
      JComponent jc = getAppContentPane();
      jc.add(EmulationConsole.console().component());
    }
  }

  /**
   * Process applet parameter tags
   */
  protected void processAppletParameters() {
    super.processAppletParameters();
    consoleEmulation = getAppletParameter("console", false);
    if (consoleEmulation) {
      for (int i = 0; i < MAX_WS; i++) {
        String w = getAppletParameter("w" + (i + 1), null);
        if (w != null) {
          if (!withWorkspace) {
            withWorkspace = true;
            workspaceFiles = new String[MAX_WS];
          }
          workspaceFiles[i] = w;
        }
      }
    }
  }

  // Maximum # workspace windows (W1, etc)
  private static final int MAX_WS = 2;

  /**
   * Process a command from the console
   * @param src : Console sending command
   * @param command : string user typed
   */
  void processConsoleCommand(String command) {

    boolean db = false;
    if (db) {
      Streams.out.println("processConsoleCommand command=" +
                          Scanner.debug(command));
    }

    // parse command into strings
    DArray arr = new DArray();
    Scanner tk = new Scanner(command);

    String problem = null;
    Application app = null;
    BuiltInCmd bc = null;
    String[] args = null;

    getCmd:do {
      tk.readWS();
      if (tk.eof()) {
        break;
      }

      String cmd = tk.readWordOrStr(true);

      // is this a built-in command?

      for (int i = 0; i < cmds.length; i++) {
        if (cmd.equals(cmds[i].toString())) {
          bc = cmds[i];
          break getCmd;
        }
      }

      // is there a PROGRAM in the applet file directory?

      SimFile sf =
          EmulationConsole.appletFileList().find(cmd, true);
      if (sf == null) {
        problem = "No such command or program: " + cmd;
        break;
      }
      if (!sf.type(SimFile.PROGRAM)) {
        problem = "Not a command or program: " + cmd;
        break;
      }

      // Use reflection to construct a program to handle this object
      try {
        Object obj = createObject(sf.actual());
        if (db) {
          Streams.out.println("created application object:\n" + obj);
        }

        app = (Application) obj;
      }
      catch (ScanException e) {
        problem = "Can't create instance of command " + sf.debug() + "\n" + e;
        break;
      }

    }
    while (false);

    do {
      if (problem != null) {
        Streams.out.println(problem);
        app = null;
      }

      if (bc == null && app == null) {
        help();
        break;
      }

      while (!tk.eof()) {
        String arg = tk.readWordOrStr(false);
        if (db) {
          Streams.out.println(" arg = " + Scanner.debug(arg));
        }
        arr.add(arg);
      }
      args = arr.toStringArray();

      if (bc != null) {
        if (args.length == 0) {
          bc.exec();
        }
        else {
          bc.exec(args);
        }
        break;
      }

      try {
        // call the application's main() with these arguments
        app.doMain(arr.toStringArray());
      }
      catch (Throwable t) {
        Streams.out.println(t.toString());
        t.printStackTrace();
      }
    }
    while (false);
  }

  private static void createCmds() {
    DArray d = new DArray();

    d.add(new Cmd_ls());
    d.add(new Cmd_clear());

    cmds = (BuiltInCmd[]) d.toArray(BuiltInCmd.class);
  }

  private static void help() {
    TreeSet list = new TreeSet(String.CASE_INSENSITIVE_ORDER);

    AppletFileList al = EmulationConsole.appletFileList();
    Iterator it = al.iterator();
    while (it.hasNext()) {
      SimFile sf = (SimFile) it.next();
      if (sf.type(SimFile.PROGRAM)) {
        list.add(sf.name());
      }
    }

    for (int i = 0; i < cmds.length; i++) {
      list.add(cmds[i].toString());
    }

    StringBuffer sb = new StringBuffer("Commands:");
    it = list.iterator();
    while (it.hasNext()) {
      String lbl = " " + it.next();
      if (sb.length() + lbl.length() >= 80) {
        Streams.out.println(sb.toString());
        sb.setLength(0);
      }
      sb.append(lbl);
    }
    if (sb.length() >= 0) {
      Streams.out.println(sb.toString());
    }
  }

  /**
   * Create an instance of an Application object to handle a command.
   * @param className : package & class name (without .class extension)
   * @return Object
   */
  private static Object createObject(String className) {
    Object object = null;
    try {
      Class classDefinition = Class.forName(className);
      object = classDefinition.newInstance();
    }
    catch (Exception e) {
      ScanException.toss(e);
    }
    return object;
  }

  private abstract static class BuiltInCmd {
    public void exec() {
      Streams.out.println("Missing argument");
      help();
    }

    public void exec(String[] args) {
      Streams.out.println("Unexpected argument: " + args[0]);
      help();
    }

    abstract public void help();
  }

  private static class Cmd_clear
      extends BuiltInCmd {
    public String toString() {
      return "clear";
    }

    public void help() {
      Streams.out.println("clear : clear screen");
    }

    public void exec() {
      EmulationConsole.console().textArea().cls();
    }
  }

  private static class Cmd_ls
      extends BuiltInCmd {
    public String toString() {
      return "ls";
    }

    public void help() {
      Streams.out.println("ls : list files");
    }

    public void exec() {
      dir();
    }

    public static void dir() {
      AppletFileList al = EmulationConsole.appletFileList();
      al.displayDir();
    }
  }

  protected void updateTitle() {
    super.updateTitle();
    if (appFrame == null && appletTitleLabel != null) {
      appletTitleLabel.setText(title());
    }
  }

// built-in shell commands
  private static BuiltInCmd[] cmds;

// true if W1..Wn exists
  private static boolean withWorkspace;
// files to read into workspace windows (read from "W1".."Wn" parameters)
  private static String[] workspaceFiles;

  private static JPanel appletPanel;
  private static JLabel appletTitleLabel;
// true if this is an applet emulating a console application ("CONSOLE" parameter)
  protected static boolean consoleEmulation;

}

class CmdProcThread
    implements Runnable {
  private Thread thread;
  private EmApplication app;
  public CmdProcThread(EmApplication app) {
    this.app = app;
    thread = new Thread(this);
    thread.setDaemon(true);
    thread.start();
  }

  public void run() {
    while (true) {
      String cmd = EmulationConsole.console().getCommand();

      // -----------------------------------------------------------
      // Test for user hitting enter while processing commands...

      if (false) {
        Random r = new Random();
        char k = (char) (r.nextInt(26) + 'A');
        if (true) {
          for (int i = 0; i < 60; i++) {
            Streams.out.print(k);
            if (r.nextInt(8) == 0) {
              Streams.out.println();
            }
            try {
              Thread.sleep(10);
            }
            catch (InterruptedException e) {}
          }
          Streams.out.println();
        }
        continue;
      }
      // -----------------------------------------------------------


      app.processConsoleCommand(cmd);
    }
  }

}
