package org.fundaciobit.pluginsib.scanweb.digitalib;

import org.fundaciobit.apisib.apiscanwebsimple.v1.ApiScanWebSimple;
import org.fundaciobit.apisib.apiscanwebsimple.v1.beans.*;
import org.fundaciobit.apisib.apiscanwebsimple.v1.jersey.ApiScanWebSimpleJersey;
import org.fundaciobit.plugins.scanweb.api.*;
import org.fundaciobit.pluginsib.core.utils.ISO8601;
import org.fundaciobit.pluginsib.core.utils.Metadata;
import org.fundaciobit.pluginsib.core.utils.MetadataConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

/**
 * 
 * Implementació del plugin per escannejar un document amb digital IB.
 * 
 * @author mgonzalez
 * @author anadal
 */
public class DigitalIBScanWebPlugin extends AbstractScanWebPlugin {

  private static final String PROPERTY_BASE = SCANWEB_PLUGINSIB_BASE_PROPERTY + "digitalib.";

  private Map<String, TransactionInfo> digitalibTransactions22 = new HashMap<String, TransactionInfo>();

  /**
   * 
   * @author anadal (u80067)
   *
   */
  public class TransactionInfo {

    protected final ScanWebSimpleStartTransactionRequest transactionRequest;

    protected final long expireDate;

    public TransactionInfo(ScanWebSimpleStartTransactionRequest transactionRequest) {
      super();
      this.transactionRequest = transactionRequest;
      expireDate = System.currentTimeMillis() + 10 * 60 * 1000; // 10 minuts
    }

    public ScanWebSimpleStartTransactionRequest getTransactionRequest() {
      return transactionRequest;
    }

    public long getExpireDate() {
      return expireDate;
    }

  }

  /**
    *
    */
  public DigitalIBScanWebPlugin() {
    super();
  }

