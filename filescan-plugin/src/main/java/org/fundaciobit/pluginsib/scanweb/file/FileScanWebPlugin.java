package org.fundaciobit.pluginsib.scanweb.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.fundaciobit.pluginsib.core.utils.Metadata;
import org.fundaciobit.pluginsib.scanweb.api.AbstractScanWebPlugin;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebDocument;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebMode;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebPlainFile;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebRequest;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebResult;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebSignedFile;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebStatus;

/**
 * 
 * @author anadal
 * 
 */
public class FileScanWebPlugin extends AbstractScanWebPlugin {

    private static final String PROPERTY_BASE = SCANWEB_PLUGINSIB_BASE_PROPERTY + "file.";

    /**
     * 
     */
    public FileScanWebPlugin() {
        super();
    }

    /**
     * @param propertyKeyBase
     * @param properties
     */
    public FileScanWebPlugin(String propertyKeyBase, Properties properties) {
        super(propertyKeyBase, properties);
    }

    /**
     * @param propertyKeyBase
     */
    public FileScanWebPlugin(String propertyKeyBase) {
        super(propertyKeyBase);
    }

    public boolean isDebug() {
        return "true".equals(getProperty(PROPERTY_BASE + "debug"));
    }

    @Override
    public String getName(Locale locale) {
        return "Fitxer ScanWeb";
    }

    @Override
    public boolean filter(HttpServletRequest request, ScanWebRequest config) {
        return super.filter(request, config);
    }

    @Override
    public String startScanWebTransaction(String absolutePluginRequestPath, String relativePluginRequestPath,
            HttpServletRequest request, ScanWebRequest scanWebRequest) throws Exception {

        ScanWebStatus status = putScanWebRequest(scanWebRequest,
                System.currentTimeMillis() + DEFAULT_TIME_BY_TRANSACTION);
        status.setStatus(ScanWebStatus.STATUS_IN_PROGRESS);

        return relativePluginRequestPath + "/" + UPLOAD_FILE_PAGE;
    }

    @Override
    public Set<String> getSupportedScanTypes() {
        final Set<String> SUPPORTED_SCAN_TYPES = Collections
                .unmodifiableSet(new HashSet<String>(Arrays.asList(ScanWebDocument.SCANTYPE_MIME_PDF)));
        return SUPPORTED_SCAN_TYPES;
    }

    @Override
    public Set<String> getSupportedFlagsByScanType(String scanType) {
        if (ScanWebDocument.SCANTYPE_MIME_PDF.equals(scanType)) {

            final Set<String> SUPPORTED_FLAGS = Collections
                    .unmodifiableSet(new HashSet<String>(Arrays.asList(ScanWebDocument.FLAG_PLAIN)));

            return SUPPORTED_FLAGS;
        }
        return null;
    }

    protected static final Set<ScanWebMode> SUPPORTED_MODES = Collections.unmodifiableSet(
            new HashSet<ScanWebMode>(Arrays.asList(ScanWebMode.ASYNCHRONOUS, ScanWebMode.SYNCHRONOUS)));

    @Override
    public Set<ScanWebMode> getSupportedScanWebModes() {
        return SUPPORTED_MODES;
    }

    @Override
    public String getResourceBundleName() {
        return "filescanweb";
    }

    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // ------------------- REQUEST GET-POST
    // ---------------------------------------
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------

