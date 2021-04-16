package org.fundaciobit.pluginsib.scanweb.tester.ejb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.fundaciobit.pluginsib.scanweb.api.IScanWebPlugin;
import org.fundaciobit.pluginsib.scanweb.tester.ejb.utils.Plugin;
import org.fundaciobit.pluginsib.scanweb.tester.ejb.utils.ScanWebInfoTester;
import org.fundaciobit.pluginsib.scanweb.tester.ejb.utils.ScanWebPluginManager;

/**
 *
 * @author anadal
 *
 */
@Stateless(name = "ScanWebModuleEJB")
public class ScanWebModuleEjb implements ScanWebModuleLocal {

  protected static Logger log = Logger.getLogger(ScanWebModuleEjb.class);
  
  
  
  
  @Override
  public List<Plugin> getAllPluginsFiltered(HttpServletRequest request, String scanWebID)
      throws Exception {

    ScanWebInfoTester scanWebInfoTester = getScanWebInfoTester(request, scanWebID);

    // TODO CHECK scanWebConfig
    List<Plugin> plugins = ScanWebPluginManager.getAllPlugins();
    if (plugins == null || plugins.size() == 0) {
      String msg = "S'ha produit un error llegint els plugins o no se n'han definit.";
      throw new Exception(msg);
    }

    List<Plugin> pluginsFiltered = new ArrayList<Plugin>();

    IScanWebPlugin scanWebPlugin;

    for (Plugin pluginDeScanWeb : plugins) {
      // 1.- Es pot instanciar el plugin ?
      scanWebPlugin = ScanWebPluginManager
          .getInstanceByPluginID(pluginDeScanWeb.getPluginID());

      if (scanWebPlugin == null) {
        throw new Exception("No s'ha pogut instanciar Plugin amb ID "
            + pluginDeScanWeb.getPluginID());
      }

      // 2.- Passa el filtre ...

      if (scanWebPlugin.filter(request, scanWebInfoTester.getScanWebRequest())) {
        pluginsFiltered.add(pluginDeScanWeb);
      } else {
        // Exclude Plugin
        log.info("Exclos plugin [" + pluginDeScanWeb.getNom() + "]: NO PASSA FILTRE");
      }

    }

    return pluginsFiltered;

  }

  @Override
  public String scanDocument(HttpServletRequest request, String absolutePluginRequestPath,
      String relativePluginRequestPath, String scanWebID) throws Exception {

    ScanWebInfoTester scanWebInfoTester = getScanWebInfoTester(request, scanWebID);

    Long pluginID = scanWebInfoTester.getPluginID();

    log.info("SMC :: scanDocument: PluginID = " + pluginID);
    log.info("SMC :: scanDocument: scanWebID = " + scanWebID);

    // El plugin existeix?
    IScanWebPlugin scanWebPlugin;

    scanWebPlugin = ScanWebPluginManager.getInstanceByPluginID(pluginID);

    if (scanWebPlugin == null) {
      String msg = "plugin.scanweb.noexist: " + String.valueOf(pluginID);
      throw new Exception(msg);
    }

    String urlToPluginWebPage;
    urlToPluginWebPage = scanWebPlugin.startScanWebTransaction(absolutePluginRequestPath,
        relativePluginRequestPath, request, scanWebInfoTester.getScanWebRequest());
    
    scanWebInfoTester.setScanWebResult(scanWebPlugin.getScanWebResult(scanWebID));

    return urlToPluginWebPage;

  }

