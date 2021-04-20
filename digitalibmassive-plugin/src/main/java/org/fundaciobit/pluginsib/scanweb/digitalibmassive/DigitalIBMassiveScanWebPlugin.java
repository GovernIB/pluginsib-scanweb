package org.fundaciobit.pluginsib.scanweb.digitalibmassive;

import org.fundaciobit.apisib.apimassivescanwebsimple.v1.ApiMassiveScanWebSimple;
import org.fundaciobit.apisib.apimassivescanwebsimple.v1.beans.*;
import org.fundaciobit.apisib.apimassivescanwebsimple.v1.jersey.ApiMassiveScanWebSimpleJersey;
import org.fundaciobit.pluginsib.core.utils.ISO8601;
import org.fundaciobit.pluginsib.core.utils.Metadata;
import org.fundaciobit.pluginsib.core.utils.MetadataConstants;
import org.fundaciobit.pluginsib.scanweb.api.AbstractScanWebPlugin;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebDocument;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebMode;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebPlainFile;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebRequest;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebRequestSignatureInfo;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebResult;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebResultSignInfo;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebResultSignValidationInfo;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebSignedFile;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * 
 * Implementació del plugin per escannejar un document amb digital IB.
 * 
 * @author anadal
 */
public class DigitalIBMassiveScanWebPlugin extends AbstractScanWebPlugin {

    private static final String DIGITALIBMASSIVE_PROPERTY_BASE = SCANWEB_PLUGINSIB_BASE_PROPERTY + "digitalibmassive.";

    private Map<String, DigitalIBTransactionInfo> digitalibTransactions = new HashMap<String, DigitalIBTransactionInfo>();

    /**
     * 
     * @author anadal (u80067)
     *
     */
    public class DigitalIBTransactionInfo {

        protected final MassiveScanWebSimpleStartTransactionRequest transactionRequest;

        protected final long expireDate;

        public DigitalIBTransactionInfo(MassiveScanWebSimpleStartTransactionRequest transactionRequest) {
            super();
            this.transactionRequest = transactionRequest;
            expireDate = System.currentTimeMillis() + DEFAULT_TIME_BY_TRANSACTION; // 20 minuts
        }

        public MassiveScanWebSimpleStartTransactionRequest getTransactionRequest() {
            return transactionRequest;
        }

        public long getExpireDate() {
            return expireDate;
        }

    }

    /**
    *
    */
    public DigitalIBMassiveScanWebPlugin() {
        super();
    }

    /**
     * @param propertyKeyBase
     * @param properties
     */
    public DigitalIBMassiveScanWebPlugin(String propertyKeyBase, Properties properties) {
        super(propertyKeyBase, properties);
    }

    /**
     * @param propertyKeyBase
     */
    public DigitalIBMassiveScanWebPlugin(String propertyKeyBase) {
        super(propertyKeyBase);
    }

    // Propietats del plugin
    public String getProfile() throws Exception {
        return getProperty(DIGITALIBMASSIVE_PROPERTY_BASE + "profile");
    }

    public String getUrl() throws Exception {
        return getPropertyRequired(DIGITALIBMASSIVE_PROPERTY_BASE + "url");
    }

    public String getUsername() throws Exception {
        return getPropertyRequired(DIGITALIBMASSIVE_PROPERTY_BASE + "username");
    }

    public String getPassword() throws Exception {
        return getPropertyRequired(DIGITALIBMASSIVE_PROPERTY_BASE + "password");
    }

    @Override
    public String getName(Locale locale) {
        return "DigitalIB Massive ScanWeb";
    }

    private void putDigitalIBTransactionInfo(String scanWebID,
            MassiveScanWebSimpleStartTransactionRequest transactionRequest) {
        synchronized (digitalibTransactions) {

            digitalibTransactions.put(scanWebID, new DigitalIBTransactionInfo(transactionRequest));

        }
    }

    private DigitalIBTransactionInfo getDigitalIBTransactionInfo(String transactionID) {

        synchronized (digitalibTransactions) {
            List<String> idsToDelete = new ArrayList<String>();

            Set<Entry<String, DigitalIBTransactionInfo>> entries = digitalibTransactions.entrySet();
            long current = System.currentTimeMillis();

            for (Entry<String, DigitalIBTransactionInfo> entry : entries) {
                if (current > entry.getValue().getExpireDate()) {
                    idsToDelete.add(entry.getKey());
                }
            }

            for (String id : idsToDelete) {
                log.warn("Eliminant DigitalIBTransactionInfo amd ID " + id + " ja que ha caducat !!!");
                digitalibTransactions.remove(id);
            }

            return digitalibTransactions.get(transactionID);
        }
    }

    @Override
    public boolean filter(HttpServletRequest request, ScanWebRequest config) {
        return super.filter(request, config);
    }

    @Override
    public boolean isMassiveScanAllowed() {
        return true;
    }

    @Override
    public ScanWebPlainFile getSeparatorForMassiveScan(String languageUI) throws Exception {

        // Agafam l'api de DigitalIB Massive
        ApiMassiveScanWebSimple api = null;
        api = getApiMassiveScanWebSimple();
        
        MassiveScanWebSimpleFile separadorAPI = api.getSeparatorPage(languageUI);

        ScanWebPlainFile plain = new ScanWebPlainFile(separadorAPI.getNom(), separadorAPI.getMime(), separadorAPI.getData());

        return plain;
        
    }

    @Override
    public String startScanWebTransaction(String absolutePluginRequestPath, String relativePluginRequestPath,
            HttpServletRequest request, ScanWebRequest scanwebRequest) throws Exception {

        // Obtenim el profile amb el que volem fer feina. Pot ser null i es
        // mostrarà a l'usuari el perfil a usar
        final String profileCode = getProfile();

        String redirectUrl;

        if (profileCode == null) {
            // Saltam a la pantalla de Selecció de Perfil
            redirectUrl = relativePluginRequestPath + "/" + SELECCIONA_PERFIL;
        } else {
            redirectUrl = relativePluginRequestPath + "/" + POSTSELECTEDPROFILE + "/" + profileCode;
        }

        ScanWebStatus sws = putScanWebRequest(scanwebRequest, System.currentTimeMillis() + DEFAULT_TIME_BY_TRANSACTION);
        sws.setStatus(ScanWebStatus.STATUS_IN_PROGRESS);

        return redirectUrl;
    }

