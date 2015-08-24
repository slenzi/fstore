package org.lenzi.fstore.cms.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.lenzi.fstore.cms.repository.model.impl.FsCmsSite;
import org.lenzi.fstore.cms.repository.model.impl.FsCmsSite_;
import org.lenzi.fstore.core.repository.AbstractRepository;
import org.lenzi.fstore.core.repository.ResultFetcher;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.service.FsResourceHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for dealing with cms site operations.
 * 
 * @author sal
 */
@Repository
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsCmsSiteRepository extends AbstractRepository {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1355098362744443645L;

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsResourceHelper fsResourceHelper;
	
	
	public FsCmsSiteRepository() {
		
	}
	
	/**
	 * Get all cms sites
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	public List<FsCmsSite> getAllSites() throws DatabaseException {
		
		return getAllSitesCriteria();
		
	}
	
	/**
	 * Get site by site id
	 * 
	 * @param siteId
	 * @return
	 * @throws DatabaseException
	 */
	public FsCmsSite getSiteBySiteId(Long siteId) throws DatabaseException {
		
		return getSiteBySiteIdCriteria(siteId);
		
	}
	
	/**
	 * Criteria query to get all cms sites
	 * 
	 * @return
	 * @throws DatabaseException
	 */
	private List<FsCmsSite> getAllSitesCriteria() throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsCmsSite> type = FsCmsSite.class;
		CriteriaQuery<FsCmsSite> query = cb.createQuery(type);
		Root<FsCmsSite> root = query.from(type);

		root.fetch(FsCmsSite_.offlineResourceStore, JoinType.LEFT);
		root.fetch(FsCmsSite_.onlineResourceStore, JoinType.LEFT);
		
		query.select(root);
		
		return ResultFetcher.getResultListOrNull(getEntityManager().createQuery(query));		
		
	}
	
	/**
	 * Criteria query to get site by site id
	 * 
	 * @param siteId
	 * @return
	 * @throws DatabaseException
	 */
	private FsCmsSite getSiteBySiteIdCriteria(Long siteId) throws DatabaseException {
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		
		Class<FsCmsSite> type = FsCmsSite.class;
		CriteriaQuery<FsCmsSite> query = cb.createQuery(type);
		Root<FsCmsSite> root = query.from(type);
		
		root.fetch(FsCmsSite_.offlineResourceStore, JoinType.LEFT);
		root.fetch(FsCmsSite_.onlineResourceStore, JoinType.LEFT);
		
		List<Predicate> andPredicates = new ArrayList<Predicate>();
		andPredicates.add( cb.equal(root.get(FsCmsSite_.siteId), siteId) );
		
		query.select(root);
		query.where(
				cb.and( andPredicates.toArray(new Predicate[andPredicates.size()]) )
				);
		
		return ResultFetcher.getSingleResultOrNull(getEntityManager().createQuery(query));
		
	}

}
