package com.impossibl.postgres.osgi;

import com.impossibl.postgres.jdbc.AbstractDataSource;
import com.impossibl.postgres.jdbc.PGConnectionPoolDataSource;
import com.impossibl.postgres.jdbc.PGDataSource;
import com.impossibl.postgres.jdbc.PGDriver;
import com.impossibl.postgres.jdbc.xa.PGXADataSource;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import org.osgi.service.jdbc.DataSourceFactory;

public class PGJDBCDataSourceFactory implements DataSourceFactory {

    @Override
    public DataSource createDataSource(final Properties properties) throws SQLException {
        final PGDataSource dataSource = new PGDataSource();
        configureDatasource(dataSource, properties);

        return dataSource;
    }


    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties properties)
        throws SQLException {
        PGConnectionPoolDataSource connectionPoolDataSource = new PGConnectionPoolDataSource();
        configureDatasource(connectionPoolDataSource, properties);
        return connectionPoolDataSource;
    }

    @Override
    public XADataSource createXADataSource(Properties properties) throws SQLException {

        PGXADataSource xaDataSource = new PGXADataSource();
        configureDatasource(xaDataSource, properties);

        return xaDataSource;
    }

    @Override
    public Driver createDriver(Properties properties) throws SQLException {
        return new PGDriver();
    }

    private static void configureDatasource(AbstractDataSource dataSource, Properties properties) {
        properties.forEach((k, v) -> {
            switch (String.valueOf(k)) {
                case DataSourceFactory.JDBC_SERVER_NAME:
                    dataSource.setHost((String) v);
                    break;
                case DataSourceFactory.JDBC_PORT_NUMBER:
                    dataSource.setPort((Integer) v);
                    break;
                case DataSourceFactory.JDBC_DATABASE_NAME:
                    dataSource.setDatabase((String) v);
                    break;
                case DataSourceFactory.JDBC_USER:
                    dataSource.setUser((String) v);
                    break;
                case DataSourceFactory.JDBC_PASSWORD:
                    dataSource.setPassword((String) v);
                    break;
            }
        });
    }

}
