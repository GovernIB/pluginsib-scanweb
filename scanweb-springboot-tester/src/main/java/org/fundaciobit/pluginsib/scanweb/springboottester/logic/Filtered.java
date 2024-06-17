package org.fundaciobit.pluginsib.scanweb.springboottester.logic;

import java.util.List;
import java.util.Map;

/**
 * @author anadal
 */
public class Filtered {

    protected final List<Plugin> pluginsIncluded;

    protected final Map<Plugin, String> pluginsExcluded;

    public Filtered(List<Plugin> pluginsIncluded, Map<Plugin, String> pluginsExcluded) {
        super();
        this.pluginsIncluded = pluginsIncluded;
        this.pluginsExcluded = pluginsExcluded;
    }

    public List<Plugin> getPluginsIncluded() {
        return pluginsIncluded;
    }

    public Map<Plugin, String> getPluginsExcluded() {
        return pluginsExcluded;
    }

}
