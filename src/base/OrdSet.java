package base;

public class OrdSet {

  public void clear() {
    a.clear();
  }

  public OrdSet() {
    a = new DArray();
  }

  public OrdSet(OrdSet src) {
    a = (DArray) src.a.clone();
  }

  /**
   * Find insertion position for value
   * @param val : value to insert
   * @return position where value is to be inserted / replace existing one
   */
  private int findInsertPos(int val) {
    int min = 0, max = length() - 1;

    while (true) {
      if (min > max) {
        break;
      }
      int test = (min + max) >> 1;
      int tSym = a.getInt(test);
      if (tSym == val) {
        min = test;
        break;
      }
      if (tSym > val) {
        max = test - 1;
      }
      else {
        min = test + 1;
      }
    }
    return min;
  }

  /**
   * Construct string containing ordered set of chars
   * @return String
   */
  public String convertToString() {
    StringBuffer s = new StringBuffer();

    int i = 0;
    while (i < length()) {
      s.append( (char) get(i));
      i++;
    }
    return s.toString();
  }

  /**
   * Add a value to the set.
   * @param n int
   * @return true if set already contained value
   */
  public boolean add(int n) {
    boolean exists = false;

    int j = findInsertPos(n);
    if (j < length() && get(j) == n) {
      exists = true;
    }
    else {
      insert(j, n);
    }
    return exists;
//    int i = 0;
//    while (i < length()) {
//      if (get(i) == n) {
//        return;
//      }
//      if (get(i) > n) {
//        break;
//      }
//      i++;
//    }
//    insert(i, n);
  }

  private void insert(int pos, int val) {
    a.insert(pos, new Integer(val));
  }

  public int removeLast() {
    if (a.isEmpty()) {
      throw new IndexOutOfBoundsException();
    }
    return a.popInt();
  }

  public void remove(int n) {
    int j = findInsertPos(n);
    if (j < length() && get(j) == n) {
      removeArrayItem(j); //return;
    }

//    Integer loc = contains(n);
//    if (loc != null) {
//      removeArrayItem(loc.intValue());
//    }
  }

  private void removeArrayItem(int pos) {
    a.delete(pos, 1);
  }

  public boolean equals(OrdSet s) {
    if (length() != s.length()) {
      return false;
    }
    for (int i = 0; i < length(); i++) {
      if (get(i) != s.get(i)) {
        return false;
      }
    }
    return true;
  }

  public int length() {
    return a.length();
  }

  public int get(int pos) {
    return a.getInt(pos);
  }

  /*	Calculate the union of two sets.
                  > s1							first set
                  > s2							second set
                  < where to store union; may be same as one of the two sets
   */
  public static void calcUnion(OrdSet s0, OrdSet s1, OrdSet dest) {
    dest.clear();
    int i = 0, j = 0;
    while (true) {
      int next = 0;
      if (j == s1.length()) {
        if (i == s0.length()) {
          break;
        }
        next = s0.get(i++);
      }
      else if (i == s0.length()) {
        next = s1.get(j++);
      }
      else {
        next = s0.get(i);
        if (next > s1.get(j)) {
          next = s1.get(j++);
        }
        else {
          next = s0.get(i++);
        }
      }
      if (dest.length() == 0 || dest.a.lastInt() != next) {
        dest.add(next);
      }
    }
  }

  public void include(OrdSet src) {
    int i = 0, j = 0;
    for (; i < src.length(); i++) {
      int n = src.get(i);
      // find insertion point for this element
      while (j < length() && n > get(j)) {
        j++;
      }
      if (j == length() || n != get(j)) {
        insert(j, n);
        j++;
      }
    }
  }

  public boolean isEmpty() {
    return length() == 0;
  }

  public Integer contains(int n) {
    int j = findInsertPos(n);
    if (j >= length() || get(j) != n) {
      return null;
    }
    return new Integer(j);

//    Integer loc = null;
//    int i = -1;
//    while (++i < length()) {
//      int test = get(i) - n;
//      if (test > 0) {
//        break;
//      }
//      if (test == 0) {
//        loc = new Integer(i);
//        break;
//      }
//    }
//    return loc;
  }

  /**
   * Get string describing object
   * @return String
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("OrdSet " + DArray.toString(a.toIntArray()));
    return sb.toString();
  }

  private DArray a;

}
