package base;

import java.util.Random;

/**
 * Graph class
 *
 * Implements a directed graph.  Each node has a unique non-zero id
 * which can be used as an index into a storage array.
 */
public class Graph
{
  public static final int IDBASE = 30;

  /**
   * Get list of nodes in a graph.
   * @param list : a list of node ids, sorted by id
   */
  public void getNodeList(DArray list) {
    list.clear();

    for (int i = 0; i < nodes.length(); i++) {
      if (!nodes.exists(i)) {
        continue;
      }
      list.addInt(nodeIdFromIndex(i));
    }
  }

  /**
   * Get list of nodes in a graph.
   * @return a list of node ids, sorted by id
   */
public DArray getNodeList() {
  DArray a = new DArray();
  getNodeList(a);
  return a;
}

  /**
   * Convert an index to a node id
   * @param index : index 0...n-1
   * @return Node ID
   */
  private final static int nodeIdFromIndex(int index) {
    return index + IDBASE;
  }

  /**
   * Convert a node id to an index
   * @param id : node id
   * @return index 0...n-1
   */
  private final static int indexFromNodeId(int id) {
    if (id < IDBASE) {
      throw new IllegalArgumentException("*!!indexFromNodeId, id=" + id);
    }
    return id - IDBASE;
  }

  /**
   * Add a new node to the graph
   * @return id of new node
   */
  public int newNode() {
    return newNode(null);
  }

  /**
   * Add a new node to the graph
   * @param data : user data to store with node, or null
   * @return id of new node
   */
  public int newNode(Object userData) {
    int index = nodes.alloc(new Node(userData));
    int id = nodeIdFromIndex(index);

    return id;
  }

  public boolean nodeExists(int id) {
    int ind = indexFromNodeId(id);
    return nodes.exists(ind);
  }

  protected Node node(int id) {
    int ind = indexFromNodeId(id);
    if (true) {
      if (!nodes.exists(ind)) {
        throw new IllegalArgumentException("*Attempt to get non-existent node " + id +
                          " in graph\n" + this);
      }
    }
    else {
      Tools.ASSERT(nodes.exists(ind));
    }
    return (Node) nodes.get(ind);
  }

  /**
   * Add a neighbor to a node by creating a directed edge to it
   * @param src : source node
   * @param dest : destination node
   * @param edgeData : data to store with edge, or null
   * @param pos : position in source's neighbor list to insert, or -1 for end
   * @param replaceFlag : if true, existing edge is replaced; else new inserted
   */
  public void addEdge(int src, int dest, Object edgeData,
                      int pos, boolean replaceFlag) {
    Node sn = node(src);
    sn.addNeighbor(dest, edgeData, pos, replaceFlag);
  }

  /**
   * Add edges between two nodes
   * @param a : id of first node
   * @param b : id of second node
   * @param edgeDataAB : data to store with edge from a->b, or null
   * @param edgeDataBA : data to store with edge from a->b, or null
   */
  public void addEdgesBetween(int a, int b, Object edgeDataAB,
                              Object edgeDataBA) {
    addEdge(a, b, edgeDataAB);
    addEdge(b, a, edgeDataBA);
  }

  /**
   * Add a neighbor to a node by creating a directed edge to it
   * @param src : source node
   * @param dest : destination node
   */
  public void addEdge(int src, int dest) {
    addEdge(src, dest, null);
  }

  /**
   * Add a neighbor to a node by creating a directed edge to it
   * @param src : source node
   * @param dest : destination node
   * @param edgeData : data to store with edge, or null
   */
  public void addEdge(int src, int dest, Object edgeData) {
    addEdge(src, dest, edgeData, -1, false);
  }

  /**
   * Determine if a node has a particular neighbor
   * @param src : id of source node
   * @param dest : id of destination node to look for
   * @return index of src neighbor leading to dest, or -1
   */
  public int hasNeighbor(int src, int dest) {
    int nIndex = -1;
    Node sn = node(src);
    for (int i = 0; i < sn.nTotal(); i++) {
      if (sn.neighbor(i) == dest) {
        nIndex = i;
        break;
      }
    }
    return nIndex;
  }

  /**
   * Remove a neighbor from a node
   * @param src : id of source node
   * @param index : index of neighbor to remove
   */
  public void removeEdge(int src, int index) {
    Node sn = node(src);
    sn.removeIndex(index);
  }

  /**
   * Remove an edge between two nodes, if it exists
   * @param src : id of source node
   * @param dest : id of dest node
   * @param all : if true, removes all such edges; if false,
   *  removes just the first one found
   * @return number of edges removed
   */
  public int findAndRemoveEdge(int src, int dest, boolean all) {
    int count = 0;
    for (int i = 0; i < nCount(src); i++) {
      if (neighbor(src, i) == dest) {
        node(src).removeIndex(i--);
        count++;
        if (!all) {
          break;
        }
      }
    }
    return count;
  }

  public void removeEdgesBetween(int n0, int n1) {
    findAndRemoveEdge(n0, n1, true);
    findAndRemoveEdge(n1, n0, true);
  }

  public void removeLoops() {
    DArray nl = getNodeList();
    for (int i = 0; i < nl.length(); i++) {
      int n = nl.getInt(i);
      for (int j = nCount(n) - 1; j >= 0; j--) {
        if (neighbor(n, j) == n) {
          removeEdge(n, j);
        }
      }
    }
  }

  /**
   * Clear the graph
   */
  public void clear() {
    nodes.clear();
    edges.clear();
  }

  /**
   * Get number of neighbors for a node
   * @param id : id of node
   */
  public int nCount(int id) {
    return node(id).nTotal();
  }

