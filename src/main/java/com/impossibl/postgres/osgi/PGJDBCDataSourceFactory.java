/*
 * Copyright (c) 2017, impossibl.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of impossibl.com nor the names of its contributors may
 *    be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
