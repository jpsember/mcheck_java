package base;

import java.io.*;
//import java.awt.*;
import java.util.*;
import java.text.NumberFormat;

//import java.awt.geom.AffineTransform;

public final class Tools {

  public static PrintWriter getPrintWriter(File f) {
    OutputStreamWriter w = null;
    try {
      w = new OutputStreamWriter(new FileOutputStream(f));
    }
    catch (java.io.FileNotFoundException e) {
      throw new RuntimeException(e.toString());
    }
    return new PrintWriter(w);
  }

  /**
   * @deprecated
   * @param t Throwable
   * @return String
   */
  public static String getStackTrace(Throwable t) {
    return stackTrace(0, 30, t);
//    StringWriter s = new StringWriter();
//    t.printStackTrace(new PrintWriter(s));
//    return s.toString();
  }

  /**
   * Return the first n characters of a string
   * @param s String
   * @return First n characters of string, or characters before first
   *   linefeed, whichever is first; "..." added if necessary
   * @deprecated
   */
  public static String strStart(String s) {
    if (s == null) {
      return "<null string!>";
    }

    int n = 40;

    int i = s.indexOf('\n');
    if (i < 0) {
      i = s.length();
    }

    boolean partial = false;
    if (i > n) {
      i = n - 3;
      partial = true;
    }

    StringBuffer sb = new StringBuffer();
    sb.append('[');
    sb.append(s.substring(0, i));
    if (partial) {
      sb.append("...");
    }
    sb.append(']');
    return sb.toString();
  }

  public static String stackTrace() {
    return stackTraceFmt(1);
//
//    // skip 1 for call to this method...
//    return stackTrace(1, 1)+": ";
  }

  public static String tr() {
    return stackTraceFmt(1);
//    // skip 1 for call to this method...
//    return stackTrace(1, 1)+": ";
  }

public static String trc() {
  return stackTraceFmt(2);
//  return stackTrace(2,1)+": ";
}

private static String stackTraceFmt(int skip) {
  StringBuffer sb = new StringBuffer();
  sb.append(stackTrace(1+skip,1));
  sb.append(" : ");
  tab(sb,24);
  return sb.toString();
}

  /**
   * Construct a string describing a stack trace
   * @param skipCount : # stack frames to skip (actually skips 1 + skipCount,
   *   to skip the call to this method)
   * @param displayCount : maximum # stack frames to display
   * @return String; iff displayCount > 1, cr's inserted after every item
   */
  public static String stackTrace(int skipCount, int displayCount) {
    // skip 1 for call to this method...
    return stackTrace(1 + skipCount, displayCount, new Throwable());
  }

  /**
   * Construct string describing stack trace
   * @param skipCount : # stack frames to skip (actually skips 1 + skipCount,
   *   to skip the call to this method)
   * @param displayCount : maximum # stack frames to display
   * @param t : Throwable containing stack trace
   * @return String; iff displayCount > 1, cr's inserted after every item
   */
  public static String stackTrace(int skipCount, int displayCount,
                                  Throwable t) {
    final boolean db = false;

    StringBuffer sb = new StringBuffer();

    StackTraceElement[] elist = t.getStackTrace();

    if (db) {
      for (int j = 0; j < elist.length; j++) {
        StackTraceElement e = elist[j];
        sb.append(j >= skipCount && j < skipCount+displayCount ? "  " : "x ");
        String cn = e.getClassName();
        cn = cn.substring(cn.lastIndexOf('.') + 1);
        sb.append(cn);
        sb.append(".");
        sb.append(e.getMethodName());
        sb.append(":");
        sb.append(e.getLineNumber());
        sb.append("\n");

      }
      return sb.toString();
    }

    int s0 = skipCount;
    int s1 = s0 + displayCount;


    for (int i = s0; i < s1; i++) {
      if (i >= elist.length) {
        break;
      }
      StackTraceElement e = elist[i];
      String cn = e.getClassName();
      cn = cn.substring(cn.lastIndexOf('.') + 1);
      sb.append(cn);
      sb.append(".");
      sb.append(e.getMethodName());
      sb.append(":");
      sb.append(e.getLineNumber());
      if (displayCount > 1) {
        sb.append("\n");
      }
    }
    return sb.toString();
  }

