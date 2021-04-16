package org.fundaciobit.pluginsib.scanweb.api;

import org.fundaciobit.plugins.documentcustody.api.SignatureCustody;

/**
 * 
 * @author anadal
 *
 */
public class ScanWebSignedFile extends SignatureCustody {

    protected ScanWebResultSignInfo signInfo;

    /**
     * 
     */
    public ScanWebSignedFile() {
        super();
    }

    /**
     * @param name
     * @param mime
     * @param data
     * @param signatureType
     * @param attachedDocument
     */
    public ScanWebSignedFile(String name, String mime, byte[] data, String signatureType, Boolean attachedDocument,
            ScanWebResultSignInfo signInfo) {
        super(name, mime, data, signatureType, attachedDocument);
        this.signInfo = signInfo;
    }

    public ScanWebResultSignInfo getSignInfo() {
        return signInfo;
    }

    public void setSignInfo(ScanWebResultSignInfo signInfo) {
        this.signInfo = signInfo;
    }

}
