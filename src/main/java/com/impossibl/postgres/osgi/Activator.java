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

import com.impossibl.postgres.jdbc.PGDriver;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

  private ServiceTracker<LogService, LogService> logTracker;
  private ServiceRegistration<DataSourceFactory> serviceRegistration;

  @Override
  public void start(BundleContext context) throws SQLException {
    System.out.println("context = " + context);
    this.logTracker = new ServiceTracker<>(context, LogService.class.getName(), null);
    LogService logger = logTracker.getService();
    if (logger != null) {
      logger.log(LogService.LOG_DEBUG, "Registering PGJDBC JDBC Provider with OSGI");
    }
    PGJDBCDataSourceFactory dataSourceFactory = new PGJDBCDataSourceFactory();
    Driver driver = dataSourceFactory.createDriver(null);
    Hashtable<String, Object> props = new Hashtable<>();
    props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, PGDriver.class.getName());
    props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, PGDriver.NAME);
    props.put(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION, PGDriver.VERSION.toString());
    serviceRegistration =
        context.registerService(DataSourceFactory.class, dataSourceFactory, props);
    if (logger != null) {
      logger.log(LogService.LOG_INFO, "Registered PGJDBC JDBC Provider with OSGI");
    }
  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    serviceRegistration.unregister();
    LogService logger = logTracker.getService();
    if (logger != null) {
      logger.log(LogService.LOG_INFO, "Unregistered PGJDBC JDBC Provider with OSGI");
    }
    logTracker.close();
  }
}
