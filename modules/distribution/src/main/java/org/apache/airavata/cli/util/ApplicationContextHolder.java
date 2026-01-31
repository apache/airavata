package org.apache.airavata.cli.util;

import org.springframework.context.ApplicationContext;

/**
 * Holder for ApplicationContext, used by CLI commands that may be instantiated
 * by picocli outside of Spring's dependency injection (e.g. init command).
 */
public final class ApplicationContextHolder {
    private static ApplicationContext context;

    public static void set(ApplicationContext ctx) {
        context = ctx;
    }

    public static ApplicationContext get() {
        return context;
    }
}
