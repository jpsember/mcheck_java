package mcheck;

import base.*;
import java.util.*;

class Vars {

//  private static final boolean db = false;

  public int var(String str, boolean addIfMissing) {
    final boolean db = false;

    if (db) Streams.out.println("Vars.var str="+Tools.d(str)+" addIfMissing="+addIfMissing);

//    int index = -1;
    Integer id = (Integer) map.get(str);
    if (db) Streams.out.println(" map returned "+Tools.d(id));

    if (id == null && addIfMissing) {
      id = new Integer(strs.size());
      strs.add(str);
      map.put(str, id);
    }
    int index = id == null ? -1 : id.intValue();

    if (db) Streams.out.println(" returning "+index);
    return index;
  }

  public String var(int index) {
    return (String) strs.get(index);
  }

  public int length() {
    return strs.size();
  }

  public void clear() {
    strs.clear();
    map.clear();
  }

  private HashMap map = new HashMap();
  private ArrayList strs = new ArrayList();

}
