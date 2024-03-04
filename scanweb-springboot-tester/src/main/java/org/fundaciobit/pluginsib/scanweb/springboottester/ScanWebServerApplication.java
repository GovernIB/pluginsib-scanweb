package org.fundaciobit.pluginsib.scanweb.springboottester;

import java.io.File;
import java.util.Collections;

import org.fundaciobit.pluginsib.scanweb.springboottester.config.Configuration;
import org.fundaciobit.pluginsib.scanweb.springboottester.logic.ScanWebPluginManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * 
 * @author anadal
 *
 */
@ServletComponentScan 
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ScanWebServerApplication {

    public static void main(String[] args) {

        try {

            System.setProperty("spring.devtools.restart.enabled", "false");

            File plugins = null;
            {
                File p;

                if (args.length == 0) {
                    // Check if file plugins.properties exists in current directory
                    p = new File("plugins.properties");

                } else {
                    p = new File(args[0]);
                }
                if (p.exists()) {
                    plugins = p;
                }
            }

            if (plugins == null) {
                throw new Exception("No s'ha trobat el fitxer de plugins.properties."
                        + " Especifiqui el fitxer com a argument de la comanda.");
            } else {
                ScanWebPluginManager.setPluginsFile(plugins);
            }

            int port = Configuration.getServerPort();
            SpringApplication app = new SpringApplication(ScanWebServerApplication.class);
            app.setDefaultProperties(Collections.singletonMap("server.port", (Object) String.valueOf(port)));
            app.run(args);

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
