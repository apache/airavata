package org.apache.airavata.db.event.manager;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.db.event.manager.messaging.DBEventManagerMessagingFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Created by Ajinkya on 3/29/17.
 */
public class DBEventManagerRunner {

    private static final Logger log = LogManager.getLogger(DBEventManagerRunner.class);

    /**
     * Start required messaging utilities
     */
    private void startDBEventManagerRunner() {
        try{
            log.info("Starting DB Event manager publisher");

            DBEventManagerMessagingFactory.getDBEventPublisher();
            log.debug("DB Event manager publisher is running");

            log.info("Starting DB Event manager subscriber");

            DBEventManagerMessagingFactory.getDBEventSubscriber();
            log.debug("DB Event manager subscriber is listening");
        } catch (AiravataException e) {
            log.error("Error starting DB Event Manager.", e);
        }
    }


    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        try {
            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    DBEventManagerRunner dBEventManagerRunner = new DBEventManagerRunner();
                    dBEventManagerRunner.startDBEventManagerRunner();
                }
            };

            // start the worker thread
            log.info("Starting the DB Event Manager runner.");
            new Thread(runner).start();
        } catch (Exception ex) {
            log.error("Something went wrong with the DB Event Manager runner. Error: " + ex, ex);
        }
    }

}
