/*
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
 *
 */

package org.apache.airavata.wsmg.commons.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for preallocating, recycling, and managing JDBC connections.
 */
public class ConnectionPool implements Runnable {
    private long MAX_IDLE_TIME = 5 * 60 * 1000; // 5 minutes
    private String driver, url, username, password, jdbcUrl;

    private int initialConnections, maxConnections;

    private boolean waitIfBusy;

    private Vector availableConnections, busyConnections;

    private boolean connectionPending = false;

    private HashMap lastAccessTimeRecord = new HashMap();

    private String urlType = "";

    private DataSource datasource;

    private boolean autoCommit = true;

    private int transactionIsolation = Connection.TRANSACTION_NONE;
    static Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    public ConnectionPool(String driver, String url, String username, String password, int initialConnections,
            int maxConnections, boolean waitIfBusy) throws SQLException {
        this(initialConnections, maxConnections, waitIfBusy);
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        urlType = "speratedURL";
        createConnectionPool();
    }

    public ConnectionPool(String driver, String jdbcUrl, int initialConnections, int maxConnections,
            boolean waitIfBusy, boolean autoCommit, int transactionIsolation) throws SQLException {
        this(initialConnections, maxConnections, waitIfBusy);
        this.driver = driver;
        this.jdbcUrl = jdbcUrl;
        urlType = "simpleURL";
        this.autoCommit = autoCommit;
        this.transactionIsolation = transactionIsolation;
        createConnectionPool();
    }

    public ConnectionPool(String driver, String jdbcUrl, int initialConnections, int maxConnections, boolean waitIfBusy)
            throws SQLException {
        this(initialConnections, maxConnections, waitIfBusy);
        this.driver = driver;
        this.jdbcUrl = jdbcUrl;
        urlType = "simpleURL";
        createConnectionPool();
    }

    public ConnectionPool(DataSource dataSource, int initialConnections, int maxConnections, boolean waitIfBusy)
            throws SQLException {
        this(initialConnections, maxConnections, waitIfBusy);
        urlType = "dataSource";
        this.datasource = dataSource;
        createConnectionPool();
    }

    protected ConnectionPool(int initialConnections, int maxConnections, boolean waitIfBusy) throws SQLException {
        this.initialConnections = initialConnections;
        this.maxConnections = maxConnections;
        this.waitIfBusy = waitIfBusy;
        if (initialConnections > maxConnections) {
            initialConnections = maxConnections;
        }
        availableConnections = new Vector(initialConnections);
        busyConnections = new Vector();
        CleanUpThread cleanUpThread = new CleanUpThread();
        new Thread(cleanUpThread).start();
    }

    private void createConnectionPool() throws SQLException {
        for (int i = 0; i < initialConnections; i++) {
            availableConnections.addElement(makeNewConnection());
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        if (!availableConnections.isEmpty()) {
            Connection existingConnection = (Connection) availableConnections.lastElement();
            int lastIndex = availableConnections.size() - 1;
            // System.out.println("ConnectionNo="+lastIndex);
            availableConnections.removeElementAt(lastIndex);

            ((Long) lastAccessTimeRecord.get(existingConnection)).longValue();
            // If connection on available list is closed (e.g.,
            // it timed out), then remove it from available list
            // and repeat the process of obtaining a connection.
            // Also wake up threads that were waiting for a
            // connection because maxConnection limit was reached.
            if (existingConnection.isClosed()) {
                notifyAll(); // Freed up a spot for anybody waiting
                Connection connection = getConnection();
                setTimeStamp(connection);
                return (connection);
            } else {
                busyConnections.addElement(existingConnection);
                setTimeStamp(existingConnection);
                return existingConnection;
            }
        } else {
            // Three possible cases:
            // 1) You haven't reached maxConnections limit. So
            // establish one in the background if there isn't
            // already one pending, then wait for
            // the next available connection (whether or not
            // it was the newly established one).
            // 2) You reached maxConnections limit and waitIfBusy
            // flag is false. Throw SQLException in such a case.
            // 3) You reached maxConnections limit and waitIfBusy
            // flag is true. Then do the same thing as in second
            // part of step 1: wait for next available connection.
            if ((totalConnections() < maxConnections) && !connectionPending) {
                makeBackgroundConnection();
            } else if (!waitIfBusy) {
                throw new SQLException("Connection limit reached");
            }
            // Wait for either a new connection to be established
            // (if you called makeBackgroundConnection) or for
            // an existing connection to be freed up.
            try {
                wait();
            } catch (InterruptedException ie) {
            }
            // Someone freed up a connection, so try again.
            Connection connection = getConnection();
            setTimeStamp(connection);
            return connection;
        }
    }

    // You can't just make a new connection in the foreground
    // when none are available, since this can take several
    // seconds with a slow network connection. Instead,
    // start a thread that establishes a new connection,
    // then wait. You get woken up either when the new connection
    // is established or if someone finishes with an existing
    // connection.
    private void makeBackgroundConnection() {
        connectionPending = true;
        try {
            Thread connectThread = new Thread(this);
            connectThread.start();
        } catch (OutOfMemoryError oome) {
            // Give up on new connection
        }
    }

    public void run() {
        try {
            Connection connection = makeNewConnection();
            synchronized (this) {
                availableConnections.addElement(connection);
                connectionPending = false;
                notifyAll();
            }
        } catch (Exception e) { // SQLException or OutOfMemory
            // Give up on new connection and wait for existing one
            // to free up.
        }
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
            setTimeStamp(connection);
            return connection;
        } catch (ClassNotFoundException cnfe) {
            // Simplify try/catch blocks of people using this by
            // throwing only one exception type.
            throw new SQLException("Can't find class for driver: " + driver);
        }
    }

