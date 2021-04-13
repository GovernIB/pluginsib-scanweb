package org.fundaciobit.pluginsib.scanweb.api;

import java.util.Date;
import java.util.List;

import org.fundaciobit.pluginsib.core.utils.Metadata;

/**
 * 
 * @author anadal
 *
 */
public class ScanWebDocument {

    public static final String FORMAT_FILE_PDF = "pdf";

    public static final String FORMAT_FILE_JPG = "jpg";

    public static final String FORMAT_FILE_TIFF = "tif";

    public static final String FORMAT_FILE_PNG = "png";

    public static final String FORMAT_FILE_GIF = "gif";

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

    /** Null significa que no ho sab. Exemples BMP JPEG TIFF PNG PDF **/
    protected String formatFile;

    /** Null significa que no ho sab. **/
    protected Boolean ocr;

    protected String documentLanguage;

    protected List<Metadata> additionalMetadatas;

    /**
     * 
     */
    public ScanWebDocument() {
        super();
    }

    public ScanWebDocument(String transactionName, ScanWebPlainFile scannedPlainFile,
            ScanWebSignedFile scannedSignedFile, Date scanDate, Integer pixelType, Integer pppResolution,
            String formatFile, Boolean ocr, String documentLanguage, List<Metadata> additionalMetadatas) {
        super();
        this.transactionName = transactionName;
        this.scannedPlainFile = scannedPlainFile;
        this.scannedSignedFile = scannedSignedFile;
        this.scanDate = scanDate;
        this.pixelType = pixelType;
        this.pppResolution = pppResolution;
        this.formatFile = formatFile;
        this.ocr = ocr;
        this.documentLanguage = documentLanguage;
        this.additionalMetadatas = additionalMetadatas;
    }

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

    public String getFormatFile() {
        return formatFile;
    }

    public void setFormatFile(String formatFile) {
        this.formatFile = formatFile;
    }

    public Boolean getOcr() {
        return ocr;
    }

    public void setOcr(Boolean ocr) {
        this.ocr = ocr;
    }

    public String getDocumentLanguage() {
        return documentLanguage;
    }

    public void setDocumentLanguage(String documentLanguage) {
        this.documentLanguage = documentLanguage;
    }

    public List<Metadata> getAdditionalMetadatas() {
        return additionalMetadatas;
    }

    public void setAdditionalMetadatas(List<Metadata> additionalMetadatas) {
        this.additionalMetadatas = additionalMetadatas;
    }

}
