/**
 * 
 */
package org.lenzi.fstore.listener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * De-register database drivers on shutdown to prevent memory leak.
 * 
 * @author slenzi
 */
public class DatabaseDriverCleanup implements ServletContextListener {

	private Logger logger = LoggerFactory.getLogger(DatabaseDriverCleanup.class);

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent context) {
		Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                logger.info(String.format("deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
            	logger.warn(String.format("Error deregistering driver %s", driver), e);
            }

        }
	}

	@Override
	public void contextInitialized(ServletContextEvent context) {
		// TODO Auto-generated method stub
		
	}

}
