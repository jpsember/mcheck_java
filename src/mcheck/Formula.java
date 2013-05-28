package mcheck;

import base.*;
import java.io.*;

class Formula
    implements IScript { //, /*IForest,*/ NodeData {

  private static final boolean db = false;

  public static final int
      TYPE_SIMPLE = 0,
      TYPE_LTL = 1,
      TYPE_CTL = 2,
      TYPE_CTL_STAR = 3
      ;

  public boolean isCTL() {
    return type() == TYPE_CTL || type() == TYPE_SIMPLE;
  }

  public boolean isLTL() {
    return type() == TYPE_LTL || type() == TYPE_SIMPLE;
  }

  public Object getNode(int nodeId) {
    return str(nodeId);
  }

  /*	Get type of formula
                  < TYPE_xxx
   */
  public int type() {
    return typeFlags_;
  }

  /*	Get token type from formula
     //		> node						node of tree, or -1 for root
                  < type of token
   */
  public int rootType() {
    return env.nType(root());
  }

//  private void printNode(int root, StringBuffer sb) {
//    printNode(root, -1, sb);
//  }

  /*	Construct the negation of the formula
                  < negated formula
   */
  public Formula negate() {
    return negate(root());
  }

  private static final int
      BF_LTL = 0x01
      , BF_CTL = 0x02
      ;

  private void calcType() {
    calcType( -1);
  }

  private void addFormula(Formula f) {
    addFormula(f, -1, -1, -1, false);
  }

  public int root() {
    return rootNodeId;
//
//    int r = rootNodeId;
//    if (r >= 0) {
//      r = forest().rootNode(r);
//    }
//    return r;
  }

  public boolean isEmpty() {
    return root() < 0;
  }

  public DArray tokens() {
    return env.tokens;
  }

  public Formula(Environment e) {
    this(e, 0, -1);
  }

  /**
   * Construct formula
   *
   * @param e Environment
   * @param flags int
   * @param root : id of root node; -1 if empty
   */
  private Formula(Environment e, int flags, int root) {
    this.env = e;
    this.typeFlags_ = flags;
    this.rootNodeId = root;
  }

  private Formula(Formula s) {
    this(s.env, s.typeFlags_, s.rootNodeId);
//    if (treeId_ >= 0) {
//      treeId_ = forest().newTree(root());
//    }
  }

  private Formula deepCopy() {
    Formula f = new Formula(this);

    f.rootNodeId = deepCopyAux(root());
//    forest().setRoot(f.treeId_, newRoot);

    return f;
  }

  private int deepCopyAux(int src) {
    int dest = forest().newNode();
    Token t = token(src);
    tokens().set(dest, t);

    for (int i = 0; i < nChildren(src); i++) {
      int dcChild = deepCopyAux(child(src, i));
      forest().addChild(dest, dcChild);
    }
    return dest;
  }

  private Forest forest() {
    return env.forest;
  }

  /*	Add a formula to the tree
    > fAdd formula containing subformula to be added
      > fStart	node of subformula's root (-1 for entire formula)
        > attachParent id of parent to attach to,
                          or -1 to attach to root; if root doesn't exist,
                                stores as root
             > attachIndex	insertion position in parent's child list; -1 to
                              attach as rightmost child
              > replaceFlag	 if true, doesn't insert child, but replaces
                               existing
   */

  private Formula addFormula(Formula f, int fStart, int ourParent,
                             int childIndex,
                             boolean replaceFlag) {
    final boolean db = false;

    if (f != null) {

      if (ourParent < 0) {
        ourParent = root();
      }

      if (fStart < 0) {
        fStart = f.root();
      }

      if (db) {
        Streams.out.println("addFormula " + fStart + " to " + ourParent);
      }

      if (fStart >= 0) {
        // if current formula is empty, make added root this one's root
        if (ourParent < 0) {
          rootNodeId = fStart;
//
//          treeId_ = forest().newTree(fStart);
//          if (db) {
//            Streams.out.println(" set treeId_ to " + treeId_);
//          }
        }
        else {
          forest().insertChild(ourParent, fStart, childIndex, replaceFlag);
          if (db) {
            Streams.out.println(" inserted child index " + childIndex);
          }
        }
      }
    }
    return this;
  }

  private static String printToken(Token t) {

    final int[] ids = {
        T_IMPLIES,
        T_NEGATION,
        T_AND,
        T_OR,
        T_ALL_UNTIL,
        T_EXISTS_UNTIL,
        T_UNTIL,
        T_RELEASE,
        T_WEAKUNTIL,
        T_TRUE,
        T_BOTTOM,
        T_NEXT,
        T_FUTURE,
        T_GLOBAL,
        T_AG,
        T_EG,
        T_AF,
        T_EF,
        T_AX,
        T_EX,
    };

    final String[] strs = {
        "->", // TK_IMPLIES,
        "!", // TK_NEGATION,
        "&", // TK_AND,
        "|", // TK_OR,
        "AU", // TK_AU,
        "EU", // TK_EU,
        "U", // TK_U,
        "R", // TK_R,
        "W", // TK_W,
        "T", // TK_TRUE,
        "B", // TK_BOTTOM,
        "X", // TK_X,
        "F", // TK_F,
        "G", // TK_G,
        "AG ", // TK_AG,
        "EG ", // TK_EG,
        "AF ", // TK_AF,
        "EF ", // TK_EF,
        "AX ", // TK_AX,
        "EX ", // TK_EX,
    };

    String s = "";

    int type = t.id();
    if (type == T_PROPVAR) {
      s = t.text();
    }
    else {
      for (int i = 0; i < ids.length; i++) {
        if (type == ids[i]) {
          s = strs[i];
          break;
        }
      }
    }
    return s;
  }

  private static int pri[] = {

      10, T_IMPLIES,
      50, T_NEGATION,
      20, T_AND,
      20, T_OR,
      80, T_PROPVAR,
      0, T_INTVAL,
      80, T_ALL_UNTIL,
      80, T_EXISTS_UNTIL,
      0, T_AE_UNTIL_END,
      0, T_PAROP,
      0, T_PARCL,
      30, T_UNTIL,
      30, T_RELEASE,
      30, T_WEAKUNTIL,
      80, T_TRUE,
      80, T_BOTTOM,
      50, T_NEXT,
      50, T_FUTURE,
      50, T_GLOBAL,
      50, T_AG,
      50, T_EG,
      50, T_AF,
      50, T_EF,
      50, T_AX,
      50, T_EX,
  };
  public Token token(int index) {
    return ( (Token) (env.tokens.get(index)));
  }

  private String printNode(int root, int priority, StringBuffer sb) {

    final boolean db = false;
    if (db) {
      Streams.out.println("printNode root=" + root + ", nchildren=" +
                          nChildren(root));
    }

    int nt = env.nType(root);

    int newPri = 0;
    for (int i = 0; i < pri.length; i += 2) {
      if (nt == pri[i + 1]) {
        newPri = pri[i + 0];
        break;
      }
    }

    // add parenthesis if new priority is less than old,
    // or new is equal to old and it's a binary operator

    boolean par = (newPri < priority)
        | (newPri == priority && nChildren(root) > 1);

    if (newPri == 30 && priority == 30) {
      par = true;
    }

    if (env.filterParen) {
      par = true;
    }

    priority = newPri;

    if (par) {
      sb.append("(");
    }

    switch (nt) {
      case T_NEXT:
      case T_GLOBAL:
      case T_FUTURE:
        sb.append(printToken(token(root)));
        sb.append(' ');
        printNode(child(root, 0), priority, sb);
        break;

      case T_IMPLIES:
      case T_AND:
      case T_OR:
      case T_UNTIL:
      case T_RELEASE:
      case T_WEAKUNTIL:
        printNode(child(root, 0), priority, sb);
        sb.append(' ');
        sb.append(printToken(token(root)));
        sb.append(' ');
        printNode(child(root, 1), priority, sb);
        break;

      case T_ALL_UNTIL:
        sb.append("A[");

//        Streams.out.print("A[");

        printNode(child(root, 0), 30, sb);
        sb.append(" U ");

        printNode(child(root, 1), 30, sb);
        sb.append("]");

        break;
      case T_EXISTS_UNTIL:
        sb.append("E[");
        printNode(child(root, 0), 30, sb);
        sb.append(" U ");
        printNode(child(root, 1), 30, sb);
        sb.append("]");
        break;

      default:
        sb.append(printToken(token(root)));
        for (int i = 0; i < nChildren(root); i++) {
          printNode(child(root, i), priority, sb);
        }
        break;
    }
    if (par) {
      sb.append(")");
    }
    return sb.toString();
  }

  private static String[] types = {"C/L ", "LTL ", "CTL ", "CTL*"};

  /**
   * Get string describing object
   * @return String
   */
  public String toString() {
    return toString(true);
  }

  public static String toString(Environment e, int root) {
    Formula f = new Formula(e, 0, root);
    f.calcType();
    return f.toString();
  }

  public String toString(boolean verbose) {
    return toString(root(), verbose, -1);
  }

  public String toString(int root) {
    return toString(root, false, -1);
  }

//  private void print(int root) {
//    print(root, false, -1);
//  }
//
  private String toString(int root, boolean verbose, int priority) {
    StringBuffer sb = new StringBuffer();

    if (verbose) {
      sb.append(types[type()] + ": ");
    }

    if (root < 0) {
      root = this.root();
    }
    printNode(root, priority, sb);
    if (verbose) {
      sb.append("\n");
    }
    return sb.toString();
  }

  public int addToken(Token t, int parent) {
    return addToken(t, parent, -1);
  }

  public int addToken(Token t) {
    return addToken(t, -1, -1);
  }

  private int addToken(Token t, int parent, int attachIndex) {

    if (parent < 0) {
      parent = root();
    }

    int tnum = -1;

    // if formula is empty, create a new node and make it the root
    if (parent < 0) {
      tnum = forest().newNode();
      rootNodeId = tnum;
      // = forest().newTree(tnum);
    }
    else {
      tnum = forest().newNode();
      forest().insertChild(parent, tnum, attachIndex, false);
      //tnum = tree_.addChild(parent,attachIndex);
    }
    tokens().set(tnum, t);
    return tnum;
  }

  public Formula(Environment e, Scanner s) {
    this(e, 0, -1);
    if (db) {
//      s.setTrace(true);
      e.trace(true);
    }

    Formula f = parse_fa(e, s);
    f.convertToDAG();

    f.calcType();
    typeFlags_ = f.typeFlags_;
    rootNodeId = f.rootNodeId;
//
//    treeId_ = f.treeId_;

    if (db) {
      Streams.out.println("Parsed formula:");
      Streams.out.println(forest().nodeString(f.rootNodeId, null));
//      forest().printTree(f.treeId_, this);
      Streams.out.println(this);
    }
    if (db) {
      e.trace(false);
    }
  }

  /*   <fa> ::=      <fb> '->' <fa>
                  |  <fb>
   */
  private static Formula parse_fa(Environment e, Scanner s) {
    e.indent("fa:");
    Formula f = null;

    Formula fb = parse_fb(e, s);
    if (s.peek().id(T_IMPLIES)) {
      Token impl = s.read();

      f = new Formula(e);
      f.addToken(impl);
      f.addFormula(fb);
      f.addFormula(parse_fa(e, s));
    }
    else {
      f = fb;
    }
    e.out(f);
    e.outdent();
    return f;
  }

  /*
           <fb> ::=  <fc> <fb'>

   */
  private static Formula parse_fb(Environment e, Scanner s) {
    e.indent("fb:");
    Formula fbp = parse_fbp(e, s, parse_fc(e, s));
    e.outdent();

    return fbp;
  }

  /*
               <fb'>::=	    "&" <fc> <fb'>
                            | "|" <fc> <fb'>
                            | e
   */
  private static Formula parse_fbp(Environment e, Scanner s, Formula fLeft) {
    e.indent("fb':");
    Formula f = fLeft;

    Token t = s.peek();
    if (t.id(T_AND) || t.id(T_OR)) {
      s.read();
      f = new Formula(e);

      f.addToken(t);

      f.addFormula(fLeft);
      f.addFormula(parse_fbp(e, s, parse_fc(e, s)));
    }
    e.outdent();
    return f;
  }

  /*    <fc> ::=     <fd>
                 |   <fd> (U/R/W) <fc>
   */

  private static Formula parse_fc(Environment e, Scanner s) {
    e.indent("fc:");
    Formula fd = parse_fd(e, s);
    Token t = s.peek();
    Formula f = null;

    if (t.id(T_UNTIL) || t.id(T_RELEASE) || t.id(T_WEAKUNTIL)) {
      s.read();

      f = new Formula(e);

      f.addToken(t);
      f.addFormula(fd);
      f.addFormula(parse_fc(e, s));
    }
    else {
      f = fd;
    }
    e.outdent();
    return f;
  }

  /*    <fd> ::=     - <fd>
                   | X <fd>
                   | f <fd>
                   | G <fd>
                   | AG <fd>
                     :
                   | EX <fd>
                   | <fe>
   */

  private static Formula parse_fd(Environment e, Scanner s) {
    e.indent("fd:");
    Formula f = null;

    Token t = s.peek();
    if (t.id(T_NEGATION) || (t.id() >= T_NEXT && t.id() <= T_EX)) {
      s.read();
      f = new Formula(e);

      f.addToken(t);
      f.addFormula(parse_fd(e, s));
    }
    else {
      f = parse_fe(e, s);
    }
    e.outdent();
    return f;
  }

  /*    <fe> ::=     A[ <fd> U <fc> ]
                 |   E[ <fd> U <fc> ]
                 |   '(' <fa> ')'
                 |   <propvar>
                 |   T
                 |   B
   */
  private static Formula parse_fe(Environment e, Scanner s) {
    e.indent("fe:");
    Formula f = null;
    Token t = s.peek();

    switch (t.id()) {
      case T_ALL_UNTIL:
      case T_EXISTS_UNTIL: {
        s.read();

        t.setText(t.id(T_ALL_UNTIL) ? "AU" : "EU");
        f = new Formula(e);
        f.addToken(t);
        // <fd> "U" <fc>

        f.addFormula(parse_fd(e, s));
        s.read(T_UNTIL);
        f.addFormula(parse_fc(e, s));

        s.read(T_AE_UNTIL_END);
      }
      break;

      case T_PAROP:
        s.read();
        f = parse_fa(e, s);
        s.read(T_PARCL);
        break;

      case T_PROPVAR:
      case T_TRUE:
      case T_BOTTOM:
        s.read();
        f = new Formula(e);
        f.addToken(t);
        break;

      default:
        t.exception("Unrecognized token in formula");
    }
    e.outdent();
    return f;
  }

  private static int tcodes[] = {
      // LTL only:
      T_UNTIL, T_RELEASE, T_WEAKUNTIL, T_NEXT, T_FUTURE, T_GLOBAL,
      -1,
      // CTL only:
      T_ALL_UNTIL, T_EXISTS_UNTIL, T_AG, T_EG, T_AF, T_EF, T_AX, T_EX,
      -2
  };
  private void calcType(int startNode) {
    if (startNode < 0) {
      typeFlags_ = 0;
      startNode = root();
      //startNode = tree_.root();
    }
    if (startNode >= 0) {
      int tk = env.nType(startNode);

      int flag = BF_LTL;
      for (int i = 0; tcodes[i] != -2; i++) {
        if (tcodes[i] == -1) {
          flag = BF_CTL;
          continue;
        }
        if (tcodes[i] == tk) {
          typeFlags_ |= flag;
          break;
        }
      }

      // recursively examine child nodes
      int j = nChildren(startNode);
      for (int i = 0; i < j; i++) {
        calcType(child(startNode, i));
      }
    }
  }

  private String str(int node) {
    return env.token(node).text();
  }

  public int child(int parent, int childIndex) {
    return forest().child(parent, childIndex);
  }

  public int nChildren(int node) {
    //if (node < 0) node = root();
    return forest().nChildren(node);
  }

  private static char ids[] = {
      '>', T_IMPLIES,
      '!', T_NEGATION,
      '&', T_AND,
      '|', T_OR,
      'V', T_PROPVAR,
      'C', T_ALL_UNTIL,
      'D', T_EXISTS_UNTIL,
      'U', T_UNTIL,
      'R', T_RELEASE,
      'W', T_WEAKUNTIL,
      'T', T_TRUE,
      'B', T_BOTTOM,
      'X', T_NEXT,
      'F', T_FUTURE,
      'G', T_GLOBAL,
      'E', T_AG,
      'H', T_EG,
      'I', T_AF,
      'J', T_EF,
      'K', T_AX,
      'L', T_EX,
  };

//  /**
//   * Print tree to StringBuffer
//   * @param root int
//   * @param dest StringBuffer
//   */
//  public void printTree(int root, StringBuffer dest) {
//    printNode(root, dest);
//  }

  /**
   * Perform initialization on a node
   * @param node : node to initialize
   * @param c : script character
   */
  private void initNode(int node, char c) {
    Token t = new Token(charToId(c));
//    t.setText(env.dfa.tokenName(t.id()));
    tokens().set(node, t);
  }

  /**
   * Get script character representation of a node
   * @param tokenId int
   * @return char
   */
  private char nodeToChar(int node) {
    Token t = token(node);
    char out = '?';
    for (int i = 0; i < ids.length; i += 2) {
      if (ids[i + 1] == t.id()) {
        out = ids[i + 0];
        break;
      }
    }
    return out;
  }

  /**
   * Get token id of script character
   * @param c char
   * @return token id
   */
  private int charToId(char c) {
    int id = -1;
    for (int i = 0; i < ids.length; i += 2) {
      if (ids[i + 0] == c) {
        id = ids[i + 1];
        break;
      }
    }
    return id;
  }


  public boolean equal(int root1, int root2) {
    Forest f = forest();

    boolean match = false;
    do {
      Token t1 = token(root1), t2 = token(root2);
      if (t1.id() != t2.id()) {
        break;
      }
      if (t1.id(T_PROPVAR)
          && !t1.text().equals(t2.text())) {
        break;
      }

      for (int i = 0; i < f.nChildren(root1); i++) {
        if (!equal(f.child(root1, i), f.child(root2, i))) {
          return false;
        }
      }
      match = true;
    }
    while (false);
    return match;
  }

  private static String[] scriptsCTL = {
      // get rid of double negation


      // match root with negation (!)
      // descend to child #0
      // match root with negation (!)
      "m! c0 m!",
      // descend to child
      // descend to child
      "c0 c0",
      // ->		(symbol = >)
      "m>", "r1 u ! d u r0 u & d d u ! d",
      // or		(symbol = |)
      "m|", "r1 u ! d u r0 u ! d u & d d u ! d",
      // CTL-specific:

      // AU		(symbol = C)
      "mC", "r1 u I d u r1 u ! d u r0 u ! d u & " +
      "d d u r1 u ! d u D d d u ! d u & d d",
      // TRUE	(symbol = T)
      "mT", "B u ! d",
      // AG		(symbol = E)
      "mE", "r0 u ! d u B u ! d u D d d u ! d",
      // EG		(symbol = H)
      "mH", "r0 u ! d u I d u ! d",
      // EF		(symbol = J)
      "mJ", "r0 u B u ! d u D d d",
      // AX		(symbol = K)
      "mK", "r0 u ! d u L d u ! d",
  },
// negation normal form
      scriptsLTL = {
      // get rid of double negation
      "m! c0 m!", "c0 c0",
      // LTL-specific:

      // ->
      "m>", "r1 u r0 u ! d u | d d",
      // W
      "mW", "r1 u r0 u | d d u r1 u R d d",
      // F
      "mF", "r0 u T u U d d",
      // G
      "mG", "r0 u B u R d d",
      // !(a & b)   =>  !a | !b
      "m! r0 m&", "r0 c1 u ! d u r0 c0 u ! d u | d d",
      // !(a | b)   =>  !a ^ !b
      "m! r0 m|", "r0 c1 u ! d u r0 c0 u ! d u & d d",
      // !(a U b)
      "m! r0 mU", "r0 c1 u ! d u r0 c0 u ! d u R d d",
      // !(a R b)
      "m! r0 mR", "r0 c1 u ! d u r0 c0 u ! d u U d d",
      // ! X a
      "m! r0 mX", "r0 c0 u ! d u X d",
  };

  private static final boolean dbr = false;

  /*	Reduce formula so it uses only a minimal sufficient set of
                  connectives.

                  For CTL, this means only using B,!,&,AF,EU,EX.
                  For LTL, reduce according to 'Model Checking, p. 132'; also
                                  see notes Apr 7 p.1
                  For CTL*,no change performed.

   > startNode				node to start at; should be -1 to start at root node
   */
  public void reduce() {

//    int newRoot = root();
    if (dbr) {
      Streams.out.println("reduce: " + this +", root=" + root() + " isLTL=" +
                          isLTL() + " isCTL=" + isCTL());
    }

    // repeat until fixed point reached.
    while (true) {
      boolean mods = false;

      String[] scr = null;

      if (isLTL()) {
        scr = scriptsLTL;
      }
      else if (isCTL()) {
        scr = scriptsCTL;
      }

      if (dbr) {
        Streams.out.println(" attempting rewrite, root is " + root() +
                            ", formula=" + toString());
      }

      if (scr != null
          && rewrite(root(), scr)) {
        mods = true;
      }

      if (!mods) {
        break;
      }
    }

    if (dbr) {
      Streams.out.println(" reduced=" + this);
    }

    convertToDAG();
  }

  public void setRoot(int id) {
    this.rootNodeId = id;
  }

  public Formula negate(int root) {
//	pr(("Negate formula root=%d\n",root));
//	printNode(root);Cout << "\n";

    Formula f = new Formula(env);
    int n = forest().newNode();
    tokens().set(n, new Token(T_NEGATION));
    f.rootNodeId = n;
//    f.treeId_ = forest().newTree(n);
    forest().addChild(n, root);
    return f;
  }

  private void convertToDAG() {
    //WARN("not cvt to dag");return;
    Forest f = forest();

    /*
            build list of nodes in formula
            compare nodes i to nodes j (j > i), and if equal, alias j = i
            modify nodes to redirect aliased nodes
     */

    DArray alias = new DArray();

    //	build list of formula nodes; put list in nl
    DArray nl = f.getNodeList(root());

    // compare nodes j to i, building alias list in alias

    for (int i = 0; i < nl.length(); i++) {
      int al = nl.getInt(i);
      int orig = al;
      for (int j = 0; j < i; j++) {
        int nn = nl.getInt(j);
        if (alias.getInt(nn) != nn) {
          continue;
        }
        if (!equal(al, nn)) {
          continue;
        }
        al = nn;
        break;
      }
//      Tools.warn("not sure if alias is set properly");
      alias.setInt(orig, al); //
    }

    // redirect all children nodes to aliases
    for (int i = 0; i < nl.length(); i++) {
      int n = nl.getInt(i);
      if (alias.getInt(n) != n) {
        continue;
      }
      for (int j = 0; j < f.nChildren(n); j++) {
        int c = alias.getInt(f.child(n, j));
        f.insertChild(n, c, j, true);
      }
    }
  }

  public Formula reduced() {
    Formula f2 = deepCopy();
    f2.reduce();
    //Utils::pad(6);
//    f2.print( -1, false, -1);
    return f2;
  }

  public int nType(int node) {
    return env.nType(node);
  }

  // id of root node (-1 if formula is empty)
  private int rootNodeId;
  // BF_xxx set for LTL, CTL specific connectives
  private int typeFlags_;
  private Environment env;

//  private static final boolean dbr = true;

  /*	Rewrite a tree
                  > root						root of tree
                  > scripts					array of string pointers (pairs of strings,
   with 0 marking end of array)
                  > cbFunc					callback function
                  < root if changes made, or -1

                  Strings are stored in pairs of recognizer + rewriter.
                  The recognizer script returns true if the tree is a match
                  for the following rewriter script, which is then applied.
   */
  private boolean rewrite(int root, String[] scripts) {
    boolean changed = false;

    if (dbr) {
//    Streams.out.println("rewrite root=" + root);
    }

//    int newRoot = -1;

    // rewrite the children
    int nc = nChildren(root);
    for (int i = 0; i < nc; i++) {
      int r = child(root, i);
      Formula fc = new Formula(env, typeFlags_, r);

      if (dbr) {
        Streams.out.println("rewriting root " + root + ", child #" + i + ": " +
                            r + ": " + fc);
      }

      if (fc.rewrite(fc.root(), scripts)) {
        changed = true;
        // replace child with new child root in case it's changed
        forest().insertChild(root, fc.root(), i, true);
      }
    }

    for (int s = 0; s < scripts.length; s += 2) {
      int r = patternMatch(root, scripts[s]);
      if (r < 0) {
        continue;
      }

      r = patternMatch(root, scripts[s + 1]);
      if (dbr) {
        Streams.out.println(" matched script " + scripts[s] + ", old root " +
                            root + ", new " + r);
      }
      setRoot(r);
      changed = true;
    }
    return changed;
  }

  private int patternMatch(int root, String script) {
    final boolean dbr = false;

    DArray stack = new DArray();

    // current node
    int node = root;

    boolean result = true;

    if (dbr) {
      Streams.out.println("matching: " + root + "\n with pattern: " +
                          script);
    }

    for (int k = 0; result && k < script.length(); ) {
      char c = script.charAt(k++);
      if (c == ' ') {
        continue;
      }
      if (dbr) {
        Streams.out.println("script char=" + c);
      }

      switch (c) {
        case 'm': {
          // get character associated with root
          char c2 = script.charAt(k++);

          char sym = nodeToChar(node); //)cbFunc.process(CMD_GETSYM, node, 0);
          if (dbr) {
            Streams.out.println("matching character for root " + node + ", c2=" +
                                c2 + " symbol=" + sym);
          }
          // match with argument
          if (sym != c2) {
            result = false;
            break;
          }
        }
        break;

        case 'c': {
          // descend to a particular child
          char c2 = script.charAt(k++);
          int ci = c2 - '0';
          node = child(node, ci);
          if (dbr) {
            Streams.out.println("set node to child #" + ci + " = " + node);
          }
        }
        break;

        case 'd': {
          // pop node, attach as rightmost child
          int n = stack.popInt();
          forest().addChild(node, n);
          if (dbr) {
            Streams.out.println("popped rightmost child " + n);
          }
        }
        break;
        case 'u': {
          if (dbr) {
            Streams.out.println("pushing node " + node);
          }
          // push current node on stack
          stack.pushInt(node);
          // create new node
          node = forest().newNode();
          if (dbr) {
            Streams.out.println(" created new node " + node);
          }
        }
        break;
        case 'r': {
          char c2 = script.charAt(k++);
          // set node as child of original
          int childNum = c2 - '0';
          node = child(root, childNum);
          if (dbr) {
            Streams.out.println("set node to child #" + childNum +
                                " of original= " + node);
          }
        }
        break;
        default: {
          // convert character to node type, create new node
          //int tokenType = cbFunc(CMD_GETCODE,c,-1);
          //ASSERT(tokenType >= 0);
//          k++;
          node = forest().newNode();
          initNode(node, c);

//          cbFunc.process(CMD_INITNODE, node, c);
          if (dbr) {
            Streams.out.println("did INITNODE for " + node + " with char " + c);
          }
          //int sym = cbFunc(CMD_GETSYM, node, 0);

          //Token t(tokenType);
          //Formula::tokens().add(t,node);
        }
        break;
      }
    }
//    root = node;
    return result ? node : -1;
  }

}
