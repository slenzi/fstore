package org.lenzi.fstore.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.lenzi.fstore.model.tree.Tree;
import org.lenzi.fstore.model.tree.TreeMeta;
import org.lenzi.fstore.rest.exception.WebServiceException;
import org.lenzi.fstore.service.TreeService;
import org.lenzi.fstore.service.exception.ServiceException;
import org.lenzi.fstore.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Path( "/tree")
@Service("TreeResource")
public class TreeResource {

    @InjectLogger
    Logger logger;
    
    @Autowired
    private TreeService treeService;
    
	/**
	 * Get service name
	 * 
	 * @return JSON object
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getServiceName(){
		logger.info(TreeResource.class.getName() + ".getServiceName() called");
		return TreeResource.class.getName();
	}    
	
    /**
     * Fetch tree by id
     * 
     * @param treeid
     * @return
     * @throws WebServiceException
     */
	@GET
	@Path("{treeid}")
	@Produces(MediaType.APPLICATION_JSON)
    public Tree<TreeMeta> getTreeById(@PathParam("treeid") String treeid) throws WebServiceException {
    	
		logger.info("Fetch tree by id = " + treeid);
		
		/*
		Long longTreeId = null;
		try {
			longTreeId = Long.parseLong(treeid);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			logger.error("Error parsing tree ID parameter: " + e.getMessage());
			throw new WebServiceException(WebServiceException.CODE_INVALID_INPUT,
					"Error parsing tree ID parameter: " + e.getMessage());
		}    	
    	
		Tree<TreeMeta> tree = null;
		try {
			tree = treeService.buildTree(longTreeId);
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error("Error building tree for tree id " + longTreeId + ". " + e.getMessage());
			throw new WebServiceException(WebServiceException.CODE_INVALID_INPUT,
					"Error building tree for tree id " + longTreeId + ". " + e.getMessage());
		}
		
		return tree;
		*/
		return null;
		
		// TODO - add this back
    	
    }
    
}
