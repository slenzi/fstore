package org.lenzi.fstore.file2.web.rs;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.web.rs.exception.WebServiceException;
import org.lenzi.fstore.web.rs.exception.WebServiceException.WebExceptionType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Jax-rs resource for FsResourceStores
 * 
 * @author slenzi
 */
@Path( "/store")
@Service("StoreResource")
public class StoreResource {

    @InjectLogger
    Logger logger;
    
    @Autowired
    private FsQueuedResourceService fsResourceService;	
	
	public StoreResource() {
		
	}
	
	/**
	 * Fetch all stores
	 * 
	 * @return
	 * @throws WebServiceException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStores() throws WebServiceException {
		
		List<FsResourceStore> stores = null;
		try {
			stores = fsResourceService.getAllStores();
		} catch (ServiceException e) {
			handleError("Failed to resource store list", WebExceptionType.CODE_DATABSE_ERROR, e);
		}
		return null;
		
	}
	
	/**
	 * Handle error
	 * 
	 * @param message
	 * @param type
	 * @param e
	 * @throws WebServiceException
	 */
	private void handleError(String message, WebExceptionType type, Throwable e) throws WebServiceException {
		
		e.printStackTrace();
		logger.error(message + ", " + e.getMessage(), e);		
		throw new WebServiceException(type, message + ", " + e.getMessage());
		
	}

}
