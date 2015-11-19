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
import org.lenzi.fstore.core.repository.security.model.impl.FsUserGroup;
import org.lenzi.fstore.core.repository.security.model.impl.FsUserGroup_;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for dealing with user groups.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsUserGroupRepository extends AbstractRepository {

	private static final long serialVersionUID = -7026271464897503390L;

	public enum FsUserGroupFetch {
		
		// just group data
		DEFAULT,
		
		// include all users of group
		WITH_USERS,
		
	}	
	
	/**
	 * 
	 */
	public FsUserGroupRepository() {
		
	}
	
	/**
	 * Fetch all users groups.
	 * 
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	public List<FsUserGroup> getAllUserGroups(FsUserGroupFetch fetch) throws DatabaseException {
		
		return getAllUserGroupsCriteria(fetch);
		
	}
	
	/**
	 * Fetch group by id
	 * 
	 * @param groupId - id of the group
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	public FsUserGroup getUserGroupById(Long groupId, FsUserGroupFetch fetch) throws DatabaseException {
		
		return getUserGroupByIdCriteria(groupId, fetch);
		
	}
	
	/**
	 * Fetch group by code
	 * 
	 * @param groupCode - code of the group
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	public FsUserGroup getUserGroupByCode(String groupCode, FsUserGroupFetch fetch) throws DatabaseException {
		
		return getUserGroupByCodeCriteria(groupCode, fetch);
		
	}	
	
	/**
	 * Fetch all users groups.
	 * 
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	private List<FsUserGroup> getAllUserGroupsCriteria(FsUserGroupFetch fetch) throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsUserGroup> type = FsUserGroup.class;
		CriteriaQuery<FsUserGroup> query = cb.createQuery(type);
		Root<FsUserGroup> root = query.from(type);

		switch(fetch){
		
			case DEFAULT:
				break;			
		
			case WITH_USERS:
				root.fetch(FsUserGroup_.users, JoinType.LEFT);
				break;			
				
			default:
				break;		
		}
		
		query.select(root);
		
		return ResultFetcher.getResultListOrNull(getEntityManager().createQuery(query));		
		
	}
	
	/**
	 * Fetch group by id
	 * 
	 * @param groupId - id of the group
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	private FsUserGroup getUserGroupByIdCriteria(Long groupId, FsUserGroupFetch fetch) throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsUserGroup> type = FsUserGroup.class;
		CriteriaQuery<FsUserGroup> query = cb.createQuery(type);
		Root<FsUserGroup> root = query.from(type);

		switch(fetch){
		
			case DEFAULT:
				break;			
		
			case WITH_USERS:
				root.fetch(FsUserGroup_.users, JoinType.LEFT);
				break;			
				
			default:
				break;		
		}
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( cb.equal(root.get(FsUserGroup_.groupId), groupId) );
		
		query.select(root);
		query.where(
				cb.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		return ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
	}
	
	/**
	 * Fetch group by code
	 * 
	 * @param groupCode - code of the group
	 * @param fetch - specify which data to fetch
	 * @return
	 * @throws DatabaseException
	 */
	private FsUserGroup getUserGroupByCodeCriteria(String groupCode, FsUserGroupFetch fetch) throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsUserGroup> type = FsUserGroup.class;
		CriteriaQuery<FsUserGroup> query = cb.createQuery(type);
		Root<FsUserGroup> root = query.from(type);

		switch(fetch){
		
			case DEFAULT:
				break;			
		
			case WITH_USERS:
				root.fetch(FsUserGroup_.users, JoinType.LEFT);
				break;			
				
			default:
				break;		
		}
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( cb.equal(root.get(FsUserGroup_.groupCode), groupCode) );
		
		query.select(root);
		query.where(
				cb.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		return ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
	}	

}