  /*
   private static int tabAmt;
   public static void tabIn(String s) {
    tab(s);
    tabAmt += 2;
   }
   public static void tabOut() {
    tabAmt -= 2;
   }
   public static void tabOut(String s) {
    tabOut();
    tab(s);
   }
   public static void tab(String s) {
    for (int i = 0; i < tabAmt; i++)
      System.out.print(' ');
    System.out.println(s);
   }
   */
//  private static String chr(int code) {
//    Character c = new Character( (char) code);
//    return c.toString();
//  }

  private Tools() {}

  /**
   * Makes the current thread sleep for a specified time.  Ignores
   * any InterruptedExceptions that occur.
   * @param time time, in milliseconds, to sleep() for
   */
  public static void delay(int time) {
    try {
      Thread.sleep(time);
    }
    catch (InterruptedException e) {}
  }

  /**
   * Tests an assertion.  If the assertion is false, prints
   * an error message to System.out, and if init(Applet) was
   * called, to the browser status line.  Also prints a stack
   * trace to System.out, and finally calls System.exit(1).
   * @see #init(Applet)
   */
  public static void ASSERT(boolean flag, String message) {
    if (!flag) {
      System.out.println("ASSERTION FAILED: " + message);
      System.out.println(stackTrace(1,12));
      System.exit(1);
    }
  }

  public static void ASSERT(boolean flag) {
    if (!flag) {
      System.out.println("ASSERTION FAILED:");
      System.out.println(stackTrace(1, 12));
      System.exit(1);
    }
  }

  /**
   * Returns a string representing x,y coordinates
   * @param x the x-coordinate
   * @param y the y-coordinate
   */
  public static String p2String(int x, int y) {
    String s;
    s = "(" + x + "," + y + ") ";
    return s;
  }

  public static boolean rndBool(Random r) {
    return r.nextInt() > 0;
  }

  /*
    // Displays a stack trace to System.out
    private static void dispStackTrace() {
      Throwable t = new Throwable();
      t.printStackTrace();
    }
   */
//public static boolean noTrimTrace;

//  public static String getStackTrace(int trimFront, int maxCalls) {
//    ByteArrayOutputStream os = new ByteArrayOutputStream();
//    Throwable t = new Throwable();
//    t.printStackTrace(new PrintStream(os));
//    String s = os.toString();
//    int start = 0;
//    while (trimFront-- > 0) {
//      while (true) {
//        if (start == s.length()) {
//          break;
//        }
//        start++;
//        if (s.charAt(start - 1) == 0x0a) {
//          break;
//        }
//      }
//    }
//
//    // include only the first n calls.
//    int end = s.length();
//    int count = 0;
//    for (int i = start; i < end; i++) {
//      if (s.charAt(i) != 0x0a) {
//        continue;
//      }
//      if (++count == maxCalls) {
//        end = i++;
//        break;
//      }
//    }
//    return s.substring(start, end);
//  }

  /** Constructs a string containing a stack trace.
      A debugging aid, used by the assert() method.

      @param trimAmount : number of calls to remove from
        front of string before returning

      @return A string describing the calls on the stack.
   */
//  public static String getStackTrace(int trimAmount) {
//    return getStackTrace(trimAmount, 12);
//  }

  // gets a stack trace into a string
  /**
   * @deprecated
   * @return String
   */
  public static String getStackTrace() {
    // skip 1 for call to this method
    return stackTrace(1, 12);
  }

  public static boolean unimp() {
    return warn("TODO", null, 1);
  }

  public static boolean unimp(String msg) {
    return warn("TODO", msg, 1);
  }

  private static boolean warn(String type, String s, int skipCount) {
    String st = Tools.stackTrace(1 + skipCount, 1);
    StringBuffer sb = new StringBuffer();
    sb.append("*** ");
    if (type == null) {
      type = "WARNING";
    }
    sb.append(type);
    if (s != null) {
      sb.append(": ");
      sb.append(s);
    }
    sb.append(" (");
    sb.append(st);
    sb.append(")");
    String keyString = sb.toString();

    {
      Object wr = warningStrings.get(keyString);
      if (wr != null) {
        return false;
      }
      warningStrings.put(keyString, Boolean.TRUE);
      System.out.println(keyString);
    }
    return true;
  }

