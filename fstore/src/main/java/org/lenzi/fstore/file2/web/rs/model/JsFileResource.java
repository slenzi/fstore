package org.lenzi.fstore.file2.web.rs.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JsFileResource extends JsPathResource {

	private String size;
	private String mimeType;
	
	public JsFileResource() {
		
	}

	/**
	 * @return the size
	 */
	public String getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(String size) {
		this.size = size;
	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	
}
