package mcheck;

import base.*;
import java.util.*;

class Buchi {

  public Buchi(Environment e) {
    this.env = e;
  }

  /**
   * Add a state
   * @param initial : if true, makes state an initial state
   * @return id of new state
   */
  public int addState(boolean initial) {
    int n = states_.length();
    states_.add(new State());
    if (initial) {
      initialStates_.add(n);
    }
    return n;
  }

  /**
   * Add a state
   * @return id of new state
   */
  public int addState() {
    return addState(false);
  }

  /**
   * Add a transition from one state to another
   * @param src source state
   * @param dest destination state
   */
  public void addTransition(int src, int dest) {
    state(src).trans_.addInt(dest);
  }

  /**
   * Specify a prop. var. that must be defined
   * @param state id of state
   * @param varNum index of variable
   * @param value true or false, the value it must have
   */
  public void addPropVar(int state, int varNum, boolean value) {
    State st = state(state);
    if (value) {
      st.pvTrue_.set(varNum);
    }
    else {
      st.pvFalse_.set(varNum);
    }
  }

  /**
   * Add a set of accepting states
   * @param set	set to add
   */
  public void addAcceptSet(BitSet set) {
    acceptSets_.add(set);
  }

  /**
   * Clear automaton to freshly-constructed state
   */
  public void clear() {
    states_.clear();
    initialStates_.clear();
    acceptSets_.clear();
  }

  /**
   * Convert a generalized automaton to a non-generalized one.
   * @param dest	non-generalized automaton to construct
   */
  public void convertGeneralized(Buchi d) {
    final boolean db = false;
    if (db) {
      Streams.out.println("converting generalized buchi:\n" + this);
    }

    d.clear();

    // multiplier factor (n+1)
    int qm = nAcceptSets() + 1;

    int rowSize = nStates();

    // add |Q| * (n+1) states
    for (int i = 0; i < qm * rowSize; i++) {
      d.addState();
    }

    // define initial states
    for (int i = 0; i < initialStates_.length(); i++) {
      int is = initialStates_.get(i);
      d.initialStates_.add(is + rowSize * 0);
    }

    // define accept set
    {
      BitSet set = new BitSet();
      for (int i = 0; i < nStates(); i++) {
        set.set(i + rowSize * (qm - 1));
      }
      d.addAcceptSet(set);
    }

    // define transitions
    for (int i = 0; i < nStates(); i++) {
      State st = state(i);
      for (int tr = 0; tr < st.trans_.length(); tr++) {
        int j = st.trans_.getInt(tr);
        for (int x = 0; x < qm; x++) {
          int y = -1;
          if (x < nAcceptSets()
              && acceptSet(x).get(j)) {
            y = x + 1;
          }
          else if (x == qm - 1) {
            y = 0;
          }
          else {
            y = x; // not sure about this last one...
          }

          d.addTransition(i + rowSize * x, j + rowSize * y);
        }
      }
    }

    // define propVars
    for (int i = 0; i < nStates(); i++) {
      State st = state(i);
      for (int j = 0; j < qm; j++) {
        State sd = d.state(i + rowSize * j);
        sd.pvFalse_ = copy(st.pvFalse_);
        sd.pvTrue_ = copy(st.pvTrue_);
      }
    }
    if (db) {
      Streams.out.println("converted to non-gen:\n" + d);
    }

  }

