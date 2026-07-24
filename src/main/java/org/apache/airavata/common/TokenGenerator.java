package org.apache.airavata.common;

import java.util.UUID;

public class TokenGenerator {
    public static String generateToken(String gatewayId, String metadata) {
        return UUID.randomUUID().toString();
    }
}