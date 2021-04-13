package org.fundaciobit.pluginsib.scanweb.api;

import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;

/**
 * 
 * @author anadal
 *
 */
public class ScanWebPlainFile extends DocumentCustody {

  /**
   * 
   */
  public ScanWebPlainFile() {
    super();
  }

  /**
   * @param dc
   */
  public ScanWebPlainFile(DocumentCustody dc) {
    super(dc);
  }

  /**
   * @param name
   * @param data
   */
  public ScanWebPlainFile(String name, byte[] data) {
    super(name, data);
  }

  /**
   * @param name
   * @param mime
   * @param data
   */
  public ScanWebPlainFile(String name, String mime, byte[] data) {
    super(name, mime, data);
  }

}
