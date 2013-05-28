package base;

import java.io.*;

public class CommandHistory {

  void setPos(int p) {
    historyPos = p;
  }

  void setPrefix(String prefix) {
    this.histPrefix = prefix;
  }

  void add(String cmd) {
    history.add(cmd);
  }

  String getCommand(int index) {
    return history.getString(index);
  }

  int getPos() {
    return historyPos;
  }

  public int length() {
    return history.length();
  }

  void adjustPos(int amt) {
    historyPos = Tools.mod(historyPos + amt, length());
  }

  String getPrefix() {
    return histPrefix;
  }

  private int historyPos;
  private String histPrefix;
  private DArray history = new DArray();
}
