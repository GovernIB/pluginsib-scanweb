package org.fundaciobit.pluginsib.scanweb.api;

/**
 * 
 * @author anadal
 *
 */
public class ScanWebResultSignValidationInfo {



    protected Boolean checkAdministrationIDOfSigner;


    protected Boolean checkDocumentModifications;


    protected Boolean checkValidationSignature;


    public ScanWebResultSignValidationInfo() {
        super();
        // TODO Auto-generated constructor stub
    }


    public ScanWebResultSignValidationInfo(Boolean checkAdministrationIDOfSigner, Boolean checkDocumentModifications,
            Boolean checkValidationSignature) {
        super();
        this.checkAdministrationIDOfSigner = checkAdministrationIDOfSigner;
        this.checkDocumentModifications = checkDocumentModifications;
        this.checkValidationSignature = checkValidationSignature;
    }


    public Boolean getCheckAdministrationIDOfSigner() {
        return checkAdministrationIDOfSigner;
    }


    public void setCheckAdministrationIDOfSigner(Boolean checkAdministrationIDOfSigner) {
        this.checkAdministrationIDOfSigner = checkAdministrationIDOfSigner;
    }


    public Boolean getCheckDocumentModifications() {
        return checkDocumentModifications;
    }


    public void setCheckDocumentModifications(Boolean checkDocumentModifications) {
        this.checkDocumentModifications = checkDocumentModifications;
    }


    public Boolean getCheckValidationSignature() {
        return checkValidationSignature;
    }


    public void setCheckValidationSignature(Boolean checkValidationSignature) {
        this.checkValidationSignature = checkValidationSignature;
    }
    
    
    
}