  public static boolean warn(String s) {
    String st = Tools.stackTrace(1, 1);
    String keyString = "*** WARNING: " + s + " (" + st + ")";
    // if (TestBed.DEBUG)
    {
      Object w = warningStrings.get(keyString);
      if (w != null) {
        return false;
      }
      warningStrings.put(keyString, Boolean.TRUE);
      System.out.println(keyString);
    }
    return true;
  }

  /**
   * Clamp a value into range
   * @param value int
   * @param min int
   * @param max int
   * @return int
   */
  public static final int clamp(int value, int min, int max) {
    if (value < min) {
      value = min;
    }
    else if (value > max) {
      value = max;
    }
    return value;
  }

  /**
   * Clamp a value into range
   * @param value double
   * @param min double
   * @param max double
   * @return double
   */
  public static final double clamp(double value, double min, double max) {
    if (value < min) {
      value = min;
    }
    else if (value > max) {
      value = max;
    }
    return value;
  }

  public static final double mod(double value, double divisor) {
    return (value - divisor * Math.floor(value / divisor));
  }

  /**
   *  A better mod function.  This behaves as expected for negative
   *  numbers.  It has the following effect:  if the input value is
   *  negative, then enough multiples of the divisor are added to it
   *  to make it positive, then the standard mod function (%) is
   *  applied.
   *  @param value the number on the top of the fraction
   *  @param divisor the number on the bottom of the fraction
   *  @return the remainder of value / divisor.
   */
  public static int mod(int value, int divisor) {
    value = value % divisor;
    if (value < 0) {
      value += divisor;
    }
    return value;
  }

  private static HashMap warningStrings = new HashMap();
//  private static DArray warningStrings = new DArray();


  public static String deg(double a) {
    return f( (int) ( (a * (180 / Math.PI))), 4);
  }

  public static String f(boolean b) {
    return b ? " T" : " F";
  }

  public static void f(StringBuffer sb) {
    System.out.println(sb.toString());
    sb.setLength(0);
  }

//  private static StringBuffer sb = new StringBuffer();
  private static ourNumFormat
      fmtFP = new ourNumFormat(false, 6, 2),
      fmtINT = new ourNumFormat(true, 6, 0),
      fmtFPVar = new ourNumFormat(false, 6, 2);

  /**
     Set format for displaying floating point numbers as strings.
     @param width : width of each string, including decimal point,
       negative sign, and digits
     @param fractionDigits : number of characters to reserve for
       digits to right of decimal point
   */
  public static void setNumberFormat(int width, int fractionDigits) {
    fmtFP.fmt.setMaximumFractionDigits(fractionDigits);
    fmtFP.fmt.setMinimumFractionDigits(fractionDigits);
    fmtFP.width = width;
  }

  /**
   * Format a string to be at least a certain size
   * @param s : string to format
   * @param length : minimum size to pad to; negative to
   *   insert leading spaces
   * @return blank-padded string
   */
  public static String f(String s, int length) {
    StringBuffer sb = new StringBuffer();
    sb.setLength(0);
    if (length >= 0) {
      sb.append(s);
      return tab(sb, length).toString();
    }
    else {
      tab(sb, ( -length) - s.length());
      sb.append(s);
      return sb.toString();
    }
  }

  /**
   * Format a string for debug purposes
   * @param s String, may be null
   * @return String
   */
  public static String d(String s) {
    return d(s, 40, false);
  }

  public static String hashCode(Object obj) {
    int hc = 0;
    if (obj != null) {
      hc = obj.hashCode();
    }
    return "[" + Tools.f(Tools.mod(hc, 10000), 4, true) + "]";
  }

  public static String d(Object obj) {
    String s = null;
//    if (obj != null) {
//      s = hashCode(obj) + obj;
//    }
    if (obj != null)
      s = obj.toString();
    return d(s);
  }

public static String d(char c) {
  StringBuffer sb = new StringBuffer();
  sb.append('\'');
  convert(c,sb);
  sb.append('\'');
  return sb.toString();
}

