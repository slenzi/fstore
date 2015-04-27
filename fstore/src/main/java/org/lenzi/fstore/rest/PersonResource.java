package org.lenzi.fstore.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.lenzi.fstore.repository.model.impl.Person;
import org.lenzi.fstore.rest.exception.WebServiceException;
import org.lenzi.fstore.service.PersonService;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Path( "/person")
@Service("PersonResource")
public class PersonResource {

    @InjectLogger
    Logger logger;
	
    @Autowired
    PersonService personService;
    
	/**
	 * Get service name
	 * 
	 * @return JSON object
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getServiceName(){
		logger.info(PersonResource.class.getName() + ".getServiceName() called");
		return PersonResource.class.getName();
	}

	/**
	 * Fetch person by id
	 * 
	 * @param id - The ID of the person.
	 * @return JSON object
	 */
	@GET
	@Path("{sid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Person getPersonById(@PathParam("sid") String id) throws WebServiceException {
		
		logger.info("Fetch person by id = " + id);
		
		Integer personId = null;
		try {
			personId = Integer.parseInt(id);
		} catch (NumberFormatException e) {
			logger.error("Error parsing person ID parameter: " + e.getMessage());
			throw new WebServiceException(WebServiceException.CODE_INVALID_INPUT,"Cannot parse provided person id.");
		}
		
		Person per = personService.getPersonById(personId.intValue());
		
		if(per == null){
			logger.warn("No person with id " + id + " found in the database.");
			throw new WebServiceException(WebServiceException.CODE_NOT_FOUND,"No person with id " + id + " found in the database.");
		}
		
		return per;
	}
	
}
