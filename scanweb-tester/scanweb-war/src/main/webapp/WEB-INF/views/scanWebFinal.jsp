<%@ include file="/WEB-INF/views/include.jsp"%>

<%@ include file="/WEB-INF/views/html_header.jsp"%>

<br>
<br>
<br>


<center style="margin:20px;">
<h3><fmt:message key="scan.final.msg1"/></h3><br/>
<i><fmt:message key="scan.final.msg2"/></i><br/>
<br/>
<b>Configuraci&oacute;:</b>
&nbsp;&nbsp;&nbsp;Tipus: <b>${LAST_CONFIG.scanType}</b>
&nbsp;&nbsp;&nbsp;| Caracter&iacute;stica: <b>${LAST_CONFIG.flag}</b>
&nbsp;&nbsp;&nbsp;| Mode: <b>${LAST_CONFIG.mode}</b><br/>
&nbsp;&nbsp;&nbsp;| TransactionName: <b>${LAST_CONFIG.transactionName}</b><br/>
<table  class="table table-bordered">
<thead>
 <tr>
    <th> &nbsp;&nbsp;#&nbsp;&nbsp; </th>
    <th> Fitxer </th>
    <th> Fitxer Signat </th>
    <th> Scan Info </th>
    <th> Sign Info </th>
    <th> Metadades </th>
 </tr>