  /**
   * Get neighbor for a node
   * @param id : id of node
   * @param nIndex : index of neighbor
   * @return id of neighbor
   */
  public int neighbor(int id, int nIndex) {
    return node(id).neighbor(nIndex);
  }

  /**
   * Get user data stored with node
   * @param id : id of node
   * @return user data, or null if none was stored
   */
  public Object nodeData(int nodeId) {
    return node(nodeId).userData();
  }

  /**
   * Set user data for a node
   * @param nodeId : id of node
   * @param data : user data to store with node, or null
   */
  public void setNodeData(int nodeId, Object data) {
    node(nodeId).setUserData(data);
  }

  /**
   * Get user data stored with edge
   * @param nodeId : id of node at start of edge
   * @param nIndex : index of edge in node's edge list
   * @return user data, or null if none was stored
   */
  public Object edgeData(int nodeId, int nIndex) {
    return node(nodeId).edgeData(nIndex);
  }

  /**
   * Delete a node.  Doesn't delete any edges that other nodes may
   * have to this node!
   * @param id : id of node to delete
   */
  public void delete(int id) {
    int ind = indexFromNodeId(id);
    nodes.free(ind);
  }

  /**
   * Construct description
   * @return String
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Graph\n");
    for (int i = 0; i < nodes.length(); i++) {
      if (!nodes.exists(i)) {
        continue;
      }

      int id = nodeIdFromIndex(i);
      Node n = node(id);
      sb.append(" ");
      sb.append(Tools.f(id));
      sb.append(": ");
      sb.append(n.toString());
      sb.append('\n');
    }
    return sb.toString();
  }

  /**
   * Test graph class
   * @param args String[]
   */
  public static void main(String[] args) {
    Random r = new Random(1965);

    Graph g = new Graph();
    int[] n = new int[20];

    for (int i = 0; i < n.length; i++) {
      n[i] = g.newNode();
      for (int j = 0; j < i; j += r.nextInt(5)) {
        if (g.hasNeighbor(n[i], n[j]) != 0) {
          continue;
        }
        g.addEdge(n[i], n[j]);
      }
    }
    System.out.println(g.toString());
  }

  public Graph() {
    nodes = new DArray();
    edges = new DArray();
  }

  // storage for nodes
  private DArray nodes;
  private DArray edges;

  /**
   * Node class for graph
   */
  private static class Node
  {

    public Node(Object userData) {
      this.data = userData;
    }

    /**
     * Construct description
     * @return String
     */
    public String toString() {
      StringBuffer sb = new StringBuffer();
      boolean big = false;
      final String padding = "   --> ";
      for (int i = 0; i < nTotal(); i++) {
        if (big) {
          sb.append(padding);
        }
        else {
          if (i > 0) {
            sb.append(' ');
          }
        }
        sb.append(Tools.f(neighbor(i)));
        Object eData = edgeData(i);
        if (eData != null) {
          sb.append(':');
          String ns = eData.toString();
          sb.append(ns);
          if ( (i == 0 && ns.length() > 10)) {
            big = true;
            sb.insert(0, "\n" + padding);
          }
          if (big) {
            sb.append('\n');
          }
        }
      }
      if (data != null) {
        sb.append(" ");
        sb.append(data);
      }
      return sb.toString();
    }

    /**
     * Get number of neighbors adjacent to node
     * @return int : # edges
     */
    public int nTotal() {
      return neighbors.length();
    }

    /**
     * Get neighbor node
     * @param nIndex : edge index (0..nTotal()-1)
     * @return id of neighbor node
     */
    public int neighbor(int nIndex) {
      GEdge e = getEdge(nIndex);
      return e.dest();
    }

    /**
     * Get GEdge object from neighbor array
     * @param nIndex : index of neighbor
     * @return GEdge read from array
     */
    private GEdge getEdge(int nIndex) {
      if (!neighbors.exists(nIndex))
        throw new IllegalArgumentException("node has no edge "+nIndex+":\n"+this);
      return (GEdge) neighbors.get(nIndex);
    }

    public void setUserData(Object data) {
      this.data = data;
    }

    /**
     * Get user data from edge
     * @param nIndex : edge index (0..nTotal()-1)
     * @return user data
     */
    public Object edgeData(int nIndex) {
      return getEdge(nIndex).data();
    }

    /**
     * Add a neighbor to this node
     * @param id : id of neighbor
     * @param edgeData : data to store with edge, or null
     * @param insertPos : position in neighbor list to insert at, or -1 to
     *   append to the end
     * @param replaceFlag : if true, neighbor replaces old instead of being inserted
     */
    public void addNeighbor(int neighborId, Object edgeData,
                            int insertPos, boolean replaceFlag) {
      //System.out.println("addNeighbor id="+neighborId+" insertPos="+insertPos+" rep="+replaceFlag);
      if (replaceFlag) {
        neighbors.set(insertPos, new GEdge(neighborId, edgeData));
      }
      else {
        neighbors.insert(insertPos, new GEdge(neighborId, edgeData));
      }
    }

    /**
     * Remove a neighbor from a particular position in the list.
     * @param loc : location to delete
     */
    public void removeIndex(int loc) {
      neighbors.delete(loc, 1);
    }

    /**
     * Get user data stored with node
     * @return Object, or null if no data is stored here
     */
    public Object userData() {
      return data;
    }

    // neighbor list; stores GEdge objects
    private DArray neighbors = new DArray();

    // user data stored with node
    private Object data;
  }

  /**
   * Edge class for graphs
   */
  private static class GEdge {
    public GEdge(int destNode, Object data) {
      this.destNode = destNode;
      this.data = data;
    }

    public int dest() {
      return destNode;
    }

    public Object data() {
      return data;
    }

    // destination of edge
    private int destNode;
    // user data stored with edge
    private Object data;
  }

}