  /**
   * Convert string to debug display
   * @param orig String
   * @param maxLen : maximum length of resulting string
   * @param pad : if true, pads with spaces after conversion
   * @return String in form [xxxxxx...xxx], with nonprintables converted to
   * unicode or escape sequences, and ... inserted if length is greater than
   * about the width of a line
   */
  public static String d(String orig, int maxLen, boolean pad) {
    if (maxLen < 8) {
      maxLen = 8;
    }

    StringBuffer sb = new StringBuffer();
    if (orig == null) {
      sb.append("<null>");
    }
    else {
      sb.append("[");
      convert(orig, sb);
      sb.append("]");
      if (sb.length() > maxLen) {
        sb.replace(maxLen - 7, sb.length() - 4, "...");
      }
    }
    if (pad) {
      Tools.tab(sb, maxLen);
    }

    return sb.toString();
  }

  public static String trimLength(String orig, int maxLen, boolean addDots) {
    String out = orig;
    if (orig.length() > maxLen) {
      StringBuffer sb = new StringBuffer(orig);
      if (addDots && maxLen < 3) {
        addDots = false;
      }
      if (addDots) {
        maxLen -= 3;
      }
      sb.setLength(maxLen);
      if (addDots) {
        sb.append("...");
      }
      out = sb.toString();
    }
    return out;

  }

  private static void convert(char c, StringBuffer dest) {
    switch (c) {
     case '\n':
       dest.append("\\n");
       break;
     default:
       if (c >= ' ' && c < (char) 0x80) {
         dest.append(c);
       }
       else {
         dest.append("\\#");
         dest.append((int)c);
       }
       break;
   }
 }

  private static void convert(String orig, StringBuffer sb) {
    for (int i = 0; i < orig.length(); i++) {
      convert(orig.charAt(i),sb);
//
//      char c = orig.charAt(i);
//      switch (c) {
//        case '\n':
//          sb.append("\\n");
//          break;
//        default:
//          if (c >= ' ' && c < (char) 0x80) {
//            sb.append(c);
//          }
//          else {
//            sb.append("\\#");
//            sb.append(c);
//          }
//          break;
//      }
    }
  }

  private static StringBuffer sbw = new StringBuffer();
  public static String sp(int len) {
    if (len <= 0) {
      return "";
    }
    while (sbw.length() < len) {
      sbw.append(' ');
    }
    return sbw.substring(0, len);
  }

  /**
     Set format for displaying integers as strings.
     @param width : width of each string
   */
  public static void setNumberFormat(int width) {
    fmtINT.width = width;
    fmtINT.fmt.setMaximumIntegerDigits(width);
  }

  /**
   * Display an array of doubles
   * @param arr : array to display
   * @param len : number of values to display; if negative, prints entire
   *   array
   * @param reversed : true to display in reversed order
   */
  public static void dispArray(double[] arr, int len, boolean reversed) {
    if (len < 0) {
      len = arr.length;
    }
    if (reversed) {
      for (int i = 0; i < len; i++) {
        System.out.print( (f(arr[len - 1 - i])));
      }
    }
    else {
      for (int i = 0; i < len; i++) {
        System.out.print(f(arr[i]));
      }
    }
  }

  public static void dispArray(double[] arr, int len) {
    dispArray(arr, len, false);
  }

  public static void dispArray(double[] arr) {
    dispArray(arr, arr.length);
  }

  /**
     Convert a double to a string, with appropriate formatting
     (align decimal place, and right justify)
     @param f : value to convert
     @return string containing value, set according to number format
   */
  private static String dblStr(double f, boolean trimTrail) {
    return fmtStr(fmtFP.width, fmtFP.fmt.format(f), trimTrail);
  }

  public static String dblStr(double f, boolean trimTrail, int intDigits,
                              int fracDigits) {
    NumberFormat fmt = fmtFPVar.fmt;
    fmt.setMaximumFractionDigits(fracDigits);
    fmt.setMinimumFractionDigits(fracDigits);
    fmt.setMaximumIntegerDigits(intDigits);
    // fmt.setMinimumIntegerDigits(intDigits);

    return fmtStr(intDigits + 1 + fracDigits, fmt.format(f), trimTrail);
  }

  public static String f(double f) {
    return dblStr(f, true);
  }

  public static String fz(double f) {
    return dblStr(f, false);
  }

  public static String fa(double radians) {
    return f(radians * 180 / Math.PI);
  }

  public static String f() {
    StringBuffer sb = new StringBuffer();
    tab(sb, fmtFP.width);
    return sb.toString();
  }

  public static String f(int f) {
    return intStr(f);
  }

