package org.fundaciobit.pluginsib.scanweb.tester.ejb.utils;


import org.fundaciobit.pluginsib.scanweb.api.ScanWebRequest;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebResult;

/**
 *
 * @author anadal
 *
 */
public class ScanWebInfoTester {

    protected final ScanWebRequest scanWebRequest;

    protected ScanWebResult scanWebResult;

    protected Long pluginID = null;

    public ScanWebInfoTester(ScanWebRequest scanWebRequest) {
        this.scanWebRequest = scanWebRequest;
    }

    public Long getPluginID() {
        return pluginID;
    }

    public void setPluginID(Long pluginID) {
        this.pluginID = pluginID;
    }

    public ScanWebResult getScanWebResult() {
        return scanWebResult;
    }

    public void setScanWebResult(ScanWebResult scanWebResult) {
        this.scanWebResult = scanWebResult;
    }

    public ScanWebRequest getScanWebRequest() {
        return scanWebRequest;
    }

    
}