  /**
   * Get string describing object
   * @return String
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();

    Vars v = env.vars;

    for (int i = 0; i < states_.length(); i++) {

      State st = state(i);
      StringBuffer s = new StringBuffer();
      s.append( (initialStates_.contains(i) != null ? '>' : ' '));
      s.append(Tools.f(i, 3) + ": ");

      int maxVar = Math.max(st.pvTrue_.length(), st.pvFalse_.length());

      boolean printed = false;
      for (int j = 0; j < maxVar; j++) {

        boolean f0 = st.pvTrue_.get(j),
            f1 = st.pvFalse_.get(j);

        if (! (f0 || f1)) {
          continue;
        }

        if (printed) {
          s.append(',');
        }

        printed = true;

        if (f0 && f1) {
          s.append('*');
        }
        else if (f1) {
          s.append('!');
        }

        if (v == null) {
          // convert var to 'a'...'z' to avoid confusion with numbers!
          char c = '?';
          if (j < 26) {
            c = (char) (j + 'a');
          }
          s.append(c);
        }
        else {
          s.append(v.var(j));
        }
      }

      Tools.tab(s, 20);
      for (int j = 0; j < st.trans_.length(); j++) {
        s.append(st.trans_.get(j) + " ");
      }

      if (st.label_ != null) {
        Tools.tab(s, 40);
        s.append(st.label_);
      }
      sb.append(s);
      sb.append("\n");
    }

    for (int i = 0; i < acceptSets_.length(); i++) {
      if (i == 0) {
        sb.append(" --- accept sets ---\n");
      }
      BitSet bs = acceptSet(i);
      sb.append(" (");
      boolean first = true;
      for (int j = 0; j < states_.length(); j++) {
        if (bs.get(j)) {
          if (!first) {
            sb.append(' ');
          }
          first = false;
          sb.append(j);
        }
      }
      sb.append(")\n");
    }
    return sb.toString();
  }

  public int nStates() {
    return states_.length();
  }

  public int nAcceptSets() {
    return acceptSets_.length();
  }

  /**
   * Convert Kripke model to Buchi automaton
   * @m Model to convert
   */
  public void convertKripke(Model m) {
    int totVars = env.vars.length();

    clear();

    // create an initial state
    addState(true);

    for (int i = 0; i < m.states(); i++) {
      int id = addState();
      addStateLabel(id, "" + m.stateName(i));
    }

    for (int i = 0; i < m.states(); i++) {
      int ds = i + 1;
      int sname = m.stateName(i);

      // add complemented/uncom. version of EVERY variable to dest state
      for (int j = 0; j < totVars; j++) {
        addPropVar(ds, j, m.propVar(sname, j));
      }

      // add transitions
      for (int j = 0; j < m.degree(sname); j++) {
        addTransition(ds, 1 + m.stateId(m.next(sname, j)));
      }
    }

    OrdSet mInit = m.initialStates();

    for (int i = 0; i < mInit.length(); i++) {
      addTransition(0, 1 + m.stateId(mInit.get(i)));
    }

    // make every state an accepting state
    BitSet set = new BitSet();
    for (int i = 0; i < nStates(); i++) {
      set.set(i);
    }
    addAcceptSet(set);
  }

  /**
   * Calculate the product of two Buchi automata, one which
   * recognizes the intersection of the respective automata.
   * Neither input automata can be generalized.
   * The labels of automata b1 are copied to the first 'row'
   * of the product automaton.
   * @param b1 : Buchi
   * @param b2 : Buchi
   */
  public void calcProduct(Buchi b1, Buchi b2) {
    clear();

    Tools.ASSERT(!b1.general() && !b2.general());

    int q1 = b1.nStates();
    int q2 = b2.nStates();

    int rowSize = q1;
    int pageSize = rowSize * q2;

    // add |Q1| * |Q2| * 3 states
    for (int i = 0; i < q1 * q2 * 3; i++) {
      int id = addState();
      addStateLabel(id, b1.stateLabel(id % q1));
    }

    // construct propVars, and determine if they are a contradiction.

    // do this only for the first set, then copy to the other sets:

    for (int i = 0; i < q1; i++) {
      State si = b1.state(i);
      for (int j = 0; j < q2; j++) {
        State sj = b2.state(j);
        for (int k = 0; k < 2; k++) {
          int d0 = (i + rowSize * j);
          int di = d0 + k * pageSize;

          State d = state(di);
          d.label_ = si.label_;

          if (k == 0) {
            d.pvFalse_ = copy(si.pvFalse_);
            d.pvTrue_ = copy(si.pvTrue_);

            d.pvFalse_.or(sj.pvFalse_);
            d.pvTrue_.or(sj.pvTrue_);
            // determine if any contradictions exist
            BitSet test = copy(d.pvTrue_);
            test.and(d.pvFalse_);
            if (test.cardinality() != 0) {
              contradictionStates_.set(di);
            }
          }
          else {
            State src = state(d0);
            d.pvFalse_ = copy(src.pvFalse_);
            d.pvTrue_ = copy(src.pvTrue_);
            contradictionStates_.set(di, contradictionStates_.get(d0));
          }
        }
      }
    }

    // define initial states
    for (int i = 0; i < b1.initialStates_.length(); i++) {
      int i1 = b1.initialStates_.get(i);
      for (int j = 0; j < b2.initialStates_.length(); j++) {
        int j1 = b2.initialStates_.get(j);
        initialStates_.add( (i1 + j1 * rowSize) + 0 * pageSize);
      }
    }

    // define accept set
    {
      BitSet set = new BitSet();
      for (int i = 0; i < q1; i++) {
        for (int j = 0; j < q2; j++) {
          set.set( (i + j * rowSize) + 2 * pageSize);
        }
      }
      addAcceptSet(set);
    }

    // define transitions
    for (int ri = 0; ri < q1; ri++) {
      State st = b1.state(ri);
      for (int tr = 0; tr < st.trans_.length(); tr++) {
        int rm = st.trans_.getInt(tr);

        for (int qj = 0; qj < q2; qj++) {
          State s2 = b2.state(qj);
          for (int t2 = 0; t2 < s2.trans_.length(); t2++) {
            int qn = s2.trans_.getInt(t2);

            if (contradictionStates_.get(rm + qn * rowSize)) {
              continue;
            }

            for (int x = 0; x < 3; x++) {
              int y = x;
              switch (x) {
                case 0:
                  if (b1.accepting(rm)) {
                    y = 1;
                  }
                  break;
                case 1:
                  if (b2.accepting(qn)) {
                    y = 2;
                  }
                  break;
                case 2:
                  y = 0;
                  break;
              }
              addTransition( (ri + qj * rowSize) + x * pageSize,
                            (rm + qn * rowSize) + y * pageSize);
            }
          }
        }
      }
    }
  }

