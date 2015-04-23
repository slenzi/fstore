package org.lenzi.fstore.setup.db.oracle;

import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.setup.db.oracle.config.OracleSetupConfig;
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
		
		// reset Oracle database objects
		databaseSetupApp.doReset();
		
	}
	
	public OracleSetupMain() {

	}
	
	/**
	 * Execute the reset process.
	 */
	public void doReset(){
	
		System.out.println("Running Oracle reset");
		
		System.out.println("Have entity manager? => " + oracleCreate.haveEntityManager());
		
		try {
			oracleCreate.resetDatabase();
		} catch (DatabaseException e) {
			e.printStackTrace();
			System.err.println("Error resetting Oracle database. " + e.getMessage());
		}
	
	}

}
