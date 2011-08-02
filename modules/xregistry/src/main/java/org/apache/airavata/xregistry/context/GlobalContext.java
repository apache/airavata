/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.airavata.xregistry.context;

import static org.apache.airavata.xregistry.XregistryOptions.DBURL;
import static org.apache.airavata.xregistry.XregistryOptions.JDBCDRIVERURL;
import static org.apache.airavata.xregistry.XregistryOptions.MYPROXY_LIFETIME;
import static org.apache.airavata.xregistry.XregistryOptions.MYPROXY_PASSWD;
import static org.apache.airavata.xregistry.XregistryOptions.MYPROXY_SERVER;
import static org.apache.airavata.xregistry.XregistryOptions.MYPROXY_USERNAME;
import static org.apache.airavata.xregistry.XregistryOptions.SECURITY_ENABLED;
import static org.apache.airavata.xregistry.XregistryOptions.SSL_HOST_KEY_FILE;
import static org.apache.airavata.xregistry.XregistryOptions.SSL_TRUSTED_CERT_FILE;
import static org.apache.airavata.xregistry.utils.Utils.createCredentials;
import static org.apache.airavata.xregistry.utils.Utils.findBooleanProperty;
import static org.apache.airavata.xregistry.utils.Utils.findIntegerProperty;
import static org.apache.airavata.xregistry.utils.Utils.findStringProperty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Timer;

import org.apache.airavata.xregistry.XregistryConstants;
import org.apache.airavata.xregistry.XregistryException;
import org.apache.airavata.xregistry.XregistryOptions;
import org.apache.airavata.xregistry.auth.Authorizer;
import org.apache.airavata.xregistry.auth.AuthorizerImpl;
import org.apache.airavata.xregistry.db.JdbcStorage;
import org.apache.airavata.xregistry.group.GroupManager;
import org.apache.airavata.xregistry.group.GroupManagerImpl;
import org.apache.airavata.xregistry.utils.CWsdlUpdateTask;
import org.apache.airavata.xregistry.utils.ProxyRenewer;
import org.globus.myproxy.MyProxy;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import xsul.MLogger;

/**
 * This is the central configuration Store for Xregistry
 */

public class GlobalContext {

	private Properties config;

	private JdbcStorage storage;

	private GroupManager groupManager;

	private Authorizer authorizer;

	private String trustedCertsFile;

	private String hostcertsKeyFile;

	private GSSCredential credential;

	private final boolean isClientSide;

	private String dbUrl;

	private String driverUrl;
	
	private String databaseType;

	private int port;

	private boolean securityEnabled;

	private String userDN;

	private X509Certificate[] trustedCertificates;

	protected static MLogger log = MLogger.getLogger(XregistryConstants.LOGGER_NAME);

	public GlobalContext(boolean clientSide) throws XregistryException {
		this(clientSide, (String) null);
	}

	public GlobalContext(boolean clientSide, String propertiesFileName)
			throws XregistryException {
		this(clientSide, loadPropertiesFromPorpertyFile(propertiesFileName));
	}

