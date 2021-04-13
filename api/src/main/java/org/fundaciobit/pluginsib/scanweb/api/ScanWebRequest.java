package org.fundaciobit.pluginsib.scanweb.api;

import java.util.List;
import java.util.Set;

import org.fundaciobit.pluginsib.core.utils.Metadata;

/**
 * 
 * @author anadal
 *
 */
public class ScanWebRequest {
    
    protected String scanWebID;

    protected String scanType;

    protected Set<String> flags;

    protected ScanWebMode mode;

    protected String languageUI;

    protected String username;

    /**
     * Opcional. Requerit en mode SINCRON
     */
    protected String urlFinal;

    /** Es permeten entrades amb claus repetides */
    protected List<Metadata> additionalMetadatas;

    protected ScanWebRequestSignatureInfo signatureInfo;

    protected ScanWebRequestCustodyInfo custodyInfo;

    public ScanWebRequest() {
        super();
    }

    public ScanWebRequest(String scanWebID, String scanType, Set<String> flags, ScanWebMode mode, String languageUI, String username,
            String urlFinal, List<Metadata> metadades) {
        super();
        this.scanWebID = scanWebID;
        this.scanType = scanType;
        this.flags = flags;
        this.additionalMetadatas = metadades;
        this.mode = mode;
        this.languageUI = languageUI;
        this.username = username;
        this.urlFinal = urlFinal;
    }

    public ScanWebRequest(String scanWebID, String scanType, Set<String> flags, ScanWebMode mode, String languageUI,
            String username, String urlFinal, List<Metadata> metadades, ScanWebRequestSignatureInfo signatureInfo) {
        super();
        this.scanWebID = scanWebID;
        this.scanType = scanType;
        this.flags = flags;
        this.additionalMetadatas = metadades;
        this.mode = mode;
        this.languageUI = languageUI;
        this.username = username;
        this.urlFinal = urlFinal;
        this.signatureInfo = signatureInfo;
    }

    public ScanWebRequest(String scanWebID, String scanType, Set<String> flags, ScanWebMode mode, String languageUI,
            String username, String urlFinal, List<Metadata> metadades, ScanWebRequestSignatureInfo signatureInfo,
            ScanWebRequestCustodyInfo custodyInfo) {
        super();
        this.scanWebID = scanWebID;
        this.scanType = scanType;
        this.flags = flags;
        this.additionalMetadatas = metadades;
        this.mode = mode;
        this.languageUI = languageUI;
        this.username = username;
        this.urlFinal = urlFinal;
        this.signatureInfo = signatureInfo;
        this.custodyInfo = custodyInfo;
    }

    public String getScanWebID() {
        return scanWebID;
    }

    public void setScanWebID(String scanWebID) {
        this.scanWebID = scanWebID;
    }

    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    public Set<String> getFlags() {
        return flags;
    }

    public void setFlags(Set<String> flags) {
        this.flags = flags;
    }

    public String getUrlFinal() {
        return urlFinal;
    }

    public void setUrlFinal(String urlFinal) {
        this.urlFinal = urlFinal;
    }

    public ScanWebMode getMode() {
        return mode;
    }

    public void setMode(ScanWebMode mode) {
        this.mode = mode;
    }

    public String getLanguageUI() {
        return languageUI;
    }

    public void setLanguageUI(String languageUI) {
        this.languageUI = languageUI;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ScanWebRequestSignatureInfo getSignatureInfo() {
        return signatureInfo;
    }

    public void setSignatureInfo(ScanWebRequestSignatureInfo signatureInfo) {
        this.signatureInfo = signatureInfo;
    }

    public ScanWebRequestCustodyInfo getCustodyInfo() {
        return custodyInfo;
    }

    public void setCustodyInfo(ScanWebRequestCustodyInfo custodyInfo) {
        this.custodyInfo = custodyInfo;
    }

    public List<Metadata> getAdditionalMetadatas() {
        return additionalMetadatas;
    }

    public void setAdditionalMetadatas(List<Metadata> additionalMetadatas) {
        this.additionalMetadatas = additionalMetadatas;
    }

}
