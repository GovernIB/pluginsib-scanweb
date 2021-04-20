package org.fundaciobit.pluginsib.scanweb.tester.ejb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.fundaciobit.pluginsib.scanweb.api.IScanWebPlugin;
import org.fundaciobit.pluginsib.scanweb.api.ScanWebPlainFile;
import org.fundaciobit.pluginsib.core.utils.PluginsManager;

/**
 * 
 * @author anadal
 *
 */
public class ScanWebPluginManager {

  private static Map<Long, Plugin> pluginsCache = new HashMap<Long, Plugin>();

  private static Map<Long, IScanWebPlugin> instancesCache = new HashMap<Long, IScanWebPlugin>();

  public static void deleteCache(Plugin instance) {
    if (instance != null) {
      synchronized (pluginsCache) {
        pluginsCache.remove(instance.getPluginID());
      }
    }
  }

  public static void clearCache() {
    synchronized (pluginsCache) {
      pluginsCache.clear();
    }
  }

  public static void addPluginToCache(Long pluginID, Plugin pluginInstance) {
    synchronized (pluginsCache) {
      pluginsCache.put(pluginID, pluginInstance);
    }
  }

  public static Plugin getPluginFromCache(Long pluginID) {
    synchronized (pluginsCache) {
      return pluginsCache.get(pluginID);
    }
  }
  
  /** Logger for this class and subclasses */
  protected static final Logger log = Logger.getLogger(ScanWebPluginManager.class);
  
  // Properties BASE
  public static final String PROPERTIES_BASE = "org.fundaciobit.scanweb.tester.";
  
  // Cache de Plugins
  private static List<Plugin> plugins = null;

  // Ara és llegeixen els plugins d'un fitxer que la ruta es troba en una
  // entrada de les propietats de sistema
  // TODO Es pot modificar per llegir-ho directament de BBDD o de System-Properties.
  public static List<Plugin> getAllPlugins() {
    if (plugins == null) {
      
      String pluginPropertiesPath = System.getProperty("scanwebplugins.path");
      
      if (pluginPropertiesPath == null) {
        log.error("La propietat 'scanwebplugins.path' "
            + "no te valor assignat.", new Exception());
        return null;
      }
      
      File pluginPropertiesFile = new File(pluginPropertiesPath);
      
      if(!pluginPropertiesFile.exists()) {
        log.error("No existeix el fitxer  " + pluginPropertiesFile.getAbsolutePath(), new Exception());
        return null;
      }
      
      Properties pluginProperties = new Properties();
      
      try {
        
        Reader reader = new InputStreamReader(new FileInputStream(pluginPropertiesFile), "UTF-8");
        pluginProperties.load(reader);
     
      } catch (Exception e) {
        log.error("Error desconegut llegint les propietats del fitxer "
             + pluginPropertiesFile.getAbsolutePath(), new Exception());
        return null;
      }
      
      final String BASESIGN_PROP = PROPERTIES_BASE + "scanwebplugins";
      String idsStr = pluginProperties.getProperty(BASESIGN_PROP);
      
      if (idsStr == null) {
        log.error(" Plugins a Carregar PROP[" + BASESIGN_PROP + "]: " + idsStr);     }
      
      // Miram els identificadors de plugins que volem
      String[] ids = idsStr.split(",");
      
      ArrayList<Plugin> tmpplugins = new ArrayList<Plugin>();
      for (int i = 0; i < ids.length; i++) {
        long pluginID = Long.parseLong(ids[i]);
        String base = BASESIGN_PROP + "." + pluginID + ".";
        
        String nom = pluginProperties.getProperty(base + "nom");
        
        log.debug("Plugin a carregar NOM[" + base + "nom" + "]: " + nom);
        String classe = pluginProperties.getProperty(base + "class");
        String descripcioCurta = pluginProperties.getProperty(base + "desc");
        
        // Llegir propietats
        Properties properties = new Properties();
        Set<Object> keys =  pluginProperties.keySet();
        
        for (Object object : keys) {
          String key = (String)object;
          
          if (key.startsWith(base)) {
              String value = pluginProperties.getProperty(key);
              
              // Posam la mateixa BASE a totes les propietats de totes els plugins
              String nomFinal = key.substring(base.length());
              properties.put(PROPERTIES_BASE + nomFinal, value);
          }
        }

        
        Plugin plugin = new Plugin(pluginID, nom, descripcioCurta, classe, properties,false);
        try {
            IScanWebPlugin pluginInstance = getInstanceByPlugin(plugin);
            plugin.setMassiveScan(pluginInstance.isMassiveScanAllowed());
        } catch (Exception e) {            
            log.error("Error instanciant plugin " + nom + " amb id " + pluginID + ": " + e.getMessage(), e);
        }

        log.info(" -------------  PLUGIN " + pluginID + "------------------");
        log.info("nom: " + nom);
        log.info("descripcioCurta: " + descripcioCurta);
        log.info("classe: " + classe);
        log.info("properties: " + properties);
        log.info("massiveScan: " + plugin.isMassiveScan());

        tmpplugins.add(plugin);
        
      }
      
      
      plugins = tmpplugins;
      
    }
    return plugins;
  }

  public static Plugin getPluginById(long pluginID) {

    List<Plugin> list = getAllPlugins();

    for (Plugin plugin : list) {
      if (plugin.getPluginID() == pluginID) {
        return plugin;
      }
    }

    return null;
  }

  public static IScanWebPlugin getInstanceByPluginID(long pluginID) throws Exception {

    IScanWebPlugin instance = instancesCache.get(pluginID);

    if (instance == null) {

      Plugin plugin = getPluginFromCache(pluginID);

      if (plugin == null) {

        plugin = getPluginById(pluginID);

        if (plugin == null) {
          return null;
        }

        addPluginToCache(pluginID, plugin);
      }

      instance = getInstanceByPlugin(plugin);

      instancesCache.put(pluginID, instance);

    }

    return instance;

  }

protected static IScanWebPlugin getInstanceByPlugin(Plugin plugin) throws Exception {
    IScanWebPlugin instance;
    Properties prop = plugin.getProperties();

      instance = (IScanWebPlugin) PluginsManager.instancePluginByClassName(
          plugin.getClasse(), PROPERTIES_BASE, prop);

      if (instance == null) {
        throw new Exception("plugin.donotinstantiate: " + plugin.getNom() + " ("
            + plugin.getClasse() + ")");
      }
    return instance;
}
  
  

  public static ScanWebPlainFile getDocumentsSeparator(Long pluginID, String languageUI) throws Exception {
      IScanWebPlugin plugin = getInstanceByPluginID(pluginID);
      ScanWebPlainFile separator = plugin.getSeparatorForMassiveScan(languageUI);
      return separator;
  }
  
  

}
