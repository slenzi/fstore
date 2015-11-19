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
import org.lenzi.fstore.core.repository.security.FsUserGroupRepository.FsUserGroupFetch;
import org.lenzi.fstore.core.repository.security.model.impl.FsUserGroup;
import org.lenzi.fstore.core.repository.security.model.impl.FsUserGroup_;
import org.lenzi.fstore.core.repository.security.model.impl.FsUserRole;
import org.lenzi.fstore.core.repository.security.model.impl.FsUserRole_;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for dealing with user roles.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsUserRoleRepository extends AbstractRepository {

	private static final long serialVersionUID = -2648423492134739129L;

	public enum FsUserRoleFetch {
		
		// just role data
		DEFAULT,
		
		// include all users of role
		WITH_USERS,
		
	}	
	
	/**
	 * 
	 */	
	public FsUserRoleRepository() {

	}
	
	/**
	 * Fetch all users groups.
	 * 
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	public List<FsUserRole> getAllUserGroups(FsUserRoleFetch fetch) throws DatabaseException {
		
		return getAllUserGroupsCriteria(fetch);
		
	}
	
	/**
	 * Fetch role by id
	 * 
	 * @param roleId - id of the role
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	public FsUserRole getUserRoleById(Long roleId, FsUserRoleFetch fetch) throws DatabaseException {
		
		return getUserRoleByIdCriteria(roleId, fetch);
		
	}	
	
	/**
	 * Fetch all users roles.
	 * 
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	private List<FsUserRole> getAllUserGroupsCriteria(FsUserRoleFetch fetch) throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsUserRole> type = FsUserRole.class;
		CriteriaQuery<FsUserRole> query = cb.createQuery(type);
		Root<FsUserRole> root = query.from(type);

		switch(fetch){
		
			case DEFAULT:
				break;			
		
			case WITH_USERS:
				root.fetch(FsUserRole_.users, JoinType.LEFT);
				break;			
				
			default:
				break;		
		}
		
		query.select(root);
		
		return ResultFetcher.getResultListOrNull(getEntityManager().createQuery(query));		
		
	}
	
	/**
	 * Fetch role by id
	 * 
	 * @param roleId - id of the role
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	private FsUserRole getUserRoleByIdCriteria(Long roleId, FsUserRoleFetch fetch) throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsUserRole> type = FsUserRole.class;
		CriteriaQuery<FsUserRole> query = cb.createQuery(type);
		Root<FsUserRole> root = query.from(type);

		switch(fetch){
		
			case DEFAULT:
				break;			
		
			case WITH_USERS:
				root.fetch(FsUserRole_.users, JoinType.LEFT);
				break;			
				
			default:
				break;		
		}
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( cb.equal(root.get(FsUserRole_.roleId), roleId) );
		
		query.select(root);
		query.where(
				cb.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		return ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
	}	

}
