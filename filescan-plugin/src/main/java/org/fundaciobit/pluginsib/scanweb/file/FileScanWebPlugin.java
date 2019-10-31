package org.fundaciobit.pluginsib.scanweb.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
import org.fundaciobit.plugins.scanweb.api.AbstractScanWebPlugin;
import org.fundaciobit.plugins.scanweb.api.ScanWebConfig;
import org.fundaciobit.plugins.scanweb.api.ScanWebMode;
import org.fundaciobit.plugins.scanweb.api.ScanWebStatus;
import org.fundaciobit.plugins.scanweb.api.ScannedPlainFile;
import org.fundaciobit.plugins.scanweb.api.ScannedDocument;

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
  public boolean filter(HttpServletRequest request, ScanWebConfig config) {
    return super.filter(request, config);
  }

  @Override
  public String startScanWebTransaction(String absolutePluginRequestPath,
      String relativePluginRequestPath, HttpServletRequest request, ScanWebConfig config)
      throws Exception {

    putTransaction(config);
    config.getStatus().setStatus(ScanWebStatus.STATUS_IN_PROGRESS);

    return relativePluginRequestPath + "/" + UPLOAD_FILE_PAGE;
  }

  protected static final List<String> SUPPORTED_SCAN_TYPES = Collections
      .unmodifiableList(new ArrayList<String>(Arrays.asList(SCANTYPE_PDF)));

  @Override
  public List<String> getSupportedScanTypes() {
    return SUPPORTED_SCAN_TYPES;
  }

  protected static final Set<String> SUPPORTED_FLAG_1 = Collections
      .unmodifiableSet(new HashSet<String>(Arrays.asList(FLAG_NON_SIGNED)));

  /*
   * protected static final Set<String> SUPPORTED_FLAG_2 = Collections .unmodifiableSet(new
   * HashSet<String>(Arrays.asList(FLAG_SIGNED)));
   */

  protected static final List<Set<String>> SUPPORTED_FLAGS = Collections
      .unmodifiableList(new ArrayList<Set<String>>(Arrays.asList(SUPPORTED_FLAG_1)));

  @Override
  public List<Set<String>> getSupportedFlagsByScanType(String scanType) {
    if (SCANTYPE_PDF.equals(scanType)) {
      return /* forceSign()? SUPPORTED_FLAGS_ONLYSIGN : */SUPPORTED_FLAGS;
    }
    return null;
  }

  protected static final Set<ScanWebMode> SUPPORTED_MODES = Collections
      .unmodifiableSet(new HashSet<ScanWebMode>(Arrays.asList(ScanWebMode.ASYNCHRONOUS,
          ScanWebMode.SYNCHRONOUS)));

  @Override
  public Set<ScanWebMode> getSupportedScanWebModes() {
    return SUPPORTED_MODES;
  }

  @Override
  public String getResourceBundleName() {
    return "filescanweb";
  }

  @Override
  public void requestGET(String absolutePluginRequestPath, String relativePluginRequestPath,
      String scanWebID, String query, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query,
        request, response, true);
  }

  @Override
  public void requestPOST(String absolutePluginRequestPath, String relativePluginRequestPath,
      String scanWebID, String query, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query,
        request, response, false);

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
  protected void requestGETPOST(String absolutePluginRequestPath,
      String relativePluginRequestPath, String scanWebID, String query,
      HttpServletRequest request, HttpServletResponse response, boolean isGet) {

    if (!absolutePluginRequestPath.endsWith("/")) {
      absolutePluginRequestPath = absolutePluginRequestPath + "/";
    }

    if (!relativePluginRequestPath.endsWith("/")) {
      relativePluginRequestPath = relativePluginRequestPath + "/";
    }

    ScanWebConfig fullInfo = getTransaction(scanWebID);

    log.info(" XYZ ZZZ FILESCANEWEB query=" + query);

    if (fullInfo == null) {
      String titol = (isGet ? "GET" : "POST") + " " + getName(new Locale("ca"))
          + " PETICIO HA CADUCAT";

      requestTimeOutError(absolutePluginRequestPath, relativePluginRequestPath, query,
          String.valueOf(scanWebID), request, response, titol);

    } else {

      Locale languageUI = new Locale(fullInfo.getLanguageUI());

      if (query.startsWith(UPLOAD_FILE_PAGE)) {

        if (isGet) {

          PrintWriter out = generateHeader(request, response, absolutePluginRequestPath,
              relativePluginRequestPath, languageUI);
          uploadFileGET(relativePluginRequestPath, query, fullInfo, out, languageUI);

          generateFooter(out);
        } else {
          uploadFilePOST(relativePluginRequestPath, request, response, fullInfo, languageUI);
        }

      } else if (query.startsWith("img")) {

        retornarRecursLocal(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
            query, request, response, languageUI);

      } else if (query.startsWith(CANCEL_PAGE)) {

        cancel(request, response, fullInfo, languageUI);

      } else {

        super.requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
            fullInfo, query, languageUI, request, response, isGet);
      }

    }

  }

  // ----------------------------------------------------------------------------
  // ----------------------------------------------------------------------------
  // ------------------- CANCEL BUTTON ----------------------
  // ----------------------------------------------------------------------------
  // ----------------------------------------------------------------------------

  protected static final String CANCEL_PAGE = "cancel";

  protected void cancel(HttpServletRequest request, HttpServletResponse response,
      ScanWebConfig fullInfo, Locale languageUI) {

    fullInfo.getStatus().setStatus(ScanWebStatus.STATUS_CANCELLED);

    if (ScanWebMode.ASYNCHRONOUS.equals(fullInfo.getMode())) {
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
      url = fullInfo.getUrlFinal();
      sendRedirect(response, url);
    }

  }

  // ----------------------------------------------------------------------------
  // ----------------------------------------------------------------------------
  // ------------------ U P L O A D C E R T I F I C A T E -------------------
  // ----------------------------------------------------------------------------
  // ----------------------------------------------------------------------------

  private static final String UPLOAD_FILE_PAGE = "uploadFile";

  private void uploadFileGET(String pluginRequestPath, String query, ScanWebConfig fullInfo,
      PrintWriter out, Locale locale) {

    out.println("<table border=0 width=\"100%\">");
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

    out.println("<button class=\"btn btn-primary\" type=\"submit\">"
        + getTraduccio("acceptar", locale) + "</button>");

    out.println("&nbsp;&nbsp;");

    out.println("<button class=\"btn\" type=\"button\"  onclick=\"location.href='"
        + pluginRequestPath + CANCEL_PAGE + "'\" >" + getTraduccio("cancelar", locale)
        + "</button>");

    out.println("</form>");

    out.println("</td>");
    out.println("</tr>");
    out.println("</table>");
  }

  private void uploadFilePOST(String pluginRequestPath, HttpServletRequest request,
      HttpServletResponse response, ScanWebConfig fullInfo, Locale locale) {

    log.info("XYZ ZZZ Entra uploadCertificatePOST");

    final String scanWebID = fullInfo.getScanWebID();

    Properties params = new Properties();
    Map<String, FileItem> uploadedFiles = readFilesFromRequest(request, response, params);

    // No s'ha pujat fitxers !!!!
    FileItem uf = uploadedFiles.get("fitxer");
    if (uf == null || uf.getSize() == 0) {

      log.info("XYZ ZZZ uploadCertificatePOST:: Error no hi ha fitxers pujats");

      saveMessageError(scanWebID, getTraduccio("error.noseleccionatp12", locale));
      sendRedirect(response, pluginRequestPath + "/" + UPLOAD_FILE_PAGE);
      return;
    }

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      org.fundaciobit.pluginsib.core.utils.FileUtils.copy(uf.getInputStream(), baos);

      ScannedPlainFile plainFile = new ScannedPlainFile(uf.getName(), uf.getContentType(),
          baos.toByteArray());

      ScannedDocument doc = new ScannedDocument();

      doc.setScanDate(new Date());
      doc.setScannedPlainFile(plainFile);

      ScanWebConfig swc = getScanWebConfig(scanWebID);
      swc.getScannedFiles().add(doc);
      swc.getStatus().setStatus(ScanWebStatus.STATUS_FINAL_OK);

      log.info("XYZ ZZZ uploadCertificatePOST:: FINAL OK");

      // final String url;
      // url = swc.getUrlFinal();
      // log.info("XYZ ZZZ Entra uploadCertificatePOST: redicreccionam a " + url);
      // sendRedirect(response, url);

      log.debug("Entra dins FINAL_PAGE(...");

      if (ScanWebMode.ASYNCHRONOUS.equals(fullInfo.getMode())) {
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
          response.sendRedirect(fullInfo.getUrlFinal());
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
   * @param params
   *          Output param. retorna els parameters NO File de la request.
   * @return null when error then you must call to "return;"
   */
  protected Map<String, FileItem> readFilesFromRequest(HttpServletRequest request,
      HttpServletResponse response, Properties params) {
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

  // ----------------------------------------------------------------------------
  // ----------------------------------------------------------------------------
  // ------------------- MISSATGES ---------------------------------------
  // ----------------------------------------------------------------------------
  // ----------------------------------------------------------------------------

  public static final String ERROR = "error";

  public static final String WARN = "warn";

  public static final String SUCCESS = "success";

  public static final String INFO = "info";

  private Map<String, Map<String, List<String>>> missatges = new HashMap<String, Map<String, List<String>>>();

  public void saveMessageInfo(String signatureID, String missatge) {
    addMessage(signatureID, INFO, missatge);
  }

  public void saveMessageWarning(String signatureID, String missatge) {
    addMessage(signatureID, WARN, missatge);

  }

  public void saveMessageSuccess(String signatureID, String missatge) {
    addMessage(signatureID, SUCCESS, missatge);
  }

  public void saveMessageError(String signatureID, String missatge) {
    addMessage(signatureID, ERROR, missatge);
  }

  public void addMessage(String signatureID, String type, String missatge) {

    Map<String, List<String>> missatgesBySignID = missatges.get(signatureID);

    if (missatgesBySignID == null) {
      missatgesBySignID = new HashMap<String, List<String>>();
      missatges.put(signatureID, missatgesBySignID);
    }

    List<String> missatgesTipus = missatgesBySignID.get(type);

    if (missatgesTipus == null) {
      missatgesTipus = new ArrayList<String>();
      missatgesBySignID.put(type, missatgesTipus);
    }

    missatgesTipus.add(missatge);

  }

  public void clearMessages(String signatureID) {
    missatges.remove(signatureID);
  }

  public Map<String, List<String>> getMessages(String signatureID) {
    return missatges.get(signatureID);
  };

}