  /**
   * Construct a copy of a BitSet
   * @param s BitSet
   * @return BitSet
   */
  private static BitSet copy(BitSet s) {
    BitSet d = new BitSet(s.size());
    d.or(s);
    return d;
  }

  /*	Determine if automaton is a generalized automaton
                  < true if it doesn't have exactly one set of accept states
   */
  public boolean general() {
    return acceptSets_.length() != 1;
  }

  /**
   * Determine if a state is accepting (in first accept set)
   * @param state : state number
   * @return boolean
   */
  public boolean accepting(int state) {
    return accepting(state, 0);
  }

  /**
   * Determine if a state is accepting
   * @param state int
   * @param set : set number
   * @return boolean
   */
  public boolean accepting(int state, int set) {
    return acceptSet(set).get(state);
  }

  /**
   * Determine if language recognized by automaton is empty
   * @return DArray containing sequence of states, if nonempty; null if empty
   */
  public DArray nonEmpty() {
    DArray seq = new DArray();
    flagged_.clear();
    hashed_.clear();
    dfsStack1_.clear();
    dfsStack2_.clear();

    boolean result = false;

    for (int i = 0; i < initialStates_.length(); i++) {
      int q0 = initialStates_.get(i);
      result = dfs1(q0);
      if (result) {
        break;
      }
    }

    if (result) {
      seq = dfsStack1_;
      for (int j = 1; j < dfsStack2_.length(); j++) {
        seq.add(dfsStack2_.get(j));
      }
    }
    return result ? seq : null;
  }

  /**
   * Add a label to a state, for display purposes
   * @param state int
   * @param label String
   */
  public void addStateLabel(int state, String label) {
    state(state).label_ = label;
  }

  /**
   * Read label from state
   * @param state int
   * @return String
   */
  public String stateLabel(int state) {
    return state(state).label_;
  }

  /**
   * Label all states according to propositional variables
   */
  public void setPropVarLabels() {
    Vars v = env.vars;

    for (int j = 0; j < nStates(); j++) {

      State s = state(j);
      StringBuffer d = new StringBuffer();

      int litCnt = 0;

      for (int i = 0; i < v.length(); i++) {
        if (s.pvTrue_.get(i) && s.pvFalse_.get(i)) {
          litCnt = 1;
          d.setLength(0);
          d.append('B');
          break;
        }

        if (s.pvTrue_.get(i) || s.pvFalse_.get(i)) {
          if (litCnt == 1) {
            d.insert(0, "(");
          }
          if (litCnt > 0) {
            d.append(" ^ ");
          }
          litCnt++;
          if (s.pvFalse_.get(i)) {
            d.append('!');
          }
          d.append(v.var(i));
        }
      }
      if (litCnt == 0) {
        d.append('T');
        litCnt++;
      }
      if (litCnt > 1) {
        d.append(')');
      }

      addStateLabel(j, d.toString());
    }
  }