</thead>
<tbody>
<c:forEach items="${LAST_SCANNED_FILES}" var="scannedDocument" varStatus="theCount">

 <tr>
    <td> &nbsp;${theCount.count}&nbsp; </td>
    <td> Fitxer 
        <c:if test="${not empty scannedDocument.scannedPlainFile }">
           <ul>
          <li>Nom: <b><a href="<c:url value="/common/scan/download/${theCount.index}" />" target="_blank">
            ${scannedDocument.scannedPlainFile.name}
            </a></b></li>           
            <li>Len: <b>${fn:length(scannedDocument.scannedPlainFile.data)} bytes</b></li>
            <li>Mime: <b> ${scannedDocument.scannedPlainFile.mime}</b></li>
            </ul>
        </c:if>
    </td>
    <td> 
       <c:if test="${not empty scannedDocument.scannedSignedFile }">
       <ul>
          <li>Nom: <b><a href="<c:url value="/common/scan/downloadSignature/${theCount.index}" />" target="_blank">
            ${scannedDocument.scannedSignedFile.name}
            </a></b></li>
            
             <li>Len: ${fn:length(scannedDocument.scannedSignedFile.data)} bytes</li>
             <li>Mime: <b> ${scannedDocument.scannedSignedFile.mime}</b></li>
             <li>Tipus Firma: <b> ${scannedDocument.scannedSignedFile.signatureType}</b></li>               
             <li>AttachedDocument: <b> ${scannedDocument.scannedSignedFile.attachedDocument}</b></li>
             </ul>
        </c:if>
    </td>
    <td>


    <ul>
    
    <li>scanDate: <b><fmt:formatDate value='${scannedDocument.scanDate}' type='date' pattern='dd-MM-yyyy hh:mm'/></b> </li>

    <li>pixelType: 
       <c:if test="${not empty scannedDocument.pixelType }">
            <b>
            <c:choose>
                <c:when test="${scannedDocument.pixelType == 0}">
                   BLACK_WHITE
                </c:when>
                <c:when test="${scannedDocument.pixelType == 1}">
                    GRAY
                </c:when>
                <c:when test="${scannedDocument.pixelType == 2}">
                    COLOR
                </c:when>
                <c:otherwise>
                    Unknown Pixel Type ${scannedDocument.pixelType}                
                </c:otherwise> 
            </c:choose>
            </b> (${scannedDocument.pixelType})
       </c:if>
    </li>

    <li>pppResolution: <b>${scannedDocument.pppResolution}</b> </li>
    
    <li>scannedFileFormat: <b>${scannedDocument.scannedFileFormat}</b> </li>

    <li>ocr: <b>${scannedDocument.ocr}</b> </li>
    <li>duplex: <b>${scannedDocument.duplex}</b> </li>

    <li>documentLanguage: <b>${scannedDocument.documentLanguage}</b> </li>
    
    <li>paperSize: <b>${scannedDocument.paperSize}</b> </li>
    
    <li>Tipus de Document: <b>${scannedDocument.documentType}</b> </li>
    
    
    
    </ul>
    </td> 
    
    <td> <!--  SIGN INFO -->
    
    <c:if test="${not empty scannedDocument.scannedSignedFile }">
    <c:set var="signInfo" value="${scannedDocument.scannedSignedFile.signInfo}" />
    <c:if test="${not empty signInfo }">
    <ul>
    <li>signOperation: 
        <b>
        <c:choose>
            <c:when test="${signInfo.signOperation == 0}">OPERATION_SIGN</c:when>
            <c:when test="${signInfo.signOperation == 1}">OPERATION_COSIGN</c:when>
            <c:when test="${signInfo.signOperation == 2}">OPERATION_COUNTERSIGN</c:when>
            <c:otherwise>Unknown</c:otherwise>
        </c:choose>
         (${signInfo.signOperation})
        </b>
    </li>

    <li>signType: <b>${signInfo.signType}</b></li>

    <li>signAlgorithm: <b>${signInfo.signAlgorithm}</b></li>

    <li>signMode: 
    
        <b>
        <c:choose>
            <c:when test="${signInfo.signMode == 0}">ATTACHED</c:when>
            <c:when test="${signInfo.signMode == 1}">DETACHED</c:when>
            <c:otherwise>Unknown</c:otherwise>
        </c:choose>
         (${signInfo.signMode})
        </b>
    </li>

    <li>signaturesTableLocation: 
        <b>
        <c:choose>
            <c:when test="${signInfo.signMode == 0}">Sense Taula</c:when>
            <c:when test="${signInfo.signMode == 1}">Primera P&agrave;gina</c:when>
            <c:when test="${signInfo.signMode == -1}">Darrera P&agrave;gina</c:when>
            <c:otherwise>Unknown</c:otherwise>
        </c:choose>
         (${signInfo.signaturesTableLocation})
        </b>   
    </li>

    <li>timeStampIncluded: <b>${signInfo.timeStampIncluded}</b></li>

    <%-- BES(falsE) o EPES(true) --%>
    <li>policyIncluded: <b>${signInfo.policyIncluded}</b></li>

    <%--
     * eEMGDE.Firma.TipoFirma.FormatoFirma (eEMGDE17.1.1): TF01 (CSV), TF02 (XAdES
     * internally detached signature), TF03 (XAdES enveloped signature), TF04 (CAdES
     * detached/explicit signature), TF05 (CAdES attached/implicit signature), TF06
     * (PAdES)
     * 
     * 
     * Denominación normalizada del tipo de firma. Los posibles valores asignables
     * son los siguientes: TF01 - CSV TF02 - XAdES internally detached signature");
     * TF03 - XAdES enveloped signature. TF04 - CAdES detached/explicit signature.
     * TF05 - CAdES attached/implicit signature. TF06 - PAdES. El tipo TF04 será
     * establecido por defecto para documentos firmados, exceptuando los documentos
     * en formato PDF o PDF/A, cuyo tipo será TF06. MetadataConstants.ENI_TIPO_FIRMA
     * = "eni:tipoFirma";
     * 
    --%>
    <li>eniTipoFirma:<b> ${signInfo.eniTipoFirma}</b></li>

    <%--
     * - eEMGDE.Firma.TipoFirma.PerfilFirma (eEMGDE17.1.2): 1.- Para las firmas
     * XADES y CADES: EPES, T, C, X, XL, A, BASELINE B-Level, BASELINE T-Level,
     * BASELINE LT-Level, BASELINE LTA-Level. 2.- Para las firmas PADES: EPES, LTV,
     * BASELINE B-Level, BASELINE T
     * 
     * Perfil empleado en una firma con certificado electrónico. Los posibles
     * valores asignables son los siguientes: EPES T C X XL A BASELINE B-Level
     * BASELINE LT-Level BASELINE LTA-Level BASELINE T-Level LTV
     * 
     * - MetadataConstants.ENI_PERFIL_FIRMA = "eni:perfil_firma";
    --%>
    <li>eniPerfilFirma:<b> ${signInfo.eniPerfilFirma}</b></li>

    <%--
     * - eEMGDE.Firma.RolFirma (eEMGDE17.2): Esquemas desarrollados a nivel local y
     * que pueden incluir valores como válida, autentica, refrenda, visa,
     * representa, testimonia, etc..
    --%>
    <li>eniRolFirma: <b>${signInfo.eniRolFirma}</b></li>

    <%--
     * eEMGDE.Firma.Firmante.NombreApellidos (eEMGDE17.5.1): Texto libre. Nombre o
     * razón social de los firmantes.
    --%>
    <li>eniSignerName: <b>${signInfo.eniSignerName}</b></li>

    <%--
     * eEMGDE.Firma.Firmante (eEMGDE17.5.2). NúmeroIdentificacionFirmantes
     --%>
    <li>eniSignerAdministrationId: <b>${signInfo.eniSignerAdministrationId}</b></li>

    <%--
     * eEMGDE.Firma.NivelFirma (eEMGDE17.5.4) Indicador normalizado que refleja el
     * grado de confianza de la firma utilizado. Ejemplos: Nick, PIN ciudadano,
     * Firma electrónica avanzada, Claves concertadas, Firma electrónica avanzada
     * basada en certificados, CSV, ..
     --%>
    <li>eniSignLevel: <b>${signInfo.eniSignLevel}</b></li>

    <%--
     * Informació de les validacions realitzades
     --%>
        
    <c:if test="${not empty signInfo.validationInfo }">
    
    <li>validationInfo::checkAdministrationIDOfSigner: <b>${signInfo.validationInfo.checkAdministrationIDOfSigner}</b></li>

    <li>validationInfo::checkDocumentModifications: <b>${signInfo.validationInfo.checkDocumentModifications}</b></li>

    <li>validationInfo::checkValidationSignature: <b>${signInfo.validationInfo.checkValidationSignature}</b></li>

    </c:if>
    </ul>
    <%-- public List<Metadata> getAdditionInformation() {
        return additionInformation;
    } --%>
    
    <c:if test="${not empty signInfo.additionInformation}">
    
         <table class="table table-bordered">
         <thead>
         <tr>
            <th> Clau </th>
            <th> Valor </th>
            <th> Tipus </th>
         </tr>
         </thead>
         <tbody>
         <c:forEach items="${signInfo.additionInformation}" var="metadata" varStatus="theCountM">
            <tr>
            <td> ${metadata.key} </td>
            <td> <b>${metadata.value}</b> </td>
            <td> ${metadata.metadataType} </td>
            </tr>
         
         </c:forEach>
         </tbody>
        </table>
    
    </c:if>
    
    
    </c:if>
    
    </c:if>
    
    
    </td>
    
    <td> 
         <c:if test="${not empty scannedDocument.additionalMetadatas }">
         
         
         <table class="table table-bordered">
         <thead>
         <tr>
            <th> Clau </th>
            <th> Valor </th>
            <th> Tipus </th>
         </tr>
         </thead>
         <tbody>
         <c:forEach items="${scannedDocument.additionalMetadatas}" var="metadata" varStatus="theCountM">
            <tr>
            <td> ${metadata.key} </td>
            <td> ${metadata.value} </td>
            <td> ${metadata.metadataType} </td>
            </tr>
         
         </c:forEach>
         </tbody>
        </table>

         </c:if>

     </td>
 </tr>

</c:forEach>
</tbody>
</table>

<br/>

<a href="<c:url value="/common/scan/form.html" />" class="btn"><fmt:message key="tornar"/></a>
</center>

<%@ include file="/WEB-INF/views/html_footer.jsp"%>