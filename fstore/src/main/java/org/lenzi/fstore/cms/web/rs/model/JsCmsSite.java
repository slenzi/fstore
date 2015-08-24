/**
 * 
 */
package org.lenzi.fstore.cms.web.rs.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.lenzi.fstore.core.util.StringUtil;
import org.lenzi.fstore.file2.web.rs.model.JsResourceStore;

/**
 * @author sal
 *
 */
@XmlRootElement
public class JsCmsSite implements Comparable<JsCmsSite> {

	private String id;
	private String name;
	private String description;
	
	private String dateCreated;
	private String dateUpdated;
	
	private JsResourceStore onlineStore;
	private JsResourceStore offlineStore;
	
	public JsCmsSite() {
		
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

	/**
	 * @return the onlineStore
	 */
	public JsResourceStore getOnlineStore() {
		return onlineStore;
	}

	/**
	 * @param onlineStore the onlineStore to set
	 */
	public void setOnlineStore(JsResourceStore onlineStore) {
		this.onlineStore = onlineStore;
	}

	/**
	 * @return the offlineStore
	 */
	public JsResourceStore getOfflineStore() {
		return offlineStore;
	}

	/**
	 * @param offlineStore the offlineStore to set
	 */
	public void setOfflineStore(JsResourceStore offlineStore) {
		this.offlineStore = offlineStore;
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
		JsCmsSite other = (JsCmsSite) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(JsCmsSite site) {

		if(site == null){
			return -1;
		}
		return StringUtil.changeNull(name).compareTo(StringUtil.changeNull(site.getName()));
				
		
	}

}
