package org.fundaciobit.pluginsib.scanweb.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.fundaciobit.pluginsib.core.utils.AbstractPluginProperties;

/**
 * 
 * @author anadal
 *
 */
public abstract class AbstractScanWebPlugin extends AbstractPluginProperties implements IScanWebPlugin {

    public static final long DEFAULT_TIME_BY_TRANSACTION = 30 * 60 * 1000; // 30 minuts per transacció per defecte

    /**
     *
     * @author anadal
     *
     */
    private class ScanWebTransaction {
        protected final ScanWebRequest scanWebConfig;

        protected final ScanWebResult scanWebResult = new ScanWebResult();

        protected final long expirationDate;

        public ScanWebTransaction(ScanWebRequest scanWebConfig, long expirationDate) {
            super();
            this.scanWebConfig = scanWebConfig;
            this.expirationDate = expirationDate;
        }

        public ScanWebResult getScanWebResult() {
            return scanWebResult;
        }

        public ScanWebRequest getScanWebRequest() {
            return scanWebConfig;
        }

        public long getExpirationDate() {
            return expirationDate;
        }

    }

    private Map<String, ScanWebTransaction> scanTransactions = new HashMap<String, ScanWebTransaction>();

    protected Logger log = Logger.getLogger(this.getClass());

    /**
     * 
     */
    public AbstractScanWebPlugin() {
        super();
    }

    /**
     * @param propertyKeyBase
     * @param properties
     */
    public AbstractScanWebPlugin(String propertyKeyBase, Properties properties) {
        super(propertyKeyBase, properties);
    }

    /**
     * @param propertyKeyBase
     */
    public AbstractScanWebPlugin(String propertyKeyBase) {
        super(propertyKeyBase);
    }

    @Override
    public boolean filter(HttpServletRequest request, ScanWebRequest config) {

        // (1) Modes d'Escaneig
        ScanWebMode mode = config.getMode();
        Set<ScanWebMode> modes = getSupportedScanWebModes();

        if (!modes.contains(mode)) {
            return false;
        }

        // (2) Comprovar Support de Tipus d'escaneig
        final String scanType = config.getScanType();
        Set<String> types = getSupportedScanTypes();
        if (!types.contains(scanType)) {
            return false;
        }

        // (3) Comprovar Support dels flags
        final String flag = config.getFlag();
       
        final Set<String> suported = getSupportedFlagsByScanType(scanType);
        for (String flagSuport : suported) {
            if (flag.equals(flagSuport)) {
                return true;
            }
        }

        return false;
    }

    // -----------------------------------------------------------------------
    // -----------------------------------------------------------------------
    // ------------------- REQUEST GET-POST ---------------------------------
    // -----------------------------------------------------------------------
    // -----------------------------------------------------------------------

    @Override
    public void endScanWebTransaction(String scanWebID, HttpServletRequest request) {
        scanTransactions.remove(scanWebID);
    }

    @Override
    public void cleanScannedFiles(String scanWebID, HttpServletRequest request) {

        ScanWebResult r = getScanWebResult(scanWebID);
        if (r != null) {
            r.getScannedDocuments().clear();
        }
    }

    protected ScanWebStatus putScanWebRequest(ScanWebRequest scanWebRequest, long expirationDate) {

        ScanWebTransaction swt;
        synchronized (scanTransactions) {

            if (scanTransactions.containsKey(scanWebRequest.getScanWebID())) {
                log.warn("S'esta registrant per segona vegada una transacció amb ID " + scanWebRequest.getScanWebID(),
                        new Exception());
            }

            swt = new ScanWebTransaction(scanWebRequest, expirationDate);

            scanTransactions.put(scanWebRequest.getScanWebID(), swt);

        }

        return swt.getScanWebResult().getStatus();
    }

