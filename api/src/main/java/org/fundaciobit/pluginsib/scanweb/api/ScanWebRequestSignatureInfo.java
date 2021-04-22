package org.fundaciobit.pluginsib.scanweb.api;

/**
 * 
 * @author anadal
 *
 */
public class ScanWebRequestSignatureInfo {

    /**
     * Nom complet
     */
    protected String functionaryFullName;

    /**
     * NIF
     */
    protected String functionaryAdministrationID;

    /**
     * DIR3 de la Unitat que està per damunt de l'Oficina on es troba el funcionari
     */
    protected String functionaryUnitDIR3;

    public ScanWebRequestSignatureInfo() {
        super();
    }

    public ScanWebRequestSignatureInfo(String functionaryFullName, String functionaryAdministrationID,
            String functionaryUnitDIR3) {
        super();
        this.functionaryFullName = functionaryFullName;
        this.functionaryAdministrationID = functionaryAdministrationID;
        this.functionaryUnitDIR3 = functionaryUnitDIR3;
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

    public String getFunctionaryUnitDIR3() {
        return functionaryUnitDIR3;
    }

    public void setFunctionaryUnitDIR3(String functionaryUnitDIR3) {
        this.functionaryUnitDIR3 = functionaryUnitDIR3;
    }

}