  /**
   * @param propertyKeyBase
   * @param properties
   */
  public DigitalIBScanWebPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
  }

  /**
   * @param propertyKeyBase
   */
  public DigitalIBScanWebPlugin(String propertyKeyBase) {
    super(propertyKeyBase);
  }

  // Propietats del plugin
  public String getProfile() throws Exception {
    return getProperty(PROPERTY_BASE + "profile");
  }

  public String getUrl() throws Exception {
    return getPropertyRequired(PROPERTY_BASE + "url");
  }

  public String getUsername() throws Exception {
    return getPropertyRequired(PROPERTY_BASE + "username");
  }

  public String getPassword() throws Exception {
    return getPropertyRequired(PROPERTY_BASE + "password");
  }

  @Override
  public String getName(Locale locale) {
    return "DigitalIB ScanWeb";
  }

  public void putTransactionInfo(ScanWebConfig config,
      ScanWebSimpleStartTransactionRequest transactionRequest) {
    synchronized (digitalibTransactions22) {

      digitalibTransactions22.put(config.getScanWebID(),
          new TransactionInfo(transactionRequest));

      config.setStatus(null);

      putTransaction(config);

    }
  }

  public TransactionInfo getTransactionInfo(String transactionID) {

    synchronized (digitalibTransactions22) {
      List<String> idsToDelete = new ArrayList<String>();

      Set<Entry<String, TransactionInfo>> entries = digitalibTransactions22.entrySet();
      long current = System.currentTimeMillis();

      for (Entry<String, TransactionInfo> entry : entries) {
        if (current > entry.getValue().getExpireDate()) {
          idsToDelete.add(entry.getKey());
        }
      }

      for (String id : idsToDelete) {
        digitalibTransactions22.remove(id);
      }

      return digitalibTransactions22.get(transactionID);
    }
  }

  @Override
  public boolean filter(HttpServletRequest request, ScanWebConfig config) {
    return super.filter(request, config);
  }

  @Override
  public String startScanWebTransaction(String absolutePluginRequestPath,
      String relativePluginRequestPath, HttpServletRequest request, ScanWebConfig config)
      throws Exception {

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

    putTransaction(config);
    config.getStatus().setStatus(ScanWebStatus.STATUS_IN_PROGRESS);

    return redirectUrl;
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
      .unmodifiableSet(new HashSet<ScanWebMode>(
          Arrays.asList(ScanWebMode.ASYNCHRONOUS, ScanWebMode.SYNCHRONOUS)));

  @Override
  public Set<ScanWebMode> getSupportedScanWebModes() {
    return SUPPORTED_MODES;
  }

  @Override
  public String getResourceBundleName() {
    return "digitalibscanweb";
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

    ScanWebConfig config = getTransaction(scanWebID);

    if (config == null) {
      String titol = (isGet ? "GET" : "POST") + " " + getName(new Locale("ca"))
          + " PETICIO HA CADUCAT";

      requestTimeOutError(absolutePluginRequestPath, relativePluginRequestPath, query,
          String.valueOf(scanWebID), request, response, titol);

    } else {

      Locale locale = new Locale(config.getLanguageUI());

      if (query.startsWith(SELECCIONA_PERFIL)) {
        seleccionaPerfil(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
            query, request, response, config, locale);
      } else if (query.startsWith(POSTSELECTEDPROFILE)) {

        postSelectedProfile(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
            query, request, response, config, locale);
      } else if (query.startsWith(FINAL_PAGE)) {

        finalPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query,
            request, response, config, locale);
      } else {

        super.requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
            config, query, locale, request, response, isGet);
      }

    }

  }

  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // ----------------------- CONNECTA AMB DIGITAL IB -------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  public static final String POSTSELECTEDPROFILE = "postSelectedProfile";

  protected void postSelectedProfile(String absolutePluginRequestPath,
      String relativePluginRequestPath, String scanWebID, String query,
      HttpServletRequest request, HttpServletResponse response, ScanWebConfig config,
      Locale locale) {

    log.info(" ENTRA DINS postSelectedProfile:  query => ]" + query + "[");

    final String languageUI = locale.getLanguage();
    ApiScanWebSimple api = null;
    String digitalIBTransactionID = null;
    try {

      final String profileCode = query.substring(query.indexOf('/') + 1);
      log.info(" ENTRA DINS postSelectedProfile:  profileCode => ]" + profileCode + "[");

      // Seleccionam el perfil indicat
      ScanWebSimpleProfileRequest profileRequest = new ScanWebSimpleProfileRequest(profileCode,
          languageUI);

      // Agafam l'api de DigitalIB
      api = getApiScanWebSimple();

      ScanWebSimpleAvailableProfile scanWebProfileSelected = api.getProfile(profileRequest);

      // Si val null es que el perfil no existeix
      if (scanWebProfileSelected == null) {
        // TODO XYZ ZZZ TRA TRADUCCIÓ
        throw new Exception("NO EXISTEIX EL PERFIL AMB CODI " + profileCode);
      }

      {
        final int view = ScanWebSimpleGetTransactionIdRequest.VIEW_FULLSCREEN;

        // TODO recollir de l'aplicació
        String funcionariUsername = getInputMetadata("Username del Funcionari", 
            MetadataConstants.FUNCTIONARY_USERNAME, "FUNCTIONARY_USERNAME",
            config.getMetadades(), locale);

        ScanWebSimpleGetTransactionIdRequest transacctionIdRequest;

        switch (scanWebProfileSelected.getProfileType()) {

          case ScanWebSimpleAvailableProfile.PROFILE_TYPE_ONLY_SCAN:

            transacctionIdRequest = new ScanWebSimpleGetTransactionIdRequest(profileCode, view,
                languageUI, funcionariUsername);

          break;

          case ScanWebSimpleAvailableProfile.PROFILE_TYPE_SCAN_AND_SIGNATURE: {
            ScanWebSimpleSignatureParameters signatureParameters = getSignatureParameters(
                config.getMetadades(), locale);

            transacctionIdRequest = new ScanWebSimpleGetTransactionIdRequest(profileCode, view,
                languageUI, funcionariUsername, signatureParameters);
          }
          break;

          case ScanWebSimpleAvailableProfile.PROFILE_TYPE_SCAN_AND_SIGNATURE_AND_CUSTODY: {

            // XYZ ZZZ ZZZ
            throw new Exception("Pefils de tipus Firma i Arxivat no es suporten.");
            /*
             * ScanWebSimpleSignatureParameters signatureParameters = getSignatureParameters();
             * 
             * ScanWebSimpleArxiuRequiredParameters arxiuRequiredParameters;
             * arxiuRequiredParameters = getArxiuRequiredParameters();
             * 
             * // See getArxiuOptionalParameters() sample ScanWebSimpleArxiuOptionalParameters
             * arxiuOptionalParameters = null;
             * 
             * transacctionIdRequest = new ScanWebSimpleGetTransactionIdRequest(profileCode,
             * view, languageUI, funcionariUsername, signatureParameters,
             * arxiuRequiredParameters, arxiuOptionalParameters);
             */
          }

          default:
            // TODO XYZ ZZZ ZZZ TRADUCCIO
            throw new Exception(
                "Tipus de perfil desconegut " + scanWebProfileSelected.getProfileType());

        }

        // Enviam la part comu de la transacció
        digitalIBTransactionID = api.getTransactionID(transacctionIdRequest);
        log.info("languageUI = |" + languageUI + "|");
        log.info("DigitalIB TransactionID = |" + digitalIBTransactionID + "|");

      }

      // Url de retorn de digitalIB a Plugin
      final String returnUrl = absolutePluginRequestPath + FINAL_PAGE + "/"
          + config.getScanWebID();

      log.info("Url retorn de digitalIB a Plugin: " + returnUrl);

      ScanWebSimpleStartTransactionRequest startTransactionInfo;
      startTransactionInfo = new ScanWebSimpleStartTransactionRequest(digitalIBTransactionID,
          returnUrl);

      putTransactionInfo(config, startTransactionInfo);

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
      processError(response, config, e, msg);
    }

  }

  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // ----------------------- SELECCIONA PERFIL -------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  public static final String SELECCIONA_PERFIL = "seleccionaPerfil";

  protected void seleccionaPerfil(String absolutePluginRequestPath,
      String relativePluginRequestPath, String scanWebID, String query,
      HttpServletRequest request, HttpServletResponse response, ScanWebConfig fullInfo,
      Locale languageUI) {

    ApiScanWebSimple api = null;
    try {

      api = getApiScanWebSimple();

      ScanWebSimpleAvailableProfiles profiles = api
          .getAvailableProfiles(languageUI.getLanguage());

      if (profiles == null || profiles.getAvailableProfiles() == null
          || profiles.getAvailableProfiles().size() == 0) {
        throw new Exception("No hi ha perfils disponibles per aquest usuari aplicació ("
            + getUsername() + ")");
      }

      List<ScanWebSimpleAvailableProfile> profilesList = profiles.getAvailableProfiles();

      response.setContentType("text/html");
      response.setCharacterEncoding("utf-8");

      PrintWriter out = generateHeader(request, response, absolutePluginRequestPath,
          relativePluginRequestPath, languageUI);

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
      out.println("   <br/>");

      out.println(" <br/>");
      out.println("  <div class=\"well\" style=\"max-width: 400px; margin: 0 auto 10px;\">");
      for (ScanWebSimpleAvailableProfile profile : profilesList) {

        String url = absolutePluginRequestPath + POSTSELECTEDPROFILE + "/"
            + URLEncoder.encode(profile.getCode());

        out.println(
            "     <button type=\"button\" class=\"btn btn-large btn-block btn-primary\" "
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

      processError(response, fullInfo, e, msg);
    }

  }

  protected void processError(HttpServletResponse response, ScanWebConfig fullInfo,
      Exception e, String msg) {

    log.error(msg, e);

    ScanWebStatus status = fullInfo.getStatus();
    if (status == null) {
      status = new ScanWebStatus();
      fullInfo.setStatus(status);
    }
    status.setStatus(ScanWebStatus.STATUS_FINAL_ERROR);
    status.setErrorMsg(msg);
    status.setErrorException(e);

    String urlFinal = fullInfo.getUrlFinal();

    if (ScanWebMode.ASYNCHRONOUS.equals(fullInfo.getMode())) {
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

  protected void finalPage(String absolutePluginRequestPath, String relativePluginRequestPath,
      String scanWebID, String query, HttpServletRequest request, HttpServletResponse response,
      ScanWebConfig fullInfo, Locale languageUI) {

    ApiScanWebSimple api = null;
    // Obtenim la transacció de Digital IB.
    TransactionInfo info = getTransactionInfo(fullInfo.getScanWebID());

    ScanWebSimpleStartTransactionRequest digitalIbRequest = info.getTransactionRequest();

    try {

      // Obtenim l'api d'ScanWebSimple
      api = getApiScanWebSimple();

      ScanWebSimpleStatus transactionStatus;

      ScanWebStatus localStatus = fullInfo.getStatus();
      ScanWebSimpleScanResult result;
      if (localStatus == null) {

        ScanWebSimpleResultRequest rr = new ScanWebSimpleResultRequest(
            digitalIbRequest.getTransactionID());

        // Obtenim el resultat de l'escanneig
        result = api.getScanWebResult(rr);

        transactionStatus = result.getStatus();
      } else {

        // Error Local

        String sStackTrace;
        Throwable th = localStatus.getErrorException();
        if (th == null) {
          sStackTrace = null;
        } else {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          th.printStackTrace(pw);
          sStackTrace = sw.toString();
        }

        transactionStatus = new ScanWebSimpleStatus(localStatus.getStatus(),
            localStatus.getErrorMsg(), sStackTrace);

        result = null;
      }

      int status = transactionStatus.getStatus();

      switch (status) {

        case ScanWebSimpleStatus.STATUS_REQUESTED_ID: // = 0;
          throw new Exception("S'ha rebut un estat inconsistent del procés"
              + " (requestedid). Pot ser el PLugin no està ben desenvolupat."
              + " Consulti amb el seu administrador.");

        case ScanWebSimpleStatus.STATUS_IN_PROGRESS: // = 1;
          throw new Exception("S'ha rebut un estat inconsistent del procés"
              + " (En Progrés). Pot ser el PLugin no està ben desenvolupat."
              + " Consulti amb el seu administrador.");

        case ScanWebSimpleStatus.STATUS_FINAL_ERROR: // = -1;
        {
          System.err.println("Error durant la realització de l'escaneig/còpia autèntica: "
              + transactionStatus.getErrorMessage());
          String desc = transactionStatus.getErrorStackTrace();
          if (desc != null) {
            System.err.println(desc);
          }
          return;
        }

        case ScanWebSimpleStatus.STATUS_CANCELLED: // = -2;
        {
          System.err.println("Durant el procés, l'usuari ha cancelat la transacció.");
          return;
        }

        case ScanWebSimpleStatus.STATUS_FINAL_OK: // = 2;
        {

          System.out.println(ScanWebSimpleScanResult.toString(result));
          ScannedPlainFile plainFile;
          ScannedSignedFile signedFile;

          ScanWebSimpleFile signed = result.getSignedFile();
          if (signed != null) {

            Boolean attachedDocument;

            ScanWebSimpleFile detached = result.getDetachedSignatureFile();
            if (detached != null) {

              plainFile = new ScannedPlainFile(detached.getNom(), detached.getMime(),
                  detached.getData());

              attachedDocument = true;
            } else {
              attachedDocument = false;

              plainFile = null;
            }

            signedFile = new ScannedSignedFile(signed.getNom(), signed.getMime(),
                signed.getData(), result.getSignedFileInfo().getSignType(), attachedDocument);

          } else {

            // Obtenim el fitxer escannejat
            plainFile = new ScannedPlainFile(result.getScannedFile().getNom(),
                result.getScannedFile().getMime(), result.getScannedFile().getData());

            signedFile = null;

          }

          ScannedDocument doc = new ScannedDocument();

          doc.setScanDate(new Date());
          doc.setScannedPlainFile(plainFile);
          doc.setScannedSignedFile(signedFile);

          List<Metadata> metadatas = new ArrayList<Metadata>();

          ScanWebSimpleFormMetadatas form = result.getFormMetadatas();
          ScanWebSimpleScannedFileInfo scannedFileInfo = result.getScannedFileInfo();
          ScanWebSimpleSignatureParameters signParams = form.getSignatureParameters();
          ScanWebSimpleArxiuRequiredParameters arxiuRequired = form
              .getArxiuRequiredParameters();
          ScanWebSimpleArxiuOptionalParameters arxiuOptional = form
              .getArxiuOptionalParameters();

          ScanWebSimpleSignedFileInfo signedInfo = result.getSignedFileInfo();

          if (signedInfo != null) {
            // PERFIL DE FIRMA
            addMetadata(metadatas, MetadataConstants.ENI_PERFIL_FIRMA,
                signedInfo.getEniPerfilFirma());

            // ROL DE FIRMA
            addMetadata(metadatas, MetadataConstants.EEMGDE_ROL_FIRMA,
                signedInfo.getEniPerfilFirma());

            // NIF del Firmant
            addMetadata(metadatas, MetadataConstants.EEMGDE_FIRMANTE_IDENTIFICADOR,
                signedInfo.getEniSignerAdministrationId());

            // Nom del Firmant
            addMetadata(metadatas, MetadataConstants.EEMGDE_FIRMANTE_NOMBRECOMPLETO,
                signedInfo.getEniSignerName());

            // Tipus Firma ENI
            addMetadata(metadatas, MetadataConstants.ENI_TIPO_FIRMA,
                signedInfo.getEniTipoFirma());

            // Nivell de Firma
            addMetadata(metadatas, MetadataConstants.EEMGDE_NIVEL_DE_FIRMA,
                signedInfo.getEniSignLevel());
          }

          // Nombre del Documento
          addMetadata(metadatas, MetadataConstants.TITULO_DOCUMENTO,
              form.getTransactionName());

          // PROFUNDITAT DE COLOR
          processarMetadadaProfundadadColor(metadatas, scannedFileInfo);

          // RESOLUCIO
          addMetadata(metadatas, MetadataConstants.EEMGDE_RESOLUCION,
              scannedFileInfo.getPppResolution());

          // OCR
          addMetadata(metadatas, MetadataConstants.OCR, scannedFileInfo.getOcr());

          // FUNCIONARI
          addMetadata(metadatas, MetadataConstants.FUNCTIONARY_USERNAME,
              form.getFunctionaryUsername());
          if (signParams != null) {
            addMetadata(metadatas, MetadataConstants.FUNCTIONARY_ADMINISTRATIONID,
                signParams.getFunctionaryAdministrationID());
            addMetadata(metadatas, MetadataConstants.FUNCTIONARY_FULLNAME,
                signParams.getFunctionaryFullName());
          }

          // IDIOMA
          addMetadata(metadatas, MetadataConstants.ENI_IDIOMA,
              signParams.getDocumentLanguage());

          if (arxiuRequired != null) {
            // TIPUS DOCUMENTAL
            addMetadata(metadatas, MetadataConstants.ENI_TIPO_DOCUMENTAL,
                arxiuRequired.getDocumentType());

            // ORIGEN
            processarMetadadaOrigen(metadatas, arxiuRequired);

            // ESTAT ELABORACIO
            processarMetadadaEstatElaboracio(metadatas, arxiuRequired);

            // INTERESSATS
            processarMetadadaInteressats(metadatas, arxiuRequired);

            //
            // arxiuRequired.getAffectedOrganisms()

            // CIUTADA
            addMetadata(metadatas, MetadataConstants.CITIZEN_ADMINISTRATIONID,
                arxiuRequired.getCitizenAdministrationID());
            addMetadata(metadatas, MetadataConstants.CITIZEN_FULLNAME,
                arxiuRequired.getCitizenFullName());
          }

          if (arxiuOptional != null) {
            // CODI PROCEDIMENT
            addMetadata(metadatas, MetadataConstants.ENI_ID_TRAMITE,
                arxiuOptional.getProcedureCode());

            // NOM PROCEDIMENT
            addMetadata(metadatas, MetadataConstants.ENI_ID_TRAMITE + ".description",
                arxiuOptional.getProcedureName());
          }

          // RESTA DE METADADES
          copyMetadatas(form.getAdditionalMetadatas(), metadatas);

          doc.setMetadatas(metadatas);

          fullInfo.getScannedFiles().add(doc);
          ScanWebStatus sws = new ScanWebStatus();
          sws.setStatus(ScanWebStatus.STATUS_FINAL_OK);
          fullInfo.setStatus(sws); //

          if (ScanWebMode.ASYNCHRONOUS.equals(fullInfo.getMode())) {

            String msg = getTraduccio("finalproces", languageUI);

            printSimpleHtmlPage(response, msg);

          } else {
            final String url;
            url = fullInfo.getUrlFinal();
            log.info("Redireccionam a " + url);
            sendRedirect(response, url);
          }

        } // Final Case Firma OK
      } // Final Switch Firma

    } catch (Exception e) {
      // TODO: handle exception
      String msg = " Error processant resultats: " + e.getMessage();

      processError(response, fullInfo, e, msg);

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
      ScanWebSimpleArxiuRequiredParameters arxiuRequired) {
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
      ScanWebSimpleArxiuRequiredParameters arxiuRequired) {
    if (arxiuRequired.getDocumentElaborationState() != null) {

      String des = arxiuRequired.getDocumentElaborationState();

      if (ScanWebSimpleArxiuRequiredParameters.DOCUMENTELABORATIONSTATE_ORIGINAL.equals(des)) {
        addMetadata(metadatas, MetadataConstants.ENI_ESTADO_ELABORACION,
            MetadataConstants.EstadoElaboracionConstants.ESTADO_ELABORACION_ORIGINAL);
      } else if (ScanWebSimpleArxiuRequiredParameters.DOCUMENTELABORATIONSTATE_COPIA_CF
          .equals(des)) {
        addMetadata(metadatas, MetadataConstants.ENI_ESTADO_ELABORACION,
            MetadataConstants.EstadoElaboracionConstants.ESTADO_ELABORACION_COPIA_CF);
      } else if (ScanWebSimpleArxiuRequiredParameters.DOCUMENTELABORATIONSTATE_COPIA_DP
          .equals(des)) {
        addMetadata(metadatas, MetadataConstants.ENI_ESTADO_ELABORACION,
            MetadataConstants.EstadoElaboracionConstants.ESTADO_ELABORACION_COPIA_DP);
      } else if (ScanWebSimpleArxiuRequiredParameters.DOCUMENTELABORATIONSTATE_COPIA_PR
          .equals(des)) {
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
      ScanWebSimpleArxiuRequiredParameters arxiuRequired) {
    if (arxiuRequired.getDocumentOrigen() != null) {
      switch (arxiuRequired.getDocumentOrigen()) {
        case ScanWebSimpleArxiuRequiredParameters.DOCUMENTORIGEN_ADMINISTRACIO:
          addMetadata(metadatas, MetadataConstants.ENI_ORIGEN,
              MetadataConstants.OrigenConstants.ADMINISTRACION);
        break;
        case ScanWebSimpleArxiuRequiredParameters.DOCUMENTORIGEN_CIUTADA:
          addMetadata(metadatas, MetadataConstants.ENI_ORIGEN,
              MetadataConstants.OrigenConstants.CIUDADANO);
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
      ScanWebSimpleScannedFileInfo scannedFileInfo) {
    Integer profundidadDeColor;
    String pixelType;
    if (scannedFileInfo.getPixelType() == null) {
      profundidadDeColor = null;
      pixelType = null;
    } else {

      switch (scannedFileInfo.getPixelType()) {

        case ScanWebSimpleScannedFileInfo.PIXEL_TYPE_BLACK_WHITE:
          profundidadDeColor = MetadataConstants.ProfundidadColorConstants.BW;
          // XYZ ZZZ TRA
          pixelType = "Blanc i negre";
        break;
        case ScanWebSimpleScannedFileInfo.PIXEL_TYPE_GRAY:
          profundidadDeColor = MetadataConstants.ProfundidadColorConstants.GRAY;
          // XYZ ZZZ TRA
          pixelType = "Escala de grisos";
        break;

        case ScanWebSimpleScannedFileInfo.PIXEL_TYPE_COLOR:
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
      metadatas
          .add(new Metadata(MetadataConstants.EEMGDE_PROFUNDIDAD_COLOR, profundidadDeColor));
      metadatas.add(new Metadata(MetadataConstants.EEMGDE_PROFUNDIDAD_COLOR + ".description",
          pixelType));
    }
  }

  public void copyMetadatas(List<ScanWebSimpleKeyValue> digitalIBMetadatas,
      List<Metadata> metadatas) {

    if (digitalIBMetadatas == null || digitalIBMetadatas.isEmpty()) {
      return;
    }

    for (ScanWebSimpleKeyValue metadata : digitalIBMetadatas) {
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
    synchronized (digitalibTransactions22) {
      super.endScanWebTransaction(scanWebID, request);
      digitalibTransactions22.remove(scanWebID);
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
  }

  protected ApiScanWebSimple getApiScanWebSimple() throws Exception {

    return new ApiScanWebSimpleJersey(getUrl(), getUsername(), getPassword());

  }

  /**
   * @return
   */
  private ScanWebSimpleSignatureParameters getSignatureParameters(List<Metadata> metadatas,
      Locale locale) throws Exception {

    final String funcionariNom = getInputMetadata("Nom complet del Funcionari", 
        MetadataConstants.FUNCTIONARY_FULLNAME, "FUNCTIONARY_FULLNAME",
        metadatas, locale);
    final String funcionariNif = getInputMetadata("Nif del Funcionari",
        MetadataConstants.FUNCTIONARY_ADMINISTRATIONID, 
        "FUNCTIONARY_ADMINISTRATIONID", metadatas, locale);
    String languageDoc;
    try {
      languageDoc = getInputMetadata("Idioma del Document",
        MetadataConstants.EEMGDE_IDIOMA, "EEMGDE_IDIOMA", metadatas, locale);
    } catch(Exception e) {
      languageDoc = null;
    }

    ScanWebSimpleSignatureParameters signatureParameters;
    signatureParameters = new ScanWebSimpleSignatureParameters(languageDoc, funcionariNom,
        funcionariNif);
    return signatureParameters;
  }

  private String getInputMetadata(String description, String metadataKey,
      String metadataName, List<Metadata> metadatas, Locale locale) throws Exception  {

    /*
     * String metadataKey = getProperty(PROPERTY_BASE + "metadata." + partialName);
     * 
     * if (metadataKey == null || metadataKey.trim().length() == 0) { log.warn("La propietat "
     * + getPropertyName(PROPERTY_BASE + partialName) + " no s'ha definit."); return null; }
     * 
     * if (metadatas == null || metadatas.size() == 0) {
     * log.warn("La llista de metadades està buida. No podem trobat el valor per la metadada "
     * + metadataKey); return null; }
     */

    String value = null;
    for (Metadata metadata : metadatas) {
      if (metadata.getKey().equals(metadataKey)) {
        value = metadata.getValue();
        break;
      }
    }

    if (value == null) {
      // Cercam dins de les Propietats
      value = getProperty(PROPERTY_BASE  + metadataKey);
    }

    if (value == null) {
        // TODO XYZ ZZZ TRA TRADUCCIÓ
        throw new Exception(
            "NO s'ha definit el camp " + description +"  ni des de les Metadades de ScanWebConfig"
                + "(Metadada  MetadataConstants." + metadataName + "=\""
                + metadataKey + "\") ni des de la propietat "
                + getPropertyName(PROPERTY_BASE + metadataKey) + " del plugin "
                + getName(locale));

    } 

    log.info("XYZ ZZZ   KEY[" + metadataKey + "] => " + value);
    
    

    return value;

  }

/*
 * private ScanWebSimpleArxiuRequiredParameters getArxiuRequiredParameters() { final
 * List<String> interessats = new ArrayList<String>(Arrays.asList("12345678X", "87654321Z"));
 * 
 * 
 * // * ScanWebSimpleArxiuRequiredParameters.CIUTADA // *
 * ScanWebSimpleArxiuRequiredParameters.ORIGEN_ADMINISTRACIO
 * 
 * final int origen = ScanWebSimpleArxiuRequiredParameters.DOCUMENTORIGEN_ADMINISTRACIO;
 * 
 * 
 * // * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_ORIGINAL // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_COPIA_CF // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_COPIA_DP // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_COPIA_PR
 * 
 * final String documentEstatElaboracio =
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTELABORATIONSTATE_ORIGINAL;
 * 
 * 
 * // * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_RESOLUCIO // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ACORD // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_CONTRACTE // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_CONVENI // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_DECLARACIO // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_COMUNICACIO // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_NOTIFICACIO // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_PUBLICACIO // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_JUSTIFICANT_RECEPCIO // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ACTA // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_CERTIFICAT // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_DILIGENCIA // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_INFORME // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_SOLICITUD // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_DENUNCIA // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ALEGACIO // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_RECURS // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_COMUNICACIO_CIUTADA // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_FACTURA // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ALTRES_INCAUTATS // * @see
 * ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ALTRES
 * 
 * final String documentTipus = ScanWebSimpleArxiuRequiredParameters.DOCUMENTTYPE_RESOLUCIO;
 * 
 * String ciutadaNif = "11223344C";
 * 
 * String ciutadaNom = "Pep Gonella";
 * 
 * List<String> organs = new ArrayList<String>(Arrays.asList("A04013511"));
 * 
 * ScanWebSimpleArxiuRequiredParameters arxiuRequiredParameters; arxiuRequiredParameters = new
 * ScanWebSimpleArxiuRequiredParameters(ciutadaNif, ciutadaNom, documentEstatElaboracio,
 * documentTipus, origen, interessats, organs); return arxiuRequiredParameters; }
 */
}