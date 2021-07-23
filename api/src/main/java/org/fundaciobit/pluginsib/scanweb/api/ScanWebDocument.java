package org.fundaciobit.pluginsib.scanweb.api;

import java.util.Date;
import java.util.List;

import org.fundaciobit.pluginsib.core.utils.Metadata;
import org.fundaciobit.pluginsib.core.utils.MetadataConstants;

/**
 * 
 * @author anadal
 *
 */
public class ScanWebDocument {

    public static final String SCANTYPE_MIME_TIFF = "image/tiff";

    public static final String SCANTYPE_MIME_JPG = "image/jpeg";

    public static final String SCANTYPE_MIME_PNG = "image/png";

    public static final String SCANTYPE_MIME_GIF = "image/gif";

    public static final String SCANTYPE_MIME_PDF = "application/pdf";

    public static final String FLAG_PLAIN = "PlainDoc";

    public static final String FLAG_SIGNED = "Signed";

    /** Firma amb Segell de Temps */
    public static final String FLAG_SIGNED_WITH_TIMESTAMP = "Signed_With_Timestamp";

    /** API NO CONTE CAMPS PER RETORNAR AQUESTA INFORMACIO */
    // public static final String FLAG_SIGNED_AND_CUSTODY = "Signed_And_Custody";

    public static final int PIXEL_TYPE_BLACK_WHITE = 0;

    public static final int PIXEL_TYPE_GRAY = 1;

    public static final int PIXEL_TYPE_COLOR = 2;

    protected String transactionName;

    protected ScanWebPlainFile scannedPlainFile;

    protected ScanWebSignedFile scannedSignedFile;

    protected Date scanDate;

    /** Pixel Type. Null significa que no ho sab. Exemples: B&W Gray Color **/
    protected Integer pixelType;

    /**
     * Píxeles por pulgada. Null significa que no es coneix. Exemples: 100, 300,
     * 600, ...
     **/
    protected Integer pppResolution;

    /**
     * Null significa que no ho sab. Els valors són els mateixos que SCANTYPE_MIME_*
     * de IScanWebPlugin.
     * 
     * @see SCANTYPE_MIME_TIFF = "image/tiff";
     * @see SCANTYPE_MIME_JPG = "image/jpeg";
     * @see SCANTYPE_MIME_PNG = "image/png";
     * @see SCANTYPE_MIME_GIF = "image/gif";
     * @see SCANTYPE_MIME_PDF = "application/pdf";
     * 
     **/
    protected String scannedFileFormat;

    /** Null significa que no ho sab. **/
    protected Boolean ocr;

    /** Null significa que no ho sab. **/
    protected Boolean duplex;

    /**
     * @see MetadataConstants.PAPER_SIZE
     * @see MetadataConstants._PAPER_SIZE
     */
    protected String paperSize;

    protected String documentLanguage;

    /**
     * @see org.fundaciobit.pluginsib.core.utils.MetadataConstants#_ENI_TIPO_DOCUMENTAL
     *
     * @see org.fundaciobit.pluginsib.core.utils.MetadataConstants#TIPO_DOCUMENTAL_TD01_RESOLUCIO
     * @see org.fundaciobit.pluginsib.core.utils.MetadataConstants#TIPO_DOCUMENTAL_TD02_ACORD
     * @see org.fundaciobit.pluginsib.core.utils.MetadataConstants#TIPO_DOCUMENTAL_TD03_CONTRACTE
     * ...
     * @see org.fundaciobit.pluginsib.core.utils.MetadataConstants#TIPO_DOCUMENTAL_TD99_ALTRES
     * 
     */
    protected String documentType;

    /**
     * @see org.fundaciobit.pluginsib.core.utils.MetadataConstants
     */
    protected List<Metadata> additionalMetadatas;

    /**
     * 
     */
    public ScanWebDocument() {
        super();
    }
    
    

