package org.lenzi.fstore.repository;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Transient;

import org.hibernate.internal.SessionImpl;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;

/**
 * Encapsulates logic for basic repository functionality.
 * 
 * 
 * @author slenzi
 */
public class AbstractRepository implements Serializable {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = -7142560902806805291L;
	
	@InjectLogger
	private Logger logger;
	
	@PersistenceContext
	protected EntityManager entityManager;	
	
	public AbstractRepository() {
		
	}
	
	/**
	 * Get default entity manager
	 * 
	 * @return
	 */
	public EntityManager getEntityManager(){
		return entityManager;
	}
	
	/**
	 * True if entity manager is not null, false otherwise.
	 * 
	 * @return
	 */
	public boolean haveEntityManager(){
		return ((entityManager != null) ? true : false);
	}
	
	public void debugEntityManager(){
		if(!haveEntityManager())
			return;
		
		logger.debug("Entity manager & factory debug info:");
		
		Map<String,Object> emProp = entityManager.getProperties();
		for(String s : emProp.keySet()){
			logger.debug("EntityManager[" + s + "] = " + emProp.get(s).toString());
		}
		EntityManagerFactory emf = entityManager.getEntityManagerFactory();
		Map<String, Object> emfProp = emf.getProperties();
		for(String s : emfProp.keySet()){
			logger.debug("EntityManagerFactory[" + s + "] = " + emfProp.get(s).toString());
		}
		Connection conn = entityManager.unwrap(SessionImpl.class).connection();
		DatabaseMetaData dbMeta = null;
		try {
			dbMeta = conn.getMetaData();
			if(dbMeta != null){
				logger.debug("Database Name = " + dbMeta.getDatabaseProductName());
				logger.debug("Database Production Version = " + dbMeta.getDatabaseProductVersion());
				logger.debug("Database Major Version = " + dbMeta.getDatabaseMajorVersion());
				logger.debug("Database Minor Version = " + dbMeta.getDatabaseMinorVersion());
				logger.debug("Database Driver Name = " + dbMeta.getDriverName());
				logger.debug("Database Driver Version = " + dbMeta.getDriverVersion());
				logger.debug("Database Driver Major Version = " + dbMeta.getDriverMajorVersion());
				logger.debug("Database Driver Minor Version = " + dbMeta.getDriverMinorVersion());
				logger.debug("Database User Name = " + dbMeta.getUserName());
			}
		} catch (SQLException e) {
			logger.error("Failded to get database meta data from connection which was unwraped from entity manager. " + e.getMessage());
		}
	}

}