	private static Properties loadPropertiesFromPorpertyFile(
			String propertiesFileName) throws XregistryException {
		try {
			Properties properties = new Properties();
			InputStream propertiesIn;
			if (propertiesFileName != null) {
				// try explicit parameter
				propertiesIn = new FileInputStream(propertiesFileName);
			} else {
				// xregistry.properties file on current directory
				File propertiesFile = new File(
						XregistryConstants.X_REGISTRY_PROPERTY_FILE);
				if (propertiesFile.exists()) {
					propertiesIn = new FileInputStream(propertiesFile);
				} else {
					// 3) xregistry/xregistry.properties file on classpath
					propertiesIn = Thread
							.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									"xregistry/"
											+ XregistryConstants.X_REGISTRY_PROPERTY_FILE);
				}
			}
			if (propertiesIn != null) {
				properties.load(propertiesIn);
				propertiesIn.close();
			} else {
				throw new XregistryException(
						"Can not load configuration, the configuration order "
								+ "1) explict parameter 2) xregistry.properties file on current directory 3) xregistry/xregistry.properties file on classpath");
			}
			return properties;
		} catch (FileNotFoundException e) {
			throw new XregistryException();
		} catch (IOException e) {
			throw new XregistryException();
		}
	}

	public GlobalContext(boolean clientSide, Properties properties)
			throws XregistryException {
		this.isClientSide = clientSide;
		this.config = properties;
		try {
			if (clientSide) {
				String loginDataFile = System.getProperty("login.file");
				if (loginDataFile == null) {
					loginDataFile = System.getProperty("user.home")
							+ "/.xregistry";
				}
				File loginPropertiesFile = new File(loginDataFile);
				if (loginPropertiesFile.exists()) {
					InputStream loginPropertiesIn = new FileInputStream(
							loginPropertiesFile);
					config.load(loginPropertiesIn);
					loginPropertiesIn.close();
				}
			}
		} catch (FileNotFoundException e) {
			throw new XregistryException();
		} catch (IOException e) {
			throw new XregistryException();
		}
		
		if (!clientSide) {
			loadConfiguration();
		
			storage = new JdbcStorage(dbUrl, driverUrl);
			if(dbUrl.contains("derby")){
				databaseType = "derby";
			}else{
				databaseType = "mysql";
			}
			// intialize databse
			log.info("Creating Database Schema");
			initDatabaseTables();
			
			// start Xregistry support modules
			groupManager = new GroupManagerImpl(this);
			authorizer = new AuthorizerImpl(this, groupManager);

			startSheduledTasks();
		}
	}

	public GSSCredential getCredential() throws XregistryException {
		try {
			if (credential == null) {
				credential = createMyProxyCredentails();
				if (credential == null) {
					if (isClientSide) {
						throw new XregistryException(
								"At the client side, myproxy credentails must present");
					} else {
						credential = createCredentials();
					}
				}
				userDN = credential.getName().toString();
			}
			return credential;
		} catch (GSSException e) {
			throw new XregistryException(e);
		}
	}

	public String getHostcertsKeyFile() {
		return hostcertsKeyFile;
	}

	public X509Certificate[] getTrustedCertificates() {
		return trustedCertificates;
	}

	public void setTrustedCertificates(X509Certificate[] trustedCertificates) {
		this.trustedCertificates = trustedCertificates;
	}

	public String getTrustedCertsFile() {
		return trustedCertsFile;
	}

	public void setCredential(GSSCredential credential)
			throws XregistryException {
		try {
			this.credential = credential;
			userDN = credential.getName().toString();
		} catch (GSSException e) {
			throw new XregistryException(e);
		}
	}

	public void loadConfiguration() throws XregistryException {

		this.hostcertsKeyFile = findStringProperty(config, SSL_HOST_KEY_FILE,
				hostcertsKeyFile);
		this.trustedCertsFile = findStringProperty(config,
				SSL_TRUSTED_CERT_FILE, trustedCertsFile);
		this.dbUrl = findStringProperty(config, DBURL, dbUrl);

		this.driverUrl = findStringProperty(config, JDBCDRIVERURL, driverUrl);
		if (driverUrl == null) {
			throw new XregistryException(
					"Database Driver for database is not defined");
		}
		this.port = findIntegerProperty(config, XregistryOptions.PORT, 6666);
		this.securityEnabled = findBooleanProperty(config, SECURITY_ENABLED,
				true);
	}

	public Connection createConnection() throws XregistryException {
		return storage.connect();
	}

	public void closeConnection(Connection connection)
			throws XregistryException {
		try {
			storage.closeConnection(connection);
		} catch (SQLException e) {
			throw new XregistryException(e);
		}
	}

	public GroupManager getGroupManager() {
		return groupManager;
	}

	public Authorizer getAuthorizer() {
		return authorizer;
	}

	public void setTrustedCertsFile(String trustedCertsFile) {
		this.trustedCertsFile = trustedCertsFile;
	}

	public int getPort() {
		return port;
	}

	public void setHostcertsKeyFile(String hostcertsKeyFile) {
		this.hostcertsKeyFile = hostcertsKeyFile;
	}

	private GSSCredential createMyProxyCredentails() throws XregistryException {
		String myproxyUserNameStr = config.getProperty(MYPROXY_USERNAME);
		String myproxyPasswdStr = config.getProperty(MYPROXY_PASSWD);
		String myproxyServerStr = config.getProperty(MYPROXY_SERVER);
		String myproxyLifetimeStr = config.getProperty(MYPROXY_LIFETIME);

		if (myproxyUserNameStr != null && myproxyPasswdStr != null
				&& myproxyServerStr != null) {
			int myproxyLifetime = 14400;
			if (myproxyLifetimeStr != null) {
				myproxyLifetime = Integer.parseInt(myproxyLifetimeStr.trim());
			}
			ProxyRenewer proxyRenewer = new ProxyRenewer(myproxyUserNameStr,
					myproxyPasswdStr, MyProxy.DEFAULT_PORT, myproxyLifetime,
					myproxyServerStr, trustedCertsFile);
			return proxyRenewer.renewProxy();
		} else {
			log.info("Can not find sufficent information to load myproxy, server = "
					+ myproxyServerStr
					+ " User Name="
					+ myproxyUserNameStr
					+ " passwd is " + myproxyPasswdStr == null ? "" : "Not"
					+ null);
			return null;
		}

	}

	/**
	 * If data base tables are defined as SQL queries in file placed at
	 * xregistry/tables.sql in the classpath, those SQL queries are execuated
	 * against the data base. On the file, any line start # is igonred as a
	 * comment.
	 * 
	 * @throws XregistryException
	 */
	private void initDatabaseTables() throws XregistryException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream sqltablesAsStream = cl.getResourceAsStream("tables-" + databaseType +".sql");
		if (sqltablesAsStream == null) {
			return;
		}
		Connection connection = createConnection();
		try {

			StringBuffer sql = new StringBuffer();
			BufferedReader reader = null;
			boolean keepFormat = false;
			String delimiter = ";";
			reader = new BufferedReader(new InputStreamReader(sqltablesAsStream));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!keepFormat) {
					if (line.startsWith("//")) {
						continue;
					}
					if (line.startsWith("--")) {
						continue;
					}
					StringTokenizer st = new StringTokenizer(line);
					if (st.hasMoreTokens()) {
						String token = st.nextToken();
						if ("REM".equalsIgnoreCase(token)) {
							continue;
						}
					}
				}
				sql.append(keepFormat ? "\n" : " ").append(line);

				// SQL defines "--" as a comment to EOL
				// and in Oracle it may contain a hint
				// so we cannot just remove it, instead we must end it
				if (!keepFormat && line.indexOf("--") >= 0) {
					sql.append("\n");
				}
				if ((checkStringBufferEndsWith(sql, delimiter))) {
					executeSQL(
							sql.substring(0, sql.length() - delimiter.length()),
							connection);
					sql.replace(0, sql.length(), "");
				}
			}
			// Catch any statements not followed by ;
			if (sql.length() > 0) {
				executeSQL(sql.toString(), connection);
			}
		} catch (Exception e) {
			throw new XregistryException(e);
		} finally {
			closeConnection(connection);
		}

	}

	/**
	 * executes given sql
	 * 
	 * @param sql
	 * @throws Exception
	 */
	private void executeSQL(String sql, Connection connection) throws Exception {
		// Check and ignore empty statements
		if ("".equals(sql.trim())) {
			return;
		}
		Statement statement = connection.createStatement();

		ResultSet resultSet = null;
		try {
			log.info("SQL : " + sql);

			boolean ret;
			int updateCount = 0, updateCountTotal = 0;
			ret = statement.execute(sql);
			updateCount = statement.getUpdateCount();
			resultSet = statement.getResultSet();
			do {
				if (!ret) {
					if (updateCount != -1) {
						updateCountTotal += updateCount;
					}
				}
				ret = statement.getMoreResults();
				if (ret) {
					updateCount = statement.getUpdateCount();
					resultSet = statement.getResultSet();
				}
			} while (ret);

			log.info(sql + " : " + updateCountTotal + " rows affected");
			SQLWarning warning = connection.getWarnings();
			while (warning != null) {
				log.info(warning + " sql warning");
				warning = warning.getNextWarning();
			}
			connection.clearWarnings();
		} catch (SQLException e) {
			if (e.getSQLState().equals("X0Y32")) {
				// eliminating the table already exception for the derby
				// database
				log.info("Table Already Exists" + e.getLocalizedMessage());
			} else {
				throw new Exception("Error occurred while executing : " + sql,
						e);
			}
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					log.warning("Error occurred while closing result set.", e);
				}
			}
		}
	}

	public static boolean checkStringBufferEndsWith(StringBuffer buffer,
			String suffix) {
		if (suffix.length() > buffer.length()) {
			return false;
		}
		int endIndex = suffix.length() - 1;
		int bufferIndex = buffer.length() - 1;
		while (endIndex >= 0) {
			if (buffer.charAt(bufferIndex) != suffix.charAt(endIndex)) {
				return false;
			}
			bufferIndex--;
			endIndex--;
		}
		return true;
	}

	public void startSheduledTasks() {
		Timer timer = new Timer();
		int delay = 5000; // delay for 5 sec.
		int period = 1000 * 60 * 10; // every 10 minute
		timer.scheduleAtFixedRate(new CWsdlUpdateTask(this), delay, period);
	}

	public void setUserDN(String userDN) {
		this.userDN = userDN;
	}

	public boolean isSecurityEnabled() {
		return securityEnabled;
	}

	public String getUserDN() {
		return userDN;
	}

	public Properties getConfig() {
		return config;
	}

}
