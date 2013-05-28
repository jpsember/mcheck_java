package base;

import java.util.BitSet;
import java.util.Random;
import java.io.*;

/**
  Using the Forest class

  A forest contains any number of trees, which share a set of nodes
  connected within a graph structure.

  A tree has a root node, and zero or more child trees.

  If two trees share the same node, then changes made to the node
  by one tree will affect the other as well.

  Trees have their own ids, and nodes have ids; the two ids are
  distinct and should not be confused.

  1)	Construct a forest.

  2) To create a tree, you must have a root node to associate with the
 tree.  Either use an existing node, or create a node with
 newNode().  Then call newTree() with this node.

  3)	Extend a tree by inserting children.  You must specify the parent
 node, and a position to insert the child at.  You can choose to
 replace a child tree with the new one instead of inserting the new
 one.  Inserting or replacing the root must be done using other
 methods (see setRoot()).

  4)	A tree can be deleted.  This doesn't delete the nodes, just
 deregisters the id associated with the tree.  A subsequent call
 to garbageCollect() WILL delete the nodes, though... it performs
 a search of the graph and retains only those nodes that are
 reachable from the root of some non-deleted tree.

  5)	Once a tree or node id is assigned, it doesn't change until
 the tree or node is deleted.  The id is an index >= 0 that can
 be used by an application for storing or associating data with
 the trees or nodes.

  The forest class uses a callback to communicate with the application.
  In particular, it performs conversion of character symbols to and from
  node data codes for tree rewriting.
 */

