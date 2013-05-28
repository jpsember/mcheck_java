package mcheck;
import java.io.*;
import base.*;

class Environment
    implements IScript {
  public void indent(String s) {
    if (tw != null) {
      pw.println(s);
      tw.indent();
    }
  }

  public void out(Formula f) {
    if (tw != null) {
      pw.println(f.toString());
    }
  }

  public void outdent() {
    if (tw != null) {
      tw.outdent();
    }
  }

  public void trace(boolean f) {
    if (f) {
      tw = new TabbedWriter(Streams.out);
      pw = new PrintWriter(tw);
    }
    else {
      tw = null;
      pw = null;
    }

  }

  public DFA dfa;

  private TabbedWriter tw;
  private PrintWriter pw;

  public void clear() {
    vars.clear();
    forest.clear();
    tokens.clear();
  }

  public Environment() {
    forest = new Forest();
    tokens = new DArray();
    vars = new Vars();
  }

  public int getLiteralCode(int root) {
    final boolean db = false;
    if (db) {
      Streams.out.println("getLiteralCode root=" + root + ":");
    }
    if (db) {
      Streams.out.println(" tree=" + forest.nodeString(root) + "\n formula=" +
                          Formula.toString(this, root));
    }

    boolean neg = false;

    {
      Token t = token(root);
      if (t.id(T_NEGATION)) {
        neg = true;
        root = forest.child(root, 0);
      }
    }

    int val = 0;

    Token t = token(root);
    if (db) {
      Streams.out.println(" token = " + t.id());
    }

    switch (t.id()) {
      case T_PROPVAR:
        val = 2 + vars.var(t.text(), true);
        break;
      case T_TRUE:
        val = 1;
        break;
      case T_BOTTOM:
        val = -1;
        break;
    }
    if (neg) {
      val = -val;
    }
    if (db) {
      Streams.out.println(" neg=" + neg + " val=" + val);
    }

    return val;
  }

  public Forest forest;
  public DArray tokens;
  public boolean filterParen;
  public Vars vars;

  public Token token(int index) {
    return ( (Token) (tokens.get(index)));
  }

  public int nType(int node) {
    return token(node).id();
  }

}
