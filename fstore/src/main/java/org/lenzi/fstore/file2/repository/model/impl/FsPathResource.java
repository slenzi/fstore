/**
 * 
 */
package org.lenzi.fstore.file2.repository.model.impl;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.InheritanceType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.lenzi.fstore.core.repository.model.impl.FSNode;

/**
 * Abstract path which can represent a file or directory
 * 
 * @author sal
 */
@Entity
@Table(name = "FS_PATH_RESOURCE")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="PATH_TYPE", discriminatorType=DiscriminatorType.STRING)
public abstract class FsPathResource extends FSNode<FsPathResource> implements FsResource {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = 4413404986186464097L;

	@Column(name = "NAME", nullable = false)
	private String name;
	
	// relative path (relative to store path)
	@Column(name = "RELATIVE_PATH", nullable = false)
	private String relativePath;	
	
	// file store (if path is a root node)
	
	@Column(name = "PATH_TYPE", nullable = false)
	@Enumerated(EnumType.STRING)
	private FsPathType pathType;
	
	/**
	 * 
	 */
	public FsPathResource() {
		
	}
	
	public FsPathResource(Long id) {
		setNodeId(id);
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FsPathResource [name=" + name + ", relativePath="
				+ relativePath + ", pathType=" + pathType.getType() + "]";
	}

}
