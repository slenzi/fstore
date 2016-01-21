/**
 * 
 */
package org.lenzi.fstore.core.security.service;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.security.FsUserGroupRepository;
import org.lenzi.fstore.core.repository.security.FsUserRepository;
import org.lenzi.fstore.core.repository.security.FsUserRepository.FsUserFetch;
import org.lenzi.fstore.core.repository.security.FsUserRoleRepository;
import org.lenzi.fstore.core.repository.security.model.impl.FsUser;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.CollectionUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for security related actions
 * 
 * @author sal
 */
@Service
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsSecurityService {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private FsUserRepository fsUserRepository;
	
	@Autowired
	private FsUserRoleRepository fsUserRoleRepository;
	
	@Autowired
	private FsUserGroupRepository fsUserGroupRepository;
	
	/**
	 * 
	 */
	public FsSecurityService() {
		
	}
	
	/**
	 * Fetch user by username
	 * 
	 * @param username
	 * @return
	 * @throws ServiceException
	 */
	public FsUser getUserByUsername(final String username) throws ServiceException {
		
		logInfo(FsSecurityService.class.getName() + ".getUserByUsername(final String username) called. [username = '" + username + "']");
		
		if(username == null || username.trim().equals("")){
			throw new ServiceException("Username is null or blank. Cannot retrieve user object.");
		}
		
		FsUser user = null;
		try {
			user = fsUserRepository.getUserByUsername(username, FsUserFetch.WITH_ROLES_AND_GROUPS);
		} catch (DatabaseException e) {
			throw new ServiceException("Failed to retrieve user object,  " + e.getMessage(), e);
		}
		
		/*
		logInfo("Got user object = " + ((user != null) ? "true" : "false"));
		
		if(user != null){
			
			logInfo("Fetched user => [first name = '" + user.getFirstName() + "', last name = '" + user.getLastName() + 
					"', username = '" + user.getUsername() + "', role count = " + user.roleCount() + 
					", group count = " + user.groupCount());
			
			CollectionUtil.emptyIfNull(user.getRoles()).forEach( (role) -> {
				logInfo("Role for " + username + ": " + role.getRoleCode());
			});
			CollectionUtil.emptyIfNull(user.getGroups()).forEach( (group) -> {
				logInfo("Group for " + username + ": " + group.getGroupCode());
			});			
			
		}else{
			logInfo("Fetched user object in null...");
		}
		*/
		
		return user;
		
	}
	
	/**
	 * Fetch user by user id
	 * 
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public FsUser getUserById(final Long userId) throws ServiceException {
	
		logInfo(FsSecurityService.class.getName() + ".getUserById(final Long userId) called. [userId = '" + userId + "']");
		
		if(userId == null || userId == 0L){
			throw new ServiceException("User ID is null or not set. Cannot retrieve user object.");
		}
		
		FsUser user = null;
		try {
			user = fsUserRepository.getUserById(userId, FsUserFetch.WITH_ROLES_AND_GROUPS);
		} catch (DatabaseException e) {
			throw new ServiceException("Failed to retrieve user object,  " + e.getMessage(), e);
		}		
		
		return user;
		
	}
	
	private void logInfo(String message){
		if(logger != null){
			logger.info(message);
		}else{
			System.out.println("> " + message);
		}
	}
	
	private void logError(String message, Throwable t){
		if(logger != null){
			logger.error(message, t);
		}else{
			System.err.println("> " + message + ((t != null) ? " " + t.getMessage() : ""));
		}
	}

}
