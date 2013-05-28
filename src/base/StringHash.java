package base;

import java.util.*;

public class StringHash
    extends HashMap {
  public StringHash() {}

  public String getString(Object key) {
    return (String) get(key);
  }

  public int getInt(Object key) {
    return ( (Integer) get(key)).intValue();
  }

  public void putInt(Object key, int value) {
    put(key, new Integer(value));
  }

  public String putString(Object key, String value) {
    return ( (String) put(key, value));
  }

  public String toString() {
    StringBuffer sb = new StringBuffer("["),
        sb2 = new StringBuffer();
    DArray k = getKeys(true);
    int len = 0;
    for (int i = 0; i < k.length(); i++) {

      String key = k.getString(i);
      sb2.setLength(0);
      sb2.append(key);
      sb2.append("=>");
      sb2.append(get(key));
      String s = sb2.toString();
      if (sb.length() - len + s.length() > 140) {
        sb.append('\n');
        len = sb.length();
      }
      if (i > 0) {
        sb.append(' ');
      }
      sb.append(s);
    }
    sb.append("]");
    return sb.toString();
  }

  public DArray getKeys() {
    return getKeys(false);
  }

  public DArray getKeys(boolean sorted) {
    DArray out = new DArray(size());
    Set keys = keySet();
    Iterator it = keys.iterator();
    while (it.hasNext()) {
      out.add(it.next());
    }
    if (sorted) {
      out.sort(new Comparator() {
        public int compare(Object a, Object b) {
          String sa = (String) a,
              sb = (String) b;
          return sa.compareTo(sb);
        }
      });
    }
    return out;

  }

}
