package com.impossibl.postgres.osgi;

import com.impossibl.postgres.jdbc.PGDriver;
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
    public void start(BundleContext context) {
        this.logTracker = new ServiceTracker<>(context, LogService.class.toString(), null);
        LogService logger = logTracker.getService();
        if(logger != null) {
            logger.log(LogService.LOG_DEBUG, "Registering PGJDBC JDBC Provider with OSGI");
        }
        Hashtable<String, Object> props = new Hashtable<>();
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, PGDriver.class);
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, "PGJDBC");
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION, "0.8");
        PGJDBCDataSourceFactory dataSourceFactory = new PGJDBCDataSourceFactory();
        serviceRegistration =
            context.registerService(DataSourceFactory.class, dataSourceFactory, props);
        if(logger != null) {
            logger.log(LogService.LOG_DEBUG,"Registered PGJDBC JDBC Provider with OSGI");
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        serviceRegistration.unregister();
        LogService logger = logTracker.getService();
        if(logger != null) {
           logger.log(LogService.LOG_DEBUG,"Unregistered PGJDBC JDBC Provider with OSGI");
        }
        logTracker.close();
    }
}
