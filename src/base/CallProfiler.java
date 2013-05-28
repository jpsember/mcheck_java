package base;

import java.util.*;

public class CallProfiler
    implements Comparator {

  private static final boolean TRACE = false;

  public static void setActive(boolean a) {
    active = a;
  }

  private static Map iMap() {
    if (instanceMap == null) {
      instanceMap = new HashMap();
    }
    return instanceMap;
  }

  public int compare(Object object, Object object1) {
    String s = (String) object, s1 = (String) object1;
    return counter(s1).val - counter(s).val;
  }

  private static CallProfiler get(String key) {
    return (CallProfiler) iMap().get(key);
  }

  public static void displayResults() {
    if (instanceMap == null) {
      return;
    }

    Iterator it = iMap().keySet().iterator();
    while (it.hasNext()) {
      CallProfiler cp = get( (String) it.next());
      Streams.out.println(cp.toString());
    }

  }

  /**
   * Profile a call to a method.
   * If this is the first call to this method, sets up a CallProfiler object
   * associated with the method and stores it in the global map.
   */
  public static void note() {
    if (!active) {
      return;
    }

    // get caller location
    String caller = Tools.trc();
    CallProfiler p = (CallProfiler) iMap().get(caller);
    if (p == null) {
      if (TRACE) {
        Streams.out.println("=== Profiling calls to " + caller);
      }
      p = new CallProfiler(caller);
      iMap().put(caller, p);
    }
    p.update(Tools.stackTrace(1, 3));
  }

  private void update(String trace) {
    counter(trace).inc();
  }

  private Counter counter(String trace) {

    Counter c = (Counter) calls.get(trace);
    if (c == null) {
      if (TRACE) {
        Streams.out.println(" == New call to [" + name + "]\n    from " +
                            oneLine(trace));
      }
      c = new Counter();
      calls.put(trace, c);
    }
    return c;
  }

  /**
   * Get string describing object
   * @return String
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Calls to [" + name + "]\n");

    TreeSet keys = new TreeSet(this);
    keys.addAll(calls.keySet());
    Iterator it = keys.iterator();

    while (it.hasNext()) {
      String s = (String) it.next();
      Counter c = counter(s);
      sb.append(c.toString());
      sb.append(" : ");
      sb.append(oneLine(s));
      sb.append("\n");
    }
    return sb.toString();
  }

  private static String oneLine(String s) {
    return s.replaceAll("\n", " ");
  }

  private CallProfiler(String name) {
    this.name = name;
  }

  private static class Counter {
    private int val;
    /**
     * Get string describing object
     * @return String
     */
    public String toString() {
      return Tools.f(val);
    }

    public void inc() {
      val++;
    }
  }

  private String name;
  private Map calls = new HashMap();

  // Map for stack trace -> CallProfiler() instances
  private static Map instanceMap;
  private static boolean active = true;
}
