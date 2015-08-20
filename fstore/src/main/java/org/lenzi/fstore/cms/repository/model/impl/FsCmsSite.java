package org.lenzi.fstore.cms.repository.model.impl;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.lenzi.fstore.core.util.StringUtil;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;

/**
 * Model for basic CMS site info
 * 
 * @author sal
 */
@Entity
@Table(name="FS_CMS_SITE")
@SequenceGenerator(name="FS_CMS_SITE_ID_SEQUENCE_GENERATOR",
	sequenceName="FS_CMS_SITE_ID_SEQUENCE", allocationSize = 1)
public class FsCmsSite implements Comparable<FsCmsSite>, Serializable {

	private static final long serialVersionUID = 5020047592914293311L;

	@Id
	@GeneratedValue(generator="FS_CMS_SITE_ID_SEQUENCE_GENERATOR")
	@Column(name = "SITE_ID", unique=true, nullable = false)
	private Long siteId;
	
	@Column(name = "SITE_NAME", nullable = false)
	private String name;
	
	@Column(name = "SITE_DESCRIPTION", nullable = false)
	private String description;
	
	@Column(name = "SITE_PATH", nullable = false)
	private String path;
	
	// id of root directory resource
	@Column(name = "NODE_ID", updatable = false, nullable = false)
	private Long nodeId;	
	
	@Column(name = "CREATION_DATE", nullable = false)
	private Timestamp dateCreated;
	
	@Column(name = "UPDATED_DATE", nullable = false)
	private Timestamp dateUpdated;
	
	/*
	//
	// the root directory for the site
	//
	@OneToOne(optional=false, fetch=FetchType.EAGER, targetEntity = FsDirectoryResource.class)
	@JoinColumn(name = "NODE_ID", insertable=false, updatable=false)
	@Fetch(FetchMode.JOIN)
	private FsDirectoryResource rootDirectoryResource = null;
	
	//
	// all the path resource under this site
	//
	@OneToMany(mappedBy="resourceStore", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	private Set<FsPathResource> pathResources = new HashSet<FsPathResource>(0);
	*/
	
	private FsResourceStore resourceStore = null;
	
	public FsCmsSite() {
		
	}
	
	/**
	 * @return the siteId
	 */
	public Long getSiteId() {
		return siteId;
	}

	/**
	 * @param siteId the siteId to set
	 */
	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the nodeId
	 */
	public Long getNodeId() {
		return nodeId;
	}

	/**
	 * @param nodeId the nodeId to set
	 */
	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @return the dateCreated
	 */
	public Timestamp getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return the dateUpdated
	 */
	public Timestamp getDateUpdated() {
		return dateUpdated;
	}

	/**
	 * @param dateUpdated the dateUpdated to set
	 */
	public void setDateUpdated(Timestamp dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	@Override
	public int compareTo(FsCmsSite site) {
	
		return StringUtil.changeNull(
				getName()).compareTo( ((site != null) ? StringUtil.changeNull(site.getName()) : "" )
				);		
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((siteId == null) ? 0 : siteId.hashCode());
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
		FsCmsSite other = (FsCmsSite) obj;
		if (siteId == null) {
			if (other.siteId != null)
				return false;
		} else if (!siteId.equals(other.siteId))
			return false;
		return true;
	}
	


}
