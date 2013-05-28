package base;

import javax.swing.filechooser.*;

/**
 * File chooser interface which can be applied to both normal applications,
 * and applets running application simulation
 */
public interface FileChooser {

    /**
     * Get name of file to open
     *
     * @param path : previously selected filename
     * @param filter : if not null, PathFilter to apply to directory
     * @return String : if not null, path of file to open
     */
    public String doOpen(String prompt, String path, PathFilter filter);
    /**
     * Get name of file to write
     *
     * @param path : previously selected filename
     * @param filter : if not null, PathFilter to apply to directory
     * @return String : if not null, path of file to write
     */
  public String doWrite(String prompt,String current, PathFilter filter);

}
