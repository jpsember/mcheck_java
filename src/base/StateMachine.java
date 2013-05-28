package base;

//import java.util.*;

public class StateMachine { //implements NetGame {

  public StateMachine() {
  }

  public StateMachine(String tokenNames, int startState) {
    if (tokenNames != null) {
      setStateNames(tokenNames);
    }
    setState0(startState);
  }

  public StateMachine(int startState) {
    setState0(startState);
  }

  public void setTrace(boolean t) {
    trace = t;
  }

  public void setMachineName(String name) {
    machineName = name;
  }

  /**
   * Inherit state names from another machine
   * @param m StateMachine
   */
  public void setStateNames(StateMachine m) {
    imap = m.imap;
  }

  private IdentifierMap imap;

  public void setStateNames(String s) {
    imap = new IdentifierMap(s);

  }

  public boolean trace() {
    return trace;
  }

  public void trace(String s) {
    trace(s, true);
  }

  public void trace(String s, boolean withCR) {
    if (trace) {
      if (machineName != null) {
        Streams.out.print(machineName + ".");
      }
      Streams.out.print(s);
      if (withCR) {
        Streams.out.println();
      }
    }
  }

  public boolean state(int s) {
    return state == s;
  }

  public String stateName() {
    return stateName(state);
  }

  public String stateName(int s) {
    StringBuffer sb = new StringBuffer();
    if (!stateNamesAvailable() || s < 0 || s >= nStates()) {
      sb.append("#" + s);
    }
    else {
      sb.append(imap.getId(s)); //)tokenNames.getString(s));
    }
    Tools.tab(sb, 12);
    return sb.toString();
  }

  public boolean stateNamesAvailable() {
    return imap != null; //tokenNames != null;
  }

  public int nStates() {
    if (!stateNamesAvailable()) {
      throw new IllegalStateException("State names unavailable");
    }
    return imap.length(); //tokenNames.length();

  }

  /**
   * Update current state, changing if necessary to reflect current conditions
   */
  public void updateState() {
    if (trace) {
      trace("updateState(); ", false);
    }
    setState(state());
  }

  /**
   * Get the state that should be active.
   * Called by updateState() to change the state until it becomes a steady state.
   * @return int
   */
  protected int getNextState() {
    return state;
  }

  /**
   * Explicitly set the state to a value
   * @param newState int
   */
  public void setState(int newState) {
    // call getNextState() until it reaches a fixed point

    int deadlockCounter = 0;

    while (true) {
      if (state != newState) {

        if (state >= 0) {
          closeState(newState);
        }
        if (history.length() == 20) {
          history.pop();
        }
        history.pushInt(state);
        if (stateNamesAvailable() && newState >= nStates()) {
          throw new IllegalStateException("state doesn't exist: " + newState);
        }

        if (trace) {
          StringBuffer sb = new StringBuffer();
          sb.append("setState: curr=");
          sb.append(stateName(state));
          sb.append(" next=");
          sb.append(stateName(newState));
          sb.append(" from=");
          sb.append(Tools.stackTrace(1,1));
//          Throwable t = new Throwable();
//          StackTraceElement[] elist = t.getStackTrace();
//          StackTraceElement e = elist[1];
//          String cn = e.getClassName();
//          cn = cn.substring(cn.lastIndexOf('.') + 1);
//          sb.append(cn);
//          sb.append(".");
//          sb.append(e.getMethodName());
//          sb.append(":");
//          sb.append(e.getLineNumber());
          trace(sb.toString());
        }
        int prevState = state;

        setState0(newState);

        openState(prevState);
      }
      int nextState = getNextState();
      if (state(nextState)) {
        break;
      }
      if (trace) {
        trace("getFixedState current=" + stateName()
              + " next=" + stateName(nextState));
      }

      newState = nextState;

      if (deadlockCounter++ > 1000) {
        throw new IllegalStateException("updateState() deadlock");
      }
    }
  }

  protected void setState0(int newState) {
    state = newState;
  }

  public void setState(String newState) {
    // make sure state names are available
    nStates();
    setState(imap.getId(newState));
  }

  /**
   * Get string describing object
   * @return String
   */
  public String toString() {
    if (true) {
      StringBuffer sb = new StringBuffer();
      sb.append("StateMachine");
      if (machineName != null) {
        sb.append(" " + machineName);
      }
      sb.append("\n States: [...");
      for (int i = 0; i < history.length(); i++) {
        if ( (i + 1) % 6 == 0) {
          sb.append("\n");
        }
        sb.append(" ");
        sb.append(stateName( ( (Integer) history.peekAt(i)).intValue()));
      }
      sb.append(" " + stateName(state));
      sb.append(" ]");

      return sb.toString();
    }
    return super.toString();
  }

  protected void closeState(int nextState) {closeState();}
  protected void closeState() {}

  protected void openState(int prevState) {openState();}
  protected void openState() {}

  public int state() {
    return state;
  }

  // true if debug tracing is active
  protected boolean trace;
  // for debug purposes, the name of this machine
  protected String machineName;
  // the last n states before the current one
  private DQueue history = new DQueue();
  // the current state
  private int state = -1;
}