    public ScanWebDocument(String transactionName, ScanWebPlainFile scannedPlainFile,
            ScanWebSignedFile scannedSignedFile, Date scanDate, Integer pixelType, Integer pppResolution,
            String scannedFileFormat, Boolean ocr, Boolean duplex, String paperSize, String documentLanguage,
            String documentType, List<Metadata> additionalMetadatas) {
        super();
        this.transactionName = transactionName;
        this.scannedPlainFile = scannedPlainFile;
        this.scannedSignedFile = scannedSignedFile;
        this.scanDate = scanDate;
        this.pixelType = pixelType;
        this.pppResolution = pppResolution;
        this.scannedFileFormat = scannedFileFormat;
        this.ocr = ocr;
        this.duplex = duplex;
        this.paperSize = paperSize;
        this.documentLanguage = documentLanguage;
        this.documentType = documentType;
        this.additionalMetadatas = additionalMetadatas;
    }



    /*
    public ScanWebDocument(String transactionName, ScanWebPlainFile scannedPlainFile,
            ScanWebSignedFile scannedSignedFile, Date scanDate, Integer pixelType, Integer pppResolution,
            String scannedFileFormat, Boolean ocr, Boolean duplex, String paperSize, 
            String documentLanguage, String documentType,
            List<Metadata> additionalMetadatas) {
        super();
        this.transactionName = transactionName;
        this.scannedPlainFile = scannedPlainFile;
        this.scannedSignedFile = scannedSignedFile;
        this.scanDate = scanDate;
        this.pixelType = pixelType;
        this.pppResolution = pppResolution;
        this.scannedFileFormat = scannedFileFormat;
        this.ocr = ocr;
        this.duplex = duplex;
        this.paperSize = paperSize;
        this.documentLanguage = documentLanguage;
        this.documentType = documentType;
        this.additionalMetadatas = additionalMetadatas;
    }
    */

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public ScanWebPlainFile getScannedPlainFile() {
        return scannedPlainFile;
    }

    public void setScannedPlainFile(ScanWebPlainFile scannedPlainFile) {
        this.scannedPlainFile = scannedPlainFile;
    }

    public ScanWebSignedFile getScannedSignedFile() {
        return scannedSignedFile;
    }

    public void setScannedSignedFile(ScanWebSignedFile scannedSignedFile) {
        this.scannedSignedFile = scannedSignedFile;
    }

    public Date getScanDate() {
        return scanDate;
    }

    public void setScanDate(Date scanDate) {
        this.scanDate = scanDate;
    }

    public Integer getPixelType() {
        return pixelType;
    }

    public void setPixelType(Integer pixelType) {
        this.pixelType = pixelType;
    }

    public Integer getPppResolution() {
        return pppResolution;
    }

    public void setPppResolution(Integer pppResolution) {
        this.pppResolution = pppResolution;
    }

    public String getScannedFileFormat() {
        return scannedFileFormat;
    }

    public void setScannedFileFormat(String scannedFileFormat) {
        this.scannedFileFormat = scannedFileFormat;
    }

    public Boolean getOcr() {
        return ocr;
    }

    public void setOcr(Boolean ocr) {
        this.ocr = ocr;
    }

    public Boolean getDuplex() {
        return duplex;
    }

    public void setDuplex(Boolean duplex) {
        this.duplex = duplex;
    }

    /**
     * @see MetadataConstants.PAPER_SIZE
     * @see MetadataConstants._PAPER_SIZE
     */
    public String getPaperSize() {
        return paperSize;
    }

    public void setPaperSize(String paperSize) {
        this.paperSize = paperSize;
    }

    public String getDocumentLanguage() {
        return documentLanguage;
    }

    public void setDocumentLanguage(String documentLanguage) {
        this.documentLanguage = documentLanguage;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public List<Metadata> getAdditionalMetadatas() {
        return additionalMetadatas;
    }

    public void setAdditionalMetadatas(List<Metadata> additionalMetadatas) {
        this.additionalMetadatas = additionalMetadatas;
    }

}
