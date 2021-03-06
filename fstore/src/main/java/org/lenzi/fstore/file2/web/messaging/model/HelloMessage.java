/**
 * 
 */
package org.lenzi.fstore.file2.web.messaging.model;

import java.io.Serializable;

import org.springframework.stereotype.Component;

/**
 * @author slenzi
 *
 */
@Component
public class HelloMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8379398273750497788L;
	
	private String message = null;
	
	public HelloMessage(){
		
	}
	
	public HelloMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
