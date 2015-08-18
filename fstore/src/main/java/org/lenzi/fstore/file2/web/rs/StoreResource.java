package org.lenzi.fstore.file2.web.rs;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.lenzi.fstore.file2.web.rs.model.JsResourceStore;
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
public class StoreResource extends AbstractResource {

    @InjectLogger
    Logger logger;
    
    @Autowired
    private FsQueuedResourceService fsQueuedResourceService;
    
    @Autowired
    private FsResourceService fsResourceService;    
	
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
		
		logger.info(StoreResource.class.getName() + ": fetch all resource stores");
		
		List<FsResourceStore> stores = null;
		try {
			stores = fsResourceService.getAllStores();
		} catch (ServiceException e) {
			handleError("Failed to fetch esource store list", WebExceptionType.CODE_DATABSE_ERROR, e);
		}
		if(stores == null){
			handleError("Failed to fetch resource store list, returned store collection was null.",
					WebExceptionType.CODE_DATABSE_ERROR);
		}
		
		logger.info("Fetched " + ((stores != null) ? stores.size() : " null ") + " stores from database");
		
		List<JsResourceStore> jstores = convertStore(stores);
		
		//return Response.ok(jstores, MediaType.APPLICATION_JSON).build();
		
		return Response.status(Response.Status.OK)
                .entity(jstores)
                .type(MediaType.APPLICATION_JSON).build();		
		
	}
	
	/**
	 * Fetch resource store by store id
	 * 
	 * @param storeId
	 * @return
	 * @throws WebServiceException
	 */
	@GET
	@Path("/{storeId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStoreById(@PathParam("storeId") Long storeId) throws WebServiceException {
		
		logger.info(StoreResource.class.getName() + ": fetch store by store id " + storeId);
		
		FsResourceStore store = null;
		try {
			store = fsResourceService.getResourceStoreById(storeId);
		} catch (ServiceException e) {
			handleError("Failed to fetch resource store for id " + storeId, WebExceptionType.CODE_DATABSE_ERROR, e);
		}
		if(store == null){
			handleError("Failed to fetch resource store for id " + storeId + ". Returned store object was null.",
					WebExceptionType.CODE_DATABSE_ERROR);
		}
		
		JsResourceStore jstore = convertStore(store);
		
		return Response.status(Response.Status.OK)
                .entity(jstore)
                .type(MediaType.APPLICATION_JSON).build();	
		
	}
	
	/**
	 * Convert database layer FsResourceStore to web service layer JsResourceStore
	 * 
	 * @param stores
	 * @return
	 */
	private List<JsResourceStore> convertStore(List<FsResourceStore> stores){
		
		if(stores == null){
			return null;
		}
		List<JsResourceStore> jsStores = new ArrayList<JsResourceStore>();
		for(FsResourceStore store : stores){
			jsStores.add(convertStore(store));
		}
		return jsStores;
		
	}
	
	private JsResourceStore convertStore(FsResourceStore store){
		
		JsResourceStore js = new JsResourceStore();
		
		js.setId(String.valueOf(store.getStoreId()));
		js.setName(store.getName());
		js.setDescription(store.getDescription());
		js.setStorePath(store.getStorePath());
		js.setDateCreated(DateUtil.defaultFormat(store.getDateCreated()));
		js.setDateUpdated(DateUtil.defaultFormat(store.getDateUpdated()));
		js.setRootDirectoryId(String.valueOf(store.getNodeId()));
		
		return js;
		
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}	

}
