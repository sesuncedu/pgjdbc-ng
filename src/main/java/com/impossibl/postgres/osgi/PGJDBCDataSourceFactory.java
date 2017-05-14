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

import com.impossibl.postgres.jdbc.PGConnectionPoolDataSource;
import com.impossibl.postgres.jdbc.PGDataSource;
import com.impossibl.postgres.jdbc.PGDriver;
import com.impossibl.postgres.jdbc.xa.PGXADataSource;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;

public class PGJDBCDataSourceFactory implements DataSourceFactory {

  private static final Pattern URL_PATTERN = Pattern.compile("jdbc:pgsql:(?://([^:/]+)(?::([0-9]+))?)?/([^?]+)");
  private static final Pattern OFFICIAL_URL_PATTERN =
      Pattern.compile("jdbc:pgsql:(?://((?:[a-zA-Z0-9\\-\\.]+|\\[[0-9a-f\\:]+\\])(?:\\:(?:\\d+))?(?:,(?:[a-zA-Z0-9\\-\\.]+|\\[[0-9a-f\\:]+\\])(?:\\:(?:\\d+))?)*)/)?((?:\\w|-|_)+)(?:[\\?\\&](.*))?");

  private static Reference propertiesToReference(Properties props) {
    Reference ref = new Reference(PGJDBCDataSourceFactory.class.toString());

    for (String key : props.stringPropertyNames()) {
      String value = props.getProperty(key);
      if (ref.get(key) == null) {
        ref.add(new StringRefAddr(key, value));
      }
    }
    String url = props.getProperty(JDBC_URL);
    if (url != null) {
      Matcher matcher = URL_PATTERN.matcher(url);
      if (!matcher.matches()) {
        throw new IllegalArgumentException("Invalid URL: " + url);
      }
      String host = matcher.group(1);
      String port = matcher.group(2);
      String database = matcher.group(3);
      if (host != null && !props.contains(JDBC_SERVER_NAME)) {
        ref.add(new StringRefAddr(JDBC_SERVER_NAME, host));
      }
      if (port != null && !props.contains(JDBC_PORT_NUMBER)) {
        ref.add(new StringRefAddr(JDBC_PORT_NUMBER, port));
      }
      if (database != null && !props.contains(JDBC_DATABASE_NAME) && !props.contains("database")) {
        ref.add(new StringRefAddr("database", database));
      }
    }

    String dbName = props.getProperty(JDBC_DATABASE_NAME);
    if (dbName != null) {
      ref.add(new StringRefAddr("database", dbName));
    }
    return ref;
  }

  @Override
  public DataSource createDataSource(final Properties properties) throws SQLException {
    final PGDataSource dataSource = new PGDataSource();
    dataSource.init(propertiesToReference(properties));

    return dataSource;
  }

  @Override
  public ConnectionPoolDataSource createConnectionPoolDataSource(Properties properties)
      throws SQLException {
    PGConnectionPoolDataSource connectionPoolDataSource = new PGConnectionPoolDataSource();
    connectionPoolDataSource.init(propertiesToReference(properties));
    return connectionPoolDataSource;
  }

  @Override
  public XADataSource createXADataSource(Properties properties) throws SQLException {

    PGXADataSource xaDataSource = new PGXADataSource();
    xaDataSource.init(propertiesToReference(properties));

    return xaDataSource;
  }

  @Override
  public Driver createDriver(Properties properties) throws SQLException {
    if (properties != null && !properties.isEmpty()) {
      throw new SQLException("PGJDBC Driver creation does accept any configuration properties but was given: " + properties.toString());
    }
    return new PGDriver();
  }
}
