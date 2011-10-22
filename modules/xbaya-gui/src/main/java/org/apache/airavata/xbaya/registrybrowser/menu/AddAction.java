package org.apache.airavata.xbaya.registrybrowser.menu;

public class AddAction extends AbstractBrowserActionItem {
    public static String ID = "action.add";

    public AddAction() {
        setCaption(getDefaultCaption());
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getDefaultCaption() {
        return "Add";
    }

}