    private ScanWebTransaction getScanWebTransaction(String transactionID) {

        synchronized (scanTransactions) {
            List<String> idsToDelete = new ArrayList<String>();

            Set<Entry<String, ScanWebTransaction>> entries = scanTransactions.entrySet();
            long current = System.currentTimeMillis();

            for (Entry<String, ScanWebTransaction> entry : entries) {
                if (current > entry.getValue().getExpirationDate()) {
                    idsToDelete.add(entry.getKey());
                }
            }

            for (String id : idsToDelete) {
                scanTransactions.remove(id);
            }

            return scanTransactions.get(transactionID);
        }
    }

    @Override
    public ScanWebRequest getScanWebRequest(String scanWebID) {
        ScanWebTransaction t = getScanWebTransaction(scanWebID);
        if (t == null) {
            return null;
        } else {
            return t.getScanWebRequest();
        }
    }

    @Override
    public ScanWebResult getScanWebResult(String scanWebID) {
        ScanWebTransaction t = getScanWebTransaction(scanWebID);
        if (t == null) {
            return null;
        } else {
            return t.getScanWebResult();
        }
    }

    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // ------------------- HTML UTILS BUTTON -------------------------------------
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------

    // TODO XYZ mogut a base

    protected void sendRedirect(HttpServletResponse response, String url) {
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected final PrintWriter generateHeader(HttpServletRequest request, HttpServletResponse response,
            String absolutePluginRequestPath, String relativePluginRequestPath, Locale languageUI) {

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html");
        PrintWriter out;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            return null;
        }

        String lang = languageUI.getLanguage();

        out.println("<!DOCTYPE html>");
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"" + lang + "\"  lang=\"" + lang + "\">");
        out.println("<head>");

        out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;\" charset=\"UTF-8\" >");

        out.println("<title>" + getName(languageUI) + "</title>");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");

        // Javascript i CSS externs
        getJavascriptCSS(request, absolutePluginRequestPath, relativePluginRequestPath, out, languageUI);

        out.println("</head>");
        out.println("<body>");

        return out;

    }

    protected final void generateFooter(PrintWriter out) {
        out.println("</body>");
        out.println("</html>");
    }

    protected void getJavascriptCSS(HttpServletRequest request, String absolutePluginRequestPath,
            String relativePluginRequestPath, PrintWriter out, Locale languageUI) {

        out.println("<script type=\"text/javascript\" src=\"" + relativePluginRequestPath + WEBRESOURCE
                + "/js/jquery.js\"></script>");
        out.println("<script type=\"text/javascript\" src=\"" + relativePluginRequestPath + WEBRESOURCE
                + "/js/bootstrap.js\"></script>");
        out.println("<link href=\"" + relativePluginRequestPath + WEBRESOURCE
                + "/css/bootstrap.css\" rel=\"stylesheet\" media=\"screen\">");

    }

