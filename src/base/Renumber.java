package base;

public class Renumber {

private boolean db;

  public void setDebug(boolean f) {db = f;}


  /*	Class for renumbering items.
   */
  /*	Add an entry for an old item
                  > retain					true if space should be reserved in new
   table for this item; if false, will be deleted
   from new table
                  > count						# old items to add
   */
  /*	Add an entry for an old item
          > retain					true if space should be reserved in new
   table for this item; if false, will be deleted
   from new table
          > count						# old items to add
   */
  public void addOldItem() {
    addOldItem(true, 1);
  }

//            public void addOldItem(bool retain, int count);

  /*	Rename an item
                  > oldIndex					current index
                  > newIndex					new index it should have
   */
//            void renameItem(int oldIndex, int newIndex);

  /*	Determine new index of item
                  > oldIndex					current index
                  < new index, or -1 if it's been deleted
   */
  public int newIndex(int oldIndex) {
    if (db) System.out.println("newIndex for "+oldIndex+state());
    return oldToNew_.getInt(oldIndex);
  }
private String state() {
  return " <newToOld="+newToOld_+" oldToNew="+oldToNew_+"> ";
}
  /*	Determine # old items
   */
  public int oldItems() {
    return oldToNew_.length();
  }

  /*	Determine # new items
   */
  public int newItems() {
    return newToOld_.length();
  }

  /*	Determine old index of new item #n
   */
  public int oldIndex(int newIndex) {
    if (db) System.out.println("oldIndex for "+newIndex+"; "+state());
    return newToOld_.getInt(newIndex);
  }

  /*	Change an index from old -> new
                  > index						index to change
   */
  public int do_renumber(int index) {
    return newIndex(index);
  }

  public void addOldItem(boolean retain, int count) {
    for (int i = 0; i < count; i++) {
       if (retain) {
        oldToNew_.addInt(newToOld_.length());
        newToOld_.addInt(oldToNew_.length() - 1);
      }
      else {
        oldToNew_.addInt( -1);
      }
      if (db) System.out.println("addOldItem retain="+retain+state());
   }
  }

  public void renameItem(int oldIndex, int newIndex) {
    if (db) System.out.println("renameItem old: "+oldIndex+" to new: "+newIndex);
    // determine swaps to old->new
    int onA = oldIndex;
    int onB = newToOld_.getInt(newIndex);

    int noA = newIndex;
    int noB = oldToNew_.getInt(oldIndex);

    if (onA != onB) {
      int temp = oldToNew_.getInt(onA);
      oldToNew_.setInt(onA, oldToNew_.getInt(onB));
      oldToNew_.setInt(onB, temp);
    }
    if (noA != noB) {
      int temp = newToOld_.getInt(noA);
      newToOld_.setInt(noA, newToOld_.getInt(noB));
      newToOld_.setInt(noB, temp);
    }
  }

  /**
   * Get string describing object
   * @return String
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("#old=" + Tools.f(oldItems()) + " #new=" + Tools.f(newItems())
              + "\n");

    int k = Math.min(20, newItems());
    for (int i = 0; i < k; i++) {
      sb.append("" + i + "(" + oldIndex(i) + ") ");
    }
    return sb.toString();
  }

  private DArray newToOld_ = new DArray(),
      oldToNew_ = new DArray();
}
