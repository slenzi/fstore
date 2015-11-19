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
 * Models a user group for the system. User groups control access to resources...
 * 
 * @author slenzi
 */
@Entity
@Table(name="FS_USER_GROUP")
@SequenceGenerator(name="FS_USER_GROUP_ID_SEQUENCE_GENERATOR",
	sequenceName="FS_USER_GROUP_ID_SEQUENCE", allocationSize = 1)
public class FsUserGroup implements Comparable<FsUserGroup>, Serializable {

	@Transient
	private static final long serialVersionUID = 2152616590375415711L;

	@Id
	@GeneratedValue(generator="FS_USER_GROUP_ID_SEQUENCE_GENERATOR")
	@Column(name = "GROUP_ID", unique=true, nullable = false)	
	private Long groupId;
	
	@Column(name = "GROUP_CODE", nullable = false)	
	private String groupCode;
	
	@Column(name = "GROUP_DESC", nullable = false)	
	private String groupDescription;
	
	// all members of the group
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups")
	private Set<FsUser> users = new HashSet<FsUser>(0);
	
	public FsUserGroup() {
		
	}

	/**
	 * @return the groupId
	 */
	public Long getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the groupCode
	 */
	public String getGroupCode() {
		return groupCode;
	}

	/**
	 * @param groupCode the groupCode to set
	 */
	public void setGroupCode(String groupCode) {
		this.groupCode = groupCode;
	}

	/**
	 * @return the groupDescription
	 */
	public String getGroupDescription() {
		return groupDescription;
	}

	/**
	 * @param groupDescription the groupDescription to set
	 */
	public void setGroupDescription(String groupDescription) {
		this.groupDescription = groupDescription;
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
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
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
		FsUserGroup other = (FsUserGroup) obj;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		return true;
	}

	@Override
	public int compareTo(FsUserGroup group) {

		if(group == null){
			return -1;
		}
		return StringUtil.changeNull(groupCode).compareTo( StringUtil.changeNull(group.getGroupCode()) );		
		
	}

}
