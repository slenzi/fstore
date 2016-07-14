package org.lenzi.fstore.core.security.acls.service;

import org.lenzi.fstore.core.security.FsSecureUser;
import org.lenzi.fstore.core.security.acls.domain.FsBasePemission;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for altering Spring Security ACL permissions on our Fstore domain objects.
 * 
 * @author slenzi
 */
//TODO - ADD THIS BACK AFTER YOU REWRITE JUNIT TESTS TO USE PURE JAVA CONFIG (NO XML)
@Service
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsAclService {

	@InjectLogger
	private Logger logger;
	
	/**
	 * @see org.lenzi.fstore.main.config.AclSecurityConfig for Spring ACL setup.
	 */
	@Autowired
	private JdbcMutableAclService aclService;
	
	public FsAclService() {
		
	}
	
	/**
	 * Sample method for setting ACL permissions.
	 * 
	 * @param domainClass - class type of domain object (e.g. FsFileMetaResource, FsDirectoryResource, or some other FSNode type...)
	 * @param domainId - Unique ID of domain object.
	 * @param userId - Unique ID of user.
	 */
	public void setDefaultPermission(Class<? extends FsPathResource> domainClass, Long domainId, Long userId){
		
		logger.info(this.getClass().getName() + ".setDefaultPermission(...) called");
		
		ObjectIdentity oi = new ObjectIdentityImpl(domainClass, domainId);
		Sid sid = new PrincipalSid(String.valueOf(userId.longValue()));
		
		Permission p = FsBasePemission.ADMINISTRATION;
		
		MutableAcl acl = null;
		try {
			acl = (MutableAcl) aclService.readAclById(oi);
		} catch (NotFoundException nfe) {
			acl = aclService.createAcl(oi);
		}
		
		acl.insertAce(acl.getEntries().size(), p, sid, true);
		aclService.updateAcl(acl);
		
	}

}