  /**
   * Reduce automaton by eliminating unreachable states
   * @param d : reduced automaton
   */
  public void reduce(Buchi d) {

    d.clear();

    BitSet flagged = new BitSet();
    DArray stk = new DArray();

    for (int i = 0; i < initialStates_.length(); i++) {
      stk.pushInt(initialStates_.get(i));
    }

    while (!stk.isEmpty()) {
      int s = stk.popInt();
      if (flagged.get(s)) {
        continue;
      }

      if (contradictionStates_.get(s)) {
        continue;
      }

      flagged.set(s);
      State st = state(s);
      for (int i = 0; i < st.trans_.length(); i++) {
        stk.pushInt(st.trans_.getInt(i));
      }
    }

    DArray newId = new DArray(), oldId = new DArray();

    int j0 = 0;
    for (int i = 0; i < nStates(); i++) {
      newId.addInt(j0);
      if (flagged.get(i)) {
        oldId.addInt(i);
        j0++;
      }
    }

    for (int i = 0; i < nStates(); i++) {
      if (!flagged.get(i)) {
        continue;
      }
      State orig = state(i);
      State s = orig;
      DArray t = s.trans_;
      for (int j = 0; j < t.length(); j++) {
        int dest = t.getInt(j);
        if (!flagged.get(dest)) {
          continue;
        }
        t.setInt(j, newId.getInt(dest));
      }
      d.states_.add(s);
    }

    for (int i = 0; i < initialStates_.length(); i++) {
      d.initialStates_.add(newId.getInt(initialStates_.get(i)));
    }

    for (int j = 0; j < acceptSets_.length(); j++) {
      BitSet src = acceptSet(j),
          set = new BitSet();

      for (int i = 0; i < nStates(); i++) {
        if (!flagged.get(i)) {
          continue;
        }
        if (!src.get(i)) {
          continue;
        }
        int k = newId.getInt(i);
        set.set(k);
      }
      d.acceptSets_.add(set);
    }
  }

  /**
   * Perform emptiness depth-first search, part 1
   * @param q : state to start from								state to start from
   * @return true if infinite path found
   */
  private boolean dfs1(int q) {
    boolean result = false;

    stacked_.set(q);
    dfsStack1_.pushInt(q);

    hashed_.set(q);
    State st = state(q);
    for (int i = 0; i < st.trans_.length(); i++) {
      int q2 = st.trans_.getInt(i);
      if (!hashed_.get(q2)) {
        if (dfs1(q2)) {
          result = true;
          break;
        }
      }
    }
    if (!result) {
      if (accepting(q)) {
        result = dfs2(q);
      }
    }
    if (!result) {
      dfsStack1_.popInt();
      stacked_.set(q, false);
    }

    return result;
  }

  /**
   * Perform emptiness depth-first search, part 2
   * @param q : state to start from								state to start from
   * @return true if infinite path found
   */
  private boolean dfs2(int q) {
    dfsStack2_.pushInt(q);
    flagged_.set(q);
    State st = state(q);
    boolean result = false;

    for (int i = 0; i < st.trans_.length(); i++) {
      int q2 = st.trans_.getInt(i);
      if (stacked_.get(q2)) {
        dfsStack2_.pushInt(q2);
        result = true;
        break;
      }
      if (!flagged_.get(q2)) {
        result = dfs2(q2);
        if (result) {
          break;
        }
      }
    }
    if (!result) {
      dfsStack2_.pop();
    }
    return result;
  }

  // dfs usage: bit is set if state is 'hashed'
  private BitSet hashed_ = new BitSet();
  // dfs usage: bit is set if state is 'flagged'
  private BitSet flagged_ = new BitSet();
  // list of states on dfs stacks
  private DArray dfsStack1_ = new DArray();
  private DArray dfsStack2_ = new DArray();
  // true if state is on dfs stack
  private BitSet stacked_ = new BitSet();

  private static class State {

    // states this state has transitions to (this embodies '->', the
    // transition relation)
    DArray trans_ = new DArray();

    // flags indicating which prop. vars must be true (or false)
    // (if bit is set, indicates it must be true (or false))
    BitSet pvTrue_ = new BitSet(),
        pvFalse_ = new BitSet();

    String label_;
  };

  // Q
  private DArray states_ = new DArray();
  private State state(int i) {
    return (State) states_.get(i);
  }

  // I
  private OrdSet initialStates_ = new OrdSet();

  // Set of sets of accepting states.  Each BitStore defines a subset
  // of the Q states which are accepting states.
  private DArray acceptSets_ = new DArray();
  private BitSet acceptSet(int index) {
    return (BitSet) acceptSets_.get(index);
  }

  // flags indicating whether a state has contradictions
  private BitSet contradictionStates_ = new BitSet();

  private Environment env;
}