public class Forest
    implements IForest {

  public static final int IDBASE = Graph.IDBASE;

//  public static final int
//      CMD_GETSYM = 0, // get symbol associated with node data
//      CMD_GETCODE = 1, // get node data associated with symbol
//      CMD_INITNODE = 2, // initialize node data
//      CMD_PRINTTREE = 3; // print tree (debugging only; can be ignored)

  /**
   * First ID to assign to trees (to attempt to keep distinct from indexes,
   * which start at 0, and graph nodes, which have a different base;
   * this is nonzero only for debug purposes, to catch errors of
   * mixing trees / nodes / indexes).
   */
  private static int TREE_ID_BASE = 200;

  /**
   * Get # children of a node
   * @param parent : id of parent node
   * @return # children
   */
  public int nChildren(int parent) {
    return graph.nCount(parent);
  }

  /**
   * Get a child node
   * @param parent : id of parent
   * @param index : index of child
   * @return id of child
   */
  public int child(int parent, int index) {
    return graph.neighbor(parent, index);
  }

  /**
   * Get description of forest
   * @return String
   */
  public String toString() {
    StringBuffer sb = new StringBuffer("Forest\n");
    for (int i = 0; i < trees.length(); i++) {
      if (trees.exists(i)) {
        //int root = trees.getInt(i);
        //sb.append(" #" + Tools.f(treeIdFromIndex(i)) + ": ");
        sb.append(treeString(treeIdFromIndex(i)));
        sb.append('\n');
      }
    }
    return sb.toString();
  }

public String treeString(int treeId) {
  return treeString(treeId, this);
}

  /**
   * Get description of tree within forest
   * @param treeId : id of tree
   */
  public String treeString(int treeId, IForest nd) {
    return nodeString(rootNode(treeId), nd);
  }

  /**
   * Get description of tree, given its root node
   * @param rootNodeId : id of root node
   * @return String
   */
  public String nodeString(int rootNodeId) {
    return nodeString(rootNodeId, null);
  }

  /**
   * Get description of tree, given its root node
   * @param rootNodeId : id of root node
   * @param nd : if not null, object that converts nodes to strings
   * @return String
   */
  public String nodeString(int rootNodeId, IForest nd) {
    if (nd == null)
      nd = this;

    StringWriter sw = new StringWriter();
    TabbedWriter w = new TabbedWriter(sw);

   // StringBuffer sb = new StringBuffer("Tree ");
    printRootedTree(rootNodeId, w, nd);

    try {
    w.close();
    sw.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    String s = sw.toString();
   return s;
  }

//  /**
//   * Print a tree into a StringBuffer
//   * @param sb : StringBuffer to append to
//   * @param root : id of root node
//   */
//  private void printRootedTree(StringBuffer sb, int root) {
//    if (root < Forest.IDBASE) {
//      sb.append("*** root=" + root);
//      throw new RuntimeException(sb.toString());
//    }
//
//    int nKids = nChildren(root);
//    if (nKids > 0) {
//      sb.append('(');
//      sb.append(root);
//      for (int i = 0; i < nKids; i++) {
//        sb.append(' ');
//        printRootedTree(sb, child(root, i));
//      }
//      sb.append(')');
//    }
//    else {
//      sb.append(root);
//    }
//  }


//  /**
//   * Print tree given its id
//   * @param treeId int
//   * @param nd NodeData
//   */
//  public void printTree(int treeId, NodeData nd) {
//    TabbedWriter w = new TabbedWriter(Streams.out);
//
//    int root = rootNode(treeId);
//    printRootedTree(root, w, nd);
//  }
//
//  public void printRootedTree(int root, NodeData nd) {
//    TabbedWriter w = new TabbedWriter(Streams.out);
////    int root = rootNode(treeId);
//    printRootedTree(root, w, nd);
//  }

  private void printRootedTree(int root, TabbedWriter w, IForest nd) {
    try {
      int nKids = nChildren(root);
      w.write(nd.getPrintObject(this, root).toString() + "\n");
      w.indent();
      for (int i = 0; i < nKids; i++) {
        printRootedTree(child(root, i), w, nd);
      }
      w.outdent();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Convert a tree index to a tree ID
   * @param index : index (0..n-1)
   * @return the ID associated with this index
   */
  private int treeIdFromIndex(int index) {
    return index + TREE_ID_BASE;
  }

  /**
   * Convert a tree ID to a tree index
   * @param id : id of tree (it must be a valid id)
   * @return the index of the tree with this ID
   */
  private int treeIndexFromId(int id) {
    if (id <= 0) {
      throw new IllegalArgumentException();
    }
    return id - TREE_ID_BASE;
  }

  /**
   * Get the root node of a tree
   * @param treeId : id of tree
   * @return id of root node, or -1 if no such tree exists
   */
  public int rootNode(int treeId) {
    int rn = -1;
    int tInd = treeIndexFromId(treeId);
    if (trees.exists(tInd)) {
      rn = trees.getInt(tInd);
    }
    return rn;
  }

  /**
   * Allocate a new node from the underlying graph
   * @return id of node
   */
  public int newNode() {
    return newNode(null);
  }

  /**
   * Allocate a new node from the underlying graph, and store some data
   * with it
   * @param data : data to store, or null
   * @return id of node
   */
public int newNode(Object data) {
  int id = graph.newNode(data);
  nodesUsed.set(id - Graph.IDBASE);
  return id;
}

/**
 * Get object for printing node
 * @param forest : Forest containing node
 * @param nodeId : id of node
 * @return Object
 */
public Object getPrintObject(Forest forest, int nodeId) {
  return forest.nodeData(nodeId);
}

/**
 * Get data associated with node
 * @param nodeId int
 * @return Object
 */
public Object nodeData(int nodeId) {
  return graph.nodeData(nodeId);
}

  /**
   * Create a new tree
   * @param root : id of root node
   * @return id of tree
   */
  public int newTree(int root) {
    int ind = trees.allocInt(root);
    return treeIdFromIndex(ind);
  }

  /**
   * Set root node of tree
   * @param treeId : id of tree
   * @param root : id of root node
   */
  public void setRoot(int treeId, int root) {
    trees.setInt(treeIndexFromId(treeId), root);
  }

  /**
   * Delete a tree
   * @param treeId : id of tree to delete
   */
  public void deleteTree(int treeId) {
    trees.free(treeIndexFromId(treeId));
  }

  /**
   * Insert a child node into a parent's list
   * @param parent : id of parent
   * @param child : id of child
   * @param position : insertion position (-1 to append to right side)
   * @param replaceFlag : true to insert, false to replace existing
   */
  public void insertChild(int parent, int child, int position,
                          boolean replaceFlag) {

    graph.addEdge(parent, child, null, position, replaceFlag);
  }

  /**
   * Add a child node to a parent.  Adds child to right of existing children.
   * @param parent : id of parent node
   * @param child : id of child
   */
  public void addChild(int parent, int child) {
    insertChild(parent, child, -1, false);
  }

  /**
   * Delete a child from a parent node
   * @param parent : id of parent node
   * @param position : position of child to delete
   */
  public void deleteChild(int parent, int position) {
    graph.removeEdge(parent, position);
  }

  /**
   * Perform garbage collection to recover unused nodes.
   * These are nodes that are not contained in any existing tree,
   * and are deleted from the underlying graph to be recycled.
   * Note that nodes that have been allocated and not attached to
   * an existing tree will be recycled!
   */
  public void garbageCollect() {
    final boolean db = false;
    if (db) {
      System.out.println("garbageCollect");
    }

    // flags for each node, for traversing operations
    BitSet nodeFlags = new BitSet(trees.length());

    for (int tid = 0; tid < trees.length(); tid++) {
      if (!trees.exists(tid)) {
        continue;
      }
      int root = trees.getInt(tid);
      if (db) {
        System.out.println(" painting tree " + treeIdFromIndex(tid) + ", root " +
                           root);
      }
      paint(root, nodeFlags);
    }

    int ln = nodeFlags.length();
    for (int i = 0; i < ln; i++) {
      if (nodesUsed.get(i)
          && !nodeFlags.get(i)) {
        nodesUsed.set(i, false);
        if (db) {
          System.out.println("  deleting node " + (Graph.IDBASE + i));
        }
        graph.delete(Graph.IDBASE + i);
      }
    }
  }

  /**
   * Clear forest of all trees & nodes.
   */
  public void clear() {
    graph.clear();
    trees.clear();
    nodesUsed.clear();
  }

  /**
   * Get number of trees.
   * @return number of trees; includes any deleted trees
   */
  public int size() {
    return trees.length();
  }

  /**
   * Get id of tree with a particular index
   * @param index : index of tree (0...trees()-1)
   * @return id of tree, or -1 if no such tree exists
   */
  public int getTreeID(int index) {
    int id = -1;
    if (trees.exists(index)) {
      id= trees.getInt(index);
    }
    return id;
  }

  /**
   * Paint tree by setting flags for every reachable node from tree
   * @param root : id of root node
   * @param nodeFlags : BitSet; bits are set to 1 by painting
   */
  private void paint(int root, BitSet nodeFlags) {
    int rootInd = root - Graph.IDBASE;
    if (!nodeFlags.get(rootInd)) {
      nodeFlags.set(rootInd);
      int nc = graph.nCount(root);
      for (int i = 0; i < nc; i++) {
        paint(graph.neighbor(root, i), nodeFlags);
      }
    }
  }

  /**
   * Get list of nodes in a tree.
   * @param root : id of root node
   * @param list : a list of node ids, sorted by id
   */
  public DArray getNodeList(int root) {
    DArray list = new DArray();
    BitSet nodeFlags = new BitSet();
    paint(root, nodeFlags);
    int nc = nodeFlags.length();

    list.clear();
    for (int i = 0; i < nc; i++) {
      if (nodeFlags.get(i)) {
        list.addInt(i + Graph.IDBASE);
      }
    }
    return list;
  }

  public Object getNode(int nodeId) {
    return graph.nodeData(nodeId);
  }


  /**
   * Test the Forest class
   *
   * @param args String[]
   */
  public static void main(String[] args) {
    {
      System.out.println("Forest main()");
      Random r = new Random(1965);

      Forest f = new Forest();

      int n = f.newNode();
      System.out.println("created node " + n);

      int tree;
      tree = f.newTree(n);
      System.out.println("constructed: " + f.treeString(tree));

      if (true) {
        int t2 = f.newTree(n);
        f.deleteTree(tree);
        tree = t2;
        System.out.println(" deleted original tree, created duplicate with id " +
                           tree + ", " + f.treeString(tree));
      }

      System.out.println("initial node is " + n);
      DArray s = new DArray();
      s.addInt(n);
      for (int i = 0; i < 5; i++) {
        int parent = s.getInt(i);
        int k = r.nextInt(5);
        for (int j = 0; j < k; j++) {
          int c = f.newNode();
          f.addChild(parent, c);
          s.addInt(c);
        }
      }

      System.out.println("Added lots of nodes:\n" + f.treeString(tree));
      f.deleteChild(Graph.IDBASE + 0, 1);
      f.deleteChild(Graph.IDBASE + 1, 2);
      System.out.println("Deleted some children:" + f.treeString(tree));

      f.setRoot(tree, Graph.IDBASE + 3);
      System.out.println("Changed root to '3:" + f.treeString(tree));

      int tree2 = f.newTree(Graph.IDBASE + 0);
      System.out.println("Created new tree " + tree2);
      System.out.println(f.toString());

      System.out.println(" performing garbage collection\n");
      f.garbageCollect();

      System.out.println(f.toString());

      {
        int c = f.newNode(),
            c2 = f.newNode();

        System.out.println(" two new nodes are " + c + " and " + c2);

        f.insertChild(1 + Graph.IDBASE, c, 0, false);
        f.insertChild(1 + Graph.IDBASE, c2, 0, false);
      }
      System.out.println(f.toString());

      System.out.println("Constructing node list for tree " + tree2 + "...");
      DArray nl = f.getNodeList(f.rootNode(tree2));
      nl.print();
    }

  }

  /**
   * list of trees (ids of root nodes)
   */
  private DArray trees = new DArray();

  /**
   * flags indicating whether node is used
   */
  private BitSet nodesUsed = new BitSet();

  // the underlying graph
  private Graph graph = new Graph();
}

//interface IForest {
//  /**
//   * Print tree to StringBuffer
//   * @param root int
//   * @param dest StringBuffer
//   */
//  public void printTree(int root, StringBuffer dest);
//}

