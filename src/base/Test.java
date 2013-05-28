package base;

import java.util.*;
import java.io.*;
import java.awt.Dimension;

public class Test
    extends EmApplication
{

  /**
   * Application entry point.  This should be overridden by the class derived
   * from TestBed, and it should call either startConsoleApp() or startGUIApp(), depending
   * upon whether it's a console app or a gui app
   * @param args : command line arguments
   */
  public static void main(String[] args) {
    new Test().doMain(args);
  }

  /**
   * Override this method to change preferred size of application frame.
   * @return Dimension
   */
  public Dimension getPreferredSize() {
    return new Dimension(1024,768);
  }


//  /**
//   * Determine string to display in console at start
//   * @return String
//   */
//  protected String welcomeMsg() {
//    return helpMsg();
//  }

  private static String helpMsg() {
    StringBuffer sb = new StringBuffer();
    sb.append("Test functions for 'base' package\n");
    sb.append("Usage: test [name]    where name is one of:\n");
    for (int i = 0; i < ts.length; i++) {
      if (i > 0) sb.append("\n");
      sb.append("  " + ts[i]);
    }
    return sb.toString();
  }

  private static String[] ts = {
      "array",
      "queue",
      "renumber",
      "scanner",
      "hash",
      "pbreader",
      "storefile",
      "filechooser",
  };


  protected void doMain(String[] args) {
    super.doMain(args);
    try {

     if (args.length == 0) {
        args = ts;
      }
      for (int i = 0; i < args.length; i++) {
        String s = args[i];

        int j = ts.length - 1;
        for (; j >= 0; j--) {
          if (s.equalsIgnoreCase(ts[j])) {
            break;
          }
        }
        Streams.out.println(
            "===============================================");
        Streams.out.println("Performing test for: " + s);
        Streams.out.println(
            "===============================================");

        switch (j) {
          default:
            Streams.out.println("*** No test defined for: " + s + "\n" +
                        helpMsg());
            break;

          case 0:
            DArray();
            break;
          case 1:
            DQueue();
            break;
          case 2:
            Renumber();
            break;
          case 3:
            Scanner();
            break;
          case 4:
            StringHash();
            break;
          case 5:
            DynamicPushbackReader();
            break;
          case 6: {
            Random r = new Random();
            String path = "testfile" + (r.nextInt() % 100) + ".dat";
            OutputStream os = Streams.outputStream(path);
            Streams.out.print("Write:");
            for (int k = 0; k < 10; k++) {
              int n = (k * k) % 256;
              Streams.out.print(" " + n);
              os.write(n);
            }
            Streams.out.println();
            os.close();
            EmulationConsole.appletFileList().displayDir();
            InputStream is = Streams.inputStream(path);
            Streams.out.print("Read: ");
            while (true) {
              int c = is.read();
              if (c < 0) {
                break;
              }
              Streams.out.print(" " + c);
            }
            Streams.out.println();
            is.close();
          }
          break;
          case 7: {
            FileChooser fc = Streams2.fileChooser();
            String op = "test.txt";
            String res = fc.doOpen("Select file to open:", op,
                                   new PathFilter("txt"));
            Streams.out.println("Result=" + res);
            if (res != null) {
              op = res;
            res = fc.doWrite("Save as:", op, null);
            Streams.out.println("Result=" + res);
            }
          }
          break;
        }
      }
    }
    catch (Exception e) {
      Streams.out.println(e.toString());
    }
  }

//  private static PrintStream out = System.out;

  private static void DynamicPushbackReader() throws IOException {
    String s = "Computational";

    for (int pass = 0; pass < 2; pass++) {
      Streams.out.println("Pass=" + pass);
      PushbackReader r = (pass == 0) ?
          new DynamicPushbackReader(new StringReader(s)) :
          new PushbackReader(new StringReader(s), 100);

//      Inf inf = new Inf("Dynamic", 50);

      Random rnd = new Random(1965);

      StringBuffer w = new StringBuffer();

      while (true) {
//        inf.update();
        int c = 0;
        if (w.length() > 0 && rnd.nextInt(100) < 40) {
          c = w.charAt(w.length() - 1);
          r.unread(c);
          w.setLength(w.length() - 1);
          Streams.out.print("Unread:");
        }
        else {
          c = r.read();
          if (c < 0) {
            break;
          }
          w.append( (char) c);
          Streams.out.print("  Read:");
        }
        Streams.out.println("" + (char) c + " w=" + w);
      }
    }
  }

  private static String debStr(DArray a) {
    StringBuffer sb = new StringBuffer();
    sb.append("DArray " + a.toString());
    Object[] n = a.n();
    int[] recycleBin = a.recycleBin();
    sb.append("\n n=" + n.toString());
    if (n != null) {
      sb.append("\n");
      for (int i = 0; i < Math.min(5, n.length); i++) {
        sb.append("   " + i + " " + n[i] + "\n");
      }
    }
    sb.append("\n rb=" + recycleBin.toString());
    if (recycleBin != null) {
      sb.append("\n");
      for (int i = 0; i < Math.min(5, recycleBin.length); i++) {
        sb.append("   " + i + " " + recycleBin[i] + "\n");
      }
    }
    sb.append("\n itemsUsed=" + a.itemsUsed());
    sb.append("\n rcUsed=" + a.rcUsed());
    sb.append("\n");
    return sb.toString();
  }

  private static void DArray() {
    class TC {
      public TC(int x, int y) {}
    };
    DArray t1 = new DArray();
    for (int i = 0; i < 5; i++) {
      t1.add(new TC(i * 10, i * 5));
    }
    t1.free(1);
    Streams.out.println("t1=\n " + debStr(t1));
    DArray t2 = (DArray) t1.clone();
    Streams.out.println("t2=\n " + debStr(t2));
    t2.alloc(new TC(50, 50));
    t2.set(2, new TC(2, 2));
    Streams.out.println("after mod, t1=\n " + debStr(t1));
    Streams.out.println("t2=\n " + debStr(t2));
  }

  private static void Renumber() {
    Renumber r = new Renumber();

    for (int i = 0; i < 20; i++) {
      r.addOldItem();
    }

    for (int i = 12; i < 15; i++) {
      r.renameItem(i, Tools.mod(i + 10, 20));
    }

    Streams.out.println("" + r);
  }

  private static void DQueue() {
    DQueue q = new DQueue();
    Random r = new Random();
    boolean poppingAll = false;

    for (int i = 100; i < 1000; i++) {
      if (poppingAll && q.length() == 0) {
        break;
      }

      if ( (poppingAll || (r.nextInt(100) < 40)) && q.length() > 0) {
        Object s = q.pop();
        Streams.out.print("Pop:  " + s);
      }
      else {
        Object obj = ">" + i;
        q.push(obj);
        Streams.out.print("Push: " + obj);
      }
      {
        Streams.out.println(" Queue=" + q + " Head:" + q.head() + " Tail:" +
                    q.tail());
      }
      if (q.length() > 50) {
        poppingAll = true;
      }
    }
  }

  private static String[] testStrs = {
      "the     time       has      come    the\n walrus\nsaid,   to speak\nof many\nthings"
      , "the time\nhas come\nthe"
  };
  private static void Scanner() throws IOException {

//    DFA dfa = new DFA(myData);
    DFA dfa = new DFA(Streams.inputStream("base.dfa")); //Tools.openResource("base.dfa"));
    //new FileInputStream("base.dfa"));
    Scanner sc = new Scanner(new StringReader(testStrs[0]), "testStr", dfa, -1);
    while (true) {
      Token t = sc.read();
      if (t.eof()) {
        break;
      }
      Streams.out.println("Token is " + t.debug());
    }

    for (int i = 0; i < testStrs.length; i++) {
      String s = testStrs[i];
      DArray lst = new DArray();
      for (int w = 1; w < 5; w++) {
        Streams.out.println("Width=" + w + ":");
        Scanner.splitString(s, w, lst);
        for (int j = 0; j < lst.length(); j++) {
          Streams.out.println("[" + lst.getString(j) + "]");
        }
        Streams.out.println();
      }
    }
  }

  private static void StringHash() {
    Scanner sc = new Scanner(testStrs[0]);
    StringHash ht = new StringHash();

    for (int i = 0; ; i++) {
      String h = sc.readWord();
      if (h == null) {
        break;
      }
      ht.putInt(h, i);

      Streams.out.println("Hash table is: " + ht);
    }
  }

}
