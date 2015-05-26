package org.lenzi.fstore.repository;

import java.io.Serializable;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockTimeoutException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.Transient;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.HibernateException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.internal.SessionImpl;
import org.lenzi.fstore.repository.exception.DatabaseException;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;

/**
 * Encapsulates logic for basic repository functionality with a single entity manager.
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
	
	/**
	 * Executes Query.getSingleResult() for the HQL query.
	 * 
	 * @param q The Query object for the HQL query.
	 * @return An Object as the query result.
	 * @throws DatabaseException
	 */
	protected Object getSingleResult(Query q) throws DatabaseException {
		try {
			return q.getSingleResult();
		}catch(NoResultException e){
			logger.warn("NoResultException was thrown. No results for HQL query " + q.toString());
			return null;
		}catch(NonUniqueResultException  e){
			logger.warn("NonUniqueResultException was thrown. Should be single result but got more than one for HQL query " + q.toString());
			return null;			
		}catch(IllegalStateException e){
			throw new DatabaseException("IllegalStateException was thrown. " + e.getMessage());
		}catch(QueryTimeoutException e){
			throw new DatabaseException("QueryTimeoutException was thrown. " + e.getMessage());
		}catch(TransactionRequiredException e){
			throw new DatabaseException("TransactionRequiredException was thrown. " + e.getMessage());
		}catch(PessimisticLockException e){
			throw new DatabaseException("PessimisticLockException was thrown. " + e.getMessage());
		}catch(LockTimeoutException e){
			throw new DatabaseException("LockTimeoutException was thrown. " + e.getMessage());
		}catch(PersistenceException e){
			throw new DatabaseException("PersistenceException was thrown. " + e.getMessage());
		}catch(HibernateException e){
			throw new DatabaseException("HibernateException was thrown. " + e.getMessage());			
		}finally{
	
		}		
	}
	
	/**
	 * Executes Query.getResultList() for the HQL query.
	 * 
	 * @param q The Query object for the HQL query.
	 * @return A List as the query result.
	 * @throws DatabaseException
	 */
	protected List getResultList(Query q) throws DatabaseException {
		try {
			return q.getResultList();
		}catch(IllegalStateException e){
			throw new DatabaseException("IllegalStateException was thrown. " + e.getMessage());
		}catch(QueryTimeoutException e){
			throw new DatabaseException("QueryTimeoutException was thrown. " + e.getMessage());
		}catch(TransactionRequiredException e){
			throw new DatabaseException("TransactionRequiredException was thrown. " + e.getMessage());
		}catch(PessimisticLockException e){
			throw new DatabaseException("PessimisticLockException was thrown. " + e.getMessage());
		}catch(LockTimeoutException e){
			throw new DatabaseException("LockTimeoutException was thrown. " + e.getMessage());
		}catch(PersistenceException e){
			throw new DatabaseException("PersistenceException was thrown. " + e.getMessage());
		}catch(HibernateException e){
			throw new DatabaseException("HibernateException was thrown. " + e.getMessage());			
		}finally{
	
		}		
	}
	
	/**
	 * Executes Query.executeUpdate() for the native SQL query.
	 * 
	 * @param q The Query object for the native SQL query.
	 * @throws DatabaseException
	 */
	protected void executeUpdate(Query q) throws DatabaseException {
		try {
			q.executeUpdate();
		}catch(TransactionRequiredException e){
			throw new DatabaseException("TransactionRequiredException was thrown. " + e.getMessage());
		}catch(IllegalStateException e){
			throw new DatabaseException("IllegalStateException was thrown. " + e.getMessage());
		}catch(QueryTimeoutException e){
			throw new DatabaseException("QueryTimeoutException was thrown. " + e.getMessage());
		}catch(PessimisticLockException e){
			throw new DatabaseException("PessimisticLockException was thrown. " + e.getMessage());
		}catch(LockTimeoutException e){
			throw new DatabaseException("LockTimeoutException was thrown. " + e.getMessage());
		}catch(PersistenceException e){
			throw new DatabaseException("PersistenceException was thrown. " + e.getMessage());
		}catch(HibernateException e){
			throw new DatabaseException("HibernateException was thrown. " + e.getMessage());
		}finally{
			
		}
	}
	
	protected void persist(Object entity) throws DatabaseException {
		
		try {
			
			getEntityManager().persist(entity);
		
		}catch(TransactionRequiredException e){
			throw new DatabaseException("TransactionRequiredException was thrown. " + e.getMessage());
		}catch(IllegalArgumentException e){
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}catch(EntityExistsException e){
			throw new DatabaseException("EntityExistsException was thrown. " + e.getMessage());
		}catch(PersistenceException e){
			throw new DatabaseException("PersistenceException was thrown. " + e.getMessage());
		}catch(ConstraintViolationException e){
			throw new DatabaseException("ConstraintViolationException was thrown. " + e.getMessage());
		}catch(HibernateException e){
			throw new DatabaseException("HibernateException was thrown. " + e.getMessage());
		}catch(Exception e){
			throw new DatabaseException("General Exception was thrown. " + e.getMessage());			
		}finally{
			
		}
		
	}
	
	protected Object merge(Object entity) throws DatabaseException {
		
		try {
			
			return getEntityManager().merge(entity);
		
		}catch(TransactionRequiredException e){
			throw new DatabaseException("TransactionRequiredException was thrown. " + e.getMessage());
		}catch(IllegalArgumentException e){
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}catch(EntityExistsException e){
			throw new DatabaseException("EntityExistsException was thrown. " + e.getMessage());
		}catch(PersistenceException e){
			throw new DatabaseException("PersistenceException was thrown. " + e.getMessage());
		}catch(ConstraintViolationException e){
			throw new DatabaseException("ConstraintViolationException was thrown. " + e.getMessage());
		}catch(HibernateException e){
			throw new DatabaseException("HibernateException was thrown. " + e.getMessage());
		}catch(Exception e){
			throw new DatabaseException("General Exception was thrown. " + e.getMessage());			
		}finally{
			
		}
		
	}	
	
	protected void remove(Object entity)  throws DatabaseException {
		
		try {
			getEntityManager().remove(entity);
		}catch(TransactionRequiredException e){
			throw new DatabaseException("TransactionRequiredException was thrown. " + e.getMessage());
		}catch(IllegalArgumentException e){
			throw new DatabaseException("IllegalArgumentException was thrown. " + e.getMessage());
		}		
		
	}
	
	protected Object getSingleResult(CriteriaQuery q) throws DatabaseException {
		
		return getSingleResult( getEntityManager().createQuery(q) ); 
		
	}
	
	protected List getResultList(CriteriaQuery q) throws DatabaseException {
		
		return getResultList( getEntityManager().createQuery(q) );
		
	}

}
