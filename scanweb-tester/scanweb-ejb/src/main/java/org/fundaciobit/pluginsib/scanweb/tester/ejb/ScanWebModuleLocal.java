package org.fundaciobit.pluginsib.scanweb.tester.ejb;

import java.util.List;
import java.util.Set;

import javax.ejb.Local;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fundaciobit.pluginsib.scanweb.tester.ejb.utils.Plugin;
import org.fundaciobit.pluginsib.scanweb.tester.ejb.utils.ScanWebInfoTester;

/**
 * 
 * @author anadal
 *
 */
@Local
public interface ScanWebModuleLocal {
  
  public static final String JNDI_NAME = "scanweb/ScanWebModuleEJB/local";

  public void closeScanWebProcess(HttpServletRequest request, String scanWebID);
  
  
  public void startScanWebProcess(ScanWebInfoTester ess);
  
  
  public String scanDocument(
      HttpServletRequest request, String absoluteRequestPluginBasePath,
      String relativeRequestPluginBasePath,      
      String scanWebID) throws Exception;
  
  
  public void requestPlugin(HttpServletRequest request, HttpServletResponse response,
      String absoluteRequestPluginBasePath, String relativeRequestPluginBasePath,
      String scanWebID, String query, boolean isPost)  throws Exception;
  
  
  public ScanWebInfoTester getScanWebInfoTester(HttpServletRequest request,
      String scanWebID);
  
  public List<Plugin> getAllPluginsFiltered(HttpServletRequest request, String scanWebID) throws Exception;
  
  
  public Set<String> getDefaultFlags(ScanWebInfoTester ss) throws Exception;
  
}