    /**
     * 
     */
    @Override
    protected void requestGETPOST(String absolutePluginRequestPath, String relativePluginRequestPath, String scanWebID,
            String query, HttpServletRequest request, HttpServletResponse response, boolean isGet) {

        if (!absolutePluginRequestPath.endsWith("/")) {
            absolutePluginRequestPath = absolutePluginRequestPath + "/";
        }

        if (!relativePluginRequestPath.endsWith("/")) {
            relativePluginRequestPath = relativePluginRequestPath + "/";
        }

        ScanWebRequest scanWebRequest = getScanWebRequest(scanWebID);
        ScanWebResult scanWebResult = getScanWebResult(scanWebID);

        log.info(" XYZ ZZZ FILESCANEWEB query=" + query);

        if (scanWebRequest == null || scanWebResult == null) {

            log.error("scanWebRequest o  scanWebResult val null !!!");
            log.error("scanWebRequest: " + scanWebRequest);
            log.error("scanWebResult:  " + scanWebResult);

            String titol = (isGet ? "GET" : "POST") + " " + getName(new Locale("ca")) + " PETICIO HA CADUCAT";

            requestTimeOutError(absolutePluginRequestPath, relativePluginRequestPath, query, String.valueOf(scanWebID),
                    request, response, titol);

        } else {

            Locale languageUI = new Locale(scanWebRequest.getLanguageUI());

            if (query.startsWith(UPLOAD_FILE_PAGE)) {

                if (isGet) {

                    PrintWriter out = generateHeader(request, response, absolutePluginRequestPath,
                            relativePluginRequestPath, languageUI);
                    uploadFileGET(relativePluginRequestPath, query, scanWebID, scanWebRequest, scanWebResult, out,
                            languageUI);

                    generateFooter(out);
                } else {
                    uploadFilePOST(relativePluginRequestPath, request, response, scanWebRequest, scanWebResult,
                            languageUI);
                }

            } else if (query.startsWith("img")) {

                retornarRecursLocal(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request,
                        response, languageUI);

            } else if (query.startsWith(CANCEL_PAGE)) {

                cancel(request, response, scanWebRequest, scanWebResult, languageUI);

            } else {

                super.requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request,
                        response, languageUI, scanWebRequest, scanWebResult, isGet);
            }

        }

    }

    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // ------------------- CANCEL BUTTON ----------------------
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------

    protected static final String CANCEL_PAGE = "cancel";

    protected void cancel(HttpServletRequest request, HttpServletResponse response, ScanWebRequest scanWebRequest,
            ScanWebResult scanWebResult, Locale languageUI) {

        scanWebResult.getStatus().setStatus(ScanWebStatus.STATUS_CANCELLED);

        if (ScanWebMode.ASYNCHRONOUS.equals(scanWebRequest.getMode())) {
            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");

            PrintWriter out;
            try {
                out = response.getWriter();
            } catch (IOException e2) {
                log.error(e2.getMessage(), e2);
                return;
            }

            out.println("<html>\n");
            out.println("<body>\n");
            out.println("<table border=0 width=\"100%\" height=\"300px\">\n");
            out.println("<tr><td align=center>\n");
            out.println("<p><h2>" + getTraduccio("cancelat", languageUI) + "</h2><p>\n");
            out.println("</td></tr>\n");
            out.println("</table>\n");
            out.println("</body>\n");
            out.println("</html>\n");

            out.flush();
        } else {
            final String url;
            url = scanWebRequest.getUrlFinal();
            sendRedirect(response, url);
        }

    }

    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // ------------------ U P L O A D C E R T I F I C A T E -------------------
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------

    private static final String UPLOAD_FILE_PAGE = "uploadFile";

    private void uploadFileGET(String pluginRequestPath, String query, String scanWebID, ScanWebRequest scanWebRequest,
            ScanWebResult scanWebResult, PrintWriter out, Locale locale) {

        out.println("<table border=0 width=\"100%\">");

        StringBuffer str = getMessagesInHtml(scanWebID);

        if (str.length() != 0) {
            out.println("<tr><td align=center>\n");
            out.println(str.toString());
            out.println("</td></tr>\n");
        }

        out.println("<tr><td align=center>");

        out.println("<h3>" + getTraduccio("selectfile", locale) + "</h3><br/>");

        out.println("<form action=\"" + pluginRequestPath + UPLOAD_FILE_PAGE
                + "\" method=\"post\" enctype=\"multipart/form-data\">");
        out.println("<table border=0>");
        out.println("<tr>");
        out.println("<td>" + getTraduccio("selectfile", locale) + ":</td>");
        out.println("<td><input type=\"file\" accept=\".pdf\" name=\"fitxer\" /></td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("<br />");

        out.println(
                "<button class=\"btn btn-primary\" type=\"submit\">" + getTraduccio("acceptar", locale) + "</button>");

        out.println("&nbsp;&nbsp;");

        out.println("<button class=\"btn\" type=\"button\"  onclick=\"location.href='" + pluginRequestPath + CANCEL_PAGE
                + "'\" >" + getTraduccio("cancelar", locale) + "</button>");

        out.println("</form>");

        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");

        clearMessages(scanWebID);
    }

    protected StringBuffer getMessagesInHtml(String scanWebID) {
        StringBuffer str = new StringBuffer("");
        Map<String, List<String>> messages = getMessages(scanWebID);
        if (messages != null && messages.size() != 0) {

            for (String level : messages.keySet()) {

                List<String> avisos = messages.get(level);

                String tipus;

                if (level.equals(ERROR)) {
                    tipus = "alert-error";
                } else if (level.equals(WARN)) {
                    tipus = "alert-warning";
                } else if (level.equals(SUCCESS)) {
                    tipus = "alert-success";
                } else if (level.equals(INFO)) {
                    tipus = "alert-info";
                } else {
                    tipus = "";
                }

                for (String av : avisos) {
                    str.append("<div class=\"alert ").append(tipus).append("alert-error\">\n");
                    str.append(av).append("\n");
                    str.append("</div>\n");
                }

            }
        }
        return str;
    }

    private void uploadFilePOST(String pluginRequestPath, HttpServletRequest request, HttpServletResponse response,
            ScanWebRequest scanWebRequest, ScanWebResult scanWebResult, Locale locale) {

        log.info("XYZ ZZZ Entra uploadCertificatePOST");

        final String scanWebID = scanWebRequest.getScanWebID();

        Properties params = new Properties();
        Map<String, FileItem> uploadedFiles = readFilesFromRequest(request, response, params);

        // No s'ha pujat fitxers !!!!
        FileItem uf = uploadedFiles.get("fitxer");
        if (uf == null || uf.getSize() == 0) {

            log.info("XYZ ZZZ uploadCertificatePOST:: Error no hi ha fitxers pujats");

            // error.noseleccionatfitxer=No ha seleccionado ningún fitxer. Vuelva a
            // intentarlo

            saveMessageError(scanWebID, getTraduccio("error.noseleccionatfitxer", locale));
            sendRedirect(response, pluginRequestPath + UPLOAD_FILE_PAGE);
            return;
        }

        // XYZ ZZZ TODO VALIDAR SI EL FITXER PUJAT ES del tipus requerit
        // scanWebRequest.getScanType() == uf.getContentType()

        String scannedFileFormat = null;
        if ("application/pdf".equals(uf.getContentType())) {
            scannedFileFormat = ScanWebDocument.SCANTYPE_MIME_PDF;
        } else if ("image/gif".equals(uf.getContentType())) {
            scannedFileFormat = ScanWebDocument.SCANTYPE_MIME_GIF;
        } else if ("image/jpeg".equals(uf.getContentType())) {
            scannedFileFormat = ScanWebDocument.SCANTYPE_MIME_JPG;
        } else if ("image/png".equals(uf.getContentType())) {
            scannedFileFormat = ScanWebDocument.SCANTYPE_MIME_PNG;
        } else if ("image/tiff".equals(uf.getContentType())) {
            scannedFileFormat = ScanWebDocument.SCANTYPE_MIME_TIFF;
        }

        if (scannedFileFormat == null || !scannedFileFormat.equals(scanWebRequest.getScanType())) {
            // error.tipusincorrecte=Ha seleccionado un fichero de tipo {0} y se requiere
            // que sea de tipo {1}.
            saveMessageError(scanWebID, getTraduccio("error.tipusincorrecte", locale, uf.getContentType(),
                    scanWebRequest.getScanType() + "(SCANFORMAT:" + scannedFileFormat + ")"));
            sendRedirect(response, pluginRequestPath + UPLOAD_FILE_PAGE);
            return;
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            org.fundaciobit.pluginsib.core.utils.FileUtils.copy(uf.getInputStream(), baos);

            ScanWebPlainFile scannedPlainFile = new ScanWebPlainFile(uf.getName(), uf.getContentType(),
                    baos.toByteArray());

            String transactionName = scanWebRequest.getTransactionName();

            ScanWebSignedFile scannedSignedFile = null;
            Date scanDate = new Date();
            Integer pixelType = null;
            Integer pppResolution = null;
            Boolean ocr = null;
            Boolean duplex = null;
            String paperSize = null;
            String documentLanguage = null;
            String documentType = null;
            List<Metadata> additionalMetadatas = null;

            // TODO XYZ ZZZ Fer que FileInfoScan obtengui més info
            ScanWebDocument doc = new ScanWebDocument(transactionName, scannedPlainFile, scannedSignedFile, scanDate,
                    pixelType, pppResolution, scannedFileFormat, ocr, duplex, paperSize, documentLanguage, documentType,
                    additionalMetadatas);

            scanWebResult.getScannedDocuments().add(doc);
            scanWebResult.getStatus().setStatus(ScanWebStatus.STATUS_FINAL_OK);

            

            // final String url;
            // url = swc.getUrlFinal();
            // log.info("XYZ ZZZ Entra uploadCertificatePOST: redicreccionam a " + url);
            // sendRedirect(response, url);

            log.debug("Entra dins FINAL_PAGE(...");

            if (ScanWebMode.ASYNCHRONOUS.equals(scanWebRequest.getMode())) {
                response.setContentType("text/html");
                response.setCharacterEncoding("utf-8");

                PrintWriter out;
                try {
                    out = response.getWriter();
                } catch (IOException e2) {
                    log.error(e2.getMessage(), e2);
                    return;
                }

                out.println("<html>\n");
                out.println("<body>\n");
                out.println("<table border=0 width=\"100%\" height=\"300px\">\n");
                out.println("<tr><td align=center>\n");
                out.println("<p><h2>" + getTraduccio("final", locale) + "</h2><p>\n");
                out.println("</td></tr>\n");
                out.println("</table>\n");
                out.println("</body>\n");
                out.println("</html>\n");

                out.flush();

                return;
            } else {
                try {
                    response.sendRedirect(scanWebRequest.getUrlFinal());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }

        } catch (Exception e) {
            // XYZ ZZZ
            String msg = getTraduccio("error.contrasenyaincorrecta", locale) + ": " + e.getMessage();
            log.error(msg, e);
            saveMessageError(scanWebID, msg);
            sendRedirect(response, pluginRequestPath + "/" + UPLOAD_FILE_PAGE);
            return;
        }

    }

    // ---------------------------------------------------------------------------
    // ---------------------------------------------------------------------------
    // ------------------- UPLOAD FILES UTILS
    // ------------------------------------
    // ---------------------------------------------------------------------------
    // ---------------------------------------------------------------------------

    /**
     * 
     * @param request
     * @param response
     * @param params   Output param. retorna els parameters NO File de la request.
     * @return null when error then you must call to "return;"
     */
    protected Map<String, FileItem> readFilesFromRequest(HttpServletRequest request, HttpServletResponse response,
            Properties params) {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        try {

            if (!isMultipart) {
                throw new Exception("Form is not Multipart !!!!!!!");
            }
            DiskFileItemFactory factory = new DiskFileItemFactory();

            File temp = getTempDir();
            factory.setRepository(temp);

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);

            // Parse the request to get file items.
            @SuppressWarnings("unchecked")
            List<FileItem> fileItems = upload.parseRequest(request);

            Map<String, FileItem> mapFile = new HashMap<String, FileItem>();

            // Process the uploaded file items
            for (FileItem fi : fileItems) {

                if (fi.isFormField()) {

                    if (params != null) {
                        String fieldname = fi.getFieldName();
                        String fieldvalue = fi.getString();
                        params.put(fieldname, fieldvalue);
                    }

                } else {
                    String fieldName = fi.getFieldName();
                    if (log.isDebugEnabled()) {
                        log.debug("Uploaded File:  PARAM = " + fieldName + " | FILENAME: " + fi.getName());
                    }

                    mapFile.put(fieldName, fi);

                }
            }

            return mapFile;

        } catch (Exception e) {
            String msg = e.getMessage();
            log.error(msg, e);
            // No emprar ni 404 ni 403
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg); // bad
                // request
            } catch (Exception ee) {
                log.error(ee.getMessage(), ee);
            }
            return null; // = ERROR
        }

    }

    @Override
    public boolean isMassiveScanAllowed() {
        return false;
    }

    @Override
    public ScanWebPlainFile getSeparatorForMassiveScan(String languageUI) throws Exception {
        return null;
    };

}