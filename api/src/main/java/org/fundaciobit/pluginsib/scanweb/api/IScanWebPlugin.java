package org.fundaciobit.pluginsib.scanweb.api;


import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fundaciobit.pluginsib.core.IPlugin;

/**
 * 
 * @author anadal
 * 
 */
public interface IScanWebPlugin extends IPlugin {

    public static final String SCANTYPE_MIME_TIFF = "image/tiff";

    public static final String SCANTYPE_MIME_JPG = "image/jpeg";

    public static final String SCANTYPE_MIME_PNG = "image/png";

    public static final String SCANTYPE_MIME_GIF = "image/gif";
    
    public static final String SCANTYPE_MIME_PDF = "application/pdf";

    public static final String FLAG_PLAIN = "PlainDoc";

    public static final String FLAG_SIGNED = "Signed";
    
    /** Firma amb Segell de Temps */
    public static final String FLAG_SIGNED_WITH_TIMESTAMP = "Signed_With_Timestamp";

    /** Codi segur de verificació */
    public static final String FLAG_SIGNED_AND_CUSTODY = "Signed_And_Custody";



    /** Pàgina o imatge addicional amb la informació de l'escaneig */
    // TODO , InfoPage

    public static final String SCANWEB_PLUGINSIB_BASE_PROPERTY = IPLUGINSIB_BASE_PROPERTIES + "scanweb.";

    public String getName(Locale locale);

    public boolean filter(HttpServletRequest request, ScanWebRequest scanWebRequest);

    /**
     * 
     * @param absolutePluginRequestPath
     * @param relativePluginRequestPath
     * @param request
     * @param config                    Configuració desitjada d'escaneig (revisar
     *                                  mètodes getSupportedScanTypes() i
     *                                  getFlagsByScanType(...)
     * @return URL cap a la pàgina inicial del plugin. Si comença per HTTP es
     *         absoluta en cas contrari es relativa
     * @throws Exception
     */
    public String startScanWebTransaction(String absolutePluginRequestPath, String relativePluginRequestPath,
            HttpServletRequest request, ScanWebRequest config) throws Exception;

    public void endScanWebTransaction(String scanWebID, HttpServletRequest request);

    public void requestGET(String absolutePluginRequestPath, String relativePluginRequestPath, String scanWebID,
            String query, HttpServletRequest request, HttpServletResponse response) throws Exception;

    public void requestPOST(String absolutePluginRequestPath, String relativePluginRequestPath, String scanWebID,
            String query, HttpServletRequest request, HttpServletResponse response) throws Exception;

    public void cleanScannedFiles(String scanWebID, HttpServletRequest request);

    // JPG, PNG, GIF, TIFF, PDF, ...
    public Set<String> getSupportedScanTypes();

    //
    /**
     * Retorna les configuracions suportades (Signed, NonSigned, CSV, Timestamp,
     * InfoPage, ...) per aquell tipus d'escaneig. El primer de la llista és la
     * configuració per defecte.
     * 
     * @param scanType
     * @return
     */
    public Set<String> getSupportedFlagsByScanType(String scanType);

    /**
     * Modes suportats: SINCRON i/o ASINCRON
     * 
     * @return
     */
    public Set<ScanWebMode> getSupportedScanWebModes();

    /**
     * 
     * @param scanWebID
     * @return
     */
    public ScanWebRequest getScanWebRequest(String scanWebID);

    /**
     * 
     * @param scanWebID
     * @return
     */
    public ScanWebResult getScanWebResult(String scanWebID);

    /**
     * 
     * @return
     */
    public boolean isMassiveScanAllowed();

    /**
     * 
     * @return
     */
    public ScanWebPlainFile getSeparatorForMassiveScan(String languageUI) throws Exception;

}