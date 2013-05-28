package base;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class Console
    implements FocusListener {

  // Thread message issues:

  // Console uses keyBuffer to notify CommandThread when new KeyEvent is available
  // CommandThread(ActiveCommandProcessor) uses Console
  //   to notify Console when command is ready
  // Console uses itself to notify CommandThread when command has been processed
  // Console uses cmdHandlerLock to notify CommandThread that handler may
  //    be available

  /**
   * Have the main console TextArea request the focus
   */
  private void requestFocus() {
    textArea.requestFocus();
  }

  /**
   * Get the output PrintStream (the main TextArea of the console)
   * @return PrintStream
   */
  public PrintStream getPrintStream() {
    return out;
  }

  /**
   * Print prompt
   */
  private void printPrompt() {
    synchronized (this) {
      out.print(prompt);
      out.flush();
    }
  }

//  /**
//   * Prepare for a new command.
//   * Prints prompt.
//   */
//  public void setReady() {
//    Tools.warn("setReady now doing nothing...");
//  }

  private void setReady2() {
    if (acceptsCommands()) {
      printPrompt();
    }
    out.flush();
    requestFocus();
    textArea.ensureCursorVisible();
  }

  /**
   * Save command processor and history onto stack, and replace with new.
   *
   * @param p CommandProcessor
   */
  public void pushCommandProcessor(CommandProcessor p) {

    // when changing active cmdHandler, make sure cmd thread not trying
    // to issue commands to it.
    synchronized (cmdHandlerLock) {
      if (cmdHandler != null) {
        cmdProcessors.push(cmdHandler);
        cmdProcessors.push(history);
      }
      if (p != null) {
        cmdHandler = p;
        history = p.getCommandHistory();
        if (history == null) {
          history = new CommandHistory();
        }
        prompt = p.getPrompt();
      }
      textArea.setEditable(p != null);
      // notify CommandThread that cmdHandler may now exist
      cmdHandlerLock.notify();
    }
  }

  /**
   * Restore command processor and history from stack
   */
  public void popCommandProcessor() {

    // when changing active cmdHandler, make sure cmd thread not trying
    // to issue commands to it.
    synchronized (cmdHandlerLock) {
      history = null;
      cmdHandler = null;
      if (!cmdProcessors.isEmpty()) {
        history = (CommandHistory) cmdProcessors.pop();
        cmdHandler = (CommandProcessor) cmdProcessors.pop();
      }
      if (cmdHandler == null) {
        textArea.setEditable(false);
      }
      else {
        prompt = cmdHandler.getPrompt();
      }
      // notify CommandThread that cmdHandler may now exist
      cmdHandlerLock.notify();
    }
  }

  /**
   * Get the component containing the console and its related components
   * @return Component
   */
  public JComponent component() {
    return component;
  }

  /**
   * Get the text area associated with the console
   * @return WorkTextArea
   */
  public WorkTextArea textArea() {
    return textArea;
  }

  /**
   * Construct a Console
   * @param title : if not null, title to display above text area
   * @param cmdProcessor : handler for user commands; if null, console
   *   is not editable
   */
  public Console(String title, CommandProcessor cmdProcessor) {

    final boolean db = false;

    if (db) {
      Streams.out.println("Constructing console");
    }

    textArea = new ourConsoleTextArea(this);
    textArea.addFocusListener(this);

    out = new NCPrintStream(new TextAreaOutputStream(textArea));

    JPanel console = new JPanel(new BorderLayout());
    console.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    if (title != null) {
      console.add(new JLabel(title, SwingConstants.CENTER), BorderLayout.NORTH);
    }
    console.add(new JScrollPane(textArea), BorderLayout.CENTER);

    component = console;

    pushCommandProcessor(cmdProcessor);

    // redirect the Stream out to the console.
    Streams.redirectOutput(getPrintStream());

    buildCommandThread();
  }

private void buildCommandThread() {
  if (cmdThread == null) {
    cmdThread = new CommandThread(this);
  }
  cmdThread.start();
}

  // -----------------------------------------------------------------
  // Focus listener interface

  /**
   * Process focus gained.  Start a command thread running
   * @param focusEvent FocusEvent
   */
  public void focusGained(FocusEvent focusEvent) {
    buildCommandThread();
  }

  /**
   * Process focus lost; stop command thread
   * @param focusEvent FocusEvent
   */
  public void focusLost(FocusEvent focusEvent) {
    if (false) {
      Tools.warn("Not stopping cmdThread when focus lost");
    }
    else {
      cmdThread.pleaseStop();
//    cmdThread = null;
    }
  }

  // -----------------------------------------------------------------

  /**
   * Determine if console accepts user commands, or is just a read-only
   * output window
   * @return boolean
   */
  public boolean acceptsCommands() {
    return textArea.isEditable();
  }

  /**
   * Get input from user.
   * @param prompt : prompt to display
   * @param history : CommandHistory to use; if null, uses default,
   *   which is distinct for each Console object
   * @return String
   */
  public String getCommand(String prompt, CommandHistory history) {
    final boolean db = false;
    if (db) {
      System.out.println("Console.getCommand");
    }

    if (history == null) {
      if (activeCommandHistory == null) {
        activeCommandHistory = new CommandHistory();
      }
      history = activeCommandHistory;
    }

    // construct a CommandProcessor to deal with the command.
    ActiveCommandProcessor ac = new ActiveCommandProcessor(prompt, history);

    String cmd = null;

    // grab lock on Console, wait until command is found
    synchronized (this) {
      pushCommandProcessor(ac);
      while (ac.theCommand == null) {
        if (db) {
          System.out.println("Waiting on ac");
        }

        try {
          wait();
        }
        catch (InterruptedException e) {
        }
      }
      cmd = ac.theCommand;
      ac.theCommand = null;
      popCommandProcessor();
      // notify CommandThread that command has been processed
      notify();
    }
    if (db) {
      System.out.println(" Console.getCommand returning " + Scanner.debug(cmd));
    }
    return cmd;
  }

  // default command history for this console, in case none specified
  private CommandHistory activeCommandHistory;

  // active CommandHistory
  private CommandHistory history;
  // stack of command processors (and histories)
  private DArray cmdProcessors = new DArray();
  private CommandProcessor cmdHandler;
  // object to synchronize access to cmdHandler with
  private Integer cmdHandlerLock = new Integer(0);

  private CommandThread cmdThread;

  private JComponent component;
  private ourConsoleTextArea textArea;
  private PrintStream out;
  private String prompt;

  /**
   * TextArea for console
   */
  private static class ourConsoleTextArea
      extends WorkTextArea implements KeyListener {

//    static final int DEFAULT_COLUMNS = 40;

    /**
     * Construct a new TextArea for use with the TextAreaPrompt. Uses the
     * default number of columns (TextArea.DEFAULT_COLUMNS).
     * @param numRows the number of lines that will fit into the text area.
     */
    public ourConsoleTextArea(Console owner) {
      super(50000);
      this.console = owner;
      construct();
    }

    private void construct() {
      addKeyListener(this);
    }

    DQueue keyBuffer = new DQueue();

    /**
     * Notes from the KeyEvent API documentation:
     *
     * keyTyped events are higher level and do not usually depend on the
     * platform or keyboard layout. KeyPressed and KeyReleased events are
     * lower-level are platform dependent. All key-processing for the
     * TextArea makes use of keyPressed events since we want to capture
     * special KeyEvents such as HOME, ENTER, arrow keys etc..
     */
    public void keyPressed(KeyEvent e) {
      e.consume();

      synchronized (keyBuffer) {
        keyBuffer.push(e);
        keyBuffer.notify();
      }
    }

    String procKeyEvent(KeyEvent e) {
      final boolean db = false;
      if (db) {
        System.out.println("procKeyEvent e=" + e.getKeyCode());
      }
      String out = null;

      if (!console.acceptsCommands()) {
        return null;
      }

      CommandHistory history = console.history;

      int keyCode = e.getKeyCode();
      int cp = getCaretPosition();
      int clen = length();

      if (db) {
        System.out.println("keyPressed, code=" + keyCode + " ASCII=" +
                           (int) e.getKeyChar()
                           + " cursorPos=" + cursorPos
                           + " cmdBuffer=" + Scanner.debug(cmdBuffer.toString()));
      }

      // if the length of the text has changed, or the caret position has
      // changed, reprint line
      if (clen != expLength || expCaretPos != cp) {
        cursorPos = cmdBuffer.length();
        if (cursorPos > 0) {
          // If the last n characters do not agree with out buffer, reprint on
          // fresh line.
          Document d = getDocument();
          try {
            if (clen < cursorPos
                ||
                !cmdBuffer.toString().equals(d.getText(clen - cursorPos,
                cursorPos))) {
              append("\n" + console.prompt);
              append("> ");
              append(cmdBuffer.toString());
            }
          }
          catch (BadLocationException ex) {
            ScanException.toss(ex);
          }
        }
        clen = length();
        cp = clen;
        setCaretPosition(cp);
      }

      if (cmdBuffer.length() == 0) {
        history.setPos(0);
        history.setPrefix("");
      }

      boolean hf = false;

      char c = e.getKeyChar();
      switch (keyCode) {
        case KeyEvent.VK_ENTER: {
          // move caret to end of text in case user moved back in line
          setCaretPosition(length());

          // push contents of command buffer onto command queue.
          String cmd = cmdBuffer.toString();
          cursorPos = 0;
          cmdBuffer.setLength(cursorPos);
          if (cmd.length() > 0) {
            history.add(cmd);
          }
          append("\n");
          out = cmd;
        }
        break;

        case KeyEvent.VK_DELETE:
          if (cursorPos < cmdBuffer.length()) {
            hf = true;
            cmdBuffer.delete(cursorPos, cursorPos + 1);
            replaceRange("", cp, cp + 1);
          }
          break;

        case KeyEvent.VK_BACK_SPACE:
          if (cursorPos > 0) {
            hf = true;
            cursorPos--;
            cmdBuffer.delete(cursorPos, cursorPos + 1);
            replaceRange("", cp - 1, cp);
            setCaretPosition(cp - 1);
          }
          break;
        case KeyEvent.VK_LEFT:
          if (cursorPos > 0) {
            cursorPos--;
            setCaretPosition(cp - 1);
          }
          break;
        case KeyEvent.VK_RIGHT:
          if (cursorPos < cmdBuffer.length()) {
            setCaretPosition(cp + 1);
            cursorPos++;
          }
          break;

        case KeyEvent.VK_HOME:
          setCaretPosition(clen - cursorPos);
          cursorPos = 0;
          break;
        case KeyEvent.VK_END:
          cursorPos = cmdBuffer.length();
          setCaretPosition(clen);
          break;
        case KeyEvent.VK_UP:
        case KeyEvent.VK_DOWN: {
          int max = history.length();
          if (max == 0) {
            break;
          }
          int cnt = max;
          while (cnt-- > 0) {
            int j = Tools.mod(max - 1 - history.getPos(), max);
            String s = console.history.getCommand(j);

            history.adjustPos( (keyCode == KeyEvent.VK_UP) ? 1 : -1);

            if (s.length() < history.getPrefix().length()) {
              continue;
            }
            String sp = s.substring(0, history.getPrefix().length());
            if (sp.equalsIgnoreCase(history.getPrefix())) {

              int cl = cmdBuffer.length();
              cmdBuffer.setLength(0);
              cmdBuffer.append(s);
              replaceRange(s, clen - cl, clen);
              clen = length();
              setCaretPosition(clen);
              cursorPos = s.length();
              break;
            }
          }
        }
        break;

        default: {
          if (c >= ' ' && c != KeyEvent.CHAR_UNDEFINED) {
            hf = true;
            cmdBuffer.insert(cursorPos, c);
            cursorPos++;
            insert("" + c, cp);
            setCaretPosition(cp + 1);
          }
        }
        break;
      }
      if (hf) {
        history.setPrefix(cmdBuffer.toString());
        history.setPos(0);
      }

      expLength = length();
      expCaretPos = getCaretPosition();

      if (db) {
        System.out.println("procKey returning " + Scanner.debug(out));
      }
      return out;
    }

    public void keyTyped(KeyEvent e) {
      e.consume();
    }

    public void keyReleased(KeyEvent e) {
    }

    private StringBuffer cmdBuffer = new StringBuffer();
    // cursor position within cmdBuffer buffer
    private int cursorPos;
    // expected length of text
    private int expLength;
    private int expCaretPos;
    private Console console;
  }

  /**
   * Thread that reads KeyEvents from queue, passes them to the console's
   * TextArea, and issues commands as they are completed
   */
  private static class CommandThread
      implements Runnable {

    public CommandThread(Console console) {
      this.console = console;
      console.cmdThread = this;
      thread = new Thread(this);
      thread.setDaemon(true);
    }

    public void pleaseStop() {
      synchronized (this) {
        stopFlag = true;
      }
    }

    public void start() {
      synchronized (this) {
        stopFlag = false;
        if (!thread.isAlive()) {
          thread.start();
        }
      }
    }

    private boolean stopFlag;

    public void run() {
      final boolean db = false;

      if (db) {
        System.out.println("CommandThread run() enter");
      }
      DQueue keyBuffer = console.textArea.keyBuffer;
      ourConsoleTextArea ta = console.textArea;

      boolean promptPrinted = false;

      // continue until thread object has been thrown away
      while (true) {
        synchronized (this) {
          if (stopFlag) {
            break;
          }
        }

        // wait until a handler exists.
        CommandProcessor handler = null;
        synchronized (console.cmdHandlerLock) {
          while (console.cmdHandler == null) {
            try {
              console.cmdHandlerLock.wait();
            }
            catch (InterruptedException exc) {}
          }
          handler = console.cmdHandler;
        }

        if (!promptPrinted) {
          console.setReady2();
          promptPrinted = true;
        }

        KeyEvent e = null;
        synchronized (keyBuffer) {
          while (keyBuffer.isEmpty()) {
            try {
              keyBuffer.wait();
            }
            catch (InterruptedException exc) {}
          }
          e = (KeyEvent) keyBuffer.pop();
        }
        if (db) {
          System.out.println(" CommandThread processing key event " +
                             e.getKeyCode());
        }

        // have text area handle key event.  If it returns
        // a command, process it.

        String command = ta.procKeyEvent(e);
        if (command != null) {
          promptPrinted = false;
          if (db) {
            System.out.println(" CommandThread, procKeyEvent returned " +
                               Scanner.debug(command));
          }

          handler.processCommand(console, command);
        }
      }
      if (db) {
        System.out.println("CommandThread run() exit");
      }
    }

    private Thread thread;
    private Console console;
  }

  /**
   * CommandProcessor for blocking until user types a line
   */
  private static class ActiveCommandProcessor
      implements CommandProcessor {

    private static final boolean db = false;

    /**
     * Constructor
     * @param prompt String
     * @param ch CommandHistory
     */
    public ActiveCommandProcessor(String prompt, CommandHistory ch) {
      this.prompt = prompt;
      this.ch = ch;
    }

    /**
     * Process a new command
     * @param command String
     */
    public void processCommand(Console c, String command) {

      // grab lock on Console...
      synchronized (c) {
        // set command, and notify Console that one is ready
        if (theCommand != null) {
          System.out.println("WARNING: console command wasn't processed");
        }
        theCommand = command;
        if (db) {
          System.out.println("activeCommandProcessor command=" + command);
        }
        // notify Console that command is ready
        c.notifyAll();

//        try {
//          Thread.sleep(20);
//        }
//        catch (InterruptedException e) {}

        // remove command processor from stack, so its prompt is not
        // printed?
     if (false)   c.popCommandProcessor();

        // wait until Console has processed the command before returning
        while (theCommand != null) {
          try {
            c.wait();
          }
          catch (InterruptedException exc) {
          }
        }

      }
    }

    /**
     * Get prompt to display to left of user commands; i.e. ">"
     * @return String
     */
    public String getPrompt() {
      return prompt;
    }

    public CommandHistory getCommandHistory() {
      return ch;
    }

    private String prompt;
    private CommandHistory ch;
    public String theCommand;
  }
}