  /**
   * 
   */
  public void requestPlugin(HttpServletRequest request, HttpServletResponse response,
      String absoluteRequestPluginBasePath, String relativeRequestPluginBasePath,
      String scanWebID, String query, boolean isPost) throws Exception {

    ScanWebInfoTester ss = getScanWebInfoTester(request, scanWebID);
    
    if (ss == null) {
      response.sendRedirect(request.getContextPath());
      return;
    }
    

    long pluginID = ss.getPluginID();

    // log.info(" TesterScanWebConfig ss = " + ss);
    // log.info(" ScanWebConfig pluginID = ss.getPluginID(); =>  " + pluginID);

    IScanWebPlugin scanWebPlugin;
    try {
      scanWebPlugin = ScanWebPluginManager.getInstanceByPluginID(pluginID);
    } catch (Exception e) {

      String msg = "plugin.scanweb.noexist: " + String.valueOf(pluginID);
      throw new Exception(msg);
    }
    if (scanWebPlugin == null) {
      String msg = "plugin.scanweb.noexist: " + String.valueOf(pluginID);
      throw new Exception(msg);
    }

    if (isPost) {
      scanWebPlugin.requestPOST(absoluteRequestPluginBasePath, relativeRequestPluginBasePath,
          scanWebID, query, request, response);
    } else {
      scanWebPlugin.requestGET(absoluteRequestPluginBasePath, relativeRequestPluginBasePath,
          scanWebID, query, request, response);
    }

  }

  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // ----------------------------- U T I L I T A T S ----------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  @Override
  public void closeScanWebProcess(HttpServletRequest request, String scanWebID) {

    ScanWebInfoTester pss = getScanWebInfoTester(request, scanWebID);

    if (pss == null) {
      log.warn("NO Existeix scanWebID igual a " + scanWebID);
      return;
    }

    closeScanWebProcess(request, scanWebID, pss);
  }

  private void closeScanWebProcess(HttpServletRequest request, String scanWebID,
      ScanWebInfoTester pss) {

    Long pluginID = pss.getPluginID();

    // final String scanWebID = pss.getscanWebID();
    if (pluginID == null) {
      // Encara no s'ha asignat plugin al proces d'escaneig
    } else {

      IScanWebPlugin scanWebPlugin = null;
      try {
        scanWebPlugin = ScanWebPluginManager.getInstanceByPluginID(pluginID);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        return;
      }
      if (scanWebPlugin == null) {
        log.error("plugin.scanweb.noexist: " + String.valueOf(pluginID));
      }

      try {
        scanWebPlugin.endScanWebTransaction(scanWebID, request);
      } catch (Exception e) {
        log.error(
            "Error borrant dades d'un Proces d'escaneig " + scanWebID + ": " + e.getMessage(),
            e);
      }
    }
    scanWebConfigMap.remove(scanWebID);
  }

  private static final Map<String, ScanWebInfoTester> scanWebConfigMap = new HashMap<String, ScanWebInfoTester>();

  private static long lastCheckScanProcessCaducades = 0;

  /**
   * Fa neteja
   * 
   * @param scanWebID
   * @return
   */
  public ScanWebInfoTester getScanWebInfoTester(HttpServletRequest request, String scanWebID) {
    // Fer net peticions caducades
    // Check si existeix algun proces de escaneig caducat s'ha d'esborrar
    // Com a mínim cada minut es revisa si hi ha caducats
      
    Long now = System.currentTimeMillis();

    final long un_minut_en_ms = 60 * 60 * 1000;

    if (now + un_minut_en_ms > lastCheckScanProcessCaducades) {
      lastCheckScanProcessCaducades = now;
      Map<String, ScanWebInfoTester> keysToDelete = new HashMap<String, ScanWebInfoTester>();

      Set<String> ids = scanWebConfigMap.keySet();
      for (String id : ids) {
          ScanWebInfoTester ss = scanWebConfigMap.get(id);
        if (now > ss.getExpiryTransaction()) {
          keysToDelete.put(id, ss);
          SimpleDateFormat sdf = new SimpleDateFormat();
          log.info("Tancant ScanWebConfig amb ID = " + id + " a causa de que està caducat "
              + "( ARA: " + sdf.format(new Date(now)) + " | CADUCITAT: "
              + sdf.format(new Date(ss.getExpiryTransaction())) + ")");
        }
      }

      if (keysToDelete.size() != 0) {
        synchronized (scanWebConfigMap) {

          for (Entry<String, ScanWebInfoTester> pss : keysToDelete.entrySet()) {
            closeScanWebProcess(request, pss.getKey(), pss.getValue());
          }
        }
      }
    }
    

    return scanWebConfigMap.get(scanWebID);
  }

  @Override
  public void startScanWebProcess(ScanWebInfoTester scanWebConfig) {
    final String scanWebID = scanWebConfig.getScanWebRequest().getScanWebID();
    synchronized (scanWebConfigMap) {
      scanWebConfigMap.put(scanWebID, scanWebConfig);
    }

  }
  
  

}
