package org.fundaciobit.pluginsib.scanweb.api;

import org.fundaciobit.plugins.documentcustody.api.SignatureCustody;

/**
 * 
 * @author anadal
 *
 */
public class ScanWebSignedFile extends SignatureCustody {

  /**
   * 
   */
  public ScanWebSignedFile() {
    super();
  }

  /**
   * @param sc
   */
  public ScanWebSignedFile(SignatureCustody sc) {
    super(sc);
  }

  /**
   * @param name
   * @param data
   * @param signatureType
   * @param attachedDocument
   */
  public ScanWebSignedFile(String name, byte[] data, String signatureType,
      Boolean attachedDocument) {
    super(name, data, signatureType, attachedDocument);
  }

  /**
   * @param name
   * @param data
   * @param signatureType
   */
  public ScanWebSignedFile(String name, byte[] data, String signatureType) {
    super(name, data, signatureType);
  }

  /**
   * @param name
   * @param mime
   * @param data
   * @param signatureType
   * @param attachedDocument
   */
  public ScanWebSignedFile(String name, String mime, byte[] data, String signatureType,
      Boolean attachedDocument) {
    super(name, mime, data, signatureType, attachedDocument);
  }

}
