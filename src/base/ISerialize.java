package base;

import java.io.*;

public interface ISerialize {
  public void write(DataOutputStream s) throws IOException;

  /**
   * Implementing classes should also include a special serializing constructor
   * of the form:
   *
   * public ClassName(DataInputStream in) throws IOException;
   */
}
