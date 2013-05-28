package base;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
//
//public class ConsoleTest
//    implements ActionListener {
//  /**
//   * Create the GUI and show it.  For thread safety,
//   * this method should be invoked from the
//   * event-dispatching thread.
//   */
//  private static Console testConsole;
//  private static boolean withGUI = false;
//
//  // Methods
//  public void actionPerformed(ActionEvent actionEvent) {
//    String cmd = testConsole.getCommand("enter a command--->", null);
//    Streams.out.println("command entered was: " + Scanner.debug(cmd));
//  }
//
//  private void createAndShowGUI() {
//    //Make sure we have nice window decorations.
//    JFrame.setDefaultLookAndFeelDecorated(true);
//
//    //Create and set up the window.
//    JFrame frame = new JFrame("ComboBoxDemo");
//    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//    //Create and set up the content pane.
//
//    Component p = null;
//
//    int testNumber = 1;
////    JButton myButton = null;
//
//
//    switch (testNumber) {
//      case 0: {
//        testConsole
//            = new Console("Test of Console2 class", new Proc1());
//      }
//      break;
//      default: {
//        testConsole
//            = new Console("Test of Console2 class", null);
//        Prompter pr = new Prompter(testConsole);
//
////         myButton = new JButton("Get line of input");
////        myButton.addActionListener(this);
////
////                         false ? null :
////                         new CommandProcessor() {
////        public void processCommand(String command) {
////          Streams.out.println("...processing command " + command);
////          testConsole.setReady();
////        }
////
////        public String getPrompt() {
////          return ">";
////        }
////
////        public CommandHistory getCommandHistory() {
////          return null;
////        }
////      });
//
//      }
//      break;
//    }
////    testConsole.setReady();
//    JPanel
//        p2 = new JPanel();
//    p2.setLayout(new BorderLayout());
////    if (myButton != null)
////    p2.add(myButton, BorderLayout.EAST);
//    p2.add(testConsole.component(), BorderLayout.CENTER);
//    p = p2;
//    JComponent newContentPane = (JComponent) p;
//
//    newContentPane.setOpaque(true); //content panes must be opaque
//    frame.setContentPane(newContentPane);
//
//    //Display the window.
//    frame.pack();
//    frame.setSize(800, 600);
//    frame.setVisible(true);
//  }
//
//  public static void main(String[] args) {
//    new ConsoleTest().doMain(args);
//  }
//
//  private void doMain(String[] args) {
//    Streams.loadResources(Console.class);
//    //Schedule a job for the event-dispatching thread:
//    //creating and showing this application's GUI.
//    javax.swing.SwingUtilities.invokeLater(new Runnable() {
//      public void run() {
//        createAndShowGUI();
//      }
//    });
//  }
//
//  private static class Proc1
//      implements CommandProcessor {
//    private static int nest;
//    public void processCommand(Console c, String command) {
//      Streams.out.println("...processing command " + command);
//
//      if (command.equals("in")) {
//        Streams.out.println(" pushing inward...");
//        nest++;
//        c.pushCommandProcessor(new Proc1());
//      }
//      else if (command.equals("out")) {
//        if (nest > 0) {
//          nest--;
//          c.popCommandProcessor();
//        }
//      }
////      c.setReady();
//    }
//
////public Proc1(Console2 console) {
////  this.console = console;
////}
////private Console2 console;
//    public String getPrompt() {
//      return "" + nest + " >";
//    }
//
//    public CommandHistory getCommandHistory() {
//      return null;
//    }
//
////  private CommandHistory h = new CommandHistory();
//  }
//
//  private class Prompter
//      implements Runnable {
//    private Thread thread;
//    private Console c;
//    public Prompter(Console c) {
//      this.c = c;
//      thread = new Thread(this);
//      thread.setDaemon(true);
//      thread.start();
//    }
//
//    private Random r = new Random(1965);
//    public void run() {
//      while (true) {
//        int del = r.nextInt(10);
//        Streams.out.println("Delaying " + del + " seconds...");
//        try {
//          thread.sleep(del * 1000 + 100);
////        Thread.wait(del*1000+100);
//        }
//        catch (InterruptedException e) {}
//        String s = c.getCommand("Enter a command:", null);
//        Streams.out.println("Command entered: " + s);
//        if (s.equals("quit")) {
//          break;
//        }
//      }
//    }
//  }
//
//}
