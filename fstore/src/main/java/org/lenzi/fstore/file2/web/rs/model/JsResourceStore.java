package org.lenzi.fstore.file2.web.rs.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.lenzi.fstore.core.util.StringUtil;

/**
 * jax-rs layer model for resource store data.
 * 
 * @author sal
 *
 */
@XmlRootElement
public class JsResourceStore implements Comparable<JsResourceStore> {

	private String id;
	private String name;
	private String description;
	private String storePath;
	
	private String rootDirectoryId;
	
	private String dateCreated;
	private String dateUpdated;
	
	public JsResourceStore() {
		
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return the rootDirectoryId
	 */
	public String getRootDirectoryId() {
		return rootDirectoryId;
	}

	/**
	 * @param rootDirectoryId the rootDirectoryId to set
	 */
	public void setRootDirectoryId(String rootDirectoryId) {
		this.rootDirectoryId = rootDirectoryId;
	}

	/**
	 * @return the dateCreated
	 */
	public String getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return the dateUpdated
	 */
	public String getDateUpdated() {
		return dateUpdated;
	}

	/**
	 * @param dateUpdated the dateUpdated to set
	 */
	public void setDateUpdated(String dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		JsResourceStore other = (JsResourceStore) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int compareTo(JsResourceStore store) {
		
		if(store == null){
			return -1;
		}
		return StringUtil.changeNull(name).compareTo(StringUtil.changeNull(store.getName()));
		
	}
	


}
