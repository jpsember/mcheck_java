package mcheck;

import base.*;
import java.util.*;
import java.io.*;

class LTLCheck
    implements IScript {

  /**
   * Constructor
   * @param env Environment
   */
  public LTLCheck(Environment env) {
    this(env, 0);
  }

  /**
   * Constructor
   * @param env Environment
   * @param options OPT_xxx flags
   */
  public LTLCheck(Environment env, int options) {
    this.env = env;
    options_ = options;
  }

  private static final boolean db = false;

  public static final int OPT_PRINTSTATES = 0x0001,
      OPT_PRINTFULLSEQ = 0x0002,
      OPT_PRINTBUCHI = 0x0004;

  /**
   * Find the point at which a sequence of integers starts to repeat.
   * If repeat sequence is found, last item in list is deleted.
   * @param seg : array of Integers
   * @return index of start of repeating subsequence, or -1 if none found
   */
  private static int repeatPoint(DArray seq) {
    int rep = -1;
    for (int i = seq.size() - 2; i >= 0; i--) {
      if (seq.getInt(i) == seq.lastInt()) {
        rep = i;
        seq.pop();
        break;
      }
    }
    return rep;
  }

  /**
   * Check a formula
   * @param model Model to check
   * @param f specification (LTL formula)
   */
  public void check(Model model, Formula f) {
    PrintStream out = Streams.out;

    Buchi ngb = new Buchi(env);
    constructAutomaton(f, true, ngb);
    if (option(OPT_PRINTBUCHI)) {
      out.println("Formula automaton:\n" + ngb);
    }

    Tools.ASSERT(model.defined());

    // convert model to Buchi automaton
    Buchi bModel = new Buchi(env);
    bModel.convertKripke(model);
    if (option(OPT_PRINTBUCHI)) {
      out.println("Model automaton:\n" + bModel);
    }

    Buchi bProd = new Buchi(env);
    Buchi bProd0 = new Buchi(env);
    bProd0.calcProduct(bModel, ngb);
    bProd0.reduce(bProd);

    if (option(OPT_PRINTBUCHI)) {
      out.println("Product automaton:\n" + bProd);
    }

    StringBuffer w = new StringBuffer();
    DArray seq = bProd.nonEmpty();
    if (seq != null) {
      w.append("Not satisfied; counterexample:\n  ");
      int rep = repeatPoint(seq);
      for (int i = 1; i < seq.length(); i++) {
        int s = seq.getInt(i);
        if (i > 1) {
          w.append(' ');
        }
        if (i == rep) {
          w.append('{');
        }
        w.append(bProd.stateLabel(s));
      }
      if (rep >= 0) {
        w.append("}*");
      }
    }
    else {
      w.append("Satisfied.");
    }
    String str = w.toString();

    if (!option(OPT_PRINTFULLSEQ)) {
      str = Tools.trimLength(str, 75, true);
    }

    out.println(str + "\n");
  }

  /**
   * Compare two LTL formulas
   * @param f1 Formula
   * @param f2 Formula
   */
  public void compare(Formula f1, Formula f2) {
    compare(f1, f2, false);
  }

  /**
   * Compare two LTL formulas
   * @param f1 Formula
   * @param f2 Formula
   * @param printReduced : true to print reduced formulas
   */
  public void compare(Formula f1, Formula f2, boolean printReduced) {
    Streams.out.println("Comparing: " + f1);
    if (printReduced) {
      Streams.out.println(Tools.sp(11) + f1.reduced());
    }

    Streams.out.println("     with: " + f2);
    if (printReduced) {
      Streams.out.println(Tools.sp(11) + f2.reduced());
    }
    Streams.out.println();

    Buchi b1 = new Buchi(env), b2 = new Buchi(env);

    boolean equiv = true;

    for (int pass = 0; pass < 2; pass++) {

      if (pass == 0) {
        if (option(OPT_PRINTSTATES)) {
          Streams.out.print("First automaton:\n");
        }
        constructAutomaton(f1, false, b1);
        if (option(OPT_PRINTSTATES)) {
          Streams.out.print("Second automaton:\n");
        }
        constructAutomaton(f2, true, b2);
      }
      else {
        if (option(OPT_PRINTSTATES)) {
          Streams.out.print("First automaton:\n");
        }
        constructAutomaton(f2, false, b1);
        if (option(OPT_PRINTSTATES)) {
          Streams.out.print("Second automaton:\n");
        }
        constructAutomaton(f1, true, b2);
      }

      // label first automaton with description of its prop.var values
      b1.setPropVarLabels();

      if (option(OPT_PRINTBUCHI)) {
        Streams.out.println("First automaton:\n" + b1);
        Streams.out.println("Second automaton:\n" + b2);
      }

      Buchi prod = new Buchi(env);
      Buchi prod0 = new Buchi(env);
      prod0.calcProduct(b1, b2);
      prod0.reduce(prod);
      if (option(OPT_PRINTBUCHI | OPT_PRINTSTATES)) {
        Streams.out.println("Product automaton:\n" + prod);
      }

      DArray seq = prod.nonEmpty();
      if (seq != null) {

        if (equiv) {
          equiv = false;
          Streams.out.print("Not equivalent.\n");
        }

        StringBuffer sb = new StringBuffer("\n");
        {
          sb.append( (pass == 0 ? " first" : "second"));
          sb.append(" allows: ");

          // find repeat point
          int rep = repeatPoint(seq);
          for (int i = 0; i < seq.length(); i++) {
            int s = seq.getInt(i);
            if (i > 0) {
              sb.append(' ');
            }

            if (i == rep) {
              sb.append("{");
            }
            sb.append(prod.stateLabel(s));
          }
          sb.append("}*");
        }
        String str = sb.toString();
        if (!option(OPT_PRINTFULLSEQ)) {
          if (false) {
            Tools.warn("not truncating");
          }
          else {
            str = Tools.trimLength(str, 75, true);
          }
        }
        Streams.out.println(str);
      }
    }
    if (equiv) {
      Streams.out.print("Equivalent.\n");
    }
    Streams.out.println();
  }

  /**
   * Determine if a particular option has been set
   * @param flag int
   * @return boolean
   */
  private boolean option(int flag) {
    return (options_ & flag) != 0;
  }

  /**
   * Construct a Buchi automaton for a formula
   * @param f specification (LTL formula)
   * @param negate true if formula should be negated
   * @param b automaton to construct
   */
  private void constructAutomaton(Formula f, boolean negate, Buchi b) {
    if (db) {
      Streams.out.println("constructAutomaton for formula:\n " + f +
                          "\n negate=" + negate);
    }

    if (!f.isLTL()) {
      throw new RuntimeException(
          "Cannot construct automaton for non-LTL formulas");
    }

    if (negate) {
      f = f.negate();
    }

    forestNodes.clear();

    pvWarn_.clear();
    nForest_.clear();
    aNodes.clear();
    initNode_ = -1;

    // reduce formula to minimal set of connectives
    f.reduce();
    if (db) {
      Streams.out.println(" reduced: " + f);
    }

    createGraph(f);

    Buchi bg = constructBuchi(f);

    Buchi bg2 = new Buchi(env);
    bg.convertGeneralized(bg2);
    bg2.reduce(b);
  }

  /**
   * Construct a new state in the forest, and allocate
   * its Node in the forest auxilliary data store
   * @return id of node
   */
  private int newNode() {
    int node = nForest_.newNode();
    forestNodes.set(node - Forest.IDBASE, new Node());
    return node;
  }

  /**
   * 	Create automaton states
   */
  private void createGraph(Formula f) {
    final boolean db = false;

    if (db) {
      Streams.out.println("createGraph for " + f);
    }

    aNodes.clear();
    aNodes.addInt(initNode_ = newNode());

    int id;
    Node np = node(id = newNode());

    np.incoming.add(initNode_);
    np.fNew.add(f.root());

    if (db) {
      printStateSet(f);
    }

    expand(f, id);

    if (db || option(OPT_PRINTSTATES)) {
      printStateSet(f, true);
    }
  }

  /**
   * Build a tableau (see p. 134).
   * The numbers '// x' correspond to line numbers from the
   * 'simple on-the-fly...' paper
   */
  private void expand(Formula f_, int q) {

    final boolean db = false;

    if (db) {
      Streams.out.println("expand formula, root=" + q);
    }

    Node qr = node(q);
// 4
    if (qr.fNew.isEmpty()) {

      // 5
      // skip the initial node
      for (int i = 1; i < aNodes.size(); i++) {

        Node t = node(aNodes.getInt(i)); //aNode(i));
        if (t.fOld.equals(qr.fOld)
            && t.fNext.equals(qr.fNext)
            ) {
          // 6
          t.incoming.include(qr.incoming);
          return;
        }
      }
      int n2 = newNode();

      Node n2p = node(n2);
      n2p.incoming.add(q);
      n2p.fNew = new OrdSet(qr.fNext);

      aNodes.addInt(q);
      expand(f_, n2);
      return;
    }

// 12
    // New(q) is not empty
    int e = qr.fNew.removeLast();

    // 13.5: not in original paper; more efficient to test for
    //  'old' added again (modifying lines 22 and 25)
    if (qr.fOld.contains(e) != null) {
      expand(f_, q);
      return;
    }

    int litCode = env.getLiteralCode(e);
    if (litCode != 0) {
// 15

      // is e False, or is its negation in q.old?
      if (litCode == -1) {
        return; // BOTTOM or FALSE
      }

      // see if negative of this exists in q.old.
      for (int i = 0; i < qr.fOld.length(); i++) {
        int litCode2 = env.getLiteralCode(qr.fOld.get(i));
        if (litCode2 == -litCode) {
          return;
        }
      }

// 18
      // add e to q.old
      if (litCode != 1) { // don't add TRUE
        qr.fOld.add(e);
      }
      expand(f_, q);
      return;
    }

// 15
    int etype = env.nType(e);
    switch (etype) {
      case T_UNTIL:
      case T_RELEASE:
      case T_OR: {
        {
          int id1 = newNode();
          Node n1 = node(id1);

          n1.incoming = new OrdSet(qr.incoming);
          n1.fNew = new OrdSet(qr.fNew);
          switch (etype) {
            case T_UNTIL:
            case T_OR:
              n1.fNew.add(f_.child(e, 0));
              break;
            case T_RELEASE:
              n1.fNew.add(f_.child(e, 1));
              break;
          }

          n1.fOld = new OrdSet(qr.fOld);
          n1.fOld.add(e);

          n1.fNext = new OrdSet(qr.fNext);
          switch (etype) {
            case T_UNTIL:
            case T_RELEASE:
              n1.fNext.add(e);
              break;
          }
          expand(f_, id1);
        }

        {
          int id2 = newNode();
          Node n2 = node(id2);

          n2.incoming = new OrdSet(qr.incoming);

          n2.fNew = new OrdSet(qr.fNew);

          switch (etype) {
            case T_UNTIL:
            case T_OR:
              n2.fNew.add(f_.child(e, 1));
              break;
            case T_RELEASE:
              n2.fNew.add(f_.child(e, 0));
              n2.fNew.add(f_.child(e, 1));
              break;
          }

          n2.fOld = new OrdSet(qr.fOld);
          n2.fOld.add(e);

          n2.fNext = new OrdSet(qr.fNext);
          expand(f_, id2);
        }
      }
      break;

      case T_AND: {
        int id1 = newNode();
        Node n1 = node(id1);

        n1.incoming = new OrdSet(qr.incoming);

        n1.fNew = new OrdSet(qr.fNew);
        n1.fNew.add(f_.child(e, 0));
        n1.fNew.add(f_.child(e, 1));

        n1.fOld = new OrdSet(qr.fOld);
        n1.fOld.add(e);

        n1.fNext = new OrdSet(qr.fNext);
        expand(f_, id1);
      }
      break;

      case T_NEXT: {
        int id1 = newNode();
        Node n1 = node(id1);

        n1.incoming = new OrdSet(qr.incoming);

        n1.fNew = new OrdSet(qr.fNew);

        n1.fOld = new OrdSet(qr.fOld);
        n1.fOld.add(e);

        n1.fNext = new OrdSet(qr.fNext);
        n1.fNext.add(f_.child(e, 0));

        expand(f_, id1);
      }
      break;

      default:
        throw new RuntimeException("node type = " + etype);
    }
  }

  /*	Node class for constructing automaton
   */
  private static class Node {

    // list of predecessor nodes
    OrdSet incoming = new OrdSet();
    // subformulas already processed
    OrdSet fOld = new OrdSet();
    OrdSet fNew = new OrdSet();
    // subformulas yet to be processed
    OrdSet fNext = new OrdSet();
  };

  /**
   * Construct a GBA (gen. buchi automaton) from the states
   */
  private Buchi constructBuchi(Formula f_) {
    Buchi b = new Buchi(env);

    // construct a translation table for existing state numbers to
    // buchi state numbers
    DArray newNums = new DArray();
    for (int i = 0; i < aNodes.size(); i++) {
      newNums.setInt(aNodes.getInt(i), i);
    }

    // add states
    for (int i = 0; i < aNodes.size(); i++) {
      int s = b.addState(i == 0);

      // add flags for prop. vars
      Node nd = node(aNodes.getInt(i));
      for (int j = 0; j < nd.fOld.length(); j++) {
        int f = nd.fOld.get(j);
        int code = env.getLiteralCode(f);
        boolean neg = (code < 0);
        code = Math.abs(code);
        if (code < 2) {
          continue;
        }
        b.addPropVar(s, code - 2, !neg);
      }
    }

    // add transitions
    for (int i = 0; i < aNodes.size(); i++) {
      int destNum = aNodes.getInt(i);
      Node dest = node(destNum);

      for (int j = 0; j < dest.incoming.length(); j++) {
        int srcNum = dest.incoming.get(j);
        b.addTransition(newNums.getInt(srcNum), newNums.getInt(destNum));
      }
    }

    // add accepting sets for (a U b) formulas

    {
      DArray nList = env.forest.getNodeList(f_.root());

      for (int i = 0; i < nList.length(); i++) {
        int root = nList.getInt(i);

        if (f_.nType(root) != T_UNTIL) {
          continue;
        }
        int childB = f_.child(root, 1);

        BitSet set = new BitSet();
        for (int j = 0; j < aNodes.size(); j++) {
          int si = aNodes.getInt(j);
          Node src = node(si);

          // see if (a U b) does not exist in src.old
          //  or b IS in src.old
          if (src.fOld.contains(root) == null
              || src.fOld.contains(childB) != null
              ) {
            set.set(newNums.getInt(si));
          }
        }
        b.addAcceptSet(set);
      }
    }
    return b;
  }

  /**
   * Get a particular node
   * @param n : id of node
   * @return Node
   */
  private Node node(int n) {
    return (Node) forestNodes.get(n - Forest.IDBASE);
  }

  private void printStateSet(Formula f) {
    printStateSet(f, false);
  }

  private void printStateSet(Formula f_, boolean skipNew) {
    Streams.out.println(
        "------------- States ------------------------------------");
    for (int i = 0; i < aNodes.size(); i++) {
      printState(aNodes.getInt(i), skipNew);
    }
    // print the formulas associated with each node
    Streams.out.println(
        "------------- Formulas ----------------------------------");
    DArray list = env.forest.getNodeList(f_.root());
    for (int i = 0; i < list.length(); i++) {
      Streams.out.println(Tools.f(list.getInt(i), 3) + ": " +
                          f_.toString(list.getInt(i)));
    }
    Streams.out.println(
        "---------------------------------------------------------\n");
  }

  private void printState(int nodeInd, boolean skipNew) {
    int pamt = 24;
    StringBuffer sb = new StringBuffer();

    Node n = node(nodeInd);
    sb.append(Tools.f(nodeInd, 2) + ": ");
    for (int j = 0; j < n.incoming.length(); j++) {
      sb.append(Tools.f(n.incoming.get(j), 2) + " ");
    }

    int p = 24;
    Tools.tab(sb, p);
    p += pamt;

    if (!skipNew) {
      sb.append("  New:");
      sb.append(n.fNew.toString());
      Tools.tab(sb, p);
      p += pamt;
    }
    sb.append("  Old:");
    sb.append(n.fOld.toString());
    Tools.tab(sb, p);
    p += pamt;
    sb.append(" Next:");
    sb.append(n.fNext.toString());
    Tools.tab(sb, p);
    p += pamt;

    Streams.out.println(sb.toString());
  }

  // flags indicating which vars we've printed warnings about
  private BitSet pvWarn_ = new BitSet();

  // symbol table
  private Environment env;

  // automaton states
  private Forest nForest_ = new Forest();

  // LTL -> automaton conversion:  ids of nodes
  private DArray aNodes = new DArray();

  // Node objects corresponding to forest nodes are stored here
  private DArray forestNodes = new DArray();

  // id of special 'init' node
  private int initNode_;

  private int options_;
}
