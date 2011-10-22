package org.apache.airavata.xbaya.registrybrowser.model;

import java.net.URL;

import org.apache.airavata.registry.api.Registry;

public class GFacURL {
    private Registry registry;
    private URL gfacURL;

    public GFacURL(Registry registry, URL gfacURL) {
        setRegistry(registry);
        setGfacURL(gfacURL);
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public URL getGfacURL() {
        return gfacURL;
    }

    public void setGfacURL(URL gfacURL) {
        this.gfacURL = gfacURL;
    }
}
