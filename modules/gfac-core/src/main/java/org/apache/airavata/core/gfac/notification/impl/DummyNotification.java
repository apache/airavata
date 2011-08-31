package org.apache.airavata.core.gfac.notification.impl;

import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.notification.NotificationService;
import org.apache.airavata.core.gfac.provider.Provider;
import org.apache.airavata.core.gfac.scheduler.Scheduler;

public class DummyNotification implements NotificationService {

    public void startSchedule(Object notifer, InvocationContext context, Scheduler scheduler) {
    }

    public void finishSchedule(Object notifer, InvocationContext context, Scheduler scheduler, Provider provider) {
    }

    public void input(Object notifier, InvocationContext context, String... data) {
    }

    public void output(Object notifier, InvocationContext context, String... data) {
    }

    public void startExecution(Object notifer, InvocationContext context) {
    }

    public void applicationInfo(Object notifier, InvocationContext context, String... data) {
    }

    public void finishExecution(Object notifer, InvocationContext context) {
    }

    public void statusChanged(Object notifer, InvocationContext context, String... data) {
    }

    public void executionFail(Object notifer, InvocationContext context, Exception e, String... data) {
    }

    public void debug(Object notifer, InvocationContext context, String... data) {
    }

    public void info(Object notifer, InvocationContext context, String... data) {
    }

    public void warning(Object notifer, InvocationContext context, String... data) {
    }

    public void exception(Object notifer, InvocationContext context, String... data) {
    }

}
