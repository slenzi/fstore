/**
 * 
 */
package org.lenzi.fstore.core.repository.security.model.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.lenzi.fstore.core.util.StringUtil;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Models a user for the system
 * 
 * @author slenzi
 */
@Entity
@Table(name="FS_USER")
@SequenceGenerator(name="FS_USER_ID_SEQUENCE_GENERATOR",
	sequenceName="FS_USER_ID_SEQUENCE", allocationSize = 1)
public class FsUser implements Comparable<FsUser>, Serializable {

	@Transient
	private static final long serialVersionUID = -3427863196450094822L;

	@Id
	@GeneratedValue(generator="FS_USER_ID_SEQUENCE_GENERATOR")
	@Column(name = "USER_ID", unique=true, nullable = false)	
	private Long userId;
	
	@Column(name = "USERNAME", nullable = false)	
	private String username;
	
	@Column(name = "PASSWORD", nullable = false)	
	private String password;
	
	@Column(name = "FIRST_NAME", nullable = false)	
	private String firstName;
	
	@Column(name = "MIDDLE_NAME", nullable = true)	
	private String middleName;
	
	@Column(name = "LAST_NAME", nullable = false)	
	private String lastName;
	
	@Column(name = "PRIMARY_EMAIL", nullable = false)	
	private String primaryEmail;
	
	// all roles the user belongs to
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "FS_USER_ROLE_LINK",
		joinColumns = { 
			@JoinColumn(name = "USER_ID", nullable = false, updatable = false)
		}, 
		inverseJoinColumns = {
			@JoinColumn(name = "ROLE_ID", nullable = false, updatable = false)
		}
	)	
	private Set<FsUserRole> roles = new HashSet<FsUserRole>(0);
	
	// all groups the user belongs to
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "FS_USER_GROUP_LINK",
		joinColumns = { 
			@JoinColumn(name = "USER_ID", nullable = false, updatable = false)
		}, 
		inverseJoinColumns = {
			@JoinColumn(name = "GROUP_ID", nullable = false, updatable = false)
		}
	)	
	private Set<FsUserGroup> groups = new HashSet<FsUserGroup>(0);
	
	/**
	 * 
	 */
	public FsUser() {
		
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the middleName
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * @param middleName the middleName to set
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the primaryEmail
	 */
	public String getPrimaryEmail() {
		return primaryEmail;
	}

	/**
	 * @param primaryEmail the primaryEmail to set
	 */
	public void setPrimaryEmail(String primaryEmail) {
		this.primaryEmail = primaryEmail;
	}

	/**
	 * @return the roles
	 */
	public Set<FsUserRole> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(Set<FsUserRole> roles) {
		this.roles = roles;
	}

	/**
	 * @return the groups
	 */
	public Set<FsUserGroup> getGroups() {
		return groups;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(Set<FsUserGroup> groups) {
		this.groups = groups;
	}
	
	public int roleCount(){
		return roles != null ? roles.size() : 0;
	}
	
	public int groupCount(){
		return groups != null ? groups.size() : 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FsUser other = (FsUser) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

	/**
	 * Default compare by last name
	 */
	@Override
	public int compareTo(FsUser user) {
		if(user == null){
			return -1;
		}
		return StringUtil.changeNull(lastName).compareTo( StringUtil.changeNull(user.getLastName()) );
	}

}
