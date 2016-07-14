package org.lenzi.fstore.setup.db.postgresql;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
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
		
		if(args == null || args.length != 1){
			
			System.err.println("No param. Usage: PostgreSQLSetupMain create|drop|reset");
			
		}else{
			
			// initialize app with PostgreSQLSetupConfig.class
			final ApplicationContext context = new AnnotationConfigApplicationContext(PostgreSQLSetupConfig.class);

			// get instance of this app
			final PostgreSQLSetupMain databaseSetupApp = context.getBean(PostgreSQLSetupMain.class);			
			
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
				
				System.err.println("Unknown param. Usage: PostgreSQLSetupMain create|drop|reset");
				
			}			
			
		}
		
	}
	
	public PostgreSQLSetupMain() {

	}
	
	public void doCreate(){
		
		System.out.println("Running PostgreSQL create");
		
		System.out.println("Have entity manager? => " + postgreSQLCreate.haveEntityManager());
		
		try {
			postgreSQLCreate.createDatabase();
		} catch (DatabaseException e) {
			e.printStackTrace();
			System.err.println("Error creating PostgreSQL objects. " + e.getMessage());
		}		
		
	}
	
	public void doDrop(){
		
		System.out.println("Running PostgreSQL drop");
		
		System.out.println("Have entity manager? => " + postgreSQLCreate.haveEntityManager());
		
		try {
			postgreSQLCreate.dropDatabase();
		} catch (DatabaseException e) {
			e.printStackTrace();
			System.err.println("Error dropping PostgreSQL objects. " + e.getMessage());
		}		
		
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
			System.err.println("Error resetting PostgreSQL objects. " + e.getMessage());
		}
	
	}

}
