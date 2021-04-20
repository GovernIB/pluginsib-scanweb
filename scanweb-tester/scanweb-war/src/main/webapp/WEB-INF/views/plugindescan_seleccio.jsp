<%@page import="org.fundaciobit.pluginsib.scanweb.tester.controller.ScanWebModuleController"%>
<%@ include file="/WEB-INF/views/html_header.jsp"%>

<style>
    body{
        background-color: #CEE3F6;
    }
</style>

  <br/>
  <br/>
  
<div class="lead" style="margin-bottom:10px; text-align:center;">
  

  <fmt:message key="plugindescan.seleccio.title"/>
  <br/>
  <h5 style="line-height: 10px; margin-top: 0px; margin-bottom: 0px;">
  <fmt:message key="plugindescan.seleccio.subtitle"/>
  </h5>
  <br/>
  <c:if test="fn:length(companies) eq 1" >
  <h6>
     Si no voleu que aparegui aquesta pantalla quan només hi ha un plugin, llavors anau a la <br/>
     classe <b><%= ScanWebModuleController.class.getName() %></b><br/>
     i editau el camp estatic stepSelectionWhenOnlyOnePlugin i assignau-li un valor true;
  </h6>
  </c:if>
  
  <br/>
  <div class="well" style="max-width: 400px; margin: 0 auto 10px;">
  <table>
  <c:forEach items="${plugins}" var="plugin">
     <tr>
     <td>
     <button type="button" class="btn btn-large btn-block btn-primary" onclick="location.href='<c:url value="/common/scanwebmodule/showscanwebmodule/${plugin.pluginID}/${scanWebID}"/>'">
     <b>${plugin.nom}</b><br>
     <small>
     <i>${plugin.descripcioCurta}</i>
     </small>
     </button>
     </td>
     <td>
        <c:if test="${plugin.massiveScan}" >
           <button type="button" class="btn btn-block btn-info" onclick="window.open('<c:url value="/common/scanwebmodule/downloadseparator/${plugin.pluginID}/${scanWebID}"/>')">
                 <b>Descarregar Separador</b><br>
                 <small>
                 <i>Document separador per Escaneig Massiu</i>
                 </small>
           </button>
        </c:if>
     </td>
     </tr>
  </c:forEach>
  </table>
  </div>
  
  <br/>
  
</div>

<%@ include file="/WEB-INF/views/html_footer.jsp"%>