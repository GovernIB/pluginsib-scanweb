package org.fundaciobit.pluginsib.scanweb.tester.form;


/**
 * 
 * @author anadal
 * 
 */
public class ScanWebConfigForm {

    String id;
    
    protected String transactionName;
    
    protected String type;

    protected String flag;

    // S => Sincon i A => Asincron
    protected String mode;

    protected String langUI;

    protected String username;
    
    protected String nom;
    
    protected String nif;
    
    protected String langDoc;
    
    protected String functionaryUnitDIR3;
    

    

    public ScanWebConfigForm() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLangUI() {
        return langUI;
    }

    public void setLangUI(String langUI) {
        this.langUI = langUI;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getLangDoc() {
        return langDoc;
    }

    public void setLangDoc(String langDoc) {
        this.langDoc = langDoc;
    }

    public String getFunctionaryUnitDIR3() {
        return functionaryUnitDIR3;
    }

    public void setFunctionaryUnitDIR3(String functionaryUnitDIR3) {
        this.functionaryUnitDIR3 = functionaryUnitDIR3;
    }

}
