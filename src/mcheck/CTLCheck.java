package mcheck;

import base.*;
import java.util.*;

class CTLCheck
    implements IScript {

  /*	Check a formula
                  > vars						symbol table
                  > model						model to check
                  > f								specification (CTL formula)
                  > bs							if not 0, where to store flags representing
   satisfying start states
   > printFormulas		if true, prints formulas satisfied in each state
   > showProgress		if true, displays formulas as they're marked for
   each state
   */
  public void check(Environment env, Model m, Formula f, BitSet bs,
                    boolean printFormulas, boolean showProgress) {

    f_ = f;
    model_ = m;
    this.env = env;

    sfAlias_.clear();
    stateFlags_.clear();
    sfOrder_.clear();
    pvWarn_.clear();

    showProgress_ = showProgress;

    if (bs != null) {
      bs.clear();
    }

    Tools.ASSERT(f_.isCTL());

    prepareModel();
    if (model().defined()) {

      // reduce formula to minimal set of connectives
      f_.reduce();
      extractSubformulas();

      for (int i = 0; i < sfOrder_.length(); i++) {
        processFormula(sfOrder_.getInt(i));
      }

      if (printFormulas) {
        for (int i = 0; i < model().states(); i++) {
          int name = model().stateName(i);
          Streams.out.println("State #" + name + ":");
          for (int j = 0; j < sfOrder_.length(); j++) {
            int form = sfOrder_.getInt(j);
            if (getFlag(i, form)) {
              Streams.out.print("  ");
              Streams.out.println(f_.toString(form));
            }
          }
          Streams.out.println();
        }
      }

      if (bs != null) {
        int satFormula = sfOrder_.lastInt();

        for (int i = 0; i < model().states(); i++) {
          if (getFlag(i, satFormula)) {
            bs.set(i);
          }
        }
      }
    }
  }

  public void check(Environment env, Model model, Formula f) {
    check(env, model, f, null, false, false);
  }

  private Model model() {
    return model_;
  }

  /*	Prepare model for checking
   */
  private void prepareModel() {
    stateFlags_.clear();
    Model m = model();

    // initialize state flags to empty
    {
      for (int i = 0; i < m.states(); i++) {
        stateFlags_.add(new BitSet());
      }
    }
  }

  private void processFormula(int root) {

    Model m = model();
    Token t = f_.token(root);

    int type = f_.nType(root);
    //	pt((" type=%d, str=%s\n",type,Formula::ts(t) ));
    //pt((" %s\n",Formula::ts(t) ));

    switch (type) {
      case T_PROPVAR: {
        int var = env.vars.var(t.text(), true);
        //int var = Model::varToInt(t.str().charAt(0));
        if (!m.propVarUsed(var)) {
          if (!pvWarn_.get(var)) {
            pvWarn_.set(var);
            Streams.out.println("Warning: Variable '" + t.text() +
                                "' not used in model");
          }
        }
        for (int i = 0; i < m.states(); i++) {

          if (m.propVar(m.stateName(i), var)) {
            markState(i, root);
          }
        }
      }
      break;
      case T_BOTTOM:
        break;
      case T_NEGATION: {
        int child = childFormula(root, 0);
        for (int i = 0; i < m.states(); i++) {
          if (!getFlag(i, child)) {
            markState(i, root);
          }
        }
      }
      break;
      case T_AND: {
        int ca = childFormula(root, 0),
            cb = childFormula(root, 1);

        for (int i = 0; i < m.states(); i++) {
          if (getFlag(i, ca) && getFlag(i, cb)) {
            markState(i, root);
          }
        }
      }
      break;
      case T_EX: {
        int c = childFormula(root, 0);

        for (int i = 0; i < m.states(); i++) {
          int in = m.stateName(i);
          for (int j = m.degree(in) - 1; j >= 0; j--) {
            int xs = m.next(in, j);
            if (getFlag(m.stateId(xs), c)) {
              markState(i, root);
              break;
            }
          }
        }
      }
      break;
      case T_AF: {
        int c = childFormula(root, 0);

        boolean changed = true;
        while (changed) {
          changed = false;
          for (int i = 0; i < m.states(); i++) {
            int iName = m.stateName(i);
            if (getFlag(i, root)) {
              continue;
            }
            if (getFlag(i, c)) {
              changed = true;
              markState(i, root);
              continue;
            }

            boolean allNb = true;
            for (int j = m.degree(iName) - 1; j >= 0; j--) {
              int xs = m.next(iName, j);
              if (!getFlag(m.stateId(xs), root)) {
                allNb = false;
                break;
              }
            }
            if (allNb) {
              changed = true;
              markState(i, root);
            }
          }
        }
      }
      break;
      case T_EXISTS_UNTIL: {

        int ca = childFormula(root, 0);
        int cb = childFormula(root, 1);

        boolean changed = true;
        while (changed) {
          changed = false;
          for (int i = 0; i < m.states(); i++) {
            int in = m.stateName(i);

            if (getFlag(i, root)) {
              continue;
            }
            if (getFlag(i, cb)) {
              changed = true;
              markState(i, root);
//							setFlag(i,root);
              //						pr(("  +#%2d: %s\n",i,f_.s(root,true)));
              continue;
            }

            // first part must be true in this state
            if (!getFlag(i, ca)) {
              continue;
            }

            // check if second part is true in any successor
            for (int j = m.degree(in) - 1; j >= 0; j--) {
              int xs = m.next(in, j);
              if (getFlag(m.stateId(xs, true), root)) {
                changed = true;
                markState(i, root);
                break;
              }
            }
          }
        }
      }
      break;
    }
  }

  /*	Extract list of subformulas from the formula
                  > root						current position in formula; -1 for start
   */
  private void extractSubformulas(int root) {
    if (root < 0) {
      root = f_.root();
    }
    if (root < 0) {
      return;
    }

    // determine if this formula already exists in list
    for (int i = 0; i < sfOrder_.length(); i++) {
      if (sfOrder_.getInt(i) == root) {
        return;
      }
    }

    // process child nodes first
    int nc = f_.nChildren(root);
    for (int i = 0; i < nc; i++) {
      extractSubformulas(f_.child(root, i));
    }

    int alias = root;
    // determine if subformula is identical to an existing one
    for (int i = 0; i < sfOrder_.length(); i++) {
      if (f_.equal(root, sfOrder_.getInt(i))) {
        alias = sfOrder_.getInt(i);
        break;
      }
    }

    sfAlias_.setInt(root, alias);

    // add root index to list describing order we'll do the checking in;
    // don't check aliased formulas.
    if (alias == root) {
      sfOrder_.addInt(root);
    }
  }

  private void extractSubformulas() {
    extractSubformulas( -1);
  }

  /*	Mark state
                  > state						id of state
   */
  private void markState(int state, int root) {
    setFlag(state, root);
    if (showProgress_) {
      Streams.out.println("  +" + Tools.f(model_.stateName(state), 2) + ": " +
                          f_.toString(root));
    }
  }

  /*	Set flag for state
                  > state						id of state
                  > fi							bit to set
   */
  private void setFlag(int state, int fi) {
    BitSet bf = stateFlag(state);
    bf.set(fi);
  }

  /*	Read flag for state
                  > state						id of state
                  > fi							bit to read
   */
  private boolean getFlag(int state, int fi) {
    BitSet bf = stateFlag(state);
    return bf.get(fi);
  }

  /*	Get child node; translate by alias if required
   */
  private int childFormula(int node, int child) {
    int c = f_.child(node, child);
    return sfAlias_.getInt(c);
  }

  // specification being checked, in reduced form
  private Formula f_;

  // model being checked
  private Model model_;

  // aliases for subformulas, to detect identical ones;
  // if alias differs from index, it already exists
  private DArray sfAlias_ = new DArray();

  // order of subformulas to check
  private DArray sfOrder_ = new DArray();

  // flags for each state in model
  private DArray stateFlags_ = new DArray();
  private BitSet stateFlag(int i) {
    return (BitSet) stateFlags_.get(i);
  }

  // flags indicating which vars we've printed warnings about
  private BitSet pvWarn_ = new BitSet();

  // symbol table
  private Environment env;

  // true if we're to display formulas as they're marked in states
  private boolean showProgress_;
}
