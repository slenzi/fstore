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
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.lenzi.fstore.repository.model.impl.FSClosure;
import org.lenzi.fstore.util.DateUtil;
import org.lenzi.fstore.util.StringUtil;

/**
 * 
 * 
 * @author sal
 */
@Entity
@Table(name="FS_CMS_FILE_STORE")
@SequenceGenerator(name="FS_CMS_STORE_ID_SEQUENCE_GENERATOR",
	sequenceName="FS_CMS_STORE_ID_SEQUENCE", allocationSize = 1)
public class CmsFileStore implements Comparable<CmsFileStore>, Serializable {
	
	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = -9067019678466864039L;

	@Id
	@GeneratedValue(generator="FS_CMS_STORE_ID_SEQUENCE_GENERATOR")
	@Column(name = "STORE_ID", unique=true, nullable = false)
	private Long storeId;
	
	@Column(name = "STORE_NAME", nullable = false)
	private String name;
	
	@Column(name = "STORE_DESCRIPTION", nullable = false)
	private String description;
	
	@Column(name = "STORE_PATH", nullable = false)
	private String storePath;
	
	@Column(name = "NODE_ID", updatable = false, nullable = false)
	private Long nodeId;	
	
	@Column(name = "CREATION_DATE", nullable = false)
	private Timestamp dateCreated;
	
	@Column(name = "UPDATED_DATE", nullable = false)
	private Timestamp dateUpdated;	
	
	@OneToOne(optional=false, fetch=FetchType.EAGER, targetEntity = CmsDirectory.class)
	@JoinColumn(name = "NODE_ID", insertable=false, updatable=false)
	@Fetch(FetchMode.JOIN)
	private CmsDirectory rootDir = null;
	
	public CmsFileStore() {
		
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
	 * @return the storePath
	 */
	public String getStorePath() {
		return storePath;
	}

	/**
	 * @param storePath the storePath to set
	 */
	public void setStorePath(String storePath) {
		this.storePath = storePath;
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

	/**
	 * @return the rootDir
	 */
	public CmsDirectory getRootDir() {
		return rootDir;
	}

	/**
	 * @param rootDir the rootDir to set
	 */
	public void setRootDir(CmsDirectory rootDir) {
		this.rootDir = rootDir;
	}
	
	public boolean hasRootDir(){
		return rootDir != null ? true : false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((storeId == null) ? 0 : storeId.hashCode());
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
		CmsFileStore other = (CmsFileStore) obj;
		if (storeId == null) {
			if (other.storeId != null)
				return false;
		} else if (!storeId.equals(other.storeId))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CmsFileStore store) {
		
		return StringUtil.changeNull(getStorePath()).compareTo( ((store != null) ? StringUtil.changeNull(store.getStorePath()) : "" ));
		
	}

	public String toString(){
		
		StringBuffer buf = new StringBuffer();
		
		buf.append("[");
		buf.append(", id => '" + getStoreId() + "'");
		buf.append(", name => '" + getName() + "'");
		buf.append(", description => '" + getDescription() + "'");
		buf.append(", path => '" + getStorePath() + "'");
		buf.append(", node id => '" + getNodeId() + "'");
		buf.append(", \nroot dir => '" + getRootDir() + "'");
		buf.append(", dt_created => '" + DateUtil.defaultFormat(getDateCreated()) + "'");
		buf.append(", dt_updated => '" + DateUtil.defaultFormat(getDateUpdated()) + "'");
		buf.append("]");
		
		return buf.toString();
		
	}
	
}
