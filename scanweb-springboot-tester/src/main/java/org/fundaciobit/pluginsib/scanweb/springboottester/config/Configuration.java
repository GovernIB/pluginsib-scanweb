package org.fundaciobit.pluginsib.scanweb.springboottester.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.fundaciobit.pluginsib.scanweb.springboottester.ScanWebServerApplication;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;



/**
 * 
 * @author anadal
 *
 */
@Service
public class Configuration {

    public static final org.slf4j.Logger log = LoggerFactory.getLogger(Configuration.class);


    public static int getServerPort() throws Exception {
        return Integer.parseInt(getServerConnection().getProperty("port"));
    }


    public static Properties getServerConnection() throws Exception {

        File currentDir = getCurrentDir();

        File conf = new File(currentDir, "serverconfiguration.properties");

        if (!conf.exists()) {
            throw new Exception("No s'ha trobat el fitxer " + conf.getAbsolutePath());
        }

        Properties prop = new Properties();

        prop.load(new FileInputStream(conf));

        return prop;

    }

    protected static File getCurrentDir() {
        ApplicationHome home = new ApplicationHome(ScanWebServerApplication.class);
        log.info("home.getDir() => |" + home.getDir() + "|"); // returns the folder where the jar is. This is what I wanted.
        log.info("home.getSource() => |" + home.getSource() + "|"); //  /

        File currentDir = home.getDir();
        if (currentDir.getName().equals("target")) {
            currentDir = currentDir.getParentFile();
        } else if (currentDir.getName().equals("classes")) {
            currentDir = currentDir.getParentFile().getParentFile();
        }
        return currentDir;
    }

}
