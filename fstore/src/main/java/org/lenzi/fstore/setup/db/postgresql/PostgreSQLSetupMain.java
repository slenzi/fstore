package org.lenzi.fstore.setup.db.postgresql;

import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.setup.db.postgresql.config.PostgreSQLSetupConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Initialize and/or reset PostgreSQL database setup.
 * 
 * @author sal
 */
@Component
public class PostgreSQLSetupMain {
	
	@Autowired
	private PostgreSQLCreate postgreSQLCreate;

	public static void main(String[] args) {
		
		// initialize app with PostgreSQLSetupConfig.class
		final ApplicationContext context = new AnnotationConfigApplicationContext(PostgreSQLSetupConfig.class);

		// get instance of this app
		final PostgreSQLSetupMain databaseSetupApp = context.getBean(PostgreSQLSetupMain.class);
		
		// reset PostgreSQL database objects
		databaseSetupApp.doReset();
		
	}
	
	public PostgreSQLSetupMain() {

	}
	
	/**
	 * Execute the reset process.
	 */
	public void doReset(){
	
		System.out.println("Running PostgreSQL reset");
		
		System.out.println("Have entity manager? => " + postgreSQLCreate.haveEntityManager());
		
		try {
			postgreSQLCreate.resetDatabase();
		} catch (DatabaseException e) {
			e.printStackTrace();
			System.err.println("Error resetting PostgreSQL database. " + e.getMessage());
		}
	
	}

}