  public static String f(int val, int width, boolean spaceLeadZeros) {
    String s = f(val, width);
    if (!spaceLeadZeros) {
      StringBuffer sb = new StringBuffer();

      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c == ' ') {
          c = '0';
        }
        sb.append(c);
      }
      s = sb.toString();
    }
    return s;
  }

  public static String f(int val, int width) {
    String s = fmtINT.fmt.format(val).trim();
    s = fmtStr(width, s, true);
    return s;
  }

  private static String fmtStr(int width, String num, boolean trimTrailingZeros) {
    StringBuffer sb = new StringBuffer();
    sb.setLength(0);
    tab(sb, width - num.length());
    sb.append(num);

    // Replace trailing decimal + zeros with spaces.
    if (trimTrailingZeros) {
      int j = sb.length() - 1;
      while (j >= 0) {
        if (sb.charAt(j) == '.') {
          while (j < sb.length()) {
            sb.setCharAt(j++, ' ');
          }
          break;

        }
        if (sb.charAt(j) != '0') {
          break;
        }
        j--;
      }
    }

    return sb.toString();
  }

  public static void add(StringBuffer sb, String s, int length) {
    int t = sb.length();
    sb.append(s);
    tab(sb, length + t);
  }

  /**
     Add spaces to a StringBuffer until its length is at
     some value.  Sort of a 'tab' feature, useful for
     aligning output.
     @param sb : StringBuffer to pad out
     @param len : desired length of StringBuffer; if
        it is already past this point, nothing is added to it
   */
  public static StringBuffer tab(StringBuffer sb, int len) {
    sb.append(sp(len - sb.length()));
    return sb;
  }

  /**
     Convert an integer to a right-justified string of fixed
     length (spaces added to left side).
     @param val : integer value to display in string
     @param len : desired length of string (if integer
        representation exceeds this amount, string is not
        truncated)
   */
  private static String intStr(int val) {
    return fmtStr(fmtINT.width, fmtINT.fmt.format(val), true);
  }

  /**
   * Insert linefeeds in a string so no line exceeds a particular width
   * @param s : source string
   * @param width : maximum width of each line
   * @return String : string with linefeeds inserted
   */
  public static String insertLineFeeds(String s, int width) {

    StringBuffer dest = new StringBuffer();

    // index of start of current line
    int lineStart = 0;

    for (int i = 0, j = 0; i < s.length(); i++, j++) {

      // set c to current char
      char c = s.charAt(i);

      if (lineStart == i && c == ' ') {
        lineStart++;
      }
      // if we've exceeded the maximum line width, scan back
      // to a space

      if (j - lineStart > width) {
        int lastSpace = j - 1;
        for (; lastSpace > 0; lastSpace--) {
          if (s.charAt(lastSpace) == ' ') {
            break;
          }
        }
        int len = lastSpace - lineStart;
        boolean removeSpace = (len > 0);
        if (!removeSpace) {
          len = width;
        }

        if (dest.length() > 0) {
          dest.append("!\n");
        }
        dest.append(s.substring(lineStart, lineStart + len));

        lineStart += len + (removeSpace ? 1 : 0);
      }
    }
    return dest.toString();
  }

  /**
   * Read a text file.
   * @param File : file to read
   * @return String containing file
   * @throws IOException if problem
   */
  public static String read(File f) throws IOException {
//    System.out.println("reading file " + f);
    StringBuffer sb = new StringBuffer();

    BufferedReader in = new BufferedReader(new FileReader(f));
    String str;
    while ( (str = in.readLine()) != null) {
      sb.append(str);
      sb.append("\n");
    }
    in.close();
    return sb.toString();
  }

  public static void write(File f, String s) throws IOException {
    BufferedWriter out = new BufferedWriter(new FileWriter(f));
    out.write(s);
    out.close();
  }

  public static int sign(double d) {
    int s = 0;
    if (d < 0) {
      s = -1;
    }
    else if (d > 0) {
      s = 1;
    }
    return s;
  }

  private static class ourNumFormat {
    public ourNumFormat(boolean integer, int intDig, int fracDig) {
      fmt = NumberFormat.getInstance();
      if (!integer) {
        fmt.setMaximumFractionDigits(fracDig);
        fmt.setMinimumFractionDigits(fracDig);
        fmt.setGroupingUsed(false);
        width = intDig + 1 + fracDig;
      }
      else {
        fmt.setGroupingUsed(false);
        width = intDig;
      }
      fmt.setMaximumIntegerDigits(intDig);
    }

    public int width;
    public NumberFormat fmt;
  }

    public static String hexDump(byte[] buffer) {
      return hexDump(buffer, true);
    }

   public static String strDump(byte[] buffer) {
      return hexDump(buffer, 0, buffer.length, false, false, true);
    }

    public static String hexDump(byte[] buffer, boolean multiline) {
      return hexDump(buffer, 0, buffer.length, multiline,
                     true, multiline);
    }

    public static String hexDump(byte[] buffer, boolean multiline,
                                 boolean withHex, boolean withASCII) {
      return hexDump(buffer, 0, buffer.length, multiline, withHex, withASCII);
    }

