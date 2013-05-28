package base;

import java.io.*;

public class FNameFilter extends javax.swing.filechooser.FileFilter
    implements FilenameFilter {

  public FNameFilter(String ext, String desc, boolean includeDirs) {
    extension = "." + ext.toLowerCase();
    this.desc = desc;
    dirsFlag = includeDirs;
  }

  public String getDescription() {
    return desc;
  }

  private String desc;
  private boolean dirsFlag;
  private String extension;

  /**
   * Accept file?
   * @param dir File, or null
   * @param name String
   * @return boolean
   */
  public boolean accept(File dir, String name) {

    boolean flag = false;
    do {
      File f = new File(name);

      if (f.isDirectory()) {
        flag = dirsFlag;
        break;
      }

      if (name.endsWith(extension)) {
        flag = true;
        break;
      }
    }
    while (false);
    return flag;
  }

  /**
   * Tests whether or not the specified abstract pathname should be
   * included in a pathname list.
   *
   * @param  pathname  The abstract pathname to be tested
   * @return  <code>true</code> if and only if <code>pathname</code>
   *          should be included
   */
  public boolean accept(File pathname) {
    return accept(null,pathname.getAbsolutePath());
  }


}
