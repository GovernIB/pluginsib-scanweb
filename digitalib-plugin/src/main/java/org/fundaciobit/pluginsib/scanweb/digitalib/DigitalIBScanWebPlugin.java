package org.fundaciobit.pluginsib.scanweb.digitalib;

import org.fundaciobit.apisib.apiscanwebsimple.v1.ApiScanWebSimple;
import org.fundaciobit.apisib.apiscanwebsimple.v1.beans.*;
import org.fundaciobit.apisib.apiscanwebsimple.v1.jersey.ApiScanWebSimpleJersey;
import org.fundaciobit.plugins.scanweb.api.*;
import org.fundaciobit.pluginsib.core.utils.Metadata;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.*;

/**
 * 
 * Implementació del plugin per escannejar un document amb digital IB.
 * 
 * @author mgonzalez
 * 
 */
public class DigitalIBScanWebPlugin extends AbstractScanWebPlugin {

  private static final String PROPERTY_BASE = SCANWEB_PLUGINSIB_BASE_PROPERTY + "digitalib.";

  private Map<String, ScanWebSimpleStartTransactionRequest> digitalibTransactions = new HashMap<String, ScanWebSimpleStartTransactionRequest>();

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
    
    if(profileCode == null) {
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
      .unmodifiableSet(new HashSet<ScanWebMode>(Arrays.asList(ScanWebMode.ASYNCHRONOUS,
          ScanWebMode.SYNCHRONOUS)));

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
  // ----------------------- CONNECTA AMB DIGITAL IB -------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  
  public static final String POSTSELECTEDPROFILE = "postSelectedProfile";

  protected void postSelectedProfile(String absolutePluginRequestPath, 
      String relativePluginRequestPath,
      String scanWebID, String query, HttpServletRequest request,
      HttpServletResponse response, ScanWebConfig config, Locale locale) {
    
    
    log.info(" ENTRA DINS postSelectedProfile:  query => ]" + query  + "[" );
        
    final String languageUI = locale.getLanguage();
    ApiScanWebSimple api = null;
    String transactionID = null;
    try {
    
    final String profileCode = query.substring(query.indexOf('/') + 1);
    log.info(" ENTRA DINS postSelectedProfile:  profileCode => ]" + profileCode  + "[" );

    // Seleccionam el perfil indicat
    ScanWebSimpleProfileRequest profileRequest = new ScanWebSimpleProfileRequest(profileCode,
        languageUI);
    
    // Agafam l'api de DigitalIB
    api = getApiScanWebSimple();
    
    ScanWebSimpleAvailableProfile scanWebProfileSelected = api.getProfile(profileRequest);

    // Si val null es que el perfil no existeix
    if (scanWebProfileSelected == null) {
      // TODO TRADUCCIÓ
      throw new Exception("NO EXISTEIX EL PERFIL AMB CODI " + profileCode);
    }

    {

      final int view = ScanWebSimpleGetTransactionIdRequest.VIEW_FULLSCREEN;

      // TODO recollir de l'aplicació
      String funcionariUsername = "u06666";

      ScanWebSimpleGetTransactionIdRequest transacctionIdRequest;

      switch (scanWebProfileSelected.getProfileType()) {

        case ScanWebSimpleAvailableProfile.PROFILE_TYPE_ONLY_SCAN:

          transacctionIdRequest = new ScanWebSimpleGetTransactionIdRequest(profileCode, view,
              languageUI, funcionariUsername);

        break;

        case ScanWebSimpleAvailableProfile.PROFILE_TYPE_SCAN_AND_SIGNATURE: {
          ScanWebSimpleSignatureParameters signatureParameters = getSignatureParameters();

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
          throw new Exception("Tipus de perfil desconegut "
              + scanWebProfileSelected.getProfileType());

      }

      // Enviam la part comu de la transacció
      transactionID = api.getTransactionID(transacctionIdRequest);
      log.info("languageUI = |" + languageUI + "|");
      log.info("TransactionID = |" + transactionID + "|");
    }

    // Url de retorn de digitalIB a Plugin
    final String returnUrl = absolutePluginRequestPath + FINAL_PAGE + "/"
        + config.getScanWebID();

    log.info("Url retorn de digitalIB a Plugin: " + returnUrl);

    ScanWebSimpleStartTransactionRequest startTransactionInfo;
    startTransactionInfo = new ScanWebSimpleStartTransactionRequest(transactionID, returnUrl);
    
    this.digitalibTransactions.put(config.getScanWebID(), startTransactionInfo);

    // Url per anar a Digital IB
    String redirectUrl = api.startTransaction(startTransactionInfo);

    log.info("RedirectUrl  a digitalIB: " + redirectUrl);
    
    response.sendRedirect(redirectUrl);

    } catch(Exception e) {
      // TODO: handle exception
      log.error(" Error Realitzant la comunicació amb DigitalIB: " + e.getMessage(), e);
      
      if (api != null && transactionID != null) {
        try {
          api.closeTransaction(transactionID);
        } catch (Throwable th) {
          th.printStackTrace();
        }
      }
      log.info("Redireccionam a " + config.getUrlFinal());
      try {
        response.sendRedirect(config.getUrlFinal());
      } catch (IOException e2) {
        log.error(e.getMessage(), e2);
      }
    
    }

    
  }
  
  
  
  
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // ----------------------- SELECCIONA PERFIL -------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  
  public static final String SELECCIONA_PERFIL = "seleccionaPerfil";

  protected void seleccionaPerfil(String absolutePluginRequestPath, 
      String relativePluginRequestPath,
      String scanWebID, String query, HttpServletRequest request,
      HttpServletResponse response, ScanWebConfig fullInfo, Locale languageUI) {

    ApiScanWebSimple api = null;
  try {
    
    api = getApiScanWebSimple();
    
    ScanWebSimpleAvailableProfiles profiles = api.getAvailableProfiles(languageUI.getLanguage());
    
    if (profiles == null || profiles.getAvailableProfiles() == null 
        || profiles.getAvailableProfiles().size() == 0) {
      throw new Exception("No hi ha perfils disponibles per aquest usuari aplicació (" + getUsername() + ")");
    }
    
    
    List<ScanWebSimpleAvailableProfile> profilesList =  profiles.getAvailableProfiles();
    
    response.setContentType("text/html");
    response.setCharacterEncoding("utf-8");

    PrintWriter out = generateHeader(request, response, absolutePluginRequestPath, relativePluginRequestPath, languageUI);

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
    for(ScanWebSimpleAvailableProfile profile : profilesList) {
      
      String url = absolutePluginRequestPath +  POSTSELECTEDPROFILE + "/" + URLEncoder.encode(profile.getCode());
      
      out.println("     <button type=\"button\" class=\"btn btn-large btn-block btn-primary\" "
          + "onclick=\"location.href='" + url  + "'\">");
      out.println("     <b>" + profile.getName()+ "</b><br>");
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
    e.printStackTrace();
    
    ScanWebStatus status = fullInfo.getStatus();
    
    status.setStatus(ScanWebStatus.STATUS_FINAL_ERROR);
  
    status.setErrorMsg(" Error mostrant la pàgina de selecció de Perfils: " + e.getMessage());
    
    status.setErrorException(e);
    
    log.info("Error: Redireccionam a " + fullInfo.getUrlFinal());

    try {
      response.sendRedirect(fullInfo.getUrlFinal());
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
      String scanWebID, String query, HttpServletRequest request,
      HttpServletResponse response, ScanWebConfig fullInfo, Locale languageUI) {

    ApiScanWebSimple api = null;
    // Obtenim la transacció de Digital IB.
    ScanWebSimpleStartTransactionRequest digitalIbRequest = digitalibTransactions.get(fullInfo
        .getScanWebID());

    try {
      // Obtenim l'api d'ScanWebSimple
      api = getApiScanWebSimple();

      // Obtenim el resultat de l'escanneig

      ScanWebSimpleResultRequest rr = new ScanWebSimpleResultRequest(
          digitalIbRequest.getTransactionID());

      ScanWebSimpleScanResult result = api.getScanWebResult(rr);

      ScanWebSimpleStatus transactionStatus = result.getStatus();

      int status = transactionStatus.getStatus();

      System.out.println(ScanWebSimpleScanResult.toString(result));

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
            plainFile = new ScannedPlainFile(result.getScannedFile().getNom(), result
                .getScannedFile().getMime(), result.getScannedFile().getData());

            signedFile = null;

          }

          ScannedDocument doc = new ScannedDocument();

          doc.setScanDate(new Date());
          doc.setScannedPlainFile(plainFile);
          doc.setScannedSignedFile(signedFile);

          List<Metadata> metadatas = new ArrayList<Metadata>();

          ScanWebSimpleFormMetadatas form = result.getFormMetadatas();

          metadatas.add(new Metadata("title", form.getTransactionName()));

          ScanWebSimpleScannedFileInfo scannedFileInfo = result.getScannedFileInfo();

          if (scannedFileInfo.getPixelType() != null) {
            metadatas.add(new Metadata("pixelType", scannedFileInfo.getPixelType()));
          }

          String pixelType;
          if (scannedFileInfo.getPixelType() == null) {
            pixelType = null;
          } else {
            
            switch (scannedFileInfo.getPixelType()) {
  
              case ScanWebSimpleScannedFileInfo.PIXEL_TYPE_BLACK_WHITE:
                pixelType = "Blanc i negre";
              break;
              case ScanWebSimpleScannedFileInfo.PIXEL_TYPE_GRAY:
                pixelType = "Escala de grisos";
              break;
  
              case ScanWebSimpleScannedFileInfo.PIXEL_TYPE_COLOR:
                pixelType = "Color";
              break;
  
              default:
               pixelType = null;
            }
          }

          if (pixelType != null) {
            metadatas.add(new Metadata("pixelType.string", pixelType));
          }

          if (scannedFileInfo.getPppResolution() != null) {
            metadatas.add(new Metadata("pppResolution", scannedFileInfo.getPppResolution()));
          } 

          doc.setMetadatas(metadatas);

          fullInfo.getScannedFiles().add(doc);
          fullInfo.getStatus().setStatus(ScanWebStatus.STATUS_FINAL_OK);

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
            out.println("<p><h2>" + getTraduccio("finalproces", languageUI) + "</h2><p>\n");
            out.println("</td></tr>\n");
            out.println("</table>\n");
            out.println("</body>\n");
            out.println("</html>\n");

            out.flush();

            return;
          } else {
            final String url;
            url = fullInfo.getUrlFinal();
            log.info("Redireccionam a " + url);
            sendRedirect(response, url);
            return;
          }

        } // Final Case Firma OK
      } // Final Switch Firma

    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
    } finally {
      if (api != null && digitalIbRequest.getTransactionID() != null) {
        try {
          api.closeTransaction(digitalIbRequest.getTransactionID());
        } catch (Throwable th) {
          th.printStackTrace();
        }
      }
    }

    log.info("Redireccionam a " + fullInfo.getUrlFinal());

    try {
      response.sendRedirect(fullInfo.getUrlFinal());
    } catch (IOException e) {
      log.error(e.getMessage(), e);
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
  private ScanWebSimpleSignatureParameters getSignatureParameters() {
    final String languageDoc = "ca";
    final String funcionariNom = "Funcionari DeProfessio";
    final String funcionariNif = "12345678X";

    ScanWebSimpleSignatureParameters signatureParameters;
    signatureParameters = new ScanWebSimpleSignatureParameters(languageDoc, funcionariNom,
        funcionariNif);
    return signatureParameters;
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
   * ScanWebSimpleArxiuRequiredParameters arxiuRequiredParameters; arxiuRequiredParameters =
   * new ScanWebSimpleArxiuRequiredParameters(ciutadaNif, ciutadaNom, documentEstatElaboracio,
   * documentTipus, origen, interessats, organs); return arxiuRequiredParameters; }
   */
}