//    private static final char[]
//        spaces = "                                        ".toCharArray();

    public static void insertSpaces(StringBuffer sb, int position, int nSpaces) {
  sb.insert(position, sp(nSpaces));
//      while (nSpaces > 0) {
//        int chunk = Math.min(nSpaces, spaces.length);
//        sb.insert(position, spaces, 0, chunk);
//        position += chunk;
//        nSpaces -= chunk;
//      }
    }

    /**
     * Hexdump a portion of a byte array to a string
     *
     * @param buffer : byte array
     * @param offset : offset to first dumped byte
     * @param length : number of dumped bytes, or 0 for remaining buffer
     * @param multiline : true split output into sets of 32 bytes, with
     *   offset printed at start of each line, and linefeeds added to the end
     * @param withHex : true to display hex dump portion
     * @param withASCII : true to display ASCII dump portion
     *
     * @return String
     *
     */
    public static String hexDump(byte[] buffer, int offset, int length,
                                 boolean multiline, boolean withHex,
                                 boolean withASCII) {
      return hexDump(buffer, offset, length, multiline, withHex, withASCII, true);
    }

    /**
     * Hexdump a portion of a byte array to a string
     *
     * @param buffer : byte array
     * @param offset : offset to first dumped byte
     * @param length : number of dumped bytes, or 0 for remaining buffer
     * @param multiline : true split output into sets of 32 bytes, with
     *   offset printed at start of each line, and linefeeds added to the end
     * @param withHex : true to display hex dump portion
     * @param withASCII : true to display ASCII dump portion
     *
     * @return String
     */
    public static String hexDump(byte[] buffer, int offset, int length,
                                 boolean multiline, boolean withHex,
                                 boolean withASCII, boolean hideZeros) {

      StringWriter sw = new StringWriter(5000);
      PrintWriter pw = new PrintWriter(sw);

      if (length == 0) {
        length = buffer.length - offset;
      }
      Tools.ASSERT(offset < buffer.length && offset + length <= buffer.length);

      int rowSize = length;
      if (multiline) {
        rowSize = 32;
        if (length < rowSize) {
          rowSize = Math.max(4, length);
        }
      }

      long len = length;
      int i = 0;
      while (i < len) {
        int rSize = rowSize;
        if (rSize + i > len) {
          rSize = (int) (len - i);
        }

        if (multiline) {
          pw.print(Scanner.toHex(i + offset, 4));
          pw.print(": ");
        }
        else {
          pw.print('|');
        }
        for (int pass = 0; pass < 2; pass++) {
          if (pass == 0 && !withHex) {
            continue;
          }
          if (pass == 1 && !withASCII) {
            continue;
          }
          if (pass == 1 && withHex) {
            pw.print("|");
          }
          for (int j = 0; j < rowSize; j++) {
            if (j < rSize) {
              int val = buffer[i + offset + j];
              if (pass == 0) {
                if (hideZeros && val == 0) {
                  pw.print("__");
                }
                else {
                  pw.print(Scanner.toHex(val, 2));
                }
              }
              else {
                int v = val & 0x7f;
                if (v < 0x20) {
                  v = '.';
                }
                pw.print( (char) v);
              }
            }
            else {
              pw.print(pass == 0 ? "  " : " ");
            }
            if (pass == 0) {
              pw.print(' ');
              if ( (j & 3) == 3) {
                pw.print(' ');
              }
            }
          }
        }
        if (multiline) {
          pw.print('\n');
        }
        else {
          pw.print('|');
        }
        i += rSize;
      }
      pw.close();
      return sw.toString();
    }


}
