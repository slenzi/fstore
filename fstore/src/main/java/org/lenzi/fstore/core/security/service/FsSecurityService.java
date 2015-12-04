/**
 * 
 */
package org.lenzi.fstore.core.security.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.repository.security.FsUserGroupRepository;
import org.lenzi.fstore.core.repository.security.FsUserRepository;
import org.lenzi.fstore.core.repository.security.FsUserRepository.FsUserFetch;
import org.lenzi.fstore.core.repository.security.FsUserRoleRepository;
import org.lenzi.fstore.core.repository.security.model.impl.FsUser;
import org.lenzi.fstore.core.security.FsSecureUser;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.CollectionUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
		
		logger.info(FsSecurityService.class.getName() + ".getUserByUsername(final String username) called. [username = '" + username + "']");
		
		if(username == null || username.trim().equals("")){
			throw new ServiceException("Username is null or blank. Cannot retrieve user object.");
		}
		
		FsUser user = null;
		try {
			user = fsUserRepository.getUserByUsername(username, FsUserFetch.WITH_ROLES_AND_GROUPS);
		} catch (DatabaseException e) {
			throw new ServiceException("Failed to retrieve user object,  " + e.getMessage(), e);
		}
		
		if(user != null){
			
			logger.info("Fetched user => [first name = '" + user.getFirstName() + "', last name = '" + user.getLastName() + 
					"', username = '" + user.getUsername() + "', role count = " + user.roleCount() + 
					", group count = " + user.groupCount());
			
			CollectionUtil.emptyIfNull(user.getRoles()).forEach( (role) -> {
				logger.info("Role for " + username + ": " + role.getRoleCode());
			});
			CollectionUtil.emptyIfNull(user.getGroups()).forEach( (group) -> {
				logger.info("Group for " + username + ": " + group.getGroupCode());
			});			
			
		}else{
			logger.info("Fetched user object in null...");
		}
		
		return user;
		
	}
	
	/**
	 * Get username of principal currently interacting with the application (the authenticated spring user.)
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public String getUsername() {
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		String username = "unknown";
		
		if (principal instanceof UserDetails) {
			username = ((UserDetails)principal).getUsername();
		} else {
			username = principal.toString();
		}		
		
		return username;
	}
	
	/**
	 * Get authorities (roles) of principal currently interacting with the application (the authenticated spring user.)
	 * 
	 * @return A list of role codes (e.g. 'ROLE_ADMINISTRATOR', 'ROLE_OTHER', etc..)
	 * @throws ServiceException
	 */
	public List<String> getAuthorities() {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>)auth.getAuthorities();
		
		List<String> roles = authorities.stream().map(
					SimpleGrantedAuthority::getAuthority).collect(Collectors.toList());
		
		return roles;
	}
	
	/**
	 * Get principal currently interacting with the application (the authenticated spring user.)
	 * 
	 * @return A FsSecureUser object which extends from the default org.springframework.security.core.userdetails.User class.
	 * 	Otherwise null.
	 */
	public FsSecureUser getLoggedInUser() {
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		FsSecureUser user = null;
		
		if (principal instanceof FsSecureUser) {
			user = (FsSecureUser)principal;
		}	
		
		return user;	
	}

}
