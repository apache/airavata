package org.apache.airavata.registry.utils;

import java.util.UUID;

public class AppCatalogUtils {
    public static String getID(String name) {
        String pro = name.replaceAll("\\s", "");
        String id = pro + "_" + UUID.randomUUID();
        System.out.println("DEBUG: Generated ID for " + name + ": " + id);
        return id;
    }
}