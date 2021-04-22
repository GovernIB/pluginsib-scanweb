package org.fundaciobit.pluginsib.scanweb.iecisa;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.fundaciobit.pluginsib.core.utils.Metadata;
import org.fundaciobit.pluginsib.scanweb.api.AbstractScanWebPlugin;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebDocument;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebMode;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebPlainFile;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebRequest;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebResult;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebSignedFile;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebStatus;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 * 
 * @author anadal
 * 
 */
public class IECISAScanWebPlugin extends AbstractScanWebPlugin {

    private static final String PROPERTY_BASE = SCANWEB_PLUGINSIB_BASE_PROPERTY + "iecisa.";

    public static final int HEIGHT = 350;

    /**
     * 
     */
    public IECISAScanWebPlugin() {
        super();
    }

    /**
     * @param propertyKeyBase
     * @param properties
     */
    public IECISAScanWebPlugin(String propertyKeyBase, Properties properties) {
        super(propertyKeyBase, properties);
    }

    /**
     * @param propertyKeyBase
     */
    public IECISAScanWebPlugin(String propertyKeyBase) {
        super(propertyKeyBase);
    }

    public boolean isDebug() {
        return "true".equals(getProperty(PROPERTY_BASE + "debug"));
    }

    public boolean forceJNLP() {
        return "true".equals(getProperty(PROPERTY_BASE + "forcejnlp"));
    }

    public boolean closeWindowWhenFinish() {
        return "true".equals(getProperty(PROPERTY_BASE + "closewindowwhenfinish"));
    }

    @Override
    public String getName(Locale locale) {
        return "JNLP ScanWeb";
    }

    @Override
    public boolean filter(HttpServletRequest request, ScanWebRequest scanWebRequest) {
        return super.filter(request, scanWebRequest);
    }

    @Override
    public String startScanWebTransaction(String absolutePluginRequestPath, String relativePluginRequestPath,
            HttpServletRequest request, ScanWebRequest scanWebRequest) throws Exception {

        ScanWebStatus status = putScanWebRequest(scanWebRequest,
                System.currentTimeMillis() + DEFAULT_TIME_BY_TRANSACTION);
        status.setStatus(ScanWebStatus.STATUS_IN_PROGRESS);

        return relativePluginRequestPath + "/" + INDEX;
    }