    private void setTimeStamp(Connection connection) {
        lastAccessTimeRecord.put(connection, Long.valueOf(System.currentTimeMillis()));
    }

    // The database connection cannot be left idle for too long, otherwise TCP connection will be broken.
    /**
     * From http://forums.mysql.com/read.php?39,28450,57460#msg-57460 Okay, then it looks like wait_timeout on the
     * server is killing your connection (it is set to 8 hours of idle time by default). Either set that value higher on
     * your server, or configure your connection pool to not hold connections idle that long (I prefer the latter). Most
     * folks I know that run MySQL with a connection pool in high-load production environments only let connections sit
     * idle for a matter of minutes, since it only takes a few milliseconds to open a connection, and the longer one
     * sits idle the more chance it will go "bad" because of a network hiccup or the MySQL server being restarted.
     * 
     * @throws SQLException
     */
    private boolean isConnectionStale(Connection connection) throws SQLException {
        long currentTime = System.currentTimeMillis();
        long lastAccess = ((Long) lastAccessTimeRecord.get(connection)).longValue();
        if (currentTime - lastAccess > MAX_IDLE_TIME) {
            // connection.close();
            // System.out.println("*************JDBC Connection Stale!");
            return true;
        } else
            return false;
    }

    private void closeStaleConnections() throws SQLException {
        // close idle connections
        Iterator iter = availableConnections.iterator();
        while (iter.hasNext()) {
            Connection existingConnection = (Connection) iter.next();
            if (isConnectionStale(existingConnection)) {
                existingConnection.close();
                iter.remove();
            }
        }
        // close busy connections that have been checked out for too long.
        // This should not happen sinc ethis means program has bug for not releasing connections .
        iter = busyConnections.iterator();
        while (iter.hasNext()) {
            Connection busyConnection = (Connection) iter.next();
            if (isConnectionStale(busyConnection)) {
                iter.remove();
                busyConnection.close();
                logger.warn("****Connection has checked out too long. Forced release. Check the program for unReleased connection.");
            }
        }
    }

    public synchronized void free(Connection connection) {
        busyConnections.removeElement(connection);
        availableConnections.addElement(connection);
        // Wake up threads that are waiting for a connection
        notifyAll();
    }

    public synchronized int totalConnections() {
        return (availableConnections.size() + busyConnections.size());
    }

    /**
     * Close all the connections. Use with caution: be sure no connections are in use before calling. Note that you are
     * not <I>required</I> to call this when done with a ConnectionPool, since connections are guaranteed to be closed
     * when garbage collected. But this method gives more control regarding when the connections are closed.
     */
    public synchronized void closeAllConnections() {
        closeConnections(availableConnections);
        availableConnections = new Vector();
        closeConnections(busyConnections);
        busyConnections = new Vector();
        lastAccessTimeRecord.clear();
    }

    private void closeConnections(Vector connections) {
        try {
            for (int i = 0; i < connections.size(); i++) {
                Connection connection = (Connection) connections.elementAt(i);
                if (!connection.isClosed()) {
                    connection.close();
                }
            }
        } catch (SQLException sqle) {
            // Ignore errors; garbage collect anyhow
        }
    }

    public synchronized String toString() {
        String info = "ConnectionPool(" + url + "," + username + ")" + ", available=" + availableConnections.size()
                + ", busy=" + busyConnections.size() + ", max=" + maxConnections;
        return (info);
    }

    class CleanUpThread implements Runnable {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(MAX_IDLE_TIME);
                } catch (InterruptedException e) {
                    logger.warn(e.getMessage(), e);
                }
                try {
                    closeStaleConnections();
                } catch (SQLException e) {
                    logger.warn(e.getMessage(), e);
                    // e.printStackTrace();
                }
            }
        }
    }

}
