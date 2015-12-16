/**
 * 
 */
package org.lenzi.fstore.core.repository.security;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.ResultFetcher;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.security.model.impl.FsUser;
import org.lenzi.fstore.core.repository.security.model.impl.FsUser_;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for dealing with users.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsUserRepository extends AbstractRepository {

	private static final long serialVersionUID = 5283613856804877431L;

	@InjectLogger
	private Logger logger;	
	
	public enum FsUserFetch {
		
		// just user data
		DEFAULT,
		
		// include user roles
		WITH_ROLES,
		
		// include user groups
		WITH_GROUPS,
		
		// include user roles and groups
		WITH_ROLES_AND_GROUPS,		
		
	}	
	
	/**
	 * 
	 */
	public FsUserRepository() {

	}
	
	/**
	 * Fetch all users with their groups and roles
	 * 
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	public List<FsUser> getAllUsers(FsUserFetch fetch) throws DatabaseException {	
	
		return getAllUsersCriteria(fetch);
		
	}
	
	/**
	 * Fetch user by ID.
	 * 
	 * @param userId - the ID of the user
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	public FsUser getUserById(Long userId, FsUserFetch fetch) throws DatabaseException {
		
		return getUserByIdCriteria(userId, fetch);
		
	}
	
	/**
	 * Fetch user by their username
	 * 
	 * @param username - the users username
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	public FsUser getUserByUsername(String username, FsUserFetch fetch) throws DatabaseException {
		
		System.out.println(FsUserRepository.class.getName() + ".getUserByUsername(String username, FsUserFetch fetch) called");
		
		//logger.debug(FsUserRepository.class.getName() + ".getUserByUsername(String username, FsUserFetch fetch) called");
		
		return getUserByUsernameCriteria(username, fetch);
		
	}
	
	/**
	 * Fetch user by their primary email adress
	 * 
	 * @param primaryEmail - the users primary email address
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	public FsUser getUserByPrimaryEmail(String primaryEmail, FsUserFetch fetch) throws DatabaseException {
		
		return getUserByPrimaryEmailCriteria(primaryEmail, fetch);
		
	}	
	
	/**
	 * Fetch all users with their groups and roles
	 * 
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	private List<FsUser> getAllUsersCriteria(FsUserFetch fetch) throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsUser> type = FsUser.class;
		CriteriaQuery<FsUser> query = cb.createQuery(type);
		Root<FsUser> root = query.from(type);

		switch(fetch){
		
			case DEFAULT:
				break;			
		
			case WITH_ROLES:
				root.fetch(FsUser_.roles, JoinType.LEFT);
				break;
				
			case WITH_GROUPS:
				root.fetch(FsUser_.groups, JoinType.LEFT);
				break;
				
			case WITH_ROLES_AND_GROUPS:
				root.fetch(FsUser_.roles, JoinType.LEFT);
				root.fetch(FsUser_.groups, JoinType.LEFT);				
				break;				
				
			default:
				break;		
		}
		
		query.select(root);
		
		return ResultFetcher.getResultListOrNull(getEntityManager().createQuery(query));		
		
	}
	
	/**
	 * Fetch user by ID.
	 * 
	 * @param userId - the ID of the user
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	private FsUser getUserByIdCriteria(Long userId, FsUserFetch fetch) throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsUser> type = FsUser.class;
		CriteriaQuery<FsUser> query = cb.createQuery(type);
		Root<FsUser> root = query.from(type);

		switch(fetch){
		
			case DEFAULT:
				break;			
		
			case WITH_ROLES:
				root.fetch(FsUser_.roles, JoinType.LEFT);
				break;
				
			case WITH_GROUPS:
				root.fetch(FsUser_.groups, JoinType.LEFT);
				break;
				
			case WITH_ROLES_AND_GROUPS:
				root.fetch(FsUser_.roles, JoinType.LEFT);
				root.fetch(FsUser_.groups, JoinType.LEFT);				
				break;				
				
			default:
				break;		
		}
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( cb.equal(root.get(FsUser_.userId), userId) );
		
		query.distinct(true);
		query.select(root);
		query.where(
				cb.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		return ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
	}
	
	/**
	 * Fetch user by username.
	 * 
	 * @param username - the username of the user
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	private FsUser getUserByUsernameCriteria(String username, FsUserFetch fetch) throws DatabaseException {
		
		System.out.println(FsUserRepository.class.getName() + ".getUserByUsernameCriteria(String username, FsUserFetch fetch) called");
		
		//logger.debug(FsUserRepository.class.getName() + ".getUserByUsernameCriteria(String username, FsUserFetch fetch) called");
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsUser> type = FsUser.class;
		CriteriaQuery<FsUser> query = cb.createQuery(type);
		Root<FsUser> root = query.from(type);

		switch(fetch){
		
			case DEFAULT:
				break;			
		
			case WITH_ROLES:
				root.fetch(FsUser_.roles, JoinType.LEFT);
				break;
				
			case WITH_GROUPS:
				root.fetch(FsUser_.groups, JoinType.LEFT);
				break;
				
			case WITH_ROLES_AND_GROUPS:
				root.fetch(FsUser_.roles, JoinType.LEFT);
				root.fetch(FsUser_.groups, JoinType.LEFT);				
				break;				
				
			default:
				break;		
		}
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( cb.equal(root.get(FsUser_.username), username) );
		
		query.distinct(true);
		query.select(root);
		query.where(
				cb.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		return ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
	}
	
	/**
	 * Fetch user by their primary email address.
	 * 
	 * @param primaryEmail - the users primary email address
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	private FsUser getUserByPrimaryEmailCriteria(String primaryEmail, FsUserFetch fetch) throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsUser> type = FsUser.class;
		CriteriaQuery<FsUser> query = cb.createQuery(type);
		Root<FsUser> root = query.from(type);

		switch(fetch){
		
			case DEFAULT:
				break;			
		
			case WITH_ROLES:
				root.fetch(FsUser_.roles, JoinType.LEFT);
				break;
				
			case WITH_GROUPS:
				root.fetch(FsUser_.groups, JoinType.LEFT);
				break;
				
			case WITH_ROLES_AND_GROUPS:
				root.fetch(FsUser_.roles, JoinType.LEFT);
				root.fetch(FsUser_.groups, JoinType.LEFT);				
				break;				
				
			default:
				break;		
		}
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( cb.equal(root.get(FsUser_.primaryEmail), primaryEmail) );
		
		query.distinct(true);
		query.select(root);
		query.where(
				cb.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		return ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
	}	

}
