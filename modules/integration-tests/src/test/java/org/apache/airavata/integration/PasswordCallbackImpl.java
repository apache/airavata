package org.apache.airavata.integration;

import org.apache.airavata.registry.api.PasswordCallback;

/**
 * Callback for tests
 */
public class PasswordCallbackImpl implements PasswordCallback {
    @Override
    public String getPassword(String username) {

        if (username.equals("admin")) {
            return "admin";
        }

        return null;
    }
}
