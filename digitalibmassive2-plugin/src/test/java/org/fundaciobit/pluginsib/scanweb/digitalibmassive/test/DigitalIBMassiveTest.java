package org.fundaciobit.pluginsib.scanweb.digitalibmassive.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

import org.fundaciobit.pluginsib.scanweb.api.ScanWebPlainFile;
import org.fundaciobit.pluginsib.scanweb.digitalibmassive.DigitalIBMassive2ScanWebPlugin;

/**
 * 
 * @author anadal (u80067)
 * 26 ago 2026 13:43:31
 */
public class DigitalIBMassiveTest {

    public static void main(String[] args) {

        try {

            // Llegir fitxer de propietats "plugin.properties" amb les propietats de configuració
            String propertiesFilePath = "plugin.properties";
            Properties properties = new Properties();
            try (InputStream input = new FileInputStream(propertiesFilePath)) {
                properties.load(input);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            String base = properties.getProperty("base");
            
            DigitalIBMassive2ScanWebPlugin plugin = new DigitalIBMassive2ScanWebPlugin(base, properties);

            ScanWebPlainFile swp = plugin.getSeparatorForMassiveScan("ca");

            if (swp != null) {
                System.out.println("Separador: " + swp.getName() + " (" + swp.getMime() + ")");

                Files.write(new File(swp.getName()).toPath(), swp.getData());

            } else {
                System.out.println("No s'ha trobat el separador per a la llengua 'ca'.");
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

}
