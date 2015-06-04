package org.lenzi.fstore.core.repository;

import java.util.List;

import javax.persistence.LockTimeoutException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;
import javax.persistence.TransactionRequiredException;

import org.hibernate.HibernateException;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.springframework.stereotype.Component;

@Component
public class ResultFetcher {
	
	/**
	 * Get result list from query
	 * 
	 * @param q
	 * @return
	 * @throws DatabaseException
	 */
	public static <T> List<T> getResultListOrNull(Query q) throws DatabaseException {
		
		List<T> results = null;
		
		try {
			
			results = q.getResultList();
			
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
		
		if(results.isEmpty()){
			return null;
		}
		
		return results;
		
	}

	/**
	 * Get single result from query
	 * 
	 * @param q
	 * @return
	 * @throws DatabaseException
	 */
	public static <T> T getSingleResultOrNull(Query q) throws DatabaseException {
		
		List<T> results = null;
		
		try {
			
			results = q.getResultList();
			
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
		
		if(results.isEmpty()){
			return null;
		}else if(results.size() == 1){
			return results.get(0);
		}
		
		throw new NonUniqueResultException();
	}

}
