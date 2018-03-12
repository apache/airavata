/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.allocation.manager.db.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.Semaphore;


/**
 * A class for preallocating, recycling, and managing JDBC connections.
 */
public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    private long MAX_IDLE_TIME = 5 * 60 * 1000; // 5 minutes

    private String driver;
    private String url;
    private String username;
    private String password;
    private String jdbcUrl;

    private int maxConnections;

    private boolean autoCommit = true;
    private boolean waitIfBusy;

    private Semaphore needConnection = new Semaphore(0);
    private boolean stop;

    private Stack<Connection> availableConnections;
    private Stack<Connection> busyConnections;

    private HashMap<Connection, Long> lastAccessTimeRecord = new HashMap<Connection, Long>();

    private String urlType = "";

    private DataSource datasource;

    private int transactionIsolation = Connection.TRANSACTION_NONE;

    private Thread clenupThread;
    private Thread producerThread;

    public ConnectionPool(String driver, String url, String username, String password, int initialConnections,
                          int maxConnections, boolean waitIfBusy) throws SQLException {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.urlType = "speratedURL";
        initialize(initialConnections, maxConnections, waitIfBusy);
    }

    public ConnectionPool(String driver, String jdbcUrl, int initialConnections, int maxConnections,
                          boolean waitIfBusy, boolean autoCommit, int transactionIsolation) throws SQLException {
        this.driver = driver;
        this.jdbcUrl = jdbcUrl;
        this.urlType = "simpleURL";
        this.autoCommit = autoCommit;
        this.transactionIsolation = transactionIsolation;
        initialize(initialConnections, maxConnections, waitIfBusy);
    }

    public ConnectionPool(String driver, String jdbcUrl, int initialConnections, int maxConnections, boolean waitIfBusy)
            throws SQLException {
        this.driver = driver;
        this.jdbcUrl = jdbcUrl;
        this.urlType = "simpleURL";
        initialize(initialConnections, maxConnections, waitIfBusy);
    }

    public ConnectionPool(DataSource dataSource, int initialConnections, int maxConnections, boolean waitIfBusy)
            throws SQLException {
        this.urlType = "dataSource";
        this.datasource = dataSource;
        initialize(initialConnections, maxConnections, waitIfBusy);
    }

    /**
     * Check if this connection pool is auto commit or not
     *
     * @return
     */
    public boolean isAutoCommit() {
        return this.autoCommit;
    }

    private void initialize(int initialConnections, int maxConnections, boolean waitIfBusy) throws SQLException {
        this.maxConnections = maxConnections;
        this.waitIfBusy = waitIfBusy;

        int sizeOfConnections = (initialConnections > maxConnections) ? maxConnections : initialConnections;

        availableConnections = new Stack<Connection>();
        busyConnections = new Stack<Connection>();

        for (int i = 0; i < sizeOfConnections; i++) {
            Connection con = makeNewConnection();
            setTimeStamp(con);
            availableConnections.push(con);

        }

        producerThread = new Thread(new FillUpThread());
        producerThread.start();

        clenupThread = new Thread(new CleanUpThread());
        clenupThread.start();
    }

    public synchronized Connection getConnection() throws SQLException {
        if (!availableConnections.isEmpty()) {
            Connection existingConnection = availableConnections.pop();

            // If connection on available list is closed (e.g.,
            // it timed out), then remove it from available list
            // and race for a connection again.
            if (existingConnection.isClosed()) {
                lastAccessTimeRecord.remove(existingConnection);
                // notifyAll for fairness
                notifyAll();
            } else {
                busyConnections.push(existingConnection);
                setTimeStamp(existingConnection);
                return existingConnection;
            }
        } else if (!waitIfBusy && busyConnections.size() >= maxConnections) {
            // You reached maxConnections limit and waitIfBusy flag is false.
            // Throw SQLException in such a case.
            throw new SQLException("Connection limit reached");
        } else {

            if (busyConnections.size() < maxConnections) {
                // available connection is empty, but total number of connection
                // doesn't reach maxConnection. Request for more connection
                needConnection.release();
            }

            try {
                // wait for free connection
                wait();
            } catch (InterruptedException ie) {
            }
        }
        // always race for connection forever
        return getConnection();
    }

    // This explicitly makes a new connection. Called in
    // the foreground when initializing the ConnectionPool,
    // and called in the background when running.
    private Connection makeNewConnection() throws SQLException {
        try {
            // Load database driver if not already loaded
            Class.forName(driver);
            Connection connection;
            // Establish network connection to database
            if (urlType.equals("speratedURL")) {
                connection = DriverManager.getConnection(url, username, password);
            } else if (urlType.equals("simpleURL")) {
                connection = DriverManager.getConnection(jdbcUrl);
            } else { // if(urlType.equals("dataSource")){
                connection = datasource.getConnection();
            }
            connection.setTransactionIsolation(this.transactionIsolation);
            connection.setAutoCommit(this.autoCommit);
            return connection;
        } catch (ClassNotFoundException cnfe) {
            // Simplify try/catch blocks of people using this by
            // throwing only one exception type.
            throw new SQLException("Can't find class for driver: " + driver);
        }
    }

    private synchronized void fillUpConnection(Connection conn) {
        setTimeStamp(conn);
        availableConnections.push(conn);

        // notify all since new connection is created
        notifyAll();
    }

    private void setTimeStamp(Connection connection) {
        lastAccessTimeRecord.put(connection, System.currentTimeMillis());
    }

    // The database connection cannot be left idle for too long, otherwise TCP
    // connection will be broken.
    /**
     * From http://forums.mysql.com/read.php?39,28450,57460#msg-57460 Okay, then it looks like wait_timeout on the
     * server is killing your connection (it is set to 8 hours of idle time by default). Either set that value higher on
     * your server, or configure your connection pool to not hold connections idle that long (I prefer the latter). Most
     * folks I know that run MySQL with a connection pool in high-load production environments only let connections sit
     * idle for a matter of minutes, since it only takes a few milliseconds to open a connection, and the longer one
     * sits idle the more chance it will go "bad" because of a network hiccup or the MySQL server being restarted.
     *
     * @throws java.sql.SQLException
     */
    private boolean isConnectionStale(Connection connection) {
        long currentTime = System.currentTimeMillis();
        long lastAccess = lastAccessTimeRecord.get(connection);
        if (currentTime - lastAccess > MAX_IDLE_TIME) {
            return true;
        } else
            return false;
    }

    private synchronized void closeStaleConnections() {
        // close idle connections
        Iterator<Connection> iter = availableConnections.iterator();
        while (iter.hasNext()) {
            Connection existingConnection = iter.next();
            if (isConnectionStale(existingConnection)) {
                try {
                    existingConnection.close();
                    iter.remove();
                } catch (SQLException sql) {
                    logger.error(sql.getMessage(), sql);
                }
            }
        }
        // close busy connections that have been checked out for too long.
        // This should not happen since this means program has bug for not
        // releasing connections .
        iter = busyConnections.iterator();
        while (iter.hasNext()) {
            Connection busyConnection = iter.next();
            if (isConnectionStale(busyConnection)) {
                try {
                    busyConnection.close();
                    iter.remove();
                    logger.warn("****Connection has checked out too long. Forced release. Check the program for calling release connection [free(Connection) method]");
                } catch (SQLException sql) {
                    logger.error(sql.getMessage(), sql);
                }
            }
        }
    }

    public synchronized void free(Connection connection) {
        busyConnections.removeElement(connection);
        availableConnections.addElement(connection);
        // Wake up threads that are waiting for a connection
        notifyAll();
    }

    /**
     * Close all the connections. Use with caution: be sure no connections are in use before calling. Note that you are
     * not <I>required</I> to call this when done with a ConnectionPool, since connections are guaranteed to be closed
     * when garbage collected. But this method gives more control regarding when the connections are closed.
     */
    public synchronized void dispose() {
        logger.info("Connection Pool Shutting down");

        // stop clean up thread
        this.stop = true;
        this.clenupThread.interrupt();

        // stop producer up thread
        this.producerThread.interrupt();

        // close all connection
        closeConnections(availableConnections);
        availableConnections = new Stack<Connection>();
        closeConnections(busyConnections);
        busyConnections = new Stack<Connection>();
        lastAccessTimeRecord.clear();

        logger.info("All connection is closed");

        try {
            this.clenupThread.join();
            this.producerThread.join();
        } catch (Exception e) {
            logger.error("Cannot shutdown cleanup thread", e);
        }

        logger.info("Connection Pool Shutdown");
    }

    private void closeConnections(Stack<Connection> connections) {
        while (!connections.isEmpty()) {
            Connection connection = connections.pop();
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException sqle) {
                // Ignore errors; garbage collect anyhow
                logger.warn(sqle.getMessage());
            }
        }
    }

    public synchronized String toString() {
        String info = "ConnectionPool(" + url + "," + username + ")" + ", available=" + availableConnections.size()
                + ", busy=" + busyConnections.size() + ", max=" + maxConnections;
        return (info);
    }

    class CleanUpThread implements Runnable {
        public void run() {
            while (!stop) {
                try {
                    Thread.sleep(MAX_IDLE_TIME);
                    closeStaleConnections();
                } catch (InterruptedException e) {
                    logger.info("Clean up thread is interrupted to close");
                }
            }
        }
    }

    class FillUpThread implements Runnable {
        public void run() {
            while (!stop) {
                try {
                    // block until get
                    needConnection.acquire();

                    Connection conn = makeNewConnection();
                    fillUpConnection(conn);
                } catch (SQLException e) {
                    // cannot create connection (increase semaphore value back)
                    needConnection.release();
                    logger.error(e.getMessage(), e);
                } catch (InterruptedException e) {
                    logger.info("Fill up thread is interrupted to close");
                    break;
                }
            }
        }
    }

    public void shutdown() throws SQLException{
        for (Connection c : availableConnections) {
            try {
                c.close();
            } catch (SQLException e) {
                logger.error("Error while closing the connection", e);
                throw new SQLException("Error while closing the connection", e);
            }
        }

        for (Connection c : busyConnections) {
            try {
                c.close();
            } catch (SQLException e) {
                logger.error("Error while closing the connection", e);
                throw new SQLException("Error while closing the connection", e);
            }
        }
    }
}