package org.airavata.appcatalog.cpi;

public class AppCatalogException extends Exception{
    private static final long serialVersionUID = -2849422320139467602L;

    public AppCatalogException(Throwable e) {
        super(e);
    }

    public AppCatalogException(String message) {
        super(message, null);
    }

    public AppCatalogException(String message, Throwable e) {
        super(message, e);
    }
}