//----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // ------------------- REQUEST GET-POST ---------------------------------------
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------

    // XYZ TODO Passat a pare web
    public static final String WEBRESOURCE = "webresource";

    @Override
    public void requestGET(String absolutePluginRequestPath, String relativePluginRequestPath, String scanWebID,
            String query, HttpServletRequest request, HttpServletResponse response) throws Exception {

        final boolean isGet = true;
        requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request, response,
                isGet);

    }

    @Override
    public void requestPOST(String absolutePluginRequestPath, String relativePluginRequestPath, String scanWebID,
            String query, HttpServletRequest request, HttpServletResponse response) throws Exception {
        final boolean isGet = false;
        requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request, response,
                isGet);
    }

    /**
    *
    */
    protected abstract void requestGETPOST(String absolutePluginRequestPath, String relativePluginRequestPath,
            String scanWebID, String query, HttpServletRequest request, HttpServletResponse response, boolean isGet);

    /**
     * 
     */
    protected void requestGETPOST(String absolutePluginRequestPath, String relativePluginRequestPath, String scanWebID,
            String query, HttpServletRequest request, HttpServletResponse response, Locale localeUI,
            ScanWebRequest scanWebRequest, ScanWebResult scanWebResult, boolean isGet) {

        if (query.startsWith(WEBRESOURCE)) {

            retornarRecursLocal(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request,
                    response, localeUI);

        } else {
            // XYZ Fer un missatges com toca
            String titol = (isGet ? "GET" : "POST") + " " + getName(new Locale("ca")) + " DESCONEGUT";
            requestNotFoundError(titol, absolutePluginRequestPath, relativePluginRequestPath, query,
                    String.valueOf(scanWebID), request, response, localeUI);
        }
    }

    // ---------------------------------------------------------------------------
    // ---------------------------------------------------------------------------
    // ------------------- UPLOAD FILES UTILS ------------------------------------
    // ---------------------------------------------------------------------------
    // ---------------------------------------------------------------------------

    // TODO XYZ Llegir de AbstractWebPlugin

    /**
     * 
     * @param request
     * @param response
     * @return null when error then you must call to "return;"
     */
    protected Map<String, FileItem> readFilesFromRequest(HttpServletRequest request, HttpServletResponse response) {
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
            List<FileItem> fileItems = upload.parseRequest(request);

            Map<String, FileItem> mapFile = new HashMap<String, FileItem>();

            // Process the uploaded file items
            for (FileItem fi : fileItems) {

                if (!fi.isFormField()) {
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

    public static File tempDir = null;

    protected synchronized File getTempDir() throws IOException {

        if (tempDir == null) {
            File temp = File.createTempFile("test", "test");

            tempDir = temp.getParentFile();

            if (!temp.delete()) {
                temp.deleteOnExit();
            }
        }

        return tempDir;

    }
    
    

    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
    // ------------------- MISSATGES D'ERROR, AVIS, INFO, ...    ------------------
    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------

    public static final String ERROR = "error";

    public static final String WARN = "warn";

    public static final String SUCCESS = "success";

    public static final String INFO = "info";

    private Map<String, Map<String, List<String>>> missatges = new HashMap<String, Map<String, List<String>>>();

    public void saveMessageInfo(String scanWebID, String missatge) {
        addMessage(scanWebID, INFO, missatge);
    }

    public void saveMessageWarning(String scanWebID, String missatge) {
        addMessage(scanWebID, WARN, missatge);

    }

    public void saveMessageSuccess(String scanWebID, String missatge) {
        addMessage(scanWebID, SUCCESS, missatge);
    }

    public void saveMessageError(String scanWebID, String missatge) {
        addMessage(scanWebID, ERROR, missatge);
    }

    public void addMessage(String scanWebID, String type, String missatge) {

        Map<String, List<String>> missatgesBySignID = missatges.get(scanWebID);

        if (missatgesBySignID == null) {
            missatgesBySignID = new HashMap<String, List<String>>();
            missatges.put(scanWebID, missatgesBySignID);
        }

        List<String> missatgesTipus = missatgesBySignID.get(type);

        if (missatgesTipus == null) {
            missatgesTipus = new ArrayList<String>();
            missatgesBySignID.put(type, missatgesTipus);
        }

        missatgesTipus.add(missatge);

    }

    public void clearMessages(String scanWebID) {
        missatges.remove(scanWebID);
    }

    public Map<String, List<String>> getMessages(String scanWebID) {
        return missatges.get(scanWebID);
    }
    

    // ---------------------------------------------------------------------------
    // ---------------------------------------------------------------------------
    // ------------------- GESTIO D'ERRORS ---------------------------------------
    // ---------------------------------------------------------------------------
    // ---------------------------------------------------------------------------

    // TODO XYZ mogut a base

    protected static final String ABSTRACT_SCAN_WEB_RES_BUNDLE = "scanwebapi";

    public void requestTimeOutError(String absolutePluginRequestPath, String relativePluginRequestPath, String query,
            String scanWebID, HttpServletRequest request, HttpServletResponse response, String titol) {
        String str = allRequestInfoToStr(request, titol, absolutePluginRequestPath, relativePluginRequestPath, query,
                scanWebID);

        // TODO Traduir
        // El procés de s'escaneig amb ID " + scanWebID + " ha caducat. Torni a
        // intentar-ho.\n" + str;
        Locale locale = request.getLocale();

        String msg = getTraduccio(ABSTRACT_SCAN_WEB_RES_BUNDLE, "timeout.error", locale, getName(locale));

        log.error(msg + "\n" + str);

        // No emprar ni 404 ni 403
        try {
            response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT, msg); // Timeout
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void requestNotFoundError(String titol, String absolutePluginRequestPath, String relativePluginRequestPath,
            String query, String ID, HttpServletRequest request, HttpServletResponse response, Locale locale) {

        // No emprar ni 404 ni 403
        int httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;
        // S'ha realitzat una petició al plugin [{0}] però no s'ha trobat cap mètode
        // per processar-la {1}

        String str = allRequestInfoToStr(request, titol, absolutePluginRequestPath, relativePluginRequestPath, query,
                ID);

        String msg = getTraduccio(ABSTRACT_SCAN_WEB_RES_BUNDLE, "notfound.error", locale, getName(locale), str);

        log.error(msg);

        try {
            response.sendError(httpStatusCode, msg); // bad request
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    // ---------------------------------------------------------
    // ------------------- I18N Utils ------------------------
    // ---------------------------------------------------------

    // TODO XYZ mogut a base
    public abstract String getResourceBundleName();

    public final String getTraduccio(String key, Locale locale, Object... params) {
        return getTraduccio(getResourceBundleName(), key, locale, params);
    }

    public final String getTraduccio(String resourceBundleName, String key, Locale locale, Object... params) {

        try {
            // TODO MILLORA: Map de resourcebundle per resourceBundleName i locale

            ResourceBundle rb = ResourceBundle.getBundle(resourceBundleName, locale, UTF8CONTROL);

            String msgbase = rb.getString(key);

            if (params != null && params.length != 0) {
                msgbase = MessageFormat.format(msgbase, params);
            }

            return msgbase;

        } catch (Exception mre) {
            log.error("No trob la traducció per '" + key + "'", new Exception());
            return key + "_" + locale.getLanguage().toUpperCase();
        }

    }

    protected UTF8Control UTF8CONTROL = new UTF8Control();

    public class UTF8Control extends ResourceBundle.Control {
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
                boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            // The below is a copy of the default implementation.
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    // Only this line is changed to make it to read properties files as
                    // UTF-8.
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }

    // ---------------------------------------------------------
    // ------------------- READ LOCAL RESOURCES ---------------
    // ---------------------------------------------------------

    // TODO XYZ mogut a base
    protected void retornarRecursLocal(String absolutePluginRequestPath, String relativePluginRequestPath,
            String scanWebID, String query, HttpServletRequest request, HttpServletResponse response,
            Locale languageUI) {
        byte[] contingut = null;
        String mime = getMimeType(query);
        query = query.replace('\\', '/');

        query = query.startsWith("/") ? query : ('/' + query);

        try {

            InputStream input = getClass().getResourceAsStream(query);

            if (input != null) {

                contingut = IOUtils.toByteArray(input);

                int pos = query.lastIndexOf('/');
                String resourcename = pos == -1 ? query : query.substring(pos + 1);

                OutputStream out = response.getOutputStream();

                response.setContentType(mime);
                response.setHeader("Content-Disposition", "inline; filename=\"" + resourcename + "\"");
                response.setContentLength(contingut.length);

                out.write(contingut);
                out.flush();

                return;
            }
        } catch (IOException e) {
            log.error("Error llegint recurs " + query, e);
        }

        // ERROR

        String titol = "No trob el recurs " + query;
        requestNotFoundError(titol, absolutePluginRequestPath, relativePluginRequestPath, query,
                String.valueOf(scanWebID), request, response, languageUI);
    }

    protected String getMimeType(String resourcename) {
        String mime = "application/octet-stream";
        if (resourcename != null && !"".equals(resourcename)) {
            String type = resourcename.substring(resourcename.lastIndexOf(".") + 1);
            if ("jar".equalsIgnoreCase(type)) {
                mime = "application/java-archive";
            } else if ("gif".equalsIgnoreCase(type)) {
                mime = "image/gif";
            } else if ("cab".equalsIgnoreCase(type)) {
                mime = "application/octet-stream";
            } else if ("exe".equalsIgnoreCase(type)) {
                mime = "application/octet-stream";
            } else if ("pkg".equalsIgnoreCase(type)) {
                mime = "application/octet-stream";
            } else if ("msi".equalsIgnoreCase(type)) {
                mime = "application/octet-stream";
            } else if ("js".equalsIgnoreCase(type)) {
                mime = "application/javascript";
            } else if ("zip".equalsIgnoreCase(type)) {
                mime = "application/zip";
            } else if ("css".equalsIgnoreCase(type)) {
                mime = "text/css";
            } else if ("png".equalsIgnoreCase(type)) {
                mime = "image/png";
            }
        }
        return mime;
    }

    // ---------------------------------------------------------
    // ------------------- DEBUG ------------------------
    // ---------------------------------------------------------

    // TODO XYZ Moure a Plugin Abstracte de tipus WEB

    protected void logAllRequestInfo(HttpServletRequest request, String titol, String absolutePluginRequestPath,
            String relativePluginRequestPath, String query, String ID) {

        log.info(allRequestInfoToStr(request, titol, absolutePluginRequestPath, relativePluginRequestPath, query, ID));

    }

    protected String allRequestInfoToStr(HttpServletRequest request, String titol, String absolutePluginRequestPath,
            String relativePluginRequestPath, String query, String ID) {

        String str1 = pluginRequestInfoToStr(titol, absolutePluginRequestPath, relativePluginRequestPath, query, ID);

        String str2 = servletRequestInfoToStr(request);

        return str1 + str2;
    }

    protected String pluginRequestInfoToStr(String titol, String absolutePluginRequestPath,
            String relativePluginRequestPath, String query, String ID) {
        StringBuffer str = new StringBuffer("======== PLUGIN REQUEST " + titol + " ===========\n");
        str.append("absolutePluginRequestPath: " + absolutePluginRequestPath + "\n");
        str.append("relativePluginRequestPath: " + relativePluginRequestPath + "\n");
        str.append("query: " + query + "\n");
        str.append("ID: " + ID + "\n");
        return str.toString();
    }

    protected String servletRequestInfoToStr(HttpServletRequest request) {
        StringBuffer str = new StringBuffer(" +++++++++++++++++ SERVLET REQUEST INFO ++++++++++++++++++++++\n");
        str.append(" ++++ Scheme: " + request.getScheme() + "\n");
        str.append(" ++++ ServerName: " + request.getServerName() + "\n");
        str.append(" ++++ ServerPort: " + request.getServerPort() + "\n");
        str.append(" ++++ PathInfo: " + request.getPathInfo() + "\n");
        str.append(" ++++ PathTrans: " + request.getPathTranslated() + "\n");
        str.append(" ++++ ContextPath: " + request.getContextPath() + "\n");
        str.append(" ++++ ServletPath: " + request.getServletPath() + "\n");
        str.append(" ++++ getRequestURI: " + request.getRequestURI() + "\n");
        str.append(" ++++ getRequestURL: " + request.getRequestURL() + "\n");
        str.append(" ++++ getQueryString: " + request.getQueryString() + "\n");
        str.append(" ++++ javax.servlet.forward.request_uri: "
                + (String) request.getAttribute("javax.servlet.forward.request_uri") + "\n");
        str.append(" ===============================================================");
        return str.toString();
    }

}
