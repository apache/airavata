package org.apache.airavata.xbaya.registrybrowser.menu;

public class RefreshAction extends AbstractBrowserActionItem {
    public static String ID = "action.refresh";

    public RefreshAction() {
        setCaption(getDefaultCaption());
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getDefaultCaption() {
        return "Refresh";
    }

}
