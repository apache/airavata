package org.apache.airavata.xbaya.registrybrowser.menu;

public class DeleteAction extends AbstractBrowserActionItem {
    public static String ID = "action.delete";

    public DeleteAction() {
        setCaption(getDefaultCaption());
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getDefaultCaption() {
        return "Remove";
    }
}