    final Set<String> SUPPORTED_SCAN_TYPES = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(ScanWebDocument.SCANTYPE_MIME_PDF,
                    ScanWebDocument.SCANTYPE_MIME_TIFF, ScanWebDocument.SCANTYPE_MIME_JPG,
                    ScanWebDocument.SCANTYPE_MIME_PNG, ScanWebDocument.SCANTYPE_MIME_GIF)));

    @Override
    public Set<String> getSupportedScanTypes() {

        return SUPPORTED_SCAN_TYPES;
    }

    protected static final Set<String> SUPPORTED_FLAG_1 = Collections
            .unmodifiableSet(new HashSet<String>(Arrays.asList(ScanWebDocument.FLAG_PLAIN)));

    protected static final List<Set<String>> SUPPORTED_FLAGS = Collections
            .unmodifiableList(new ArrayList<Set<String>>(Arrays.asList(SUPPORTED_FLAG_1)));

    @Override
    public Set<String> getSupportedFlagsByScanType(String scanType) {
        if (SUPPORTED_SCAN_TYPES.contains(scanType)) {

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
        return "iecisascanweb";
    }

    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // ------------------- REQUEST GET-POST --------------------------------------
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------

    /**
     * 
     */
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

        if (scanWebRequest == null || scanWebResult == null) {
            String titol = (isGet ? "GET" : "POST") + " " + getName(new Locale("ca")) + " PETICIO HA CADUCAT";

            requestTimeOutError(absolutePluginRequestPath, relativePluginRequestPath, query, String.valueOf(scanWebID),
                    request, response, titol);

        } else {

            Locale languageUI = new Locale(scanWebRequest.getLanguageUI());

            if (query.startsWith(ISFINISHED_PAGE)) {
                isFinishedRequest(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request,
                        response, scanWebRequest, scanWebResult, languageUI);
            } else if (query.startsWith(INDEX)) {

                indexPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request, response,
                        scanWebRequest, scanWebResult, languageUI);

            } else if (query.startsWith(APPLET) || query.startsWith("img")) {

                retornarRecursLocal(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request,
                        response, languageUI);

            } else if (query.startsWith(JNLP)) {

                jnlpPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request, response,
                        languageUI);

            } else if (query.startsWith(UPLOAD_SCAN_FILE_PAGE)) {

                uploadPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request, response,
                        scanWebRequest, scanWebResult, languageUI);
            } else if (query.startsWith(FINALPAGE)) {

                finalPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request, response,
                        scanWebRequest, scanWebResult, languageUI);
            } else {

                super.requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request,
                        response, languageUI, scanWebRequest, scanWebResult, isGet);
            }

        }

    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------- INDEX ----------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    public static final String INDEX = "index.html";

    protected void indexPage(String absolutePluginRequestPath, String relativePluginRequestPath, String scanWebID,
            String query, HttpServletRequest request, HttpServletResponse response, ScanWebRequest scanWebRequest,
            ScanWebResult scanWebResults, Locale languageUI) {

        boolean debug = isDebug();

        String browser = request.getHeader("user-agent");

        final boolean isIE = (browser != null)
                && (browser.toLowerCase().indexOf("msie") != -1 || browser.indexOf("rv:11.0") != -1);

        if (debug) {
            log.info(" BROWSER= " + browser);
            log.info(" IS INTERNET EXPLORER = " + isIE);
        }

        PrintWriter out;
        out = generateHeader(request, response, absolutePluginRequestPath, relativePluginRequestPath, languageUI);

        String tstyle = debug ? "border: 2px solid red" : "";

        out.println("  <table style=\"min-height:200px;width:100%;height:100%;" + tstyle + "\">");

        // ---------------- FILA DE INFORMACIO DE FITXERS ESCANEJATS

        out.println("  <tr valign=\"middle\">");
        out.println("    <td align=\"center\">");
        // out.println(" <h3 style=\"padding:5px\">" + getTraduccio("llistatescanejats",
        // languageUI) + "</h3>");

        out.println("    <table style=\"border: 2px solid black;\">");
        out.println("     <tr><td>");
        out.println("      <div id=\"escanejats\" style=\"width:400px;\">");
        out.println("        <img alt=\"Esperi\" style=\"vertical-align:middle;z-index:200\" src=\""
                + absolutePluginRequestPath + WEBRESOURCE + "/img/ajax-loader2.gif" + "\">");
        out.println("        &nbsp;&nbsp;<i>" + getTraduccio("esperantservidor", languageUI) + "</i>");
        out.println("      </div>");
        out.println("     </td>");
        if (scanWebRequest.getMode() == ScanWebMode.SYNCHRONOUS) {
            out.println("<td>");
            // out.println("<br/><input type=\"button\" class=\"btn btn-success\" value=\""
            // +
            // getTraduccio("final", languageUI) + "\" onclick=\"finalScanProcess()\" />");
            out.println("<button class=\"btn btn-success\" onclick=\"finalScanProcess()\">"
                    + getTraduccio("final", languageUI) + "</button>");
            out.println("</td>");
        }
        out.println("     </tr></table>");

        out.println("      <br/>");
        // out.println(" <input type=\"button\" class=\"btn btn-primary\"
        // onclick=\"gotoCancel()\" value=\""
        // + getTraduccio("cancel", locale) + "\">");
        out.println("    </td>");
        out.println("  </tr>");
        out.println("  <tr valign=\"middle\">");
        out.println("    <td align=\"center\">");

        // ------------------ APPLET O BOTO DE CARREGA D'APPLET

        String dstyle = debug ? "border-style:double double double double;" : "";
        out.println("<div style=\"" + dstyle + "\">");
        out.println("<center>");

        boolean forceJNLP = forceJNLP();

        if (forceJNLP || !isIE) { // JNLP

            out.println("<script>\n\n" + "  function downloadJNLP() {\n" + "     location.href=\""
                    + relativePluginRequestPath + JNLP + "\";\n" + "     ocultar('botojnlp');\n"
                    + "     mostrar('missatgejnlp');\n" + "  }\n" + "\n\n" + " function mostrar(id) {\n"
                    + "    document.getElementById(id).style.display = 'block';\n" + "};\n" + "\n"
                    + " function ocultar(id){\n" + "   document.getElementById(id).style.display = 'none';\n" + " };\n"
                    + "\n" + "</script>");

            // + " document.write('<br/><br/><input type=\"button\" value=\"" +
            // getTraduccio("pitja", languageUI) + "\" onclick=\"downloadJNLP()\" />');\n"
            out.println("  <div id=\"missatgejnlp\" style=\"display: none;\" >");
            out.println("    <br/><br/><h4> S´està descarregant un arxiu jnlp.");
            out.println("     L´ha d´executar per obrir l´aplicació d´escaneig ... </h4><br/>");
            out.println("  </div>\n");

            out.println("  <div id=\"botojnlp\" >");
            out.println("    <input type=\"button\" class=\"btn btn-primary\" value=\""
                    + getTraduccio("pitja", languageUI) + "\" onclick=\"downloadJNLP();\" /><br/>");
            // + " setTimeout(downloadJNLP, 1000);\n" // directament obrim el JNLP
            out.println("  </div>");

        } else {
            // ----------- APPLET --------------------------
            out.println("<script src=\"https://www.java.com/js/deployJava.js\"></script>\n");

            out.println("<script>\n\n" + "   var attributes = {\n" + "    id:'iecisa_scan',\n"
                    + "    code:'es.ieci.tecdoc.fwktd.applets.scan.applet.IdocApplet',\n" + "    archive:'"
                    + absolutePluginRequestPath + "applet/plugin-scanweb-iecisascanweb-applet.jar',\n" + "    width: "
                    + getWidth() + ",\n" + "    height: " + HEIGHT + "\n" + "   };\n" + "   var parameters = {\n"
                    + "    servlet:'" + absolutePluginRequestPath + UPLOAD_SCAN_FILE_PAGE + "',\n"
                    + "    fileFormName:'" + UPLOAD_SCANNED_FILE_PARAMETER + "'\n" + "   } ;\n"
                    + "   deployJava.runApplet(attributes, parameters, '1.6');");
            out.println("</script>");
        }

        out.println("</center></div>");

        // +
        // " var isOpera = (!!window.opr && !!opr.addons) || !!window.opera ||
        // navigator.userAgent.indexOf(' OPR/') >= 0;\n"
        // + " // Firefox 1.0+\n"
        // + " var isFirefox = typeof InstallTrigger !== 'undefined';\n"
        // + " // At least Safari 3+: \"[object HTMLElementConstructor]\"\n"
        // +
        // " var isSafari =
        // Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') >
        // 0;\n"
        // + " // Internet Explorer 6-11\n"
        // + " var isIE = false || !!document.documentMode;\n"
        // + " // Edge 20+\n"
        // + " var isEdge = !isIE && !!window.StyleMedia;\n"
        // + " // Chrome 1+\n"
        // + " var isChrome = !!window.chrome && !!window.chrome.webstore;\n"
        // + " // Blink engine detection\n"
        // + " var isBlink = (isChrome || isOpera) && !!window.CSS;\n"
        // + "\n"
        // + " var home;\n"
        // +
        // " home = window.location.protocol + '//' + window.location.hostname + ':' +
        // window.location.port + '"
        // + context + "';\n"
        // + "\n"
        // + " function escanejarAmbFinestra() {\n"
        // + " var scan; \n"
        // + " scan = document.getElementById('iecisa_scan');\n"
        // + " var result;\n"
        // + " result = scan.showInWindow();\n"
        // + " if (result) {\n"
        // + " alert(\"" + getTraduccio("error.nofinestra", languageUI)+ "\" +
        // result);\n"
        // + " } else {\n"
        // + " // OK\n"
        // + " }\n"
        // + " }\n"

        // + "\n\n\n"
        // + " alert('IS CROME? ' + isChrome);"
        // + " if (!isIE) {\n"
        // + " document.write('<input type=\"button\" class=\"btn btn-primary\"
        // value=\"" +
        // getTraduccio("pitja", languageUI) +
        // "\" onclick=\"escanejarAmbFinestra();\" /><br/>');\n"
        // + " }\n"
        // + "\n\n"
        //
        // + " if (!isIE) {\n"
        //
        // + " }\n");

        if ((scanWebRequest.getMode() == ScanWebMode.SYNCHRONOUS)) {
            out.println("<script>\n\n");
            out.println("  function finalScanProcess() {");
            out.println("    if (document.getElementById(\"escanejats\").innerHTML.indexOf(\"ajax\") !=-1) {");
            out.println("      if (!confirm('" + getTraduccio("noenviats", languageUI) + "')) {");
            out.println("        return;");
            out.println("      };");
            out.println("    };");
            out.println("    location.href=\"" + relativePluginRequestPath + FINALPAGE + "\";");
            out.println("  }\n");
            out.println("</script>");
        }

        out.println("  </td></tr>");
        out.println("</table>");

        out.println("<script type=\"text/javascript\">");

        out.println();
        out.println("  var myTimer;");
        out.println("  myTimer = setInterval(function () {closeWhenSign()}, 20000);");
        out.println();
        out.println("  function closeWhenSign() {");
        out.println("    var request;");
        out.println("    if(window.XMLHttpRequest) {");
        out.println("        request = new XMLHttpRequest();");
        out.println("    } else {");
        out.println("        request = new ActiveXObject(\"Microsoft.XMLHTTP\");");
        out.println("    }");
        out.println("    request.open('GET', '" + absolutePluginRequestPath + ISFINISHED_PAGE + "', false);");
        out.println("    request.send();");
        out.println();
        out.println("    if ((request.status + '') == '" + HttpServletResponse.SC_OK + "') {");
        out.println("      clearTimeout(myTimer);");
        out.println("      myTimer = setInterval(function () {closeWhenSign()}, 4000);");
        out.println(
                "      document.getElementById(\"escanejats\").innerHTML = 'Documents pujats:' + request.responseText;");
        out.println("    } else if ((request.status + '') == '" + HttpServletResponse.SC_REQUEST_TIMEOUT + "') {"); //
        out.println("      clearTimeout(myTimer);");
        out.println("      window.location.href = '" + scanWebRequest.getUrlFinal() + "';");
        out.println("    } else {");
        out.println("      clearTimeout(myTimer);");
        out.println("      myTimer = setInterval(function () {closeWhenSign()}, 4000);");
        out.println("    }");
        out.println("  }");
        out.println();
        out.println();
        out.println("</script>");

        generateFooter(out);

    }

    public int getWidth() {
        return 550;
    }

    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // ------------------------------ IS_FINISHED ------------------------------
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------

    protected static final String ISFINISHED_PAGE = "isfinished";

    protected void isFinishedRequest(String absolutePluginRequestPath, String relativePluginRequestPath,
            String scanWebID, String query, HttpServletRequest request, HttpServletResponse response,
            ScanWebRequest scanWebRequest, ScanWebResult scanWebResult, Locale languageUI) {

        List<ScanWebDocument> list = scanWebResult.getScannedDocuments();

        try {
            if (list.size() == 0) {
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            } else {
                // response.setStatus(HttpServletResponse.SC_NOT_FOUND);

                if (list.size() == 1) {
                    // "S'ha rebut <b>" + list.size() + "</b> fitxer"
                    response.getWriter()
                            .println(getTraduccio("rebut.1.fitxer", languageUI, String.valueOf(list.size())));
                } else {
                    // "S'han rebut <b>" + list.size() + "</b> fitxers"
                    response.getWriter()
                            .println(getTraduccio("rebut.n.fitxers", languageUI, String.valueOf(list.size())));
                }
                response.setStatus(HttpServletResponse.SC_OK);
            }

        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // ---------------------- RECURSOS LOCALS ----------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    public static final String APPLET = "applet/";

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // ---------------------- JNLP ----------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    public static final String JNLP = "jnlp/";

    protected void jnlpPage(String absolutePluginRequestPath, String relativePluginRequestPath, String scanWebID,
            String query, HttpServletRequest request, HttpServletResponse response, Locale languageUI) {

        String appletUrlBase = absolutePluginRequestPath + "applet/";

        log.info(" appletUrlBase = ]" + appletUrlBase + "[ ");

        String appletUrl = appletUrlBase + "plugin-scanweb-iecisascanweb-applet.jar";

        log.info(" appletUrl = ]" + appletUrl + "[ ");

        response.setContentType("application/x-java-jnlp-file");
        response.setHeader("Content-Disposition", "filename=\"ScanWebIECISA.jnlp\"");
        response.setCharacterEncoding("utf-8");

        PrintWriter out;
        try {
            out = response.getWriter();
        } catch (IOException e2) {
            log.error(e2.getMessage(), e2);
            return;
        }

        out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        out.println("<jnlp spec=\"1.0+\" codebase=\"" + appletUrl + "\" >");
        out.println("    <information>");
        out.println("        <title>ScanWeb Applet</title>");
        out.println("        <vendor>IECISA</vendor>");
        out.println("        <homepage href=\"http://www.fundaciobit.org/\" />");
        out.println("        <description>ScanWeb Applet</description>");
        // out.println(" <icon href=\"" + absolutePluginRequestPath +
        // "/img/portafib.ico" + "\" />");
        out.println("    </information>");
        out.println("    <security>");
        out.println("        <all-permissions/>");
        out.println("    </security>");
        out.println("    <resources>");
        out.println("        <j2se version=\"1.6+\" java-vm-args=\"-Xmx1024m\" />");
        out.println("        <jar href=\"" + appletUrl + "\" main=\"true\" />");
        // out.println(" <property name=\"isJNLP\" value=\"true\"/>");
        // out.println(" <property name=\"closeWhenUpload\" value=\"" +
        // closeWindowWhenFinish() + "\"/>");
        out.println("    </resources>");

        out.println("    <application-desc");
        out.println("      name=\"ScanWeb Applet\"");
        out.println("      main-class=\"es.ieci.tecdoc.fwktd.applets.scan.ui.MainFrame\" >");
        out.println("       <argument>servlet=" + absolutePluginRequestPath + UPLOAD_SCAN_FILE_PAGE + "</argument>");
        out.println("       <argument>fileFormName=" + UPLOAD_SCANNED_FILE_PARAMETER + "</argument>");
        out.println("       <argument>isJNLP=true</argument>");
        out.println("       <argument>closeWhenUpload=true</argument>");
        out.println("    </application-desc>");
        /*
         * out.println("    <applet-desc"); out.println("      documentBase=\"" +
         * appletUrlBase + "\"");
         * out.println("      name=\"ScanWeb Applet de IECISA\""); out.
         * println("      main-class=\"es.ieci.tecdoc.fwktd.applets.scan.applet.IdocApplet\""
         * ); out.println("      width=\"" + getWidth() + " \"");
         * out.println("      height=\"" + HEIGHT + "\">"); out.println();
         * 
         * // ---------------- GLOBALS ----------------
         * 
         * out.println("       <param name=\"servlet\" value=\"" +
         * absolutePluginRequestPath + UPLOAD_SCAN_FILE_PAGE + "\"/>");
         * out.println("       <param name=\"fileFormName\" value=\"" +
         * UPLOAD_SCANNED_FILE_PARAMETER + "\"/>");
         * 
         * 
         * 
         * out.println("   </applet-desc>");
         */
        out.println("</jnlp>");

        out.flush();

    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // --------------- FINAL PAGE (SINCRON MODE) -------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    public static final String FINALPAGE = "finalPage";

    protected void finalPage(String absolutePluginRequestPath, String relativePluginRequestPath, String scanWebID,
            String query, HttpServletRequest request, HttpServletResponse response, ScanWebRequest scanWebRequest,
            ScanWebResult scanWebResults, Locale languageUI) {

        if(isDebug()) {
          log.info("Entra dins FINAL_PAGE(...");
        }
        
        
        if (scanWebResults.getStatus().getStatus() == ScanWebStatus.STATUS_IN_PROGRESS) {
        
            if (scanWebResults.getScannedDocuments().size() == 0) {
                scanWebResults.getStatus().setStatus(ScanWebStatus.STATUS_FINAL_ERROR);
                scanWebResults.getStatus().setErrorMsg(getTraduccio("noenviatcapdoc", languageUI));
            } else {
                scanWebResults.getStatus().setStatus(ScanWebStatus.STATUS_FINAL_OK);
            }
        }


        try {
            response.sendRedirect(scanWebRequest.getUrlFinal());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // ---------------------- UPLOAD PAGE --------------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    public static final String UPLOAD_SCAN_FILE_PAGE = "upload/";

    public static final String UPLOAD_SCANNED_FILE_PARAMETER = "scannedfileparam";

    protected void uploadPage(String absolutePluginRequestPath, String relativePluginRequestPath, String scanWebID,
            String query, HttpServletRequest request, HttpServletResponse response, ScanWebRequest scanWebRequest,
            ScanWebResult scanWebResult, Locale languageUI) {

        if (isDebug()) {
          log.info("Entra dins uploadPage(...)");
        }

        Map<String, FileItem> map = super.readFilesFromRequest(request, response);

        if (map == null) {
            return;
        }

        FileItem fileItem = map.get(UPLOAD_SCANNED_FILE_PARAMETER);
        if (fileItem == null) {
            log.error(" No s'ha rebut cap fitxer amb paràmetre " + UPLOAD_SCANNED_FILE_PARAMETER);
            return;
        }

        byte[] data;
        try {
            data = IOUtils.toByteArray(fileItem.getInputStream());
        } catch (IOException e) {
            log.error(" No s'ha pogut llegir del request el fitxer amb paràmetre " + UPLOAD_SCANNED_FILE_PARAMETER);
            return;
        }

        String name = fileItem.getName();
        if (name != null) {
            name = FilenameUtils.getName(name);
        }

        final String scanTypeExpected = scanWebRequest.getScanType();

        String mime = null;
        String scannedFileFormat = null;;

        if (ScanWebDocument.SCANTYPE_MIME_PDF.equals(scanTypeExpected)) {

            if (!is_pdf(data)) {
                scanWebResult.getStatus().setStatus(ScanWebStatus.STATUS_FINAL_ERROR);
                String errorMsg = getTraduccio("error.format", new Locale(scanWebRequest.getLanguageUI()),
                        scanTypeExpected);
                log.error(errorMsg);
                scanWebResult.getStatus().setErrorMsg(errorMsg);
            }

            mime = "application/pdf";
            scannedFileFormat = ScanWebDocument.SCANTYPE_MIME_PDF;

        } else if (ScanWebDocument.SCANTYPE_MIME_JPG.equals(scanTypeExpected)
                || ScanWebDocument.SCANTYPE_MIME_PNG.equals(scanTypeExpected)
                || ScanWebDocument.SCANTYPE_MIME_PDF.equals(scanTypeExpected)) {

            String format = getImageFormat(data);
            if (format == null) {
                String errorMsg = "S'ha rebut un format d'imatge desconegut però es requeria " + scanTypeExpected;
                log.error(errorMsg);
                scanWebResult.getStatus().setStatus(ScanWebStatus.STATUS_FINAL_ERROR);
                scanWebResult.getStatus().setErrorMsg(errorMsg);
                
            } else {

                boolean errorFormat = false;
                if (ScanWebDocument.SCANTYPE_MIME_JPG.equals(scanTypeExpected)) {
                    if (!"JPEG".equalsIgnoreCase(format)) {
                        errorFormat = true;
                        
                    } else {
                        mime = "image/jpeg";
                        scannedFileFormat = ScanWebDocument.SCANTYPE_MIME_JPG;
                    }
                } else if (ScanWebDocument.SCANTYPE_MIME_PNG.equals(scanTypeExpected)) {
                    if (!"png".equalsIgnoreCase(format)) {
                        errorFormat = true;
                        
                    } else {
                        mime = "image/png";
                        scannedFileFormat = ScanWebDocument.SCANTYPE_MIME_PNG;
                    }
                } else if (ScanWebDocument.SCANTYPE_MIME_GIF.equals(scanTypeExpected)) {
                    if (!"gif".equalsIgnoreCase(format)) {
                        errorFormat = true;
                        
                    } else {
                        mime = "image/gif";
                        scannedFileFormat = ScanWebDocument.SCANTYPE_MIME_GIF;
                    }
                } else {
                    mime = "application/octet-stream";
                    
                }

                if (errorFormat) {
                    String errorMsg = "S'ha rebut un format d'imatge " + format + " però es requeria "
                            + scanTypeExpected;
                    log.error(errorMsg);
                    scanWebResult.getStatus().setStatus(ScanWebStatus.STATUS_FINAL_ERROR);
                    scanWebResult.getStatus().setErrorMsg(errorMsg);
                    
                }
            }

        } else if (ScanWebDocument.SCANTYPE_MIME_TIFF.equals(scanTypeExpected)) {
            // Suposam que és TIFF
            mime = "image/tiff";
            scannedFileFormat = ScanWebDocument.SCANTYPE_MIME_TIFF;
        } else {
            // Altre tipus ????
            String m = fileItem.getContentType();
            if (m == null) {
                m = "application/octet-stream";
            }
            mime = m;
            
        }

        ScanWebPlainFile scannedPlainFile = new ScanWebPlainFile(name, mime, data);

        ScanWebSignedFile scannedSignedFile = null;

        
        List<Metadata> additionalMetadatas = new ArrayList<Metadata>();
        //metadatas.add(new Metadata("FechaCaptura", date));
        additionalMetadatas.add(new Metadata("VersionNTI", "http://administracionelectronica.gob.es/ENI/XSD/v1.0/documento-e"));

        
        String transactionName = scanWebRequest.getTransactionName();
        
        Date scanDate = new Date(System.currentTimeMillis());
        Integer pixelType = null;
        Integer pppResolution = null;        
        
        Boolean ocr = null;
        Boolean duplex = null;
        String paperSize = null;
        String documentLanguage = null;
        String documentType = null;
       
        ScanWebDocument scannedDoc = new ScanWebDocument(transactionName, scannedPlainFile,
                scannedSignedFile, scanDate, pixelType, pppResolution,
                scannedFileFormat, ocr, duplex,  paperSize,  documentLanguage,
                 documentType,  additionalMetadatas);
        
        
        scanWebResult.getScannedDocuments().add(scannedDoc);
    }

    protected String getImageFormat(byte[] data) {

        ImageInputStream iis = null;
        try {
            iis = ImageIO.createImageInputStream(data);

            // get all currently registered readers that recognize the image format

            Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);

            if (!iter.hasNext()) {
                return null;
            }

            // get the first reader
            ImageReader reader = iter.next();

            return reader.getFormatName();
        } catch (Exception e) {
            log.error("Error descobrint format de la imatge:" + e.getMessage());
            return null;
        } finally {
            // close stream
            if (iis != null) {
                try {
                    iis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static final String username = "scanweb"; // configuracio

    /**
     * Test if the data in the given byte array represents a PDF file.
     */
    public static boolean is_pdf(byte[] data) {
        if (data != null && data.length > 4 && data[0] == 0x25 && // %
                data[1] == 0x50 && // P
                data[2] == 0x44 && // D
                data[3] == 0x46 && // F
                data[4] == 0x2D) { // -

            return true;

        }
        return false;
    }

    @Override
    public boolean isMassiveScanAllowed() {
        return false;
    }

    @Override
    public ScanWebPlainFile getSeparatorForMassiveScan(String languageUI) throws Exception {
        return null;
    }

}