    @Override
    public Set<String> getSupportedScanTypes() {
        final Set<String> SUPPORTED_SCAN_TYPES = Collections
                .unmodifiableSet(new HashSet<String>(Arrays.asList(SCANTYPE_MIME_PDF)));
        return SUPPORTED_SCAN_TYPES;
    }

    @Override
    public Set<String> getSupportedFlagsByScanType(String scanType) {

        if (SCANTYPE_MIME_PDF.equals(scanType)) {
            // XYZ ZZZ S'ha de cridar al Servidor i verure quins PLugins HIHA

            Set<String> flags = new HashSet<String>();

            try {

                ApiMassiveScanWebSimple api = getApiMassiveScanWebSimple();

                MassiveScanWebSimpleAvailableProfiles profiles = api.getAvailableProfiles("ca");

                for (MassiveScanWebSimpleAvailableProfile profile : profiles.getAvailableProfiles()) {

                    switch (profile.getProfileType()) {

                        case MassiveScanWebSimpleAvailableProfile.PROFILE_TYPE_ONLY_SCAN:
                            flags.add(FLAG_PLAIN);
                        break;
                        case MassiveScanWebSimpleAvailableProfile.PROFILE_TYPE_SCAN_AND_SIGNATURE:
                            flags.add(FLAG_SIGNED);
                        break;

                        case MassiveScanWebSimpleAvailableProfile.PROFILE_TYPE_SCAN_AND_SIGNATURE_AND_CUSTODY:
                            flags.add(FLAG_SIGNED_AND_CUSTODY);
                        break;

                        default:
                            log.error("Tipus de Perfil desconegut: " + profile.getProfileType(), new Exception());
                    }

                }

            } catch (Exception e) {

                log.error("No s'han pogut consultar els Flags del Plugin " + this.getName(new Locale("ca")) + ":"
                        + e.getMessage(), e);
            }

            return flags;
        }
        return null;
    }

    @Override
    public Set<ScanWebMode> getSupportedScanWebModes() {
        final Set<ScanWebMode> SUPPORTED_MODES = Collections
                .unmodifiableSet(new HashSet<ScanWebMode>(Arrays.asList(ScanWebMode.SYNCHRONOUS)));
        return SUPPORTED_MODES;
    }

    @Override
    public String getResourceBundleName() {
        return "digitalibmassivescanweb";
    }

    // ----------------------------------------------------------
    // ----------------------------------------------------------
    // --------------- REQUEST GET-POST ------------------------
    // ----------------------------------------------------------
    // ----------------------------------------------------------

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

        if (scanWebRequest == null) {
            String titol = (isGet ? "GET" : "POST") + " " + getName(new Locale("ca")) + " PETICIO HA CADUCAT";

            requestTimeOutError(absolutePluginRequestPath, relativePluginRequestPath, query, String.valueOf(scanWebID),
                    request, response, titol);

        } else {

            Locale locale = new Locale(scanWebRequest.getLanguageUI());

            if (query.startsWith(SELECCIONA_PERFIL)) {
                seleccionaPerfil(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request,
                        response, scanWebRequest, scanWebResult, locale);
            } else if (query.startsWith(POSTSELECTEDPROFILE)) {

                postSelectedProfile(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request,
                        response, scanWebRequest, scanWebResult, locale);
            } else if (query.startsWith(FINAL_PAGE)) {

                finalPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request, response,
                        scanWebRequest, scanWebResult, locale);
            } else {

                super.requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query, request,
                        response, locale, scanWebRequest, scanWebResult, isGet);
            }

        }

    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // ----------------------- CONNECTA AMB DIGITAL IB -------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    public static final String POSTSELECTEDPROFILE = "postSelectedProfile";

    protected void postSelectedProfile(String absolutePluginRequestPath, String relativePluginRequestPath,
            String scanWebID, String query, HttpServletRequest request, HttpServletResponse response,
            ScanWebRequest scanWebRequest, ScanWebResult scanWebResult, Locale locale) {

        log.info(" ENTRA DINS postSelectedProfile:  query => ]" + query + "[");

        final String languageUI = locale.getLanguage();
        ApiMassiveScanWebSimple api = null;
        String digitalIBTransactionID = null;
        try {

            final String profileCode = query.substring(query.indexOf('/') + 1);
            log.info(" ENTRA DINS postSelectedProfile:  profileCode => ]" + profileCode + "[");

            // Seleccionam el perfil indicat
            MassiveScanWebSimpleProfileRequest profileRequest = new MassiveScanWebSimpleProfileRequest(profileCode,
                    languageUI);

            // Agafam l'api de DigitalIB
            api = getApiMassiveScanWebSimple();

            MassiveScanWebSimpleAvailableProfile scanWebProfileSelected = api.getProfile(profileRequest);

            // Si val null es que el perfil no existeix
            if (scanWebProfileSelected == null) {
                // TODO XYZ ZZZ TRA TRADUCCIÓ
                throw new Exception("NO EXISTEIX EL PERFIL AMB CODI " + profileCode);
            }

            {
                final int view = MassiveScanWebSimpleGetTransactionIdRequest.VIEW_FULLSCREEN;

                // TODO recollir de l'aplicació XYZ ZZZ
                /*
                 * String funcionariUsername = getInputMetadata("Username del Funcionari",
                 * MetadataConstants.FUNCTIONARY_USERNAME, "FUNCTIONARY_USERNAME",
                 * config.getMetadades(), locale);
                 */
                String funcionariUsername = scanWebRequest.getUsername();

                MassiveScanWebSimpleGetTransactionIdRequest transacctionIdRequest;

                switch (scanWebProfileSelected.getProfileType()) {

                    case MassiveScanWebSimpleAvailableProfile.PROFILE_TYPE_ONLY_SCAN:
                       
                        transacctionIdRequest = new MassiveScanWebSimpleGetTransactionIdRequest(
                                scanWebRequest.getTransactionName(), profileCode,
                                 view, languageUI, funcionariUsername);

                    break;

                    case MassiveScanWebSimpleAvailableProfile.PROFILE_TYPE_SCAN_AND_SIGNATURE: {
                        MassiveScanWebSimpleSignatureParameters signatureParameters = getSignatureParameters(
                                scanWebRequest, locale);

                        transacctionIdRequest = new MassiveScanWebSimpleGetTransactionIdRequest(
                                scanWebRequest.getTransactionName(), profileCode,
                                 view, languageUI, funcionariUsername,
                                signatureParameters);
                    }
                    break;

                    case MassiveScanWebSimpleAvailableProfile.PROFILE_TYPE_SCAN_AND_SIGNATURE_AND_CUSTODY: {

                        // XYZ ZZZ ZZZ
                        throw new Exception("Pefils de tipus Firma i Arxivat no es suporten.");
                        /*
                         * MassiveScanWebSimpleSignatureParameters signatureParameters =
                         * getSignatureParameters();
                         * 
                         * MassiveScanWebSimpleArxiuRequiredParameters arxiuRequiredParameters;
                         * arxiuRequiredParameters = getArxiuRequiredParameters();
                         * 
                         * // See getArxiuOptionalParameters() sample
                         * MassiveScanWebSimpleArxiuOptionalParameters arxiuOptionalParameters = null;
                         * 
                         * transacctionIdRequest = new
                         * MassiveScanWebSimpleGetTransactionIdRequest(scanWebRequest.getTransactionName(),profileCode, view, languageUI,
                         * funcionariUsername, signatureParameters, arxiuRequiredParameters,
                         * arxiuOptionalParameters);
                         */
                    }

                    default:
                        // TODO XYZ ZZZ ZZZ TRADUCCIO
                        throw new Exception("Tipus de perfil desconegut " + scanWebProfileSelected.getProfileType());

                }

                // Enviam la part comu de la transacció
                digitalIBTransactionID = api.getTransactionID(transacctionIdRequest);
                log.info("languageUI = |" + languageUI + "|");
                log.info("DigitalIB TransactionID = |" + digitalIBTransactionID + "|");

            }

            // Url de retorn de digitalIB a Plugin
            final String returnUrl = absolutePluginRequestPath + FINAL_PAGE + "/" + scanWebID;

            log.info("Url retorn de digitalIB a Plugin: " + returnUrl);

            MassiveScanWebSimpleStartTransactionRequest startTransactionInfo;
            startTransactionInfo = new MassiveScanWebSimpleStartTransactionRequest(digitalIBTransactionID, returnUrl);

            putDigitalIBTransactionInfo(scanWebID, startTransactionInfo);

            // Url per anar a Digital IB
            String redirectUrl = api.startTransaction(startTransactionInfo);

            log.info("RedirectUrl a digitalIB: " + redirectUrl);

            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            // TODO: handle exception
            String msg = " Error Realitzant la comunicació amb DigitalIB: " + e.getMessage();

            if (api != null && digitalIBTransactionID != null) {
                try {
                    api.closeTransaction(digitalIBTransactionID);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }

            // ja fa redirecció.
            processError(response, scanWebRequest, scanWebResult, e, msg);
        }

    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // ----------------------- SELECCIONA PERFIL -------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    public static final String SELECCIONA_PERFIL = "seleccionaPerfil";

    protected void seleccionaPerfil(String absolutePluginRequestPath, String relativePluginRequestPath,
            String scanWebID, String query, HttpServletRequest request, HttpServletResponse response,
            ScanWebRequest scanWebRequest, ScanWebResult scanWebResult, Locale languageUI) {

        ApiMassiveScanWebSimple api = null;
        try {

            api = getApiMassiveScanWebSimple();

            MassiveScanWebSimpleAvailableProfiles profiles = api.getAvailableProfiles(languageUI.getLanguage());

            if (profiles == null || profiles.getAvailableProfiles() == null
                    || profiles.getAvailableProfiles().size() == 0) {
                throw new Exception("No hi ha perfils disponibles per aquest usuari aplicació (" + getUsername() + ")");
            }

            List<MassiveScanWebSimpleAvailableProfile> profilesList = profiles.getAvailableProfiles();

            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");

            PrintWriter out = generateHeader(request, response, absolutePluginRequestPath, relativePluginRequestPath,
                    languageUI);

            out.println("<table border=0 width=\"100%\" height=\"300px\">\n");
            out.println("<tr><td align=center>\n");

            out.println(" <div class=\"lead\" style=\"margin-bottom:10px; text-align:center;\">");

            // Selección del Perfil de Escaneo
            out.println(getTraduccio("plugindescan.seleccio.title", languageUI));

            out.println("  <br/>");
            out.println("  <h5 style=\"line-height: 10px; margin-top: 0px; margin-bottom: 0px;\">");

            // Seleccione el perfil d´escaneo que quiera realizar
            out.println(getTraduccio("plugindescan.seleccio.subtitle", languageUI));

            out.println("  </h5>");
            out.println("  <br/>");
            out.println("  <br/>");
            out.println("  <div class=\"well\" style=\"max-width: 400px; margin: 0 auto 10px;\">");

            String flag = scanWebRequest.getFlag();
            for (MassiveScanWebSimpleAvailableProfile profile : profilesList) {

                // NOMES ELEGIR ELS QUE PASSIN EL FILTRE !!

                switch (profile.getProfileType()) {

                    case MassiveScanWebSimpleAvailableProfile.PROFILE_TYPE_ONLY_SCAN:
                        if (!flag.equals(FLAG_PLAIN)) {
                            continue;
                        }
                    break;
                    case MassiveScanWebSimpleAvailableProfile.PROFILE_TYPE_SCAN_AND_SIGNATURE:
                        if (!flag.equals(FLAG_SIGNED)) {
                            continue;
                        }
                    break;

                    case MassiveScanWebSimpleAvailableProfile.PROFILE_TYPE_SCAN_AND_SIGNATURE_AND_CUSTODY:
                        if (!flag.equals(FLAG_SIGNED_AND_CUSTODY)) {
                            continue;
                        }
                    break;

                    default:
                        log.error("Tipus de Perfil desconegut: " + profile.getProfileType(), new Exception());
                        continue;
                }

                @SuppressWarnings("deprecation")
                String url = absolutePluginRequestPath + POSTSELECTEDPROFILE + "/"
                        + URLEncoder.encode(profile.getCode());

                out.println("     <button type=\"button\" class=\"btn btn-large btn-block btn-primary\" "
                        + "onclick=\"location.href='" + url + "'\">");
                out.println("     <b>" + profile.getName() + "</b><br>");
                out.println("     <small style=\"color: white\">");
                out.println("     <i>" + profile.getDescription() + "</i>");
                out.println("     </small>");
                out.println("     </button>");
            }

            out.println("  </div>");
            out.println("  <br/>");
            out.println("</div>");
            out.println("</td></tr>\n");
            out.println("</table>\n");

            generateFooter(out);

            out.flush();

        } catch (Exception e) {
            // TODO: handle exception

            String msg = " Error mostrant la pàgina de selecció de Perfils: " + e.getMessage();

            processError(response, scanWebRequest, scanWebResult, e, msg);
        }

    }

    protected void processError(HttpServletResponse response, ScanWebRequest scanWebRequest,
            ScanWebResult scanWebResult, Exception e, String msg) {

        log.error(msg, e);

        ScanWebStatus status = scanWebResult.getStatus();
        status.setStatus(ScanWebStatus.STATUS_FINAL_ERROR);
        status.setErrorMsg(msg);
        status.setErrorException(e);

        String urlFinal = scanWebRequest.getUrlFinal();

        if (ScanWebMode.ASYNCHRONOUS.equals(scanWebRequest.getMode())) {
            // XYZ ZZZ TRA
            String msgErr = "Error durant el procés d´escaneig " + msg;

            printSimpleHtmlPage(response, msgErr);

        } else {
            log.info("Error: Redireccionam a " + urlFinal);

            try {
                response.sendRedirect(urlFinal);
            } catch (IOException e2) {
                log.error(e2.getMessage(), e2);
            }
        }
    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // --------------- FINAL PAGE (SINCRON MODE) -------------------------------
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------

    public static final String FINAL_PAGE = "finalPage";

    protected void finalPage(String absolutePluginRequestPath, String relativePluginRequestPath, String scanWebID,
            String query, HttpServletRequest request, HttpServletResponse response, ScanWebRequest scanWebRequest,
            ScanWebResult scanWebResult, Locale languageUI) {
        
        
        log.info(" =========    ENTRA A FINAL PAGE " +  scanWebID + "!!!! ===============");
        
        

        ApiMassiveScanWebSimple api = null;
        // Obtenim la transacció de Digital IB.
        DigitalIBTransactionInfo info = getDigitalIBTransactionInfo(scanWebID);

        MassiveScanWebSimpleStartTransactionRequest digitalIbRequest = info.getTransactionRequest();

        try {

            ScanWebStatus status = scanWebResult.getStatus();

            ScanWebStatus localStatus = scanWebResult.getStatus();
            log.info("LocalStatus(INIT=0|PROG=1|OK=2|ERR=-1|CAN=-2): " + localStatus.getStatus());
            if (localStatus.getStatus() != ScanWebStatus.STATUS_IN_PROGRESS) {

                // Error Local
                /*
                 * String sStackTrace; Throwable th = localStatus.getErrorException(); if (th ==
                 * null) { sStackTrace = null; } else { StringWriter sw = new StringWriter();
                 * PrintWriter pw = new PrintWriter(sw); th.printStackTrace(pw); sStackTrace =
                 * sw.toString(); }
                 */

                status.setStatus(localStatus.getStatus());
                status.setErrorMsg(localStatus.getErrorMsg());
                status.setErrorException(localStatus.getErrorException());

            } else {

                // Obtenim l'api d'ScanWebSimple
                api = getApiMassiveScanWebSimple();

                // MassiveScanWebSimpleResultRequest rr = new
                // MassiveScanWebSimpleResultRequest(digitalIbRequest.getTransactionID());

                // Obtenim el resultat de l'escanneig
                MassiveScanWebSimpleStatus transResult = api.getMassiveTransactionStatus(digitalIbRequest.getTransactionID());

                
                if (transResult.getStatus() == MassiveScanWebSimpleStatus.STATUS_FINAL_OK) {

                    // REVISAM CADA UNA DE LES SUBTRANSACCIONS
                    MassiveScanWebSimpleSubTransactionsOfTransaction a = api.getSubTransactionsOfTransaction(digitalIbRequest.getTransactionID());
                    List<String> subtransaccions = a.getSubtransacions();
                    
                    log.info(" # de SUBTRANSACCIONS = " + subtransaccions.size());
                    
                    
                    for (String sub : subtransaccions) {
                        MassiveScanWebSimpleStatus subtransactionStatus = api.getSubTransactionStatus(sub);
                        
                        int statusID = subtransactionStatus.getStatus();

                        log.info("Status de Subtransaction amb WebID=" + sub + " val(REQ=0|PROG=1|OK=2|ERR=-1|CAN=-2|EXP=-3): " + statusID);

                        switch (statusID) {

                            case MassiveScanWebSimpleStatus.STATUS_REQUESTED_ID: // = 0;
                                status.setStatus(ScanWebStatus.STATUS_FINAL_ERROR);
                                status.setErrorMsg("S'ha rebut un estat inconsistent del procés"
                                        + " (requestedid). Pot ser el Plugin no està ben desenvolupat."
                                        + " Consulti amb el seu administrador.");
                            break;

                            case MassiveScanWebSimpleStatus.STATUS_FINAL_ERROR: // = -1;
                            {
                                String desc = subtransactionStatus.getErrorStackTrace();
                                if (desc != null) {
                                    log.error(desc);
                                }

                                status.setStatus(ScanWebStatus.STATUS_FINAL_ERROR);
                                status.setErrorMsg("Error durant la realització de l'escaneig/còpia autèntica: "
                                        + subtransactionStatus.getErrorMessage());

                                // XYZ ZZZ Convertir desc a Throwable !!!!
                                status.setErrorException(null);
                            }
                            break;

                            case MassiveScanWebSimpleStatus.STATUS_CANCELLED: // = -2;
                            {
                                log.error("Durant el procés, l'usuari ha cancelat la transacció.");
                                status.setStatus(ScanWebStatus.STATUS_CANCELLED);
                                status.setErrorMsg(subtransactionStatus.getErrorMessage());

                            }
                            break;

                            case MassiveScanWebSimpleStatus.STATUS_IN_PROGRESS: // = 1;
                                // XYZ ZZZ NO ESTA BE !!! HAURIA DE LLANÇAR UNA EXCEPCIO
                                // throw new Exception(
                                String msg = "S'ha rebut un estat inconsistent del procés"
                                        + " (En Progrés). Pot ser el Plugin no està ben desenvolupat."
                                        + " Consulti amb el seu administrador.";

                                status.setStatus(ScanWebStatus.STATUS_FINAL_ERROR);
                                status.setErrorMsg(msg);
                                log.error(msg);
                            break;

                        } // Final Switch Firma

                    } // Final For

                    if (status.getStatus() != ScanWebStatus.STATUS_FINAL_ERROR
                            && status.getStatus() != ScanWebStatus.STATUS_CANCELLED) {
                        
                        // Significa que totes les subtransaccions són OK

                        for (String subID : subtransaccions) {

                            MassiveScanWebSimpleSubtransactionResultRequest srr;
                            srr = new MassiveScanWebSimpleSubtransactionResultRequest(subID, true, true);
                            MassiveScanWebSimpleSubtransactionResult subResult = api.getSubTransactionResult(srr);

                            ScanWebDocument doc = processarSubtransactionScan(subResult);
                            scanWebResult.getScannedDocuments().add(doc);
                        }

                        status.setStatus(ScanWebStatus.STATUS_FINAL_OK);
                    }

                } else {
                    status.setStatus(transResult.getStatus());
                    status.setErrorMsg(transResult.getErrorMessage());
                    // XYZ ZZZ Falta crear Exception a partir de String de StrackTrace
                    // status.setErrorException(transResult.getErrorStackTrace());
                }

            } // Final de IF de estat local OK

            if (ScanWebMode.ASYNCHRONOUS.equals(scanWebRequest.getMode())) {

                printSimpleHtmlPage(response, getTraduccio("finalproces", languageUI));

            } else {
                final String url;
                url = scanWebRequest.getUrlFinal();
                log.info("Redireccionam a " + url);
                sendRedirect(response, url);
            }

        } catch (Exception e) {
            // TODO: handle exception
            String msg = " Error processant resultats: " + e.getMessage();

            processError(response, scanWebRequest, scanWebResult, e, msg);

        } finally {
            if (api != null && digitalIbRequest.getTransactionID() != null) {
                try {
                    api.closeTransaction(digitalIbRequest.getTransactionID());
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }

            endScanWebTransaction(scanWebID, request);

        }

    }

    protected ScanWebDocument processarSubtransactionScan(MassiveScanWebSimpleSubtransactionResult result) {

        ScanWebPlainFile plainFile;
        ScanWebSignedFile signedFile;

        MassiveScanWebSimpleFile signed = result.getSignedFile();
        if (signed != null) {

            Boolean attachedDocument;

            MassiveScanWebSimpleFile detached = result.getDetachedSignatureFile();
            if (detached != null) {

                plainFile = new ScanWebPlainFile(detached.getNom(), detached.getMime(), detached.getData());

                attachedDocument = true;
            } else {
                attachedDocument = false;

                plainFile = null;
            }

            MassiveScanWebSimpleSignedFileInfo sssfi = result.getSignedFileInfo();

            ScanWebResultSignInfo signInfo = null;
            if (sssfi != null) {

                ScanWebResultSignValidationInfo validationInfo = null;
                if (sssfi.getValidationInfo() != null) {
                    MassiveScanWebSimpleValidationInfo vi = sssfi.getValidationInfo();
                    validationInfo = new ScanWebResultSignValidationInfo(vi.getCheckAdministrationIDOfSigner(),
                            vi.getCheckDocumentModifications(), vi.getCheckValidationSignature());
                }

                List<Metadata> additionInformation = null;

                if (sssfi.getAdditionInformation() != null) {
                    additionInformation = new ArrayList<Metadata>();
                    for (MassiveScanWebSimpleKeyValue kv : sssfi.getAdditionInformation()) {
                        additionInformation.add(new Metadata(kv.getKey(), kv.getValue()));
                    }
                }

                signInfo = new ScanWebResultSignInfo(sssfi.getSignOperation(), sssfi.getSignType(),
                        sssfi.getSignAlgorithm(), sssfi.getSignMode(), sssfi.getSignaturesTableLocation(),
                        sssfi.getTimeStampIncluded(), sssfi.getPolicyIncluded(), sssfi.getEniTipoFirma(),
                        sssfi.getEniPerfilFirma(), sssfi.getEniRolFirma(), sssfi.getEniSignerName(),
                        sssfi.getEniSignerAdministrationId(), sssfi.getEniSignLevel(), validationInfo,
                        additionInformation);

            }

            signedFile = new ScanWebSignedFile(signed.getNom(), signed.getMime(), signed.getData(),
                    result.getSignedFileInfo().getSignType(), attachedDocument, signInfo);

        } else {

            // Obtenim el fitxer escannejat
            plainFile = new ScanWebPlainFile(result.getScannedFile().getNom(), result.getScannedFile().getMime(),
                    result.getScannedFile().getData());

            signedFile = null;

        }

        ScanWebDocument doc = new ScanWebDocument();

        doc.setScanDate(new Date());
        doc.setScannedPlainFile(plainFile);
        doc.setScannedSignedFile(signedFile);

        List<Metadata> metadatas = new ArrayList<Metadata>();

        MassiveScanWebSimpleFormMetadatas form = result.getFormMetadatas();
        /*
         * MassiveScanWebSimpleScannedFileInfo scannedFileInfo =
         * result.getScannedFileInfo(); MassiveScanWebSimpleSignatureParameters
         * signParams = form.getSignatureParameters();
         * MassiveScanWebSimpleArxiuRequiredParameters arxiuRequired =
         * form.getArxiuRequiredParameters();
         * MassiveScanWebSimpleArxiuOptionalParameters arxiuOptional =
         * form.getArxiuOptionalParameters();
         * 
         * 
         * MassiveScanWebSimpleSignedFileInfo signedInfo = result.getSignedFileInfo();
         * 
         * if (signedInfo != null) { // PERFIL DE FIRMA addMetadata(metadatas,
         * MetadataConstants.ENI_PERFIL_FIRMA, signedInfo.getEniPerfilFirma());
         * 
         * // ROL DE FIRMA addMetadata(metadatas, MetadataConstants.EEMGDE_ROL_FIRMA,
         * signedInfo.getEniPerfilFirma());
         * 
         * // NIF del Firmant addMetadata(metadatas,
         * MetadataConstants.EEMGDE_FIRMANTE_IDENTIFICADOR,
         * signedInfo.getEniSignerAdministrationId());
         * 
         * // Nom del Firmant addMetadata(metadatas,
         * MetadataConstants.EEMGDE_FIRMANTE_NOMBRECOMPLETO,
         * signedInfo.getEniSignerName());
         * 
         * // Tipus Firma ENI addMetadata(metadatas, MetadataConstants.ENI_TIPO_FIRMA,
         * signedInfo.getEniTipoFirma());
         * 
         * // Nivell de Firma addMetadata(metadatas,
         * MetadataConstants.EEMGDE_NIVEL_DE_FIRMA, signedInfo.getEniSignLevel()); }
         * 
         * // Nombre del Documento addMetadata(metadatas,
         * MetadataConstants.TITULO_DOCUMENTO, form.getTransactionName());
         * 
         * // PROFUNDITAT DE COLOR processarMetadadaProfundadadColor(metadatas,
         * scannedFileInfo);
         * 
         * // RESOLUCIO addMetadata(metadatas, MetadataConstants.EEMGDE_RESOLUCION,
         * scannedFileInfo.getPppResolution());
         * 
         * // OCR addMetadata(metadatas, MetadataConstants.OCR,
         * scannedFileInfo.getOcr());
         * 
         * // FUNCIONARI addMetadata(metadatas, MetadataConstants.FUNCTIONARY_USERNAME,
         * form.getFunctionaryUsername()); if (signParams != null) {
         * addMetadata(metadatas, MetadataConstants.FUNCTIONARY_ADMINISTRATIONID,
         * signParams.getFunctionaryAdministrationID()); addMetadata(metadatas,
         * MetadataConstants.FUNCTIONARY_FULLNAME, signParams.getFunctionaryFullName());
         * }
         * 
         * // IDIOMA addMetadata(metadatas, MetadataConstants.ENI_IDIOMA,
         * signParams.getDocumentLanguage());
         */

        // XYZ ZZZ FALTA DADES D'ARXIU !!!!
        /*
         * if (arxiuRequired != null) { // TIPUS DOCUMENTAL addMetadata(metadatas,
         * MetadataConstants.ENI_TIPO_DOCUMENTAL, arxiuRequired.getDocumentType());
         * 
         * // ORIGEN processarMetadadaOrigen(metadatas, arxiuRequired);
         * 
         * // ESTAT ELABORACIO processarMetadadaEstatElaboracio(metadatas,
         * arxiuRequired);
         * 
         * // INTERESSATS processarMetadadaInteressats(metadatas, arxiuRequired);
         * 
         * // // arxiuRequired.getAffectedOrganisms()
         * 
         * // CIUTADA addMetadata(metadatas, MetadataConstants.CITIZEN_ADMINISTRATIONID,
         * arxiuRequired.getCitizenAdministrationID()); addMetadata(metadatas,
         * MetadataConstants.CITIZEN_FULLNAME, arxiuRequired.getCitizenFullName()); }
         * 
         * if (arxiuOptional != null) { // CODI PROCEDIMENT addMetadata(metadatas,
         * MetadataConstants.ENI_ID_TRAMITE, arxiuOptional.getProcedureCode());
         * 
         * // NOM PROCEDIMENT addMetadata(metadatas, MetadataConstants.ENI_ID_TRAMITE +
         * ".description", arxiuOptional.getProcedureName()); }
         */

        // RESTA DE METADADES
        copyMetadatas(form.getAdditionalMetadatas(), metadatas);

        doc.setAdditionalMetadatas(metadatas);
        return doc;
    }

    protected void printSimpleHtmlPage(HttpServletResponse response, String msg) {
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");

        PrintWriter out;
        try {
            out = response.getWriter();

            out.println("<html>\n");
            out.println("<body>\n");
            out.println("<table border=0 width=\"100%\" height=\"300px\">\n");
            out.println("<tr><td align=center>\n");
            out.println("<p><h2>" + msg + "</h2><p>\n");
            out.println("</td></tr>\n");
            out.println("</table>\n");
            out.println("</body>\n");
            out.println("</html>\n");

            out.flush();

        } catch (IOException e2) {
            log.error(e2.getMessage(), e2);

        }

    }

    protected void processarMetadadaInteressats(List<Metadata> metadatas,
            MassiveScanWebSimpleArxiuRequiredParameters arxiuRequired) {
        List<String> interessats = arxiuRequired.getInterestedPersons();
        if (interessats != null && interessats.size() != 0) {

            StringBuffer str = new StringBuffer();
            for (String interessat : interessats) {

                if (str.length() == 0) {
                    str.append(interessat);
                } else {
                    str.append(", ").append(interessat);
                }
            }
            addMetadata(metadatas, MetadataConstants.ENI_INTERESADOS_EXP, str.toString());
        }
    }

    protected void processarMetadadaEstatElaboracio(List<Metadata> metadatas,
            MassiveScanWebSimpleArxiuRequiredParameters arxiuRequired) {
        if (arxiuRequired.getDocumentElaborationState() != null) {

            String des = arxiuRequired.getDocumentElaborationState();

            if (MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTELABORATIONSTATE_ORIGINAL.equals(des)) {
                addMetadata(metadatas, MetadataConstants.ENI_ESTADO_ELABORACION,
                        MetadataConstants.EstadoElaboracionConstants.ESTADO_ELABORACION_ORIGINAL);
            } else if (MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTELABORATIONSTATE_COPIA_CF.equals(des)) {
                addMetadata(metadatas, MetadataConstants.ENI_ESTADO_ELABORACION,
                        MetadataConstants.EstadoElaboracionConstants.ESTADO_ELABORACION_COPIA_CF);
            } else if (MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTELABORATIONSTATE_COPIA_DP.equals(des)) {
                addMetadata(metadatas, MetadataConstants.ENI_ESTADO_ELABORACION,
                        MetadataConstants.EstadoElaboracionConstants.ESTADO_ELABORACION_COPIA_DP);
            } else if (MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTELABORATIONSTATE_COPIA_PR.equals(des)) {
                addMetadata(metadatas, MetadataConstants.ENI_ESTADO_ELABORACION,
                        MetadataConstants.EstadoElaboracionConstants.ESTADO_ELABORACION_COPIA_PR);
            } else {
                // DOCUMENTELABORATIONSTATE_ALTRES = "EE99";
                addMetadata(metadatas, MetadataConstants.ENI_ESTADO_ELABORACION,
                        MetadataConstants.EstadoElaboracionConstants.ESTADO_ELABORACION_OTROS);

            }
        }
    }

    protected void processarMetadadaOrigen(List<Metadata> metadatas,
            MassiveScanWebSimpleArxiuRequiredParameters arxiuRequired) {
        if (arxiuRequired.getDocumentOrigen() != null) {
            switch (arxiuRequired.getDocumentOrigen()) {
                case MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTORIGEN_ADMINISTRACIO:
                    addMetadata(metadatas, MetadataConstants.ENI_ORIGEN,
                            MetadataConstants.OrigenConstants.ADMINISTRACION);
                break;
                case MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTORIGEN_CIUTADA:
                    addMetadata(metadatas, MetadataConstants.ENI_ORIGEN, MetadataConstants.OrigenConstants.CIUDADANO);
                break;
            }
        }
    }

    protected void addMetadata(List<Metadata> metadatas, String key, Boolean value) {
        if (value != null) {
            metadatas.add(new Metadata(key, value));
        }
    }

    protected void addMetadata(List<Metadata> metadatas, String key, String value) {
        if (value != null) {
            metadatas.add(new Metadata(key, value));
        }
    }

    protected void addMetadata(List<Metadata> metadatas, String key, Integer value) {
        if (value != null) {
            metadatas.add(new Metadata(key, value));
        }
    }

    protected void processarMetadadaProfundadadColor(List<Metadata> metadatas,
            MassiveScanWebSimpleScannedFileInfo scannedFileInfo) {
        Integer profundidadDeColor;
        String pixelType;
        if (scannedFileInfo.getPixelType() == null) {
            profundidadDeColor = null;
            pixelType = null;
        } else {

            switch (scannedFileInfo.getPixelType()) {

                case MassiveScanWebSimpleScannedFileInfo.PIXEL_TYPE_BLACK_WHITE:
                    profundidadDeColor = MetadataConstants.ProfundidadColorConstants.BW;
                    // XYZ ZZZ TRA
                    pixelType = "Blanc i negre";
                break;
                case MassiveScanWebSimpleScannedFileInfo.PIXEL_TYPE_GRAY:
                    profundidadDeColor = MetadataConstants.ProfundidadColorConstants.GRAY;
                    // XYZ ZZZ TRA
                    pixelType = "Escala de grisos";
                break;

                case MassiveScanWebSimpleScannedFileInfo.PIXEL_TYPE_COLOR:
                    profundidadDeColor = MetadataConstants.ProfundidadColorConstants.COLOR;
                    // XYZ ZZZ TRA
                    pixelType = "Color";
                break;

                default:
                    profundidadDeColor = null;
                    pixelType = null;
            }
        }

        if (pixelType != null) {
            metadatas.add(new Metadata(MetadataConstants.EEMGDE_PROFUNDIDAD_COLOR, profundidadDeColor));
            metadatas.add(new Metadata(MetadataConstants.EEMGDE_PROFUNDIDAD_COLOR + ".description", pixelType));
        }
    }

    public void copyMetadatas(List<MassiveScanWebSimpleKeyValue> digitalIBMetadatas, List<Metadata> metadatas) {

        if (digitalIBMetadatas == null || digitalIBMetadatas.isEmpty()) {
            return;
        }

        for (MassiveScanWebSimpleKeyValue metadata : digitalIBMetadatas) {
            String key = metadata.getKey();

            String value = metadata.getValue();

            try {
                Date d = ISO8601.ISO8601ToDate(value);
                metadatas.add(new Metadata(key, d));
            } catch (ParseException e) {
                try {
                    Long l = Long.parseLong(value);
                    metadatas.add(new Metadata(key, l));
                } catch (NumberFormatException e2) {
                    metadatas.add(new Metadata(key, value));
                }
            }

        }

    }

    @Override
    public void endScanWebTransaction(String scanWebID, HttpServletRequest request) {
        synchronized (digitalibTransactions) {
            super.endScanWebTransaction(scanWebID, request);
            digitalibTransactions.remove(scanWebID);
        }
    }

    // ---------------------------------------------------------------------
    // ---------------------------------------------------------------------
    // ------------------- MISSATGES ---------------------------------------
    // ---------------------------------------------------------------------
    // ---------------------------------------------------------------------

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
    }

    protected ApiMassiveScanWebSimple getApiMassiveScanWebSimple() throws Exception {

        return new ApiMassiveScanWebSimpleJersey(getUrl(), getUsername(), getPassword());

    }

    /**
     * @return
     */
    private MassiveScanWebSimpleSignatureParameters getSignatureParameters(ScanWebRequest scanWebRequest, Locale locale)
            throws Exception {

        ScanWebRequestSignatureInfo swsi = scanWebRequest.getSignatureInfo();

        if (swsi == null) {
            throw new Exception("El Perfil d'Escaneig requereix una firma, "
                    + "però des de l'aplicació no s'ha enviat informació de firma dins de la classe "
                    + ScanWebRequestSignatureInfo.class.getName());
        }

        String languageDoc = swsi.getDocumentLanguage();
        /*
         * try { languageDoc = getInputMetadata("Idioma del Document",
         * MetadataConstants.EEMGDE_IDIOMA, "EEMGDE_IDIOMA",
         * scanWebRequest.getAdditionalMetadatas(), locale); } catch (Exception e) {
         * languageDoc = null; }
         */
        MassiveScanWebSimpleSignatureParameters signatureParameters;
        signatureParameters = new MassiveScanWebSimpleSignatureParameters(languageDoc, swsi.getFunctionaryFullName(),
                swsi.getFunctionaryAdministrationID());

        /*
         * XYZ ZZZ
         * 
         * final String funcionariNom = getInputMetadata("Nom complet del Funcionari",
         * MetadataConstants.FUNCTIONARY_FULLNAME, "FUNCTIONARY_FULLNAME", metadatas,
         * locale); final String funcionariNif = getInputMetadata("Nif del Funcionari",
         * MetadataConstants.FUNCTIONARY_ADMINISTRATIONID,
         * "FUNCTIONARY_ADMINISTRATIONID", metadatas, locale); String languageDoc; try {
         * languageDoc = getInputMetadata("Idioma del Document",
         * MetadataConstants.EEMGDE_IDIOMA, "EEMGDE_IDIOMA", metadatas, locale); }
         * catch(Exception e) { languageDoc = null; }
         * 
         * MassiveScanWebSimpleSignatureParameters signatureParameters;
         * signatureParameters = new
         * MassiveScanWebSimpleSignatureParameters(languageDoc, funcionariNom,
         * funcionariNif);
         */
        return signatureParameters;
    }

    /*
     * private String getInputMetadata(String description, String metadataKey,
     * String metadataName, List<Metadata> metadatas, Locale locale) throws
     * Exception {
     * 
     * 
     * // * String metadataKey = getProperty(PROPERTY_BASE + "metadata." +
     * partialName); // * // * // * if (metadataKey == null ||
     * metadataKey.trim().length() == 0) { // * log.warn("La propietat " // * +
     * getPropertyName(PROPERTY_BASE + partialName) // * + " no s'ha definit.");
     * return null; } // * // * if (metadatas == null || // * metadatas.size() == 0)
     * { // * log. // *
     * warn("La llista de metadades està buida. No podem trobat el valor per la metadada "
     * // * // * + metadataKey); return null; }
     * 
     * 
     * String value = null; for (Metadata metadata : metadatas) { if
     * (metadata.getKey().equals(metadataKey)) { value = metadata.getValue(); break;
     * } }
     * 
     * if (value == null) { // Cercam dins de les Propietats value =
     * getProperty(DIGITALIBMASSIVE_PROPERTY_BASE + metadataKey); }
     * 
     * if (value == null) { // TODO XYZ ZZZ TRA TRADUCCIÓ throw new
     * Exception("NO s'ha definit el camp " + description +
     * "  ni des de les Metadades de ScanWebConfig" +
     * "(Metadada  MetadataConstants." + metadataName + "=\"" + metadataKey +
     * "\") ni des de la propietat " +
     * getPropertyName(DIGITALIBMASSIVE_PROPERTY_BASE + metadataKey) +
     * " del plugin " + getName(locale));
     * 
     * }
     * 
     * log.info("XYZ ZZZ   KEY[" + metadataKey + "] => " + value);
     * 
     * return value;
     * 
     * }
     */

    /*
     * private MassiveScanWebSimpleArxiuRequiredParameters
     * getArxiuRequiredParameters() { final List<String> interessats = new
     * ArrayList<String>(Arrays.asList("12345678X", "87654321Z"));
     * 
     * 
     * // * MassiveScanWebSimpleArxiuRequiredParameters.CIUTADA // *
     * MassiveScanWebSimpleArxiuRequiredParameters.ORIGEN_ADMINISTRACIO
     * 
     * final int origen =
     * MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTORIGEN_ADMINISTRACIO;
     * 
     * 
     * // * @see
     * MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_ORIGINAL
     * // * @see
     * MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_COPIA_CF
     * // * @see
     * MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_COPIA_DP
     * // * @see
     * MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_COPIA_PR
     * 
     * final String documentEstatElaboracio =
     * MassiveScanWebSimpleArxiuRequiredParameters.
     * DOCUMENTELABORATIONSTATE_ORIGINAL;
     * 
     * 
     * // * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_RESOLUCIO
     * // * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ACORD //
     * * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_CONTRACTE //
     * * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_CONVENI //
     * * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_DECLARACIO
     * // * @see
     * MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_COMUNICACIO //
     * * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_NOTIFICACIO
     * // * @see
     * MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_PUBLICACIO //
     * * @see MassiveScanWebSimpleArxiuRequiredParameters.
     * DOCUMENTTIPUS_JUSTIFICANT_RECEPCIO // * @see
     * MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ACTA // * @see
     * MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_CERTIFICAT //
     * * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_DILIGENCIA
     * // * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_INFORME
     * // * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_SOLICITUD
     * // * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_DENUNCIA
     * // * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ALEGACIO
     * // * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_RECURS //
     * * @see
     * MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_COMUNICACIO_CIUTADA
     * // * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_FACTURA
     * // * @see
     * MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ALTRES_INCAUTATS //
     * * @see MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ALTRES
     * 
     * final String documentTipus =
     * MassiveScanWebSimpleArxiuRequiredParameters.DOCUMENTTYPE_RESOLUCIO;
     * 
     * String ciutadaNif = "11223344C";
     * 
     * String ciutadaNom = "Pep Gonella";
     * 
     * List<String> organs = new ArrayList<String>(Arrays.asList("A04013511"));
     * 
     * MassiveScanWebSimpleArxiuRequiredParameters arxiuRequiredParameters;
     * arxiuRequiredParameters = new
     * MassiveScanWebSimpleArxiuRequiredParameters(ciutadaNif, ciutadaNom,
     * documentEstatElaboracio, documentTipus, origen, interessats, organs); return
     * arxiuRequiredParameters; }
     */
}