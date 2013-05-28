package base;

import java.util.*;

public class IdentifierMap {

  public IdentifierMap(String script) {
    tokenNames = new DArray();
    tokenMap = new HashMap();
    StringTokenizer tk = new StringTokenizer(script);
    while (tk.hasMoreTokens()) {
      String name = tk.nextToken();
      int id = tokenNames.length();
      if (tokenMap.containsKey(name)) {
        throw new IllegalStateException("duplicate identifier: "+name);
      }
      tokenMap.put(name, new Integer(id));
      tokenNames.add(name);
    }
  }

  /**
   * Get id associated with string.  Throws IllegalStateException() if no match found
   * @param token String
   * @return int
   */
  public int getId(String token) {
    Integer k = (Integer)tokenMap.get(token);
    if (k == null)
      throw new IllegalStateException("unknown token: "+token);
    return k.intValue();
  }
  public String getId(int token) {
    return tokenNames.getString(token);
  }
  public int length() {
    return tokenNames.length();
  }

  private HashMap tokenMap;
  private DArray tokenNames;
}
