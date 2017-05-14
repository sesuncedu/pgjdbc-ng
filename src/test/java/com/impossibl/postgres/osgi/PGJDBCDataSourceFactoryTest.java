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
 */package com.impossibl.postgres.osgi;

import com.impossibl.postgres.jdbc.AbstractDataSource;
import com.impossibl.postgres.jdbc.PGConnectionPoolDataSource;
import com.impossibl.postgres.jdbc.PGDataSource;
import com.impossibl.postgres.jdbc.PGDriver;
import com.impossibl.postgres.jdbc.TestUtil;
import com.impossibl.postgres.jdbc.xa.PGXADataSource;
import com.impossibl.postgres.system.Settings;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import javax.sql.CommonDataSource;
import org.junit.Test;
import org.osgi.service.jdbc.DataSourceFactory;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_DATABASE_NAME;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_PASSWORD;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_PORT_NUMBER;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_SERVER_NAME;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_URL;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_USER;

/**
 * This is a unit test for the  PGJDBCDataSourceFactory.  It does not test the functionality of the driver or
 * datasource, or operation inside a container. Integration testing of this class is performed in ActivatorTest.
 */

public class PGJDBCDataSourceFactoryTest {
  private Map<String, Function<AbstractDataSource, Object>> getterMap = new HashMap<>();
  private PGJDBCDataSourceFactory factory = new PGJDBCDataSourceFactory();

  public PGJDBCDataSourceFactoryTest() {

    getterMap.put(JDBC_SERVER_NAME, AbstractDataSource::getHost);
    getterMap.put(JDBC_PORT_NUMBER, AbstractDataSource::getPort);
    getterMap.put(JDBC_DATABASE_NAME, AbstractDataSource::getDatabase);
    getterMap.put(JDBC_USER, AbstractDataSource::getUser);
    getterMap.put(JDBC_PASSWORD, AbstractDataSource::getPassword);
    getterMap.put(JDBC_URL, this::getURL);
    getterMap.put(Settings.APPLICATION_NAME, AbstractDataSource::getApplicationName);
  }

  public String getURL(AbstractDataSource ds) {
    StringBuilder buf = new StringBuilder();
    buf.append("jdbc:pgsql:");
    String host = ds.getHost();
    if (host != null) {
      buf.append("//").append(host);
      int port = ds.getPort();
      if (port != 5432) {
        buf.append(":").append(port);
      }
    }
    buf.append("/").append(ds.getDatabase());
    return buf.toString();
  }

  @Test
  public void testCreateDriverNullProperties() throws SQLException {
    Driver driver = factory.createDriver(null);
    assertThat(driver,
        allOf(notNullValue(),
            instanceOf(PGDriver.class)));
    assertEquals("major version", driver.getMajorVersion(), 0);
    assertEquals("minor version", driver.getMinorVersion(), 1);
  }

  @Test
  public void testCreateDriverEmptyProperties() throws SQLException {
    Driver driver = factory.createDriver(new Properties());
    assertThat(driver,
        allOf(notNullValue(),
            instanceOf(PGDriver.class)));
    assertEquals("major version", driver.getMajorVersion(), 0);
    assertEquals("minor version", driver.getMinorVersion(), 1);
  }

  @Test
  public void testCreateDriverNonEmptyProperties() throws SQLException {
    Properties properties = new Properties();
    properties.setProperty("user", "baldrick");
    try {
      Driver driver = factory.createDriver(properties);
      fail("created driver with property");
    }
    catch (SQLException e) {
      // expected
    }
  }

  @Test
  public void testCreateDataSource() throws SQLException {
    Properties properties = new Properties();
    checkDatasourceCreation(PGDataSource.class, DataSourceFactory::createDataSource, properties, properties);
    properties.clear();
    properties.put(JDBC_SERVER_NAME, TestUtil.getServer());
    properties.put(JDBC_PORT_NUMBER, TestUtil.getPort());
    properties.put(JDBC_URL, TestUtil.getURL());
    properties.put(JDBC_DATABASE_NAME, TestUtil.getDatabase());
    checkDatasourceCreation(PGDataSource.class, DataSourceFactory::createDataSource, properties, properties);

  }
  @Test
  public void testCreateDataSourceWithURL() throws SQLException {
    Properties createProperties = new Properties();
    createProperties.put(JDBC_URL, "jdbc:pgsql://localhost:5432/test");

    Properties validationProperties = new Properties();
    validationProperties.put(JDBC_SERVER_NAME, TestUtil.getServer());
    validationProperties.put(JDBC_PORT_NUMBER, TestUtil.getPort());
    validationProperties.put(JDBC_DATABASE_NAME, TestUtil.getDatabase());
    checkDatasourceCreation(PGDataSource.class, DataSourceFactory::createDataSource, createProperties, validationProperties);

  }

  @Test
  public void testCreateConnectionPoolDataSource() throws SQLException {
    final Properties properties = new Properties();
    checkDatasourceCreation(PGConnectionPoolDataSource.class, DataSourceFactory::createConnectionPoolDataSource, properties, properties);
    properties.clear();
    properties.put(JDBC_SERVER_NAME, TestUtil.getServer());
    properties.put(JDBC_PORT_NUMBER, TestUtil.getPort());
    properties.put(JDBC_URL, TestUtil.getURL());
    properties.put(JDBC_DATABASE_NAME, TestUtil.getDatabase());
    checkDatasourceCreation(PGConnectionPoolDataSource.class, DataSourceFactory::createConnectionPoolDataSource, properties, properties);
  }

  @Test
  public void testCreateXADataSource() throws SQLException {
    final Properties properties = new Properties();
    checkDatasourceCreation(PGXADataSource.class, DataSourceFactory::createXADataSource, properties, properties);
  }

  private void checkDatasourceCreation(Class<? extends AbstractDataSource> dataSourceClass, DatasourceCreateMethod createMethod, Properties properties, Properties validationProperties) throws SQLException {
    CommonDataSource dataSource = createMethod.apply(factory, properties);
    assertThat(dataSource,
        allOf(notNullValue(),
            instanceOf(dataSourceClass)));
    validateDatasource(dataSourceClass.cast(dataSource), validationProperties);
  }

  private void validateDatasource(AbstractDataSource dataSource, Properties properties) {
    for (Object key : properties.keySet()) {
      Function<AbstractDataSource, Object> getter = getterMap.get(key);
      if(getter == null) {
        assertNotNull("test configuration error: no mapped getter for " + key, getter);
      }
      assertEquals(key.toString(), String.valueOf(properties.get(key)), String.valueOf(getter.apply(dataSource)));
    }
  }

  private interface DatasourceCreateMethod<T extends CommonDataSource> {
    T apply(DataSourceFactory factory, Properties properties) throws SQLException;
  }
}
