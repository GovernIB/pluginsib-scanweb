package org.fundaciobit.pluginsib.scanweb.springboottester.controller;

import org.apache.log4j.Logger;
import org.fundaciobit.plugins.documentcustody.api.AnnexCustody;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebDocument;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebMode;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebRequest;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebRequestCustodyInfo;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebRequestSignatureInfo;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebStatus;
import org.fundaciobit.pluginsib.scanweb.springboottester.form.ScanWebConfigForm;
import org.fundaciobit.pluginsib.scanweb.springboottester.form.ScanWebConfigValidator;
import org.fundaciobit.pluginsib.scanweb.springboottester.logic.ScanWebInfoTester;
import org.fundaciobit.pluginsib.scanweb.springboottester.logic.ScanWebModuleEjb;
import org.fundaciobit.pluginsib.core.utils.Metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.Writer;
import java.util.*;

/**
 *
 * @author anadal
 *
 */
@Controller
@RequestMapping(value = ScanWebProcessController.CONTEXTWEB)
@SessionAttributes(types = { ScanWebConfigForm.class })
public class ScanWebProcessController {

    /** Logger for this class and subclasses */
    protected final Logger log = Logger.getLogger(getClass());

    public static final String CONTEXTWEB = "/common/scan";

    public static final String LAST_SCANNED_FILES = "LAST_SCANNED_FILES";

    public static final String LAST_CONFIG = "LAST_CONFIG";
    
    public static final String LAST_RESULT = "LAST_RESULT";

    @Autowired
    protected ScanWebConfigValidator scanWebValidator;

    
    protected ScanWebModuleEjb scanWebModuleEjb = new ScanWebModuleEjb();

