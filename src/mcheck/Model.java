package mcheck;

import base.*;
import java.util.*;
import java.io.*;

class Model
    implements IScript {
  private Environment env;

  public Model(Environment env) {this.env = env;}

  /*	Clear the model to its initial empty state
   */
  public void clear() {
    states_.clear();
    varsUsed_.clear();
    initialStates_.clear();
    tbl_.clear();
    names_.clear();
    ids_.clear();
  }

  public void parse(base.Scanner scan) {
    final boolean db = false;

    boolean initDef = false;
    BitSet statesDefined = new BitSet();

    scan.read(T_MODELOP);
    Token t = Token.eofToken();
    if (db) {
      Streams.out.println("Model.parse:");
    }
    while (true) {
      t = scan.peek();
      if (db) {
        Streams.out.println(t.toString());
      }
      if (t.id(T_MODELCL)) {
        scan.read();
        break;
      }

      boolean initial = false;

      if (scan.peek(T_INITIALSTATE)) {
        scan.read();
        initial = true;
      }

      t = scan.read(T_INTVAL);
      int num = base.Scanner.parseInt(t.text());

      int id = stateId(num);
      if (id >= 0
          && statesDefined.get(id)) {
        t.exception("Duplicate state definition");
      }

      if (id < 0) {
        id = addState(num);
      }
      statesDefined.set(id);
      if (initial) {
        setInitialState(num);
        initDef = true;
      }

      boolean first = true; //	require at least one transition?

      while (true) {
        t = scan.peek();
        if (!first && !t.id(T_INTVAL)) {
          break;
        }
        first = false;
        scan.read();
        int num2 = base.Scanner.parseInt(t.text());
        if (stateId(num2) < 0) {
          addState(num2);
        }
        addTransition(num, num2);
      }

      first = true;

      while (true) {
        t = scan.peek();
        if (!first && !t.id(T_PROPVAR)) {
          break;
        }
        first = false;
        t = scan.read(T_PROPVAR);
        if (!t.text().equals("_")) {
          if (db) {
            Streams.out.println(" searching for variable " + t.text());
          }

          //pr((" searching for %s\n",t.str().s() ));
          int varNum = env.vars.var(t.text(), true);
          if (db) {
            Streams.out.println(" varNum=" + varNum);
          }

          //pr(("  returned %d\n",varNum));
//			int varNum = varToInt(t.str().charAt(0));
          varsUsed_.set(varNum);

          addPropVar(num, varNum);
        }
      }
    }

    // verify that no undefined transitions are occurring


    for (int i = 0; i < ids_.length(); i++) {
      //if (!statesUsed_.get(i)) continue;
      KState s = state(i);
//		if (!s.used()) continue;
//		pt((" trans length = %d\n",s.trans_.length() ));
      for (int j = 0; j < s.trans_.length(); j++) {
        int destName = s.trans_.get(j);
        int dest = stateId(destName);
        if (!statesDefined.get(dest)) {
          t.exception("Transition to unknown state: state "
                      + stateName(i) +
                      " to " + destName);
        }
      }
    }

    // if no initial states were defined, make every state
    // an initial one
    if (!initDef) {
      for (int i = 0; i < ids_.length(); i++) {
//			if (statesUsed_.get(i)) {
        initialStates_.add(stateName(i));
      }
//			}
    }

    if (db) {
      Streams.out.println("Model.parse:\n" + this);
    }
  }

  /*	Add a state to the model
                  > name						name of state to add
                  < id of state
   */
  public int addState(int name) {
    KState st = new KState();
    if (stateId(name) >= 0) {
      throw new RuntimeException("state already defined");
    }

    int id = ids_.addInt(0);
    ids_.setInt(id, id);

    states_.add(st);

    names_.addInt(name);

    tbl_.put("" + name, ids_.get(id));

//	states_.add(st,number);
//	if (firstState_ < 0 || firstState_ > number)
//		firstState_ = number;
    return id;
  }

  /*	Determine if prop. variable is set in a particular state
                  > state						name of state
                  > vn							id of variable
   */
  public boolean propVar(int stateName, int vn) {
    return state(stateId(stateName, true)).pv_.get(vn);
  }

  /*	Add a propositional variable to a state
                  > state						name of state
                  > var							variable to set true (0..MAX_PROP_VARS-1)
   */
  public void addPropVar(int state, int var) {
    int id = stateId(state, true);
    state(id).setPropVar(var);
  }

  /*	Determine number of transitions from a state
                  > src							name of state
   */
  public int degree(int src) {
    return state(stateId(src, true)).trans_.length();
  }

  /*	Get next state
                  > current					name of current state
                  > neighborInd			index of neighbor in list (0..degree-1)
                  < next state			name of next state
   */
  public int next(int current, int neighborInd) {
    int currentId = stateId(current);
    return state(currentId).trans_.get(neighborInd);
  }

  private KState state(int index) {
    return (KState) states_.get(index);
  }

  /*	Add a transition from one state to another
                  > src							name of source state
                  > dest						name of destination state
   */
  public void addTransition(int src, int dest) {
    int srcId = stateId(src);
    state(srcId).addTransition(dest);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();

     sb.append("Kripke model:\n");

     final int MAX_TRANS = 4, STW = 4;

     for (int i = 0; i < states(); i++) {
//		if (!statesUsed_.get(i)) continue;
       int name = stateName(i);
       if (initialStates_.contains(name) != null) {
         sb.append('>');
       }
       else {
         sb.append(' ');
       }

       sb.append(Tools.f(name, STW));
       sb.append(':');
       int j = degree(name);
       int tot = 0;
       for (int k = 0; k < j; k++) {
         if (tot++ == MAX_TRANS) {
           tot = 1;
           sb.append("\n");
           sb.append(Tools.sp(STW + 1));
         }
         sb.append(Tools.f(next(name, k), STW));
       }
       sb.append(Tools.sp((MAX_TRANS - tot) * STW + 1));
//
//       Tools.tab(sb, len + (MAX_TRANS - tot) * STW + 1);

       for (int k = 0; k < env.vars.length(); k++) {
         if (propVar(name, k)) {
           sb.append(env.vars.var(k) + " ");
         }
       }
       sb.append("\n");
     }
     return sb.toString();
}

  public void print() {
    Streams.out.print(this.toString());
  }

  /*	Determine number of states
   */
  public int states() {
    return states_.length();
  }

  /*	Determine if a model has been defined.
                  It must have some states.
   */
  public boolean defined() {
    return states() > 0;
  }

  /*	Determine if a variable has been used in the model
                  > var							id of variable
                  < true if used
   */
  public boolean propVarUsed(int var) {
    return varsUsed_.get(var);
  }

  /*	Determine if a state with a particular name exists
                  > s								name of state
                  < true if so
   */
  public boolean stateUsed(int name) {
    return stateId(name) >= 0;
  }

  /*	Add a state to the list of initial states
                  > s								name of state (it must exist)
   */
  public void setInitialState(int s) {
    //ASSERT(s >= 0 && s < states());
    initialStates_.add(s);
  }

  /*	Get set of initial states
                  < OrdSet containing names of initial states
   */
  public OrdSet initialStates() {
    return initialStates_;
  }

  /*	Convert state id to name
                  > id							id of state
                  < name of state
   */
  public int stateName(int id) {
    return names_.getInt(id);
  }

  /*	Convert state name to id
                  > name						name of state
                  > mustExist				if true, and doesn't exist, throws exception
   (DEBUG only)
                  < id of state, or -1 if it doesn't exist
   */
  public int stateId(int name, boolean mustExist) {
    String work = Integer.toString(name);
    Integer ptr = (Integer) tbl_.get(work);
    if (ptr == null) {
      if (mustExist) {
        throw new IllegalStateException("stateId called with undefined state");
      }
      return -1;
    }
    return ptr.intValue();
  }

  public int stateId(int name) {
    return stateId(name, false);
  }

  public DArray getNames() {
    return names_;
  }

  // array of states
  private DArray states_ = new DArray();

  // flags indicating which prop. vars are used in this model
  private BitSet varsUsed_ = new BitSet();

  // initial states
  private OrdSet initialStates_ = new OrdSet();

  // hash table containing strings of state names, with pointers to
  // state ids
  private HashMap tbl_ = new HashMap();

  // names associated with each state
  private DArray names_ = new DArray();
  // ids of each state; these are pointed to by the hash table entries
  private DArray ids_ = new DArray();

  private static class KState {

    /*	Add a transition from this state to another
                    > state						id of state to transition to
     */
    void addTransition(int state) {
      if (state < 0) {
        throw new IllegalArgumentException();
      }
      trans_.add(state);
    }

    /*	Set propositional variable true in state
                    > vn							prop. var index
     */
    void setPropVar(int vn) {
      pv_.set(vn);
    }

    // flags indicating which prop. vars are true in the state
    BitSet pv_ = new BitSet();
    // list of state names this state can transition to.
    // Note that these are NAMES and not IDS.
    OrdSet trans_ = new OrdSet();
  };

}
