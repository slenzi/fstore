package org.lenzi.fstore.core.repository.security.model.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.lenzi.fstore.core.util.StringUtil;

/**
 * Models a user role within the system. Roles control access to sections within the application,
 * and define permissions to various functions & actions/activities.
 * 
 * @author slenzi
 */
@Entity
@Table(name="FS_USER_ROLE")
@SequenceGenerator(name="FS_USER_ROLE_ID_SEQUENCE_GENERATOR",
	sequenceName="FS_USER_ROLE_ID_SEQUENCE", allocationSize = 1)
public class FsUserRole implements Comparable<FsUserRole>, Serializable {

	@Transient
	private static final long serialVersionUID = 7866177628583325620L;
	
	/**
	 * Default system roles
	 * 
	 * @author sal
	 */
	public static enum Role {
		
		ADMINISTRATOR("ADMINISTRATOR"),
		FILE_MANAGER_ADMINISTRATOR("FILE_MANAGER_ADMINISTRATOR"),
		CMS_WORKPLACE_ADMINISTRATOR("CMS_WORKPLACE_ADMINISTRATOR"),
		FILE_MANAGER_USER("FILE_MANAGER_USER"),
		CMS_WORKPLACE_USER("CMS_WORKPLACE_USER"),
		GUEST("GUEST");
		
		private final String roleCode;
		
		private Role(final String roleCode){
			this.roleCode = roleCode;
		}
		
		public String getRoleCode(){
			return roleCode;
		}

		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return roleCode;
		}
	}
	
	@Id
	@GeneratedValue(generator="FS_USER_ROLE_ID_SEQUENCE_GENERATOR")
	@Column(name = "ROLE_ID", unique=true, nullable = false)		
	private Long roleId;
	
	@Column(name = "ROLE_CODE", nullable = false)	
	private String roleCode;
	
	@Column(name = "ROLE_DESC", nullable = false)		
	private String roleDescription;
	
	// all members of the role
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "roles")
	private Set<FsUser> users = new HashSet<FsUser>(0);	
	
	public FsUserRole() {
	
	}

	/**
	 * @return the roleId
	 */
	public Long getRoleId() {
		return roleId;
	}

	/**
	 * @param roleId the roleId to set
	 */
	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	/**
	 * @return the roleCode
	 */
	public String getRoleCode() {
		return roleCode;
	}

	/**
	 * @param roleCode the roleCode to set
	 */
	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}
	
	public void setRole(Role role) {
		this.roleCode = role.getRoleCode();
	}
	
	public Role getRole(){
		
		if( roleCode.equals( Role.ADMINISTRATOR.getRoleCode()) ){
			return Role.ADMINISTRATOR;
		} else if( roleCode.equals( Role.FILE_MANAGER_ADMINISTRATOR.getRoleCode()) ){
			return Role.FILE_MANAGER_ADMINISTRATOR;
		} else if( roleCode.equals( Role.CMS_WORKPLACE_ADMINISTRATOR.getRoleCode()) ){
			return Role.CMS_WORKPLACE_ADMINISTRATOR;
		} else if( roleCode.equals( Role.FILE_MANAGER_USER.getRoleCode()) ){
			return Role.FILE_MANAGER_USER;
		} else if( roleCode.equals( Role.CMS_WORKPLACE_USER.getRoleCode()) ){
			return Role.CMS_WORKPLACE_USER;
		} else if( roleCode.equals( Role.GUEST.getRoleCode()) ){
			return Role.GUEST;
		} else {
			return Role.GUEST;
		}
		
	}

	/**
	 * @return the roleDescription
	 */
	public String getRoleDescription() {
		return roleDescription;
	}

	/**
	 * @param roleDescription the roleDescription to set
	 */
	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}

	/**
	 * @return the users
	 */
	public Set<FsUser> getUsers() {
		return users;
	}

	/**
	 * @param users the users to set
	 */
	public void setUsers(Set<FsUser> users) {
		this.users = users;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roleId == null) ? 0 : roleId.hashCode());
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
		FsUserRole other = (FsUserRole) obj;
		if (roleId == null) {
			if (other.roleId != null)
				return false;
		} else if (!roleId.equals(other.roleId))
			return false;
		return true;
	}

	/**
	 * Compare on role code
	 */
	@Override
	public int compareTo(FsUserRole role) {

		if(role == null){
			return -1;
		}
		return StringUtil.changeNull(roleCode).compareTo( StringUtil.changeNull(role.getRoleCode()) );
		
	}

}
