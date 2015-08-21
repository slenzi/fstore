package org.lenzi.fstore.cms.repository.model.impl;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.lenzi.fstore.core.util.StringUtil;
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
	
	@Column(name = "STORE_ID", nullable = false)
	private Long storeId;	
	
	@Column(name = "CREATION_DATE", nullable = false)
	private Timestamp dateCreated;
	
	@Column(name = "UPDATED_DATE", nullable = false)
	private Timestamp dateUpdated;
	
	// resource store containing all files for the cms site
	@OneToOne(cascade=CascadeType.ALL, optional=false, fetch=FetchType.EAGER)
    @JoinColumn(name="STORE_ID", insertable=false, updatable=false, unique=true)
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

	/**
	 * @return the storeId
	 */
	public Long getStoreId() {
		return storeId;
	}

	/**
	 * @param storeId the storeId to set
	 */
	public void setStoreId(Long storeId) {
		this.storeId = storeId;
	}

	/**
	 * @return the resourceStore
	 */
	public FsResourceStore getResourceStore() {
		return resourceStore;
	}

	/**
	 * @param resourceStore the resourceStore to set
	 */
	public void setResourceStore(FsResourceStore resourceStore) {
		this.resourceStore = resourceStore;
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
