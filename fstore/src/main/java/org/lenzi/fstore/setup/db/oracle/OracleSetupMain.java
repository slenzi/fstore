package org.lenzi.fstore.setup.db.oracle;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
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
		
		if(args == null || args.length != 1){
			
			System.err.println("No param. Usage: OracleSetupMain create|drop|reset");
			
		}else{
			
			// initialize app with OracleSetupConfig.class
			final ApplicationContext context = new AnnotationConfigApplicationContext(OracleSetupConfig.class);

			// get instance of this app
			final OracleSetupMain databaseSetupApp = context.getBean(OracleSetupMain.class);			
			
			if(args[0].trim().equalsIgnoreCase("create")){
				
				// create all objects
				databaseSetupApp.doCreate();				
				
			}else if(args[0].trim().equalsIgnoreCase("drop")){
				
				// drop all objects
				databaseSetupApp.doDrop();
				
			}else if(args[0].trim().equalsIgnoreCase("reset")){
				
				// drop and re-add all objects
				databaseSetupApp.doReset();
				
			}else{
				
				System.err.println("Unknown param. Usage: OracleSetupMain create|drop|reset");
				
			}
			
		}
		
	}
	
	public OracleSetupMain() {

	}
	
	public void doCreate(){
		
		System.out.println("Running Oracle create");
		
		System.out.println("Have entity manager? => " + oracleCreate.haveEntityManager());
		
		try {
			oracleCreate.createDatabase();
		} catch (DatabaseException e) {
			e.printStackTrace();
			System.err.println("Error creating Oracle objects. " + e.getMessage());
		}		
		
	}
	
	public void doDrop(){
		
		System.out.println("Running Oracle drop");
		
		System.out.println("Have entity manager? => " + oracleCreate.haveEntityManager());
		
		try {
			oracleCreate.dropDatabase();
		} catch (DatabaseException e) {
			e.printStackTrace();
			System.err.println("Error dropping Oracle objects. " + e.getMessage());
		}		
		
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
			System.err.println("Error resetting Oracle objects. " + e.getMessage());
		}
	
	}

}
