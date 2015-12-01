package org.lenzi.fstore.core.security;

import java.util.Collection;

import org.lenzi.fstore.core.repository.security.model.impl.FsUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * User class which extends from the default spring security user class. This class wraps and
 * encapsulates the default FsUser entity model.
 * 
 * @author sal
 */
public class FsSecureUser extends User {

	private static final long serialVersionUID = -7969212657349744255L;
	
	private FsUser fsUser = null;
	
	/**
	 * 
	 * @param fsUser
	 * @param authorities
	 */
	public FsSecureUser(FsUser fsUser, Collection<? extends GrantedAuthority> authorities){
		
		super(fsUser.getUsername(), fsUser.getPassword(), true, true, true, true, authorities);
		
		this.fsUser = fsUser;
		
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @param authorities
	 */
	public FsSecureUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
		
		super(username, password, authorities);
		
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @param enabled
	 * @param accountNonExpired
	 * @param credentialsNonExpired
	 * @param accountNonLocked
	 * @param authorities
	 */
	public FsSecureUser(String username, String password, boolean enabled, boolean accountNonExpired,
			boolean credentialsNonExpired, boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		
		super(username, password, true, true, true, true, authorities);

	}

	/**
	 * Retrieve the underlying FsUser entity model.
	 * 
	 * @return the fsUser
	 */
	public FsUser getFsUser() {
		return fsUser;
	}
	
	/**
	 * Check if underlying FsUser model is present.
	 * 
	 * @return
	 */
	public boolean haveFsUser(){
		return fsUser != null ? true : false;
	}

}
