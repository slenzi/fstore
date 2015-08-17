/**
 * 
 */
package org.lenzi.fstore.file2.web.messaging.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author sal
 *
 */
public abstract class AbstractMessage {

	private LocalDate date = null;
	private LocalTime time = null;
	
	private String message = null;
	
	/**
	 * 
	 */
	public AbstractMessage() {
		
	}

	/**
	 * @return the date
	 */
	public LocalDate getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(LocalDate date) {
		this.date = date;
	}

	/**
	 * @return the time
	 */
	public LocalTime getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(LocalTime time) {
		this.time = time;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
}
