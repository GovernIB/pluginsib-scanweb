package org.fundaciobit.pluginsib.scanweb.springboottester.logic;

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

    protected final long expiryTransaction;

    public ScanWebInfoTester(ScanWebRequest scanWebRequest) {
        this.scanWebRequest = scanWebRequest;
        this.expiryTransaction = System.currentTimeMillis() + 20 * 60 * 1000;
        this.scanWebResult = new ScanWebResult();
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

    public ScanWebRequest getScanWebRequest() {
        return scanWebRequest;
    }

    public long getExpiryTransaction() {
        return expiryTransaction;
    }

    public void setScanWebResult(ScanWebResult scanWebResult) {
        this.scanWebResult = scanWebResult;
    }

}
