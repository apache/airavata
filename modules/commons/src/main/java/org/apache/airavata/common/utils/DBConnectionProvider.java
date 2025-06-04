package org.apache.airavata.common.utils;

import org.apache.commons.dbcp2.BasicDataSource;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.Stoppable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;

@SuppressWarnings({"unused"}) // implicitly used by the ORM
public class DBConnectionProvider implements ConnectionProvider, Configurable, Stoppable {
    private static final Logger logger = LoggerFactory.getLogger(DBConnectionProvider.class);
    private static final String dataSourceAsString = """
            BasicDataSource:
              Driver={0},
              URL={1},
              Username={2},
              ValidationQuery={3},
              InitialSize={4},
              MaxTotal={5},
              MaxIdle={6},
              MinIdle={7},
              MaxWaitMs={8}
            """;
    private static BasicDataSource dataSource;

    @Override
    public void configure(Map configValues) throws HibernateException {
        String driverClass = (String) configValues.get("hibernate.connection.driver_class");
        String url = (String) configValues.get("hibernate.connection.url");
        String username = (String) configValues.get("hibernate.connection.username");
        String password = (String) configValues.get("hibernate.connection.password");
        String validationQuery = (String) configValues.get("hibernate.connection.validationQuery");
        int initialSize = Integer.parseInt((String) configValues.get("hibernate.dbcp2.initialSize"));
        int maxTotal = Integer.parseInt((String) configValues.get("hibernate.dbcp2.maxTotal"));
        int maxIdle = Integer.parseInt((String) configValues.get("hibernate.dbcp2.maxIdle"));
        int minIdle = Integer.parseInt((String) configValues.get("hibernate.dbcp2.minIdle"));
        int maxWaitMillis = Integer.parseInt((String) configValues.get("hibernate.dbcp2.maxWaitMillis"));

        dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        if (validationQuery != null && !validationQuery.isEmpty()) {
            dataSource.setValidationQuery(validationQuery);
        }
        dataSource.setInitialSize(initialSize);
        dataSource.setMaxTotal(maxTotal);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxWait(Duration.ofMillis(maxWaitMillis));
        logger.debug("DBCP2 DataSource configured : {}", url);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    /**
     * Called by Hibernate during shutdown to close the DataSource.
     */
    @Override
    public void stop() {
        try {
            dataSource.close();
            logger.debug("DBCP2 DataSource closed.");
        } catch (SQLException e) {
            logger.error("Error closing DBCP2 DataSource", e);
        }
    }

    /**
     * For the Wrapped interface. Determines if this provider can be unwrapped as the given type.
     */
    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return unwrapType.isAssignableFrom(getClass()) || ConnectionProvider.class.equals(unwrapType);
    }

    /**
     * For the Wrapped interface. Returns an instance of the specified unwrap type.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <@UnknownKeyFor @NonNull @Initialized T> T unwrap(@UnknownKeyFor @NonNull @Initialized Class<T> unwrapType) {
        if (isUnwrappableAs(unwrapType)) {
            return (T) this;
        }
        throw new UnknownUnwrapTypeException(unwrapType);
    }
}