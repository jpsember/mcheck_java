package base;

import java.io.*;
import java.util.*;

public class MacroReader
    extends Reader {
  private static final int BUFFER_SIZE = 1000;
  static final boolean db = false;
  static final boolean VERBOSE = false;

  // Markup characters
  private char
      muParop
      , muParcl
      , muEquals
      , muEscape
      , muCond
      , muUnicode
      ;
  private String markupChars;

  /**
   * Construct a MacroReader that performs macro substitution with an
   * underlying Reader.
   *
   * Uses default values for markup characters:  { } = / ? #
   *
   * @param reader : Underlying Reader
   * @param description : description of underlying reader, for
   *  messages
   */
  public MacroReader(Reader reader, String description) {
    this(reader, description, (Environment)null);
  }

  /**
   * Set the characters used for parsing macros.
   *
   * @param str : String containing markup characters; format should
   *  equal that of the default "{}=/?#"
   */
  public void setMarkupCharacters(String str) {
    markupChars = str;
    muParop = str.charAt(0);
    muParcl = str.charAt(1);
    muEquals = str.charAt(2);
    muEscape = str.charAt(3);
    muCond = str.charAt(4);
    muUnicode = str.charAt(5);
  }

  /**
   * Construct a MacroReader that performs macro substitution with an
   * underlying Reader.
   *
   * @param reader : underlying Reader
   * @param description : description of underlying reader, for
   *  messages
   * @param prev : existing MacroReader to inherit environment (macros) and
   *  markup characters from, or null to start with new environment and
   *  default markup
   */
  public MacroReader(Reader reader, String description, MacroReader prev) {
    this(reader, description, prev != null ? prev.environment : null);
    if (prev != null) {
      setMarkupCharacters(prev.markupChars);
    }
  }

  private MacroReader(Reader reader, String description, Environment e) {
    if (e == null) {
      e = new Environment();
    }
    e.setReader(this);
    this.environment = e;
    if (description == null) {
      description = reader.toString();
    }

    readerDescription = description;
    pbReader = new DynamicPushbackReader(reader);
    initBuffer();
    setMarkupCharacters("{}=\\?#");
  }

  private int peek(int amt) throws IOException {
    return pbReader.peek(amt);
  }

  private int read0() throws IOException {
    int c = pbReader.read();
    adjustLineColumn(c);
    return c;
  }

  /**
   * Get string describing object
   * @return String
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(readerDescription);
    sb.append(": line ");
    sb.append(1 + lineNumber);
    sb.append(", column ");
    sb.append(1 + rowNumber);

    return sb.toString();
  }

  // Methods
  private boolean escapeFlag;

  /**
   * Read a character from the stream
   * @return character read, or -1 if eof
   * @throws IOException
   */
  public int read() throws IOException {

    int out = -1;
    boolean escaped = false;
    while (true) {

      // if no literal exists, parse and evaluate another expression
      if (cursor == parseBuffer.length()) {

        // if parse buffer is quite full, start a new one (leave the old one
        // untouched since it may still be in use)
        if (cursor >= BUFFER_SIZE) {
          initBuffer();
        }

        if (escapeFlag || !atMarkup()) {
          escapeFlag = false;
          int ch = read0();
          if (ch < 0) {
            break;
          }
          if (ch == muEscape) {
            escapeFlag = true;
          }
          parseBuffer.append( (char) ch);
        }
        else {

          Expr e = parse(parseBuffer);
          if (e == null) {
            break;
          }
          e.evaluate(environment, parseBuffer);
          continue;
        }
      }

      out = parseBuffer.charAt(cursor);
      cursor++;

      // replace escaped markup chars with unescaped chars
      if (!escaped && out == muEscape) {
        // If next character exists, and is one of the markup chars,
        // set escape mode.
        if (cursor < parseBuffer.length()) {
          char next = parseBuffer.charAt(cursor);
          if (markupChars.indexOf(next) >= 0) {
            escaped = true;
            continue;
          }
        }
      }
      break;
    }
    return out;

  }

  /**
   * Read an expected character from the underlying reader
   * @param c char
   * @return char
   * @throws IOException
   */
  private char read(char c) throws IOException {
    int c2 = read0();
    if (c2 != c) {
      throw environment.exc("illegal char, " + (char) c2 + ", expected " + c);
    }
    return c;
  }

  private StringBuffer idWork = new StringBuffer();

  /**
   * Read an id from the underlying reader
   * @return String
   * @throws IOException
   */
  private String readId() throws IOException {
    readWS();
    idWork.setLength(0);
    idWork.append(read(muEscape));
    while (true) {
      int c = peek(0);
      if (!isIdChar(c)) {
        break;
      }
      read0();
      idWork.append( (char) c);
    }
    if (idWork.length() == 1) {
      throw environment.exc("bad or missing id");
    }
    return idWork.toString();
  }

  /**
   * Consume any whitespace from underlying reader
   * @throws IOException
   */
  private void readWS() throws IOException {
    while (true) {
      int c = peek(0);
      if (c > ' ') {
        break;
      }
      read0();
    }
  }

  private static boolean isIdChar(int c) {
    return (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'A')
        || (c == '_')
        || (c >= '0' && c <= '9');
  }

  /**
   * Read portion of array of characters
   * @param charArray char[]
   * @param off int
   * @param len int
   * @return int
   * @throws IOException
   */
  public int read(char[] charArray, int off, int len) throws IOException {
    int i;
    for (i = 0; i < len; i++) {
      int c = read();
      if (c < 0) {
        if (i == 0) {
          i = -1;
        }
        break;
      }
      charArray[off + i] = (char) c;
    }
    return i;
  }

  /**
   * Close the reader.
   * @throws IOException
   */
  public void close() throws IOException {
    pbReader.close();
  }

  /**
   * Determine if next chars in reader may contain markup.
   * @return boolean
   */
  private boolean atMarkup() throws IOException {
    int c1 = peek(0);
    return (c1 == muEscape || c1 == muCond || c1 == muUnicode);
  }

  /**
   * Parse an expression from the underlying reader.
   * @param parseBuffer : buffer to store literals in, if expression
   *   is not a markup expression
   *
   * @return Expr, or null if end of file
   */
  private Expr parse(StringBuffer parseBuffer) throws IOException {

    Expr out = null;
    int c1 = peek(0), c2 = peek(1);

    if (c1 == muEscape && isIdChar(c2)) {
      String id = readId();
      if (peek(0) == muEquals && peek(1) == muParop) {
        FnDef fd = new FnDef(id);
        read();
        read();
        while (true) {
          readWS();
          if (peek(0) != muEscape) {
            break;
          }
          fd.addFormal(readId());
        }
        read(muParcl);
        fd.setBody(parseExprList());
        out = fd;
      }
      else {
        FnCall fc = new FnCall(id);
        while (peek(0) == muParop) {
          fc.addArg(parseExprList());
        }
        out = fc;
      }
    }
    else if (c1 == muCond && c2 == muParop) {
      read0();
      out = new Cond(parseExprList(), parseExprList(), parseExprList());
    }
    else if (c1 == muUnicode && c2 == muParop) {
      read0();
      read0();
      int val = 0;
      int i;
      for (i = 0; ; i++) {
        c2 = read0();
        if (c2 == muParcl) {
          break;
        }
        val = (val << 4) | Scanner.parseHex( (char) c2);
      }
      if (i < 1 || i > 4) {
        throw environment.exc("bad unicode string");
      }
      StringBuffer work = new StringBuffer("" + (char) val);
      out = new Literal(work, 0, 1);
    }
    else if (c1 >= 0) {
      read0();
      int start = parseBuffer.length();
      parseBuffer.append( (char) c1);
      if (c1 == muEscape) {
        c2 = read0();
        if (c2 >= 0) {
          parseBuffer.append( (char) c2);
        }
      }
      Literal lit = new Literal(parseBuffer, start,
                                parseBuffer.length() - start);
      out = lit;
    }
    return out;
  }

  /**
   * Parse an ExprList from the underlying Reader.
   * { <expr>* }
   *
   * @return ExprList
   * @throws IOException
   */
  private ExprList parseExprList() throws IOException {
    ExprList lst = new ExprList();
    readWS();
    read(muParop);

    StringBuffer work = new StringBuffer();

    while (peek(0) != muParcl) {
      Expr e = parse(work);
      if (e == null) {
        throw environment.exc("Unexpected eof");
      }
      lst.add(e);
    }
    read(muParcl);
    return lst;
  }

  private void initBuffer() {
    parseBuffer = new StringBuffer(BUFFER_SIZE);
    cursor = 0;
  }

  private void adjustLineColumn(int c) {
    if (c == '\n') {
      lineNumber++;
      rowNumber = 0;
    }
    else if (c >= 0) {
      if (c != 0x0d) {
        rowNumber++;
      }
    }
  }

  // environment (contains bindings)
  private Environment environment;

  // pushback reader constructed from underlying Reader, to allow
  // peeking at upcoming characters (only 2 character lookahead required)
  private DynamicPushbackReader pbReader;

  // buffer containing parsed literals
  private StringBuffer parseBuffer;
  private int lineNumber;
  private int rowNumber;
  private String readerDescription;

  // position within parseBuffer to read next character from
  private int cursor;

  private static class Environment {
    public void setReader(MacroReader r) {
      this.reader = r;
    }

    private MacroReader reader;

    /**
     * Get string describing object
     * @return String
     */
    public String toString() {
      if (MacroReader.VERBOSE) {
        StringBuffer sb = new StringBuffer();
        sb.append("Environment level=" + level);
        sb.append("\nbindings:\n");

        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
          String k = (String) it.next();
          BindingHandle b = binding(k);
          sb.append(" id= " + k + " binding= " + b.handle);
          sb.append("\n");
        }

        return sb.toString();
      }
      else {
        return super.toString();
      }
    }

    public void scopeIn() {
      bindingStack.push(Boolean.TRUE);
      this.level++;
    }

    public void scopeOut() {
      while (true) {
        if (bindingStack.isEmpty()) {
          break;
        }
        Object obj = bindingStack.pop();
        if (obj instanceof Boolean) {
          break;
        }
        BindingHandle b = (BindingHandle) obj;

        String id = b.handle.id;

        if (!b.scopeOut()) {
          map.remove(id);
        }
      }
      this.level--;
    }

    /**
     * Find binding for id
     * @param id : identifier
     * @param mustExist : if true, and no binding exists, throws exception
     * @return Expr
     */
    public Expr findBindingFor(String id, boolean mustExist) {
      BindingHandle b = binding(id);
      if (b == null) {
        if (mustExist) {
          throw exc("undefined id: " + id);
        }
        return null;
      }
      return b.handle.expr;
    }

    public ScanException exc(String msg) {
      return new ScanException(msg + " (" + reader + ")");
    }

    public void addBinding(String id, Expr e) {
      Binding bnew = new Binding(id, e, level);
      BindingHandle curr = binding(id);

      if (MacroReader.db) {
        Streams.out.println("addBinding id=" + id + " to " + e);
      }
      if (curr != null) {
        if (curr.handle.level == this.level) {
          throw exc("id " + id + " already bound");
        }
        bnew.prev = curr.handle;
        curr.handle = bnew;
      }
      else {
        curr = new BindingHandle(bnew);
        map.put(id, curr);
      }
      bindingStack.push(curr);
    }

    private BindingHandle binding(String id) {
      return (BindingHandle) map.get(id);
    }

    private static class Binding {
      public Binding(String id, Expr expr, int level) {
        this.id = id;
        this.expr = expr;
        this.level = level;
      }

      /**
       * Get string describing object
       * @return String
       */
      public String toString() {
        if (MacroReader.VERBOSE) {
          StringBuffer sb = new StringBuffer();
          sb.append("Binding");
          sb.append(" level=" + level + " expr=" + expr);
          return sb.toString();
        }
        else {
          return super.toString();
        }
      }

      Binding prev;
      Expr expr;
      int level;
      String id;
    }

    private static class BindingHandle {
      public BindingHandle(Binding b) {
        this.handle = b;
      }

      public boolean scopeOut() {
        handle = handle.prev;
        return handle != null;
      }

      Binding handle;
    }

    private Map map = new HashMap();
    private int level;
    private DArray bindingStack = new DArray();
  }

  private static interface Expr {
    public Literal evaluate(Environment e, StringBuffer dest);
  }

  private static class FnCall
      implements Expr {

    public FnCall(String id) {
      this.id = id;
    }

    public void addArg(ExprList e) {
      args.add(e);
    }

    public int nArgs() {
      return args.size();
    }

    public ExprList arg(int i) {
      return (ExprList) args.get(i);
    }

    public Literal evaluate(Environment e, StringBuffer dest) {
      final boolean db = MacroReader.db;

      Literal s;

      if (db) {
        System.out.println("evaluate FnCall " + id);
      }

      int argCount = nArgs();
      boolean optionalArg = (argCount == 1 && arg(0).size() == 0);

      // Find the function definition.
      Expr expr = e.findBindingFor(id, true);

      // If it's a Literal, make sure no arguments were supplied.
      if (expr instanceof Literal) {
        if (argCount > 0 && !optionalArg) {
          throw e.exc(id + " takes no arguments");
        }
        if (db) {
          Streams.out.println(" fnCall, evaluating no arguments");
        }
        s = expr.evaluate(e, dest);
      }
      else {
        if (! (expr instanceof FnDef)) {
          throw e.exc("cannot evaluate as function, id=" + id + " bound to " +
                      expr);
        }
        FnDef fn = (FnDef) expr;

        // If a single blank argument was provided, and function has no formals,
        // ignore the single argument.
        if (optionalArg && fn.nFormals() == 0) {
          argCount = 0;
        }

        if (fn.nFormals() != argCount) {
          throw e.exc(fn.id() + " expected " + fn.nFormals() +
                      " arguments, got " + argCount);
        }

        if (argCount == 0) {
          if (db) {
            System.out.println(" evaluating zero argument FnCall");
          }
          s = fn.body().evaluate(e, dest);
        }
        else {
          DArray argVals = new DArray();
          // evaluate each of the arguments into a temporary buffer
          StringBuffer fncallWork = new StringBuffer();
          for (int i = 0; i < argCount; i++) {
            if (db) {
              System.out.println(" evaluate FnCall arg " + i +
                                 " to work buffer");
            }
            Literal lt = arg(i).evaluate(e, fncallWork);
            argVals.add(lt);
          }
          // now scope in, and bind the formal parameters to the arg values
          e.scopeIn();
          for (int i = 0; i < argCount; i++) {
            e.addBinding(fn.formal(i), (Expr) argVals.get(i));
          }
          if (db) {
            System.out.println("evaluate FnCall body for Fn " + fn.id());
          }

          s = fn.body().evaluate(e, dest);
          e.scopeOut();
        }
      }
      return s;
    }

    /**
     * Get string describing object
     * @return String
     */
    public String toString() {
      if (MacroReader.VERBOSE) {
        StringBuffer sb = new StringBuffer();
        sb.append("FnCall");
        sb.append(" id=" + id + "\n");
        for (int i = 0; i < nArgs(); i++) {
          sb.append(" arg #" + i + ": " + arg(i));
        }
        return sb.toString();
      }
      else {
        return super.toString();
      }
    }

    private DArray args = new DArray();
    private String id;
  }

  private static class Cond
      implements Expr {

    public Cond(Expr flag, Expr trueExpr, Expr falseExpr) {
      this.flag = flag;
      this.trueExpr = trueExpr;
      this.falseExpr = falseExpr;
    }

    public Literal evaluate(Environment e, StringBuffer dest) {
      StringBuffer work = new StringBuffer();
      if (MacroReader.db) {
        System.out.println("evaluate, Cond");
      }
      Literal lit = flag.evaluate(e, work);

      String s = lit.text();
      boolean flag = false;

      if (s.length() > 0) {
        try {
          flag = Integer.parseInt(s) != 0;
        }
        catch (NumberFormatException ex) {
          throw e.exc("failed to parse integer in conditional expression");
        }
      }
      if (MacroReader.db) {
        System.out.println(" evaluating Cond side, flag=" + flag);
      }
      Literal out = flag ? trueExpr.evaluate(e, dest) :
          falseExpr.evaluate(e, dest);
      return out;
    }

    private Expr flag;
    private Expr trueExpr, falseExpr;
  }

  private static class ExprList
      implements Expr {
    public Literal evaluate(Environment e, StringBuffer dest) {
      if (MacroReader.db) {
        Streams.out.println("ExprList, evaluate");
      }
      int start = dest.length();
      for (int i = 0; i < size(); i++) {
        if (MacroReader.db) {
          Streams.out.println(" ExprList evaluating expr #" + i);
        }

        get(i).evaluate(e, dest);
      }
      return new Literal(dest, start, dest.length() - start);
    }

    /**
     * Get string describing object
     * @return String
     */
    public String toString() {
      if (MacroReader.VERBOSE) {
        StringBuffer sb = new StringBuffer();
        sb.append("ExprList " + super.toString());
        for (int i = 0; i < size(); i++) {
          sb.append(" expr #" + i + ": " + get(i));
        }
        return sb.toString();
      }
      else {
        return super.toString();
      }
    }

    public void add(Expr e) {
      list.add(e);
    }

    public int size() {
      return list.size();
    }

    public Expr get(int i) {
      return (Expr) list.get(i);
    }

    private DArray list = new DArray();
  }

  private static class FnDef
      implements Expr {

    public FnDef(String id) {
      this.id = id;
    }

    public void addFormal(String formalId) {
      formals.add(formalId);
    }

    public void setBody(ExprList body) {
      this.body = body;
    }

    public Literal evaluate(Environment e, StringBuffer dest) {
      e.addBinding(id, this);
      if (MacroReader.db) {
        Streams.out.println("evaluate FnDef id=" + id + ", returning EMPTY");
      }

      return Literal.EMPTY;
    }

    public String formal(int i) {
      return formals.getString(i);
    }

    public int nFormals() {
      return formals.size();
    }

    public ExprList body() {
      return body;
    }

    public String id() {
      return id;
    }

    /**
     * Get string describing object
     * @return String
     */
    public String toString() {
      if (MacroReader.VERBOSE) {
        StringBuffer sb = new StringBuffer();
        sb.append("FnDef");
        sb.append(" id=" + id + "\n");
        for (int i = 0; i < nFormals(); i++) {
          sb.append(" formal #" + i + ": " + formal(i) + "\n");
        }
        sb.append(" body: " + body());
        return sb.toString();
      }
      else {
        return super.toString();
      }
    }

    private String id;
    private DArray formals = new DArray();
    private ExprList body;
  }

  private static class Literal
      implements Expr {
    public static final Literal EMPTY = new Literal();

    private Literal() {
      text = "";
    }

    public Literal(StringBuffer sb, int start, int length) {
      this.sb = sb;
      this.start = start;
      this.length = length;
    }

    public Literal evaluate(Environment e, StringBuffer dest) {
      if (MacroReader.db) {
        Streams.out.println("evaluate Literal " +
                            sb.substring(start, start + length));
      }

      if (dest != sb) {
        if (MacroReader.db) {
          Streams.out.println(" dest != sb, copying");
        }

        for (int i = start; i < start + length; i++) {
          dest.append(sb.charAt(i));
        }
      }
      return this;
    }

    /**
     * Construct string of literal.
     * For efficiency, this should not be done willy-nilly.
     * @return String
     */
    public String text() {
      if (text == null) {
        text = sb.substring(start, start + length);
      }
      return text;
    }

    /**
     * Get string describing object
     * @return String
     */
    public String toString() {
      if (MacroReader.VERBOSE) {
        StringBuffer sb = new StringBuffer();
        sb.append("Literal: " + Tools.d(text()) + "\n");
        return sb.toString();
      }
      else {
        return super.toString();
      }
    }

    private StringBuffer sb;
    private int start, length;
    private String text;
  }
}
