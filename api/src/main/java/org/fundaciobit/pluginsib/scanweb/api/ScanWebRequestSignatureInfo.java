package org.fundaciobit.pluginsib.scanweb.api;

/**
 * 
 * @author anadal
 *
 */
public class ScanWebRequestSignatureInfo {

    protected String documentLanguage;

    protected String functionaryFullName;

    protected String functionaryAdministrationID;

    public ScanWebRequestSignatureInfo() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ScanWebRequestSignatureInfo(String documentLanguage, String functionaryFullName,
            String functionaryAdministrationID) {
        super();
        this.documentLanguage = documentLanguage;
        this.functionaryFullName = functionaryFullName;
        this.functionaryAdministrationID = functionaryAdministrationID;
    }

    public String getDocumentLanguage() {
        return documentLanguage;
    }

    public void setDocumentLanguage(String documentLanguage) {
        this.documentLanguage = documentLanguage;
    }

    public String getFunctionaryFullName() {
        return functionaryFullName;
    }

    public void setFunctionaryFullName(String functionaryFullName) {
        this.functionaryFullName = functionaryFullName;
    }

    public String getFunctionaryAdministrationID() {
        return functionaryAdministrationID;
    }

    public void setFunctionaryAdministrationID(String functionaryAdministrationID) {
        this.functionaryAdministrationID = functionaryAdministrationID;
    }

}
