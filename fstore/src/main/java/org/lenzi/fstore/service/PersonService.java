package org.lenzi.fstore.service;

import org.lenzi.fstore.repository.model.impl.Person;

/**
 * Person service interface.
 * 
 * @author slenzi
 */
public interface PersonService {

	/**
	 * Fetch person by id
	 * 
	 * @param id - the person id
	 * @return Person object
	 */
	public Person getPersonById(int id);
	
}
