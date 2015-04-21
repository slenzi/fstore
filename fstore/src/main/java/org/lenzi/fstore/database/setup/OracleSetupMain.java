package org.lenzi.fstore.database.setup;

import org.lenzi.fstore.database.setup.config.OracleCreate;
import org.lenzi.fstore.database.setup.config.OracleSetupConfig;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Initialize and/or reset Oracle database setup.
 * 
 * @author sal
 */
@Component
public class OracleSetupMain {
	
	@Autowired
	private OracleCreate oracleCreate;

	public static void main(String[] args) {
		
		// initialize app with OracleSetupConfig.class
		final ApplicationContext context = new AnnotationConfigApplicationContext(OracleSetupConfig.class);

		// get instance of this app
		final OracleSetupMain databaseSetupApp = context.getBean(OracleSetupMain.class);
		
		// reset oracle database
		databaseSetupApp.doReset();
		
	}
	
	public OracleSetupMain() {

	}
	
	/**
	 * Execute the reset process.
	 */
	public void doReset(){
	
		System.out.println("Running Oracle setup");
		
		System.out.println("Have entity manager? => " + oracleCreate.haveEntityManager());
		
		try {
			oracleCreate.resetDatabase();
		} catch (DatabaseException e) {
			e.printStackTrace();
			System.err.println("Error resetting database. " + e.getMessage());
		}
	
	}

}
