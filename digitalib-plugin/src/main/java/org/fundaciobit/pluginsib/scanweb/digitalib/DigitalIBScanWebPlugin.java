package org.fundaciobit.pluginsib.scanweb.digitalib;

import org.fundaciobit.apisib.apiscanwebsimple.v1.ApiScanWebSimple;
import org.fundaciobit.apisib.apiscanwebsimple.v1.beans.*;
import org.fundaciobit.apisib.apiscanwebsimple.v1.jersey.ApiScanWebSimpleJersey;
import org.fundaciobit.plugins.scanweb.api.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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


   //Propietats del plugin
   public String getProfile() throws Exception {
      return getPropertyRequired(PROPERTY_BASE + "profile");
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

      ApiScanWebSimple api = null;
      String transactionID = null;
      final String languageUI = config.getLanguageUI();

      //Agafam l'api de DigitalIB
      api = getApiScanWebSimple();

      //Obtenim els diferents profiles definits a DIGITALIB
      ScanWebSimpleAvailableProfiles profiles = api.getAvailableProfiles(languageUI);

      List<ScanWebSimpleAvailableProfile> profilesList = profiles.getAvailableProfiles();

      //Si no hi ha profiles definits llançam excepció
      if (profilesList == null || profilesList.size() == 0) {
         //TODO FALTA TRADUIR
         throw new Exception("NO HI HA PERFILS PER AQUEST USUARI APLICACIÓ");
      }


      ScanWebSimpleAvailableProfile scanWebProfileSelected = null;

      //Obtenim el profile amb el que volem fer feina(el que ens indicaran des de l'aplicació que l'emplearà.
      final String profileCode = getProfile();

      //Seleccionam el perfil indicat
      for (ScanWebSimpleAvailableProfile profile : profilesList) {
         if (profileCode.equals(profile.getCode())) {
            scanWebProfileSelected = profile;
            break;
         }
      }

      //Si no han triat cap perfil es perque el perfil indicat no està dins la llista de perfils disponibles
      if (scanWebProfileSelected == null) {
         //TODO TRADUCCIÓ
         throw new Exception("NO EXISTEIX EL PERFIL INDICAT DINS LA LLISTA DE PERFILS DISPONIBLES");
      }

      {

         final int view = ScanWebSimpleGetTransactionIdRequest.VIEW_FULLSCREEN;

         //TODO recollir de l'aplicació
         String funcionariUsername = "u06666";

         ScanWebSimpleGetTransactionIdRequest transacctionIdRequest;

         switch (scanWebProfileSelected.getProfileType()) {

            case ScanWebSimpleAvailableProfile.PROFILE_TYPE_ONLY_SCAN:

               transacctionIdRequest = new ScanWebSimpleGetTransactionIdRequest(profileCode,
                  view, languageUI, funcionariUsername);

               break;

            case ScanWebSimpleAvailableProfile.PROFILE_TYPE_SCAN_AND_SIGNATURE: {
               ScanWebSimpleSignatureParameters signatureParameters = getSignatureParameters();

               transacctionIdRequest = new ScanWebSimpleGetTransactionIdRequest(profileCode,
                  view, languageUI, funcionariUsername, signatureParameters);
            }

            break;

            case ScanWebSimpleAvailableProfile.PROFILE_TYPE_SCAN_AND_SIGNATURE_AND_CUSTODY: {

               ScanWebSimpleSignatureParameters signatureParameters = getSignatureParameters();

               ScanWebSimpleArxiuRequiredParameters arxiuRequiredParameters;
               arxiuRequiredParameters = getArxiuRequiredParameters();

               // See getArxiuOptionalParameters() sample
               ScanWebSimpleArxiuOptionalParameters arxiuOptionalParameters = null;

               transacctionIdRequest = new ScanWebSimpleGetTransactionIdRequest(profileCode,
                  view, languageUI, funcionariUsername, signatureParameters,
                  arxiuRequiredParameters, arxiuOptionalParameters);
            }
            break;

            default:
               //TODO TRADUCCIO
               throw new Exception("Tipus de perfil desconegut "
                  + scanWebProfileSelected.getProfileType());

         }

         // Enviam la part comu de la transacció
         transactionID = api.getTransactionID(transacctionIdRequest);
         log.info("languageUI = |" + languageUI + "|");
         log.info("TransactionID = |" + transactionID + "|");
      }

      //Url de retorn de digitalIB a Plugin
      final String returnUrl = absolutePluginRequestPath + "/" + FINAL_PAGE + "/" + config.getScanWebID();

      log.info("Url retorn de digitalIB a Plugin: " + returnUrl);

      ScanWebSimpleStartTransactionRequest startTransactionInfo;
      startTransactionInfo = new ScanWebSimpleStartTransactionRequest(transactionID, returnUrl);

      //Url per anar a Digital IB
      String redirectUrl = api.startTransaction(startTransactionInfo);

      log.info("RedirectUrl  a digitalIB: " + redirectUrl);

      putTransaction(config);
      config.getStatus().setStatus(ScanWebStatus.STATUS_IN_PROGRESS);

      this.digitalibTransactions.put(config.getScanWebID(), startTransactionInfo);

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

      ScanWebConfig fullInfo = getTransaction(scanWebID);

      if (fullInfo == null) {
         String titol = (isGet ? "GET" : "POST") + " " + getName(new Locale("ca"))
            + " PETICIO HA CADUCAT";

         requestTimeOutError(absolutePluginRequestPath, relativePluginRequestPath, query,
            String.valueOf(scanWebID), request, response, titol);

      } else {

         Locale languageUI = new Locale(fullInfo.getLanguageUI());

         if (query.startsWith(FINAL_PAGE)) {

            finalPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query,
               request, response, fullInfo, languageUI);
         } else {

            super.requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
               fullInfo, query, languageUI, request, response, isGet);
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
      //Obtenim la transacció de Digital IB.
      ScanWebSimpleStartTransactionRequest digitalIbRequest = digitalibTransactions.get(fullInfo.getScanWebID());

      try {
         //Obtenim l'api d'ScanWebSimple
         api = getApiScanWebSimple();

         //Obtenim el resultat de l'escanneig
         ScanWebSimpleScanResult result = api.getScanWebResult(digitalIbRequest.getTransactionID());

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

               {//Obtenim la info del document detached si el te.
                  ScanWebSimpleFile detachedSignInfo = result.getDetachedSignatureFile();

                  if (detachedSignInfo != null) {
                     //TODO s'ha d'arreglar si se volen només pdf o tots els tipus.

                     throw new Exception("No soportam detached");
                  }
               }

               //Obtenim el fitxer escannejat
               ScannedPlainFile plainFile = new ScannedPlainFile(result.getScannedFile().getNom(), result.getScannedFile().getMime(),
                  result.getScannedFile().getData());

               ScannedDocument doc = new ScannedDocument();

               doc.setScanDate(new Date());
               doc.setScannedPlainFile(plainFile);


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

   ;


   protected ApiScanWebSimple getApiScanWebSimple() throws Exception {

      return new ApiScanWebSimpleJersey(getUrl(), getUsername(),
         getPassword());

   }


   /**
    * @return
    */
   public static ScanWebSimpleSignatureParameters getSignatureParameters() {
      final String languageDoc = "ca";
      final String funcionariNom = "Funcionari DeProfessio";
      final String funcionariNif = "12345678X";

      ScanWebSimpleSignatureParameters signatureParameters;
      signatureParameters = new ScanWebSimpleSignatureParameters(languageDoc, funcionariNom,
         funcionariNif);
      return signatureParameters;
   }


   //TODO Aixó s'enviarà desde l'aplicació
   public static ScanWebSimpleArxiuRequiredParameters getArxiuRequiredParameters() {
      final List<String> interessats = new ArrayList<String>(Arrays.asList("12345678X",
         "87654321Z"));

      /**
       * ScanWebSimpleArxiuRequiredParameters.CIUTADA
       * ScanWebSimpleArxiuRequiredParameters.ORIGEN_ADMINISTRACIO
       */
      final int origen = ScanWebSimpleArxiuRequiredParameters.ORIGEN_ADMINISTRACIO;

      /**
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_ORIGINAL
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_COPIA_CF
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_COPIA_DP
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_COPIA_PR
       */
      final String documentEstatElaboracio = ScanWebSimpleArxiuRequiredParameters.DOCUMENTESTATELABORACIO_ORIGINAL;

      /**
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_RESOLUCIO
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ACORD
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_CONTRACTE
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_CONVENI
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_DECLARACIO
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_COMUNICACIO
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_NOTIFICACIO
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_PUBLICACIO
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_JUSTIFICANT_RECEPCIO
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ACTA
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_CERTIFICAT
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_DILIGENCIA
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_INFORME
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_SOLICITUD
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_DENUNCIA
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ALEGACIO
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_RECURS
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_COMUNICACIO_CIUTADA
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_FACTURA
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ALTRES_INCAUTATS
       * @see ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_ALTRES
       */
      final String documentTipus = ScanWebSimpleArxiuRequiredParameters.DOCUMENTTIPUS_RESOLUCIO;

      String ciutadaNif = "11223344C";

      String ciutadaNom = "Pep Gonella";

      List<String> organs = new ArrayList<String>(Arrays.asList("A04013511"));

      ScanWebSimpleArxiuRequiredParameters arxiuRequiredParameters;
      arxiuRequiredParameters = new ScanWebSimpleArxiuRequiredParameters(ciutadaNif, ciutadaNom,
         interessats, origen, documentEstatElaboracio, documentTipus, organs);
      return arxiuRequiredParameters;
   }


}