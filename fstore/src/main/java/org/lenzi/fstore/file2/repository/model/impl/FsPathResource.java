/**
 * 
 */
package org.lenzi.fstore.file2.repository.model.impl;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.FetchType;
import javax.persistence.InheritanceType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.lenzi.fstore.core.repository.tree.model.impl.FSNode;
import org.lenzi.fstore.core.util.StringUtil;

/**
 * Abstract path which can represent a file or directory
 * 
 * @author sal
 */
@Entity
@Table(name = "FS_PATH_RESOURCE")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="PATH_TYPE", discriminatorType=DiscriminatorType.STRING)
public abstract class FsPathResource extends FSNode<FsPathResource> implements FsResource, Comparable<FsPathResource> {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = 4413404986186464097L;

	@Column(name = "NAME", nullable = false)
	private String name;
	
	//
	// Id of the resource store that this path resource is under. We can get the store id by
	// traversing the resouce tree upwards to the root node, but that takes time. Having the store
	// id mapped here is a nice convenience.
	//
	@Column(name = "STORE_ID", nullable = false)
	private Long storeId;
	
	//
	// Path of resource relative to the store's path.
	//
	@Column(name = "RELATIVE_PATH", nullable = false)
	private String relativePath;
	
	//
	// Path type, either FsPathType.FILE or FsPathType.DIRECTORY, or maybe some future FsPathType value...
	//
	@Column(name = "PATH_TYPE", nullable = false)
	@Enumerated(EnumType.STRING)
	private FsPathType pathType;
	
	//
	// The resource store that this path resource is under
	//
	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "STORE_ID", nullable = false, insertable=false, updatable=false)
    private FsResourceStore resourceStore;
	
	/**
	 * 
	 */
	public FsPathResource() {
		
	}
	
	/**
	 * 
	 * @param resourceId - same as FSNode node id.
	 */
	public FsPathResource(Long resourceId) {
		setNodeId(resourceId);
	}
	
	/**
	 * Same as FSNode.getNodeId()
	 * 
	 * @return
	 */
	public Long getResourceId(){
		return getNodeId();
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
	 * @return the relativePath
	 */
	public String getRelativePath() {
		return relativePath;
	}

	/**
	 * @param relativePath the relativePath to set
	 */
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	/**
	 * @return the pathType
	 */
	public FsPathType getPathType() {
		return pathType;
	}

	/**
	 * @param pathType the pathType to set
	 */
	public void setPathType(FsPathType pathType) {
		this.pathType = pathType;
	}
	
	public String getPathTypeValue() {
		return pathType.getType();
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FsPathResource [id=" + getNodeId() + ", store id=" + storeId + ", name=" + name + ", relativePath="
				+ relativePath + ", pathType=" + pathType.getType() + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FsPathResource other) {
		
		if(other == null){
			return -1;
		}
		
		if(other.getPathType() != null && getPathType() != null){

			if(getPathTypeValue().equals(FsPathType.DIRECTORY.getType()) && other.getPathTypeValue().equals(FsPathType.DIRECTORY.getType())){
		
				// both directories, compare names
				return StringUtil.changeNull(getName()).toLowerCase().compareTo(StringUtil.changeNull(other.getName()).toLowerCase());
				
			}else if(getPathTypeValue().equals(FsPathType.FILE.getType()) && other.getPathTypeValue().equals(FsPathType.DIRECTORY.getType())){
				
				// dir comes first
				return 1;
				
			}else if(getPathTypeValue().equals(FsPathType.DIRECTORY.getType()) && other.getPathTypeValue().equals(FsPathType.FILE.getType())){
				
				// dir comes first
				return -1;
				
			}else if(getPathTypeValue().equals(FsPathType.FILE.getType()) && other.getPathTypeValue().equals(FsPathType.FILE.getType())){
				
				// both files, compare names
				return StringUtil.changeNull(getName()).toLowerCase().compareTo(StringUtil.changeNull(other.getName()).toLowerCase());
				
			}else{
				
				// not sure of types...
				return StringUtil.changeNull(getName()).toLowerCase().compareTo(StringUtil.changeNull(other.getName()).toLowerCase());
				
			}
			
		}else if(other.getPathType() == null && getPathType() != null){
			
			return -1;
			
		}else if(other.getPathType() != null && getPathType() == null){
			
			return 1;
			
		}else{
			
			// compare names
			return StringUtil.changeNull(getName()).toLowerCase().compareTo(StringUtil.changeNull(other.getName()).toLowerCase());
			
		}
		
	}
	
	

}