    /**
     * 
     */
    public ScanWebProcessController() {
        super();
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public ModelAndView scanWebGet(HttpServletRequest request) throws Exception {

        ScanWebConfigForm form = new ScanWebConfigForm();

        form.setMode("S"); // S = Sincron | A=Asincron

        // Acceptam qualsevol combinació        
        form.setFlag(ScanWebDocument.FLAG_PLAIN);
        String id = String.valueOf(ScanWebModuleController.generateUniqueScanWebID());
        form.setId(id);
        form.setTransactionName("ScanWeb Tester " + id);
        
        // FIRMA 
        form.setUsername("pgonella");
        form.setNif("87654321Z");
        form.setNom("Pep Gonella");
        form.setFunctionaryUnitDIR3("A04026951"); // Dirección General de Pesca y Medio Marino

        List<String> supportedTypes = new ArrayList<String>();
        supportedTypes.add(ScanWebDocument.SCANTYPE_MIME_PDF);
        supportedTypes.add(ScanWebDocument.SCANTYPE_MIME_JPG);
        supportedTypes.add(ScanWebDocument.SCANTYPE_MIME_PNG);
        supportedTypes.add(ScanWebDocument.SCANTYPE_MIME_GIF);
        supportedTypes.add(ScanWebDocument.SCANTYPE_MIME_TIFF);

        List<String> supportedFlags = new ArrayList<String>();
        supportedFlags.add(ScanWebDocument.FLAG_PLAIN);
        supportedFlags.add(ScanWebDocument.FLAG_SIGNED);
        supportedFlags.add(ScanWebDocument.FLAG_SIGNED_WITH_TIMESTAMP);
        //supportedFlags.add(ScanWebDocument.FLAG_SIGNED_AND_CUSTODY);

        ModelAndView mav = new ModelAndView("scanWebForm");
        mav.addObject(form);

        mav.addObject("supportedTypes", supportedTypes);
        mav.addObject("supportedFlags", supportedFlags);

        return mav;

    }

    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public ModelAndView scanWebPost(HttpServletRequest request, HttpServletResponse response,
            @ModelAttribute org.fundaciobit.pluginsib.scanweb.springboottester.form.ScanWebConfigForm form, BindingResult result) throws Exception {

        scanWebValidator.validate(form, result);

        if (result.hasErrors()) {
            ModelAndView mav = new ModelAndView("scanWebForm");
            mav.addObject(form);
            return mav;
        }

        final String scanWebID = form.getId();

        // TODO per ara està buit
        final List<Metadata> metadades = new ArrayList<Metadata>();

        ScanWebMode mode = "S".equals("" + form.getMode()) ? ScanWebMode.SYNCHRONOUS : ScanWebMode.ASYNCHRONOUS;

        String relativeControllerBase = ScanWebModuleController.getRelativeControllerBase(request, CONTEXTWEB);
        final String urlFinal = relativeControllerBase + "/final/" + scanWebID;

        // Vull suposar que abans de 20 minuts haurà escanejat
        /*
         * Calendar caducitat = Calendar.getInstance(); caducitat.add(Calendar.MINUTE,
         * 20); long expiryTransaction = caducitat.getTimeInMillis();
         */
        String flag = form.getFlag();
        ScanWebRequestSignatureInfo signatureInfo = null;
        if (flag.equals(ScanWebDocument.FLAG_SIGNED) 
                || flag.equals(ScanWebDocument.FLAG_SIGNED_WITH_TIMESTAMP)
                //|| flag.equals(ScanWebDocument.FLAG_SIGNED_AND_CUSTODY)
                ) {

            
            String functionaryFullName = form.getNom();
            String functionaryAdministrationID = form.getNif();
            String functionaryUnitDIR3 = form.getFunctionaryUnitDIR3();
            signatureInfo = new ScanWebRequestSignatureInfo(functionaryFullName,
                    functionaryAdministrationID,functionaryUnitDIR3);
        }

        ScanWebRequestCustodyInfo custodyInfo = null;

        ScanWebInfoTester swc = new ScanWebInfoTester(new ScanWebRequest(scanWebID, form.getTransactionName(), form.getType(), flag, mode,
                form.getLangUI(), form.getUsername(), urlFinal, metadades, signatureInfo, custodyInfo));
        
     

        // /WEB-INF/views/plugindescan_contenidor.jsp
        final String view = "/plugindescan_contenidor";
        ModelAndView mav = ScanWebModuleController.startScanWebProcess(request, view, scanWebModuleEjb, swc);

        return mav;

    }
    
    
    @RequestMapping(value = "/final/{scanWebID}")
    public void finalProcesDeScanAndSortirIFrame(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("scanWebID") String scanWebID) throws Exception {

        
        response.setContentType("text/html");
        
        Writer out = response.getWriter();
        
        String redirect = request.getContextPath() + ScanWebProcessController.CONTEXTWEB+ "/resultats/" + scanWebID;
        
        out.write("<html><body onload=\"javascript:window.parent.location.href='" + redirect +  "'\"></body></html>");

    }
    
    

    @RequestMapping(value = "/resultats/{scanWebID}")
    public ModelAndView finalProcesDeScan(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("scanWebID") String scanWebID) throws Exception {

        ScanWebInfoTester swc;
        swc = scanWebModuleEjb.getScanWebInfoTester(request, scanWebID);

        ScanWebStatus scanWebStatus = swc.getScanWebResult().getStatus();

        ScanWebStatus statusError = null;

        int status = scanWebStatus.getStatus();

        switch (status) {
        case ScanWebStatus.STATUS_IN_PROGRESS:
            HtmlUtils.saveMessageWarning(request,
                    "El plugin ha retornat el control amb un estat no correcte IN PROGRESS. Per favor revisar el plugin ");
        case ScanWebStatus.STATUS_FINAL_OK: {

            // Comprovam que s'hagin escanejat coses

            List<ScanWebDocument> listDocs = swc.getScanWebResult().getScannedDocuments();

            if (listDocs.size() == 0) {
                statusError = new ScanWebStatus();
                statusError.setErrorMsg(" L'usuari no ha escanejat cap fitxer.");
            } else {
                request.getSession().setAttribute(LAST_SCANNED_FILES, listDocs);
                request.getSession().setAttribute(LAST_CONFIG, swc.getScanWebRequest());
                request.getSession().setAttribute(LAST_RESULT, swc.getScanWebResult());
            }
            break;
        }
        case ScanWebStatus.STATUS_FINAL_ERROR: {
            statusError = scanWebStatus;
            break;
        }
        case ScanWebStatus.STATUS_CANCELLED: {

            if (scanWebStatus.getErrorMsg() == null) {
                scanWebStatus.setErrorMsg("plugindescan.cancelat");
            }
            statusError = scanWebStatus;
            break;
        }
        default: {
            String inconsistentState = "El mòdul d´escaneig ha finalitzat inesperadament" + " amb un estat desconegut "
                    + status;
            statusError = new ScanWebStatus();
            scanWebStatus.setErrorMsg(inconsistentState);
            scanWebStatus.setErrorException(new Exception());
        }
        }

        scanWebModuleEjb.closeScanWebProcess(request, scanWebID);

        if (statusError != null) {

            if (statusError.getErrorMsg() == null) {
                statusError.setErrorMsg("Error desconegut ja que no s'ha definit el missatge de l'error !!!!!");
            }
            // Mostrar excepció per log
            if (statusError.getErrorException() == null) {
                log.error(statusError.getErrorMsg());
            } else {
                log.error(statusError.getErrorMsg(), statusError.getErrorException());
            }

            HtmlUtils.saveMessageError(request, statusError.getErrorMsg());

            return new ModelAndView(new RedirectView(ScanWebProcessController.CONTEXTWEB + "/form", true));
        } else {

            ModelAndView mav = new ModelAndView("scanWebFinal");

            return mav;
        }

    }

    /**
     * Descàrrega del document escanejat
     */
    @RequestMapping(value = "/download/{index}", method = RequestMethod.GET)
    public void download(@PathVariable("index") int index, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        download(index, false, response, request);
    }

    /**
     * Descàrrega del document escanejat ja firmat
     */
    @RequestMapping(value = "/downloadSignature/{index}", method = RequestMethod.GET)
    public void downloadSignature(@PathVariable("index") int index, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        download(index, true, response, request);
    }

    /**
     * 
     * @param index
     * @param isSignature
     * @param response
     * @param request
     * @throws Exception
     */
    private void download(int index, boolean isSignature, HttpServletResponse response, HttpServletRequest request)
            throws Exception {

        
        List<ScanWebDocument> listDocs = (List<ScanWebDocument>) request.getSession().getAttribute(LAST_SCANNED_FILES);

        ScanWebDocument sd = listDocs.get(index);

        AnnexCustody file = isSignature ? sd.getScannedSignedFile() : sd.getScannedPlainFile();

        final byte[] data = file.getData();

        if (data == null || data.length == 0) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        String mime = file.getMime();
        String filename = file.getName();
        if (mime == null || mime.trim().length() == 0) {
            mime = "application/octet-stream";
        }

        {
            response.setContentType(mime);
            response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
            response.setContentLength(data.length);

            java.io.OutputStream output = response.getOutputStream();

            output.write(data);

            output.flush();
        }
    }

    @InitBinder("scanWebForm")
    public void initBinder(WebDataBinder binder) {

        binder.setValidator(this.scanWebValidator);

        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));

    }

}
