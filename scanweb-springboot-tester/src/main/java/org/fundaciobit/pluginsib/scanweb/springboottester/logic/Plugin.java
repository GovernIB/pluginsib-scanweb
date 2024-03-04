package org.fundaciobit.pluginsib.scanweb.springboottester.logic;

import java.util.Properties;

/**
 * 
 * @author anadal
 *
 */
public class Plugin {

    long pluginID;// PK
    String nom;
    String descripcioCurta;
    java.lang.String classe;
    Properties properties;
    boolean massiveScan;

    /** Constructor Buit */
    public Plugin() {
    }

    /** Constructor amb tots els camps */
    public Plugin(long pluginID, String nom, String descripcioCurta, java.lang.String classe, Properties properties,
            boolean massiveScan) {
        this.pluginID = pluginID;
        this.nom = nom;
        this.descripcioCurta = descripcioCurta;
        this.classe = classe;
        this.properties = properties;

    }

    public long getPluginID() {
        return (pluginID);
    };

    public void setPluginID(long _pluginID_) {
        this.pluginID = _pluginID_;
    };

    public String getNom() {
        return (nom);
    };

    public void setNom(String _nom_) {
        this.nom = _nom_;
    };

    public String getDescripcioCurta() {
        return (descripcioCurta);
    };

    public void setDescripcioCurta(String _descripcioCurta_) {
        this.descripcioCurta = _descripcioCurta_;
    };

    public java.lang.String getClasse() {
        return (classe);
    };

    public void setClasse(java.lang.String _classe_) {
        this.classe = _classe_;
    };

    public Properties getProperties() {
        return (properties);
    };

    public void setProperties(Properties _properties_) {
        this.properties = _properties_;
    }

    public boolean isMassiveScan() {
        return massiveScan;
    }

    public void setMassiveScan(boolean massiveScan) {
        this.massiveScan = massiveScan;
    }

}
