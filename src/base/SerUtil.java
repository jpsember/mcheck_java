package base;

import java.io.*;

public class SerUtil {
  public static void write(DataOutputStream s, int[] a) throws IOException {
    for (int i = 0 ; i < a.length; i++)
      s.writeInt(a[i]);
  }
  public static void read(DataInputStream s, int[] a) throws IOException {
    for (int i = 0 ; i < a.length; i++)
      a[i] = s.readInt();
  }
  public static void write(DataOutputStream s, ISerialize[] a) throws IOException {
    for (int i = 0; i < a.length; i++)
      a[i].write(s);
  }
}
