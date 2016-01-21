/**
 * 
 */
package org.lenzi.fstore.core.security.auth.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lenzi.fstore.core.repository.security.model.impl.FsUser;
import org.lenzi.fstore.core.repository.security.model.impl.FsUserRole;
import org.lenzi.fstore.core.security.FsSecureUser;
import org.lenzi.fstore.core.security.service.FsSecurityService;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridge between FsUser and Spring Security User. Pulls users data from database and converts
 * FsUser to a FsSecureUser. The FsSecureUser extends from default Spring User object, which in
 * turn implements UserDetails.
 * 
 * This service is used in the Spring Security configuration setup.
 * @see org.lenzi.fstore.main.config.SecurityConfig
 * 
 * @author sal
 */
@Service("fsUserDetailsService")
public class FsUserDetailsService implements UserDetailsService {

	@InjectLogger
	private Logger logger;	
	
	@Autowired
	private FsSecurityService fsSecurityService;
	
	/**
	 * 
	 */
	public FsUserDetailsService() {
		
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		
		logger.info(FsUserDetailsService.class.getName() + ".loadUserByUsername(final String username) called");
		
		FsUser fsUser = null;
		try {
			fsUser = fsSecurityService.getUserByUsername(username);
		} catch (ServiceException e) {
			
			logger.error(FsUserDetailsService.class.getName() + " => ServiceException, failed to fetch user by username [username='" + username + "']. " + e.getMessage());
			
			throw new UsernameNotFoundException(e.getMessage(), e);
			
		} catch (UsernameNotFoundException e){
			
			logger.error(FsUserDetailsService.class.getName() + " => UsernameNotFoundException, failed to fetch user by username [username='" + username + "']. " + e.getMessage());
			
			throw new UsernameNotFoundException(e.getMessage(), e);
		}
		
		logger.info(FsUserDetailsService.class.getName() + ".loadUserByUsername(final String username) fetch user finished");
		
		List<GrantedAuthority> authorities = buildUserAuthority(fsUser.getRoles());		
		
		final User springUser = buildUserForAuthentication(fsUser, authorities);
		
		logger.info("Spring User => [username='" + springUser.getUsername() + "', password='" + springUser.getPassword() + "']");
		
		springUser.getAuthorities().forEach( (authority) ->{
			logger.info("Granted Authority for '" + springUser.getUsername() + "' => " + authority.getAuthority());
		});
		
		return springUser;
		
	}
	
	/**
	 * Load user by user id.
	 * 
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public UserDetails loadUserByUserId(final Long userId) throws ServiceException {
		
		logger.info(FsUserDetailsService.class.getName() + ".loadUserByUserId(final Long userId) called");
		
		FsUser fsUser = null;
		try {
			fsUser = fsSecurityService.getUserById(userId);
		} catch (ServiceException e) {
			throw new ServiceException("Failed to fetch user with user id " + userId, e);
		}
		List<GrantedAuthority> authorities = buildUserAuthority(fsUser.getRoles());	
		final User springUser = buildUserForAuthentication(fsUser, authorities);
		return springUser;
	}
	
	/**
	 * Convert FsUser to FsSecureUser. The later extends from org.springframework.security.core.userdetails.User.
	 * 
	 * @param user
	 * @param authorities
	 * @return
	 */
	private FsSecureUser buildUserForAuthentication(FsUser user, List<GrantedAuthority> authorities) {
		
		logger.debug(FsUserDetailsService.class.getName() + ".buildUserForAuthentication(...) called");
		
		FsSecureUser fsUser = new FsSecureUser(user, authorities);
		
		return fsUser;
		
	}	
	
	/**
	 * Builds a list of spring GrantedAuthority objects from a set of FsUserRole objects.
	 * 
	 * @param userRoles
	 * @return
	 */
	private List<GrantedAuthority> buildUserAuthority(Set<FsUserRole> userRoles) {

		logger.debug(FsUserDetailsService.class.getName() + ".buildUserAuthority(...) called");
		
		Set<GrantedAuthority> setAuths = new HashSet<GrantedAuthority>();

		// Build user's authorities
		for (FsUserRole userRole : userRoles) {
			logger.debug("Granted Authority Role Code: " + userRole.getRoleCode());
			setAuths.add(new SimpleGrantedAuthority(userRole.getRoleCode()));
		}

		List<GrantedAuthority> Result = new ArrayList<GrantedAuthority>(setAuths);

		return Result;
	}	

}
