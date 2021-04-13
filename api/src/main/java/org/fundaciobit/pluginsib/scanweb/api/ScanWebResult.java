package org.fundaciobit.pluginsib.scanweb.api;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author anadal
 *
 */
public class ScanWebResult {

    protected final ScanWebStatus status = new ScanWebStatus();

    protected final List<ScanWebDocument> scannedDocuments = new ArrayList<ScanWebDocument>();

    public ScanWebResult() {
        super();
    }

    public ScanWebStatus getStatus() {
        return status;
    }

    public List<ScanWebDocument> getScannedDocuments() {
        return scannedDocuments;
    }

}
