<%@page import="org.fundaciobit.pluginsib.scanweb.api.ScanWebDocument"
%><%@page import="org.fundaciobit.pluginsib.scanweb.api.IScanWebPlugin"
%><%@ include file="html_header.jsp"
%>
<h3 class="tabs_involved">
  &nbsp;&nbsp;&nbsp;<fmt:message key="scan.proces" />
</h3>
  
<form:form modelAttribute="scanWebConfigForm" method="post"  enctype="multipart/form-data">
 
  <div style="margin:20px 20px 20px 20px;" style="width:auto;">

  <div class="module_content" style="width:auto;">
    <div class="tab_container" style="width:auto;">
    
    <table class="tdformlabel table-condensed table table-bordered marTop10" style="width:auto;" > 
    <tbody>   

        <tr>
          <td><label>TransactionName &nbsp;(*)</label></td>
          <td>
              <form:errors path="transactionName" cssClass="errorField alert alert-error" />
              <form:input  cssClass="input-xxlarge" path="transactionName" />
          </td>
        </tr>
     

        <tr>
          <td><label>Tipus &nbsp;(*)</label></td>
            <td>
              <form:errors path="type" cssClass="errorField alert alert-error" />
              <form:select path="type">
                <c:forEach items="${supportedTypes}" var="tipus">
                   <form:option value="${tipus}">${tipus}</form:option>
                </c:forEach>
              </form:select>
           </td>
         </tr>

         <tr>
          <td><label>Caracter&iacute;stiques &nbsp;(*)</label></td>
            <td>
              <form:errors path="flag" cssClass="errorField alert alert-error" />
              <form:select path="flag" id="flag"  onchange="canviatFlag()">
                <c:forEach items="${supportedFlags}" var="flag">
                   <form:option value="${flag}"  >${flag} </form:option>
                </c:forEach>
              </form:select>
           </td>
         </tr>

         <tr>
          <td><label>Mode &nbsp;(*)</label></td>
            <td>
              <form:errors path="mode" cssClass="errorField alert alert-error" />
              <form:radiobutton path="mode" value="S"/>S&iacute;ncron.
               &nbsp; &nbsp;
              <form:radiobutton path="mode" value="F"/>As&iacute;ncron.
           </td>
         </tr>
         
         <tr>
          <td><label>Idioma de la UI &nbsp;(*)</label></td>
            <td>
          <form:errors path="langUI" cssClass="errorField alert alert-error" />
          <form:select path="langUI">
            <form:option value="ca" selected="true" >Catal&agrave;</form:option>
            <form:option value="es" >Castell&agrave;</form:option>
          </form:select>
           </td>
         </tr>
         
         
         <tr>
          <td><label>Username &nbsp;(*)</label></td>
            <td>
          <form:errors path="username" cssClass="errorField alert alert-error" />
          <form:input  path="username" />
           </td>
         </tr>
         
         
         
         <tr id="nomtr" style="visibility: none">
          <td><label>Nom complet funcionari &nbsp;(*)</label></td>
            <td>
          <form:errors path="nom" cssClass="errorField alert alert-error" />
          <form:input  path="nom" />
           </td>
         </tr>
         
         
         <tr id="niftr" style="visibility: none">
          <td><label>NIF del funcionari&nbsp;(*)</label></td>
            <td>
          <form:errors path="nif" cssClass="errorField alert alert-error" />
          <form:input  path="nif" />
           </td>
         </tr>
         
         <tr id="functionaryUnitDIR3tr" style="visibility: none">
          <td><label>Unitat DIR3 del funcionari</label></td>
            <td>
          <form:errors path="functionaryUnitDIR3" cssClass="errorField alert alert-error" />
          <form:input  path="functionaryUnitDIR3" />
           </td>
         </tr>
         
     </tbody>
    </table>
    
    </div>
    
    <center>
      <input id="submitbutton" type="submit" class="btn btn-primary" value="<fmt:message key="escanejar"/>">
    </center>

   <form:hidden id="id" path="id" />
   
   </div>
   
   </div>
  
</form:form>

<script>
    function canviatFlag() {
    	var d = document.getElementById("flag").value;
    	
    	if (d == '<%=ScanWebDocument.FLAG_PLAIN %>') {
    	   document.getElementById("nomtr").style.display = 'none';
    	   document.getElementById("niftr").style.display = 'none';
    	   document.getElementById("functionaryUnitDIR3tr").style.display = 'none';
    	} else {
           document.getElementById("nomtr").style.display = '';
           document.getElementById("niftr").style.display = '';
           document.getElementById("functionaryUnitDIR3tr").style.display = '';
    	}
    }

    canviatFlag();

</script>


<%@ include file="html_footer.jsp"